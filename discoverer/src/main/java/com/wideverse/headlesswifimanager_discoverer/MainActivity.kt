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

package com.wideverse.headlesswifimanager_discoverer

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.wideverse.headlesswifimanager.interfaces.DiscoveryCallback
import com.wideverse.headlesswifimanager.HeadlessWifiManager
import com.wideverse.headlesswifimanager.data.WifiScanResult
import com.wideverse.headlesswifimanager.interfaces.NetworkCallback
import com.karumi.dexter.Dexter
import com.wideverse.headlesswifimanager_discoverer.adapter.MainPagerAdapter
import com.wideverse.headlesswifimanager_discoverer.fragment.BaseFragment
import com.wideverse.headlesswifimanager_discoverer.fragment.OnFragmentWifiSelected
import com.wideverse.headlesswifimanager_discoverer.helper.DiscovererViewPager.Companion.VIEW_ADVERTISER_CONNECTED
import com.wideverse.headlesswifimanager_discoverer.helper.DiscovererViewPager.Companion.VIEW_SCANNING
import com.wideverse.headlesswifimanager_discoverer.helper.DiscovererViewPager.Companion.VIEW_WELCOME
import com.wideverse.headlesswifimanager_discoverer.helper.DiscovererViewPager.Companion.VIEW_WIFI_CONNECTING
import com.wideverse.headlesswifimanager_discoverer.helper.DiscovererViewPager.Companion.VIEW_WIFI_DONE
import com.wideverse.headlesswifimanager_discoverer.helper.DiscovererViewPager.Companion.VIEW_WIFI_ERROR
import com.wideverse.headlesswifimanager_discoverer.helper.DiscovererViewPager.Companion.VIEW_WIFI_LIST
import com.wideverse.headlesswifimanager_discoverer.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener


const val APP_ID = "headless_wifi_configurator"
const val TAG = "HEADLESS_DISCOVERER"

class MainActivity : AppCompatActivity(),
    BaseFragment.OnFragmentInteractionListener,
    OnFragmentWifiSelected {

    lateinit var viewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {/* ... */
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {/* ... */
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest,
                    token: PermissionToken
                ) {/* ... */
                }
            }).check()
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        viewModel.headlessWifiManager = HeadlessWifiManager(applicationContext, APP_ID)

        viewModel.pagerAdapter =
            MainPagerAdapter(supportFragmentManager)

        mainPager.adapter = viewModel.pagerAdapter

        mainPager.setCurrentItem(VIEW_WELCOME, false)
    }

    private fun initiateProcedure() {
        mainPager.invalidate()

        // Set Scanning fragment at start
        mainPager.setCurrentItem(VIEW_SCANNING, false)

        viewModel.headlessWifiManager
            .startDiscovery(object : DiscoveryCallback {
                override fun onDiscoveryStarted() {
                    Log.d(TAG, "Successfully started looking for nearby devices to configure")
                    mainPager.setCurrentItem(VIEW_SCANNING, false)
                }

                override fun onDeviceFound(endpointId: String, deviceName: String) {
                    Log.d(TAG, "Trying to connect to $deviceName")
                    // Right now first device discovered is automatically
                    // targeted for connection
                    viewModel.headlessWifiManager.connectToEndpoint(endpointId)
                    // TODO: add here multi device support
                }

                override fun onConnected() {
                    Log.d(TAG, "Sucessifully connected to a hub device")
                    Log.d(TAG, "Now waiting to get WiFi List from advertiser")
                    mainPager.setCurrentItem(VIEW_ADVERTISER_CONNECTED, false)
                }

                override fun onNetworkListAvailable(results: List<WifiScanResult>) {
                    Log.d(TAG, "Successfully made a connection. Wifi list is available.")
                    mainPager.setCurrentItem(VIEW_WIFI_LIST, false)
                    viewModel.pagerAdapter.getWifiFragment().setWifiScanList(results)
                }

                override fun onError(e: Exception) {
                    mainPager.setCurrentItem(VIEW_WIFI_ERROR, false)
                }
            })
    }

    override fun onFragmentWifiSelected(result: WifiScanResult?) {
        if (result != null) {
            mainPager.setSlide(VIEW_WIFI_CONNECTING)
            viewModel.headlessWifiManager.sendWifiCredentials(result,
                object : NetworkCallback {
                    override fun onError(e: Exception) {
                        mainPager.setCurrentItem(VIEW_WIFI_ERROR, false)
                    }

                    override fun onConnected(SSID: String) {
                        mainPager.setCurrentItem(VIEW_WIFI_DONE, false)
                    }
                })
        } else {
            cancelProcedure(false)
        }
    }

    override fun onFragmentInteraction(id: String) {
        when (id) {
            "welcomeButton" -> initiateProcedure()
            "cancelButton" -> cancelProcedure(false)
            "doneButton" -> cancelProcedure(true)
        }
    }

    private fun cancelProcedure(finish: Boolean) {
        mainPager.setCurrentItem(VIEW_WELCOME, false)
        viewModel.headlessWifiManager.abortProcedure()

        if (finish) {
            finish()
        }
    }
}