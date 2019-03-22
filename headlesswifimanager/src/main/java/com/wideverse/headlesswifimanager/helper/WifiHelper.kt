/*
 * Copyright 2019 Wideverse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wideverse.headlesswifimanager.helper

import android.net.wifi.WifiManager
import androidx.annotation.DrawableRes
import android.net.wifi.ScanResult
import com.wideverse.headlesswifimanager.R


class WifiHelper {
    companion object {


        // HeadlessWifiManagerConstants used for different security types
        val WPA2 = "WPA2"
        val WPA = "WPA"
        val WEP = "WEP"

        /* For EAP Enterprise fields */
        val WPA_EAP = "WPA-EAP"
        val IEEE8021X = "IEEE8021X"


        fun isNetworkProtected(scanResult: ScanResult): Boolean {
            val cap = scanResult.capabilities
            val securityModes = arrayOf<String>(WEP, WPA, WPA2, WPA_EAP, IEEE8021X)
            val num = securityModes.filter { it in cap }

            return num.isNotEmpty()
        }

        /**
         * Get a good looking drawable resource to quickly identify
         * network's level signal and its security policy.
         *
         * @param level: RSSI of the scanned network
         * @param protected: false if the network does not require a password
         *
         * @return a drawable to represent the network to the user
         */
        @DrawableRes
        fun getDrawableFromRSSI(level: Int, protected: Boolean): Int {
            val signal = WifiManager.calculateSignalLevel(level, 3)
            if (protected) {
                when (signal) {
                    0 -> return R.drawable.ic_signal_wifi_1_bar_lock_black_24dp
                    1 -> return R.drawable.ic_signal_wifi_2_bar_lock_black_24dp
                    2 -> return R.drawable.ic_signal_wifi_3_bar_lock_black_24dp
                    3 -> return R.drawable.ic_signal_wifi_4_bar_lock_black_24dp
                }

                return R.drawable.ic_signal_wifi_4_bar_lock_black_24dp
            } else {
                when (signal) {
                    0 -> return R.drawable.ic_signal_wifi_1_bar_black_24dp
                    1 -> return R.drawable.ic_signal_wifi_2_bar_black_24dp
                    2 -> return R.drawable.ic_signal_wifi_3_bar_black_24dp
                    3 -> return R.drawable.ic_signal_wifi_4_bar_black_24dp
                }

                return R.drawable.ic_signal_wifi_4_bar_black_24dp
            }
        }
    }
}