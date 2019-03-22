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

package com.wideverse.headlesswifimanager

import android.content.Context
import android.net.wifi.ScanResult
import android.util.Log
import com.wideverse.headlesswifimanager.data.GenericPayload
import com.wideverse.headlesswifimanager.data.WifiConnectionAck
import com.wideverse.headlesswifimanager.data.WifiCredentialsPayload
import com.wideverse.headlesswifimanager.data.WifiScanResult
import com.wideverse.headlesswifimanager.helper.ScanResultConverter
import com.wideverse.headlesswifimanager.interfaces.AdvertisingCallback
import com.wideverse.headlesswifimanager.interfaces.DiscoveryCallback
import com.wideverse.headlesswifimanager.interfaces.NetworkCallback
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.gson.Gson
import com.thanosfisherman.wifiutils.WifiUtils
import java.lang.Exception
import java.nio.charset.Charset


class  HeadlessWifiManager{

    lateinit var advertisingCallback: AdvertisingCallback
    lateinit var discoveryCallback: DiscoveryCallback
    lateinit var networkCallback: NetworkCallback
    private lateinit var applicationContext: Context
    var appID: String

    var currentConnectedEndpointId: String? = null

    /**
     * Main constructor for HeadlessWifiManager.
     * @param applicationContext: Context.
     * @param appID: String. Unique identifier for the application
     */
    constructor(applicationContext: Context, appID: String){
        this.applicationContext = applicationContext
        this.appID = appID
    }

    /**
     * Starts advertising
     * Called on the advertiser that awaits configuration
     * @param callback: AdvertisingCallback. Callback used by the advertiser to manage
     * all the steps of the configuration process
     */
    fun startAdvertising(callback: AdvertisingCallback) {
        advertisingCallback = callback
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build()

        Nearby.getConnectionsClient(applicationContext)
            .startAdvertising(
                "adv", appID,  advertisingNearbyCallback, advertisingOptions
            )
            .addOnSuccessListener {
                // We're advertising!
                Log.d("start advertising","  ok")
                advertisingCallback.onAdvertisingStarted()

            }
            .addOnFailureListener {
                advertisingCallback.onError(it)
            }
    }

    /**
     * Starts searching for advertisers
     * Called on the phone to configure the advertiser
     * @param callback: DiscoveryCallback. Used by the discoverer to manage all the steps of
     * the process
     */
    fun startDiscovery(callback: DiscoveryCallback) {
        discoveryCallback = callback
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        Nearby.getConnectionsClient(applicationContext)
            .startDiscovery(appID, object: EndpointDiscoveryCallback(){
                override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                    discoveryCallback.onDeviceFound(endpointId, info.endpointName)
                    //connectToEndpoint(endpointId)

                }

                override fun onEndpointLost(p0: String) {
                }
            }, discoveryOptions)
            .addOnSuccessListener {
                Log.d("start discovery","  ok")
                discoveryCallback.onDiscoveryStarted()

            }
            .addOnFailureListener {
                Log.d("start discovery","  ex ${it.localizedMessage}")
                discoveryCallback.onError(it)
            }

    }

    /**
     * Connects the discoverer to a specific advertiser.
     * @param endpointId: String. Identifier of the advertiser requested for the connection
     */
    fun connectToEndpoint(endpointId: String){
        Nearby.getConnectionsClient(applicationContext)
            .requestConnection("discov", endpointId, discoveryNearbyCallback)
            .addOnSuccessListener {
                Log.d("connected","  ok")

            }
            .addOnFailureListener {
                Log.d("connected",it.localizedMessage)

            }
    }

    /**
     * Sends WiFi credentials from Advertiser to Discoverer
     * @param chosenOne: WifiScanResult. Wifi Network you want to connect to.
     * @param callback: NetworkCallback. Callback used to manage the result of the communication
     */
    fun sendWifiCredentials(chosenOne: WifiScanResult, callback: NetworkCallback) {

        networkCallback = callback

        if (currentConnectedEndpointId != null) {

            // Object wrapping WiFi credentials
            val credentials = WifiCredentialsPayload().apply {
                this.result = chosenOne
                this.password = chosenOne.password
            }

            val genericPayload = GenericPayload().apply {
                id = HeadlessWifiManagerConstants.PAYLOAD_CHOSEN_WIFI_ID
                theChosenOne = credentials
            }

            // Byte array that contains a serialized object of WiFi credentials to send over Nearby
            val payload = Gson().toJson(genericPayload).toByteArray()

            Nearby.getConnectionsClient(applicationContext)
                .sendPayload(
                    currentConnectedEndpointId!!,
                    Payload.fromBytes(payload)
                )
        } else {
            // Endpoint ID was null
            advertisingCallback.onError(Exception("Endpoint ID is null"))
        }
    }

    /**
     * Closes the current connection and stops discovery
     */
    fun abortProcedure() {
        Nearby.getConnectionsClient(applicationContext).stopDiscovery()
        Nearby.getConnectionsClient(applicationContext).stopAllEndpoints()
    }

    /**
     * Connects the Hub to WiFi with the given credentials
     *
     */
    private fun connectAdvertiserToWifi(payload: GenericPayload) {
        // Call after successfully connection to WiFi
        WifiUtils.withContext(applicationContext)
            .connectWith(payload.theChosenOne!!.result!!.SSID, payload.theChosenOne!!.password)
            .setTimeout(30000)
            .onConnectionResult {
                sendWifiConnectionAck(payload.theChosenOne!!.result!!.SSID, it)

                if (it){
                    advertisingCallback.onSuccess()
                }else {
                    advertisingCallback.onError(Exception("Unable to connect to WiFi"))
                }
            }.start()
    }

    private fun sendWifiConnectionAck(name: String, ackResult: Boolean) {
        val genericPayload = GenericPayload().apply {
            id = HeadlessWifiManagerConstants.PAYLOAD_WIFI_ACK_ID
            connectionAck = WifiConnectionAck().apply {
                SSID = name
                result = ackResult
            }
        }

        val payload = Gson().toJson(genericPayload).toByteArray()
        Nearby.getConnectionsClient(applicationContext).sendPayload(
                currentConnectedEndpointId!!,
                Payload.fromBytes(payload)
            )
    }

    /**
     * Populates a list of WiFi ScanResults trough system's WifiManager
     */
    interface ScanResultListener {
        fun onScanResultAvailable(results: List<ScanResult>)
    }

    private fun getWifiScanResults(listener: ScanResultListener){
        WifiUtils.withContext(applicationContext).scanWifi {
            val filteredScanResults = it.filter { it.SSID != "" }
            listener.onScanResultAvailable(filteredScanResults)

        }.start()
    }



    /**
     * NEARBY CALLBACKS
     * Internal callbacks used by Nearby Connections API
     */
    private var advertisingNearbyCallback = object: ConnectionLifecycleCallback() {
        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    currentConnectedEndpointId = endpointId

                    Log.d("connection result","  ok")
                    getWifiScanResults(object : ScanResultListener {
                        override fun onScanResultAvailable(scanResults: List<ScanResult>) {
                            val genericPayload = GenericPayload().apply {
                                id = HeadlessWifiManagerConstants.PAYLOAD_SCAN_RESULTS_ID
                                results = ScanResultConverter.scanResultsToWifiScanResults(scanResults)
                            }

                            val payload = Gson().toJson(genericPayload).toByteArray()
                            Nearby.getConnectionsClient(applicationContext)
                                .sendPayload(
                                    currentConnectedEndpointId!!,
                                    Payload.fromBytes(payload)
                                )
                        }
                    })
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.d("connection result"," rejected")

                }
                else -> {
                }
            }
        }

        override fun onDisconnected(p0: String) {
        }

        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Nearby.getConnectionsClient(applicationContext).acceptConnection(endpointId,
                object : PayloadCallback() {
                    override fun onPayloadReceived(p0: String, payload: Payload) {
                        Log.d("onPayloadReceived","  ok")
                        val payloadBytes = payload.asBytes()

                        // Connect to Wifi here
                        if (payloadBytes != null) {
                            val genericPayload = Gson().fromJson(String(payloadBytes), GenericPayload::class.java)
                            when {
                                genericPayload.id == HeadlessWifiManagerConstants.PAYLOAD_CHOSEN_WIFI_ID -> connectAdvertiserToWifi(genericPayload)
                            }
                        } else {
                            // Payload was empty
                            advertisingCallback.onError(Exception("Nearby payload was empty"))
                        }

                    }

                    override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
                    }

                })
        }
    }

    private fun reactToNetworkAck(payload: GenericPayload?) {
        if (payload != null) {
            val ack = payload.connectionAck
            if (ack!!.result) {
                networkCallback.onConnected(ack.SSID)
            } else {
                networkCallback.onError(Exception("Unable to connect to WiFi"))
            }
        } else {
            networkCallback.onError(Exception("WiFi ack payload was null"))
        }
    }

    private var discoveryNearbyCallback = object: ConnectionLifecycleCallback() {

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    Log.d("connection result","  ok")

                    currentConnectedEndpointId = endpointId
                    discoveryCallback.onConnected()
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.d("connection result"," rejected")

                }
                else -> {
                }
            }
        }

        override fun onDisconnected(p0: String) {

        }

        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            Nearby.getConnectionsClient(applicationContext).acceptConnection(endpointId,
                object : PayloadCallback() {
                    override fun onPayloadReceived(p0: String, payload: Payload) {
                        val receivedString = String(payload.asBytes()!!, Charset.defaultCharset())
                        val genericPayload = Gson().fromJson(receivedString, GenericPayload::class.java)

                        when {
                            genericPayload.id == HeadlessWifiManagerConstants.PAYLOAD_SCAN_RESULTS_ID -> discoveryCallback.onNetworkListAvailable(genericPayload.results!!)
                            genericPayload.id == HeadlessWifiManagerConstants.PAYLOAD_WIFI_ACK_ID -> reactToNetworkAck(genericPayload)
                        }
                    }

                    override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
                    }
                })
        }
    }
}

