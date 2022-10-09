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

import android.os.Bundle
import androidx.preference.*

import org.lineageos.settings.device.R

const val CREATOR_MODE_KEY = "switchCreatorMode"

class DisplaySettingsFragment : PreferenceFragment(), Preference.OnPreferenceChangeListener {

    private var mCMCtrl: CMUtils? = null

    private var creatorModePreference : SwitchPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.advanced_disp_settings)
        this.mCMCtrl = CMUtils(context)

        creatorModePreference = findPreference(CREATOR_MODE_KEY)
        creatorModePreference?.isChecked = CMUtils.isCMEnabled(context)
        creatorModePreference?.onPreferenceChangeListener = this
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        if (preference.key == CREATOR_MODE_KEY) {
            if (newValue as Boolean) {
                mCMCtrl?.enableCM(context)
            } else {
                mCMCtrl?.disableCM(context)
            }
        }

        return true
    }
}
