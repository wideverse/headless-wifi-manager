# Headless Wifi Manager
<img src="https://github.com/wideverse/headless-wifi-manager/blob/master/images/demo01.png" width="200"> <img src="https://github.com/wideverse/headless-wifi-manager/blob/master/images/demo02.png" width="200"><img src="https://github.com/wideverse/headless-wifi-manager/blob/master/images/demo03.png" width="200">

<img src="https://github.com/wideverse/headless-wifi-manager/blob/master/images/demo04.png" width="200"><img src="https://github.com/wideverse/headless-wifi-manager/blob/master/images/demo05.png" width="200"> <img src="https://github.com/wideverse/headless-wifi-manager/blob/master/images/demo06.png" width="200">

## Why this library
Imagine the classic "Google Home" situation.

You have an Headless device (your Google Home) that isn't connected to WiFi.
Using your phone, you can configure your Google Home to connect to a specific network and then communicate with it more easily.

## What this library does
This library assumes you have 2 devices.
 * An **advertiser**, your headless device that is totally disconnected to WiFi and any other network. 
 * A **discoverer**, your phone that has a screen indeed (\o/) and can pick a WiFi access point for the advertiser to connect on.
 
Using Android **Nearby API**, the **discoverer** and the **advertiser** communicate without the need to be on the same network using a combination of Wifi hotspots and Bluetooth.

The whole process can be summarized as follows:
 1. The **advertiser** starts advertising its presence to nearby devices;
 2. The **discoverer** connects to an available advertiser and **receives a list of Wifi Networks from it**;
 3. The **discoverer** selects a network and sends its credentials back to the **advertiser**;
 4. The **advertiser** connects to the network with the given credentials;
 5. The **advertiser** sends an acknowledgment with a positive or negative result to the **discoverer**;

At the end of the procedure, the **advertiser** will be connected to the WiFi network and you'll be able to communicate with it, for example via standard HTTP requests.
 
 ## How to use it
1. **Add the JitPack repository to your build file**

 Add it in your root build.gradle at the end of repositories:
```gradle
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

2. **Add the dependency**

```gradle
	dependencies {
	        // AndroidX capable version
	        implementation 'com.github.wideverse:headless-wifi-manager:1.0.0'
	}
```

**Initialize** the main object:



```kotlin
        val headlessWifiManager = HeadlessWifiManager(applicationContext, APP_ID)
```

**APP_ID** is an unique identifier that will allow your discoverers to filter and talk only with your advertisers and not other Nearby devices.

## Device configuration
The following steps differs if you're deploying on your **Advertiser** or your **Discoverer**

### Advertiser
```kotlin
headlessWifiManager.startAdvertising(object: AdvertisingCallback {

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
```

### Discoverer
```kotlin
headlessWifiManager.startDiscovery(object: DiscoveryCallback {
                override fun onDiscoveryStarted() {
                    Log.d(TAG, "Successfully started looking for nearby devices to configure")
                }

                override fun onDeviceFound(endpointId: String,deviceName: String) {
                    Log.d(TAG, "Trying to connect to $deviceName")
                    // Here you can show a list with all the devices available
                    
                    // Let's assume we want to configure the first one discovered
                    headlessWifiManager.connectToEndpoint(endpointId)
                }

                override fun onConnected() {
                    Log.d(TAG, "Sucessifully connected to a hub device")
                    Log.d(TAG, "Now waiting to get WiFi List from advertiser")
                }

                override fun onNetworkListAvailable(results: List<WifiScanResult>) {
                    Log.d(TAG, "Successfully made a connection. Wifi list is available.")
                    
                    // Here you can show a list with all the WiFi network available
                    // that are stored in results
                    
                    // Let's take the first discovered network for semplicity
		    val chosenAccessPoint = results[0]
                    chosenAccessPoint.password = "sherLocked"
                    // If the network doesn't have a protection, leave the password filed empty
                    
                    // Then call sendWifiCredentials to send data back to the advertiser
                    headlessWifiManager.sendWifiCredentials(chosenAccessPoint,
                    object : NetworkCallback {
                        override fun onError(e: Exception) {
                             Log.e(TAG, "An error has occurred")
                        }

                        override fun onConnected(SSID: String) {
                            Log.d(TAG, "ALL DONE! \o/")
                            
                            // Your advertiser is now connected to internet!
                        }
                    })  
                }

                override fun onError(e: Exception) {
                  Log.e(TAG, "An error has occurred.")
                }
            })
            
```

## WifiHelper
This library contains an helper you can use to show users more accurate info about available WiFi networks in a nicer way.

Using the method ```getDrawableFromRSSI(level: Int, protected: Boolean)``` you can directly get the appropriate resource drawable to show the **detected network RSSI** and a **lock if the network is protected** in an immediate way, similar to Android WiFi dialog.

<img src="https://github.com/wideverse/headless-wifi-manager/blob/master/images/demo04.png" width="400">

## WifiScanResult
```WifiScanResult``` is our internal object to pass info about WiFi networks.

We decided to convert system object [ScanResult](https://developer.android.com/reference/android/net/wifi/ScanResult) available in the Android SDK to our [WifiScanResult](https://github.com/wideverse/headless-wifi-manager/blob/master/headlesswifimanager/src/main/java/com/wideverse/headlesswifimanager/data/WifiScanResult.kt).

This allows us to **transfer only the useful data** between advertiser and discoverer and keep the payload simple and small.

You can expect a ```List<WifiScanResults>``` after a successful scanning and pass a ```WifiScanResult``` with a valorized ```password``` field to advertiser to connect on. You can find how the mapping between the two objects is done [here](https://github.com/wideverse/headless-wifi-manager/blob/master/headlesswifimanager/src/main/java/com/wideverse/headlesswifimanager/helper/ScanResultConverter.kt).

If you think more fields of ```ScanResults``` needs to be added to our ```WifiScanResult``` to improve your logic, please feel free to **open a Pull Request**.

# Example
An fully working Example app is available for you on this repo to see how to use the API and for insipration.
When you ```Import``` the project in Android Studio, you will see two modules ```advertiser``` and ```discoverer``` that you can deploy on your testing devices.

The Example app also shows how to handle edge cases like **errors** and **empty WiFi list** received from the advertiser.

The adveriser module DOESN'T have an UI since the device is supposed to be headless. Refer to system logs to figure what's happening.

JavaDoc is available [here](https://wideverse.github.io/headless-wifi-manager/headlesswifimanager/).

# About
This library has been developed at [Wideverse](https://www.wideverse.com/it/home-it/) @ [Polytechnic University of Bari](https://www.poliba.it/) and it's shared under Apache 2.0.
