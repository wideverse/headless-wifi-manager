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

package com.wideverse.headlesswifimanager.interfaces

import com.wideverse.headlesswifimanager.data.WifiScanResult
import java.lang.Exception

interface  DiscoveryCallback{
    fun onError(e: Exception)
    fun onDiscoveryStarted()
    fun onConnected()
    fun onNetworkListAvailable(results: List<WifiScanResult>)
    fun onDeviceFound(deviceId: String, deviceName: String)

}