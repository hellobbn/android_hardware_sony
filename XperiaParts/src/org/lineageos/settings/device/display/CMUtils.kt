/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.device.display;

import android.util.Log
import android.content.Context
import android.content.Intent
import android.hardware.display.ColorDisplayManager
import android.os.IBinder
import android.os.RemoteException
import android.os.ServiceManager
import android.os.UserHandle
import android.provider.Settings

import vendor.semc.hardware.display.V2_0.IDisplay
import vendor.semc.hardware.display.V2_0.IDisplayCallback
import vendor.semc.hardware.display.V2_0.PccMatrix
import java.lang.RuntimeException

import lineageos.hardware.LiveDisplayManager;
import lineageos.hardware.ILiveDisplayService
import lineageos.app.LineageContextConstants

class CMCallback : IDisplayCallback.Stub() {
    private var mContext : Context? = null

    fun setup(context: Context) {
        // Register callback
        mContext = context
        try {
            val service: IDisplay = IDisplay.getService()!!
            service.registerCallback(this)
            service.setup()
        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to get IDisplay due to remote exception [$e]")
        } catch (e: RuntimeException) {
            Log.e(TAG, "setup: RuntimeException")
        }
    }

    override fun onWhiteBalanceMatrixChanged(matrix : PccMatrix?) {
        val colorMatrix: FloatArray = floatArrayOf(matrix!!.red, matrix.green, matrix.blue);
        Log.i(TAG, "Got white balance matrix: " + matrix.red + ", " + matrix.green + ", " + matrix.blue);
        val sLiveDisplay : LiveDisplayManager = LiveDisplayManager.getInstance(mContext)
        sLiveDisplay.setColorAdjustment(colorMatrix)
    }

    companion object {
        private const val TAG = "CMCallback"
    }
}

class CMUtils() {
    private var mCdm : ColorDisplayManager? = null
    private var mHidl : IDisplay? = null
    private var mCMCallback : CMCallback? = null

    constructor(context: Context?) : this() {
        if (this.mCdm != null) {
            return
        }

        if (context == null) {
            Log.e(TAG, "Error: context NULL!")
            return
        }

        this.mCdm = context.getSystemService(ColorDisplayManager::class.java);
        if (this.mCdm == null) {
            Log.e(TAG, "Error: Display manager NULL")
            return
        }

        this.mHidl = IDisplay.getService();
        if (this.mHidl == null) {
            Log.e(TAG, "Error, HIDL not found")
            return
        }
    }

    fun enableCM(context: Context) : Unit {
        if (this.mHidl != null) {
            this.mHidl?.set_sspp_color_mode(0);
            this.mCdm?.setColorMode(0);
            this.mHidl?.set_color_mode(0);
            Settings.Secure.putInt(context.contentResolver, CREATOR_MODE_ENABLE, 1)
        }
    }

    fun disableCM(context: Context) : Unit {
        if (this.mHidl != null) {
            this.mHidl?.set_sspp_color_mode(1);
            this.mCdm?.setColorMode(3);
            this.mHidl?.set_color_mode(1);
            Settings.Secure.putInt(context.contentResolver, CREATOR_MODE_ENABLE, 0)
        }
    }

    fun initialize(context: Context) : Boolean {
        Log.e(TAG, "Creator Mode controller setup")

        // Start service
        if (mCMCallback == null) {
            mCMCallback = CMCallback()!!
        }
        mCMCallback?.setup(context)

        return true
    }

    companion object {
        private const val TAG = "CMUtils"
        private const val CREATOR_MODE_ENABLE = "cm_enable"

        fun isCMEnabled(context : Context) : Boolean {
            return Settings.Secure.getInt(context.contentResolver, CREATOR_MODE_ENABLE, 0) != 0
        }
    }

}
