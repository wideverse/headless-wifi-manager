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

package com.wideverse.headlesswifimanager_advertiser

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.wideverse.headlesswifimanager.interfaces.AdvertisingCallback
import com.wideverse.headlesswifimanager.HeadlessWifiManager
import java.lang.Exception

const val TAG = "HEADLESS_ADVERTISER"
const val APP_ID = "headless_wifi_configurator"


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Headless device starts Advertising as soon as connected
        HeadlessWifiManager(applicationContext, APP_ID)
            .startAdvertising(object: AdvertisingCallback {

                override fun onAdvertisingStarted() {
                    Log.d(TAG, "Successfully started Advertising.")
                }

                override fun onError(e: Exception) {
                    Log.e(TAG, "Procedure failed")
                    e.printStackTrace()
                }

                override fun onSuccess() {
                    Log.d(TAG, "Successfully connected to Wifi.")
                }
        })
    }
}
