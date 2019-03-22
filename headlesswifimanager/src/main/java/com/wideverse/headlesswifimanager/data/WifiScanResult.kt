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

package com.wideverse.headlesswifimanager.data

/**
 * Internal class to transfer data about Wifi networks
 * You can expect a List<WifiScanResults> after a successful scanning and
 * pass a WifiScanResult with a valid password field to advertiser to connect on.
 *
 */
class WifiScanResult {
    var SSID: String = ""
    var level: Int = -1
    var protected: Boolean = false
    var password: String = ""
}