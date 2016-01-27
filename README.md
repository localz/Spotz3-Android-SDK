<a href="http://www.localz.co/"><img alt="Localz logo" width="127" height="35" align="right" src="http://localz.com/wp-content/uploads/2015/02/localz_logo.png" /></a> Spotz Android SDK
=================

[Spotz](https://console.localz.io/) is a smartphone engagement platform that simplifies integration of micro-location technologies including Bluetooth Low Energy beacons, NFC and geofence (Location-Based Services, GPS and WiFi).  

A ‘spot’ is a specific area of interaction.  It can be defined as either:
* Absolute location: a geo position and expressed in terms like latitude and longitude
* Relative location: a position relative to real-world objects and expressed in terms like distance from X.

Once a location is set, an action is defined to trigger an event:
* on entry to the spot
* on exit of the spot
* at a specified proximity to a spot 

In the context of a conference experience, a spot could be defined as:
* a building: send a welcome message on entry to the conference venue
* a presentation room: present a survey on exit of a presentation room
* an exhibitor stand: trigger an exchange of contact details on approach to an exhibitor

The Spotz3 Android SDK allows your Android app to detect when it is in range of defined spots and receive payload data such as text, messages, key-pair values, images or videos.  The SDK will operate either in the foreground or background.  Your application does not need to be active for the SDK to trigger experiences. 

Changelog
=========

**1.0.1**
* Updated SDK and sample app to run under Android 6.x.
* Bug fixes and improvements.

**1.0.0**
* Initial public release.

What does the sample app do?
============================

The app triggers a background notification or application event when in proximity of a spot.

You will receive a notification on entry to a spot. When the app is open and in the foreground, you can view data associated with that spot. Further, if you define a spot as "ranging", you can view distance to the spot.

Monitoring will continue even if you exit the app or reboot your phone.

How to run the sample app
=========================

The sample app requires devices running Android 2.3.3 or newer. However, Bluetooth Low Energy (BLE) is only supported on devices running Android 4.3 or newer and equipped with a Bluetooth v4.0 or later chipset.

  1. Clone the repository:
  
        git clone git@github.com:localz/Spotz3-Android-SDK.git

  2. Import the project:
    
    If you're using **Android Studio**, simply 'Open' the project.

    If you're using **Eclipse ADT**, in your workspace do File -> Import -> General -> Existing Projects into Workspace first for google-play-services-lib library project and then for the main project.
    
    *The project targets Android 6.0 (API level 23) so check you have this version in your Android SDK.*
    
  3. Define a spot using the [Spotz console](https://console.localz.io). If using Bluetooth Low Energy, don't forget to add a beacon to your Spot. If you don't have a real beacon, you can use our Beacon Toolkit app to emulate an iBeacon:
  
    <a href="https://itunes.apple.com/us/app/beacon-toolkit/id838735159?ls=1&mt=8">
    <img alt="Beacon Toolkit on App Store" width="100" height="33"
         src="http://localz.wpengine.com/wp-content/uploads/2014/03/app-store-300x102.jpg" />
    </a>    
    As Android L supports BLE peripheral mode, an Android version of our Beacon Toolkit will be released soon. 

  4. Insert your Spotz Application ID and Application Key into MainActivity.java - these can be found in the Spotz console under your application. Be sure to use the *android* client key:

        ...
        Spotz.getInstance().initialize(this,
                "your-application-id", // Your application ID goes here
                "your-client-key", // Your client key goes here
        ...

  5. Run it!


How to add the SDK to your own Project
======================================

Your project must support minimum Android 2.3.3 API level 10.	
Ensure that using ["Android SDK Manager"](http://developer.android.com/tools/help/sdk-manager.html) you downloaded "Google Play Services" Rev.22 or later. 

If you're a **Gradle** user you can easily include the library by specifying it as a dependency in your build.gradle script:

    allprojects {
        repositories {
            maven { url "http://localz.github.io/mvn-repo" }
            ...
        }
    }
    ...
    dependencies {
        compile 'com.localz.spotz.sdk:spotz-api:0.2.8'

        compile 'com.localz.proximity.blesmart:ble-smart-sdk-android:1.0.7@aar'
        or
        compile 'com.localz.proximity.blesmart:ble-smart-sdk-android:1.0.7@jar'

        compile 'com.localz.spotz.sdk:spotz-sdk-android:3.0.4@aar'
        or
        compile 'com.localz.spotz.sdk:spotz-sdk-android:3.0.4@jar'

        // additional dependencies required by SDK
        compile 'com.google.android.gms:play-services-location:8.3.0'
        compile 'com.android.support:support-v4:23.1.1'

        compile 'com.google.code.gson:gson:2.4'
        compile 'com.google.http-client:google-http-client:1.20.0'
        compile 'com.google.http-client:google-http-client-gson:1.20.0'

        compile 'io.reactivex:rxjava:1.0.10'
        compile 'io.reactivex:rxandroid:0.24.0'
        compile 'io.reactivex:rxjava-async-util:0.21.0'
        ...
    }

If you're a **Maven** user you can include the library in your pom.xml:

    ...
    <dependency>
      <groupId>com.localz.spotz.sdk</groupId>
      <artifactId>spotz-sdk-android</artifactId>
      <version>3.0.4</version>
      <type>aar</type> or <type>jar</type>
    </dependency>
    
    <dependency>
      <groupId>com.localz.spotz.sdk</groupId>
      <artifactId>spotz-api</artifactId>
      <version>0.2.8</version>
    </dependency>
    
    <dependency>
      <groupId>com.localz.proximity.blesmart</groupId>
      <artifactId>ble-smart-sdk-android</artifactId>
      <version>1.0.7</version>
      <type>aar</type> or <type>jar</type>
    </dependency>

    ...

    <repositories>
        ...
        <repository>
            <id>Localz mvn repository</id>
            <url>http://localz.github.io/mvn-repo</url>
        </repository>
        ...
    </repositories>
    ...
    
You will also need to add dependencies to google play services and support library. Google play services and support library are not available via public maven repositories. You will need to create a package (apklib or aar), load to your local maven repository and then use it as a reference in your pom.xml. The following tool should help: [https://github.com/simpligility/maven-android-sdk-deployer/](https://github.com/simpligility/maven-android-sdk-deployer/).

If rolling old school, you can manually copy all the JARs in your libs folder and add them to your project's dependencies. Your libs folder will have at least the following JARs:

- spotz-api-0.2.8.jar
- ble-smart-sdk-android-1.0.7.jar
- spotz-sdk-android-3.0.4.jar
- google-http-client-1.20.0.jar
- google-http-client-gson-1.20.0.jar
- gson-2.4.jar
- rxjava-1.0.10.jar
- rxandroid-0.24.0.jar
- rxjava-async-util-0.21.0.jar

and also add "google play services lib" library project to your project. For instructions refer to [http://developer.android.com/google/play-services/setup.html](http://developer.android.com/google/play-services/setup.html). Select "Using Eclipse with ADT". 

How to use the SDK
==================

**Starting with release 3.0.0 of the SDK, devices that do not support Bluetooth Low Energy (generally Android 4.3 API level 18 or newer) are still able to make use of the Spotz SDK**. But only Geofence and NFC spots can be triggered.

There are only 3 actions to implement - **initialize**, **scan**, and **listen**!

*Refer to the sample app code for a working implementation of the SDK.*

###Initialize the SDK

  1. Ensure your AndroidManifest.xml has these permissions:

        <uses-permission android:name="android.permission.INTERNET" />
        <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
        <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
        <uses-permission android:name="android.permission.WAKE_LOCK"/>
        <uses-permission android:name="android.permission.BLUETOOTH"/>
        <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
        <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
        <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
        <uses-permission android:name="android.permission.NFC" />
        <uses-permission android:name="com.google.android.gms.permission.ACTIVITY_RECOGNITION"/>
        <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/> 
Note: `android.permission.RECEIVE_BOOT_COMPLETED` permission is only required if you want to restart monitoring after a phone reboot.

  2. Define the following service in your AndroidManifest.xml:

        <service android:name="com.localz.proximity.ble.services.BleHeartbeat" />
        <service android:name="com.localz.spotz.sdk.geofence.GeofenceTransitionsIntentService"/>
        <service android:name="com.localz.spotz.sdk.geofence.LocationUpdateHeartbeat"/>
        
  3. Define the following broadcast receivers in your AndroidManifest.xml:

    3.1.These broadcast receivers are used internally in Spotz SDK. They must be registered in AndroidManifest file (make sure you use your app's package name):
    
        <receiver android:name="com.localz.spotz.sdk.OnBeaconDiscoveryFoundReceiver" android:exported="false">
            <intent-filter>
                <action android:name="*your.app.package.name*.LOCALZ_BLE_SCAN_FOUND" />
            </intent-filter>
        </receiver>

        <receiver android:name="com.localz.spotz.sdk.OnBeaconDiscoveryFinishedReceiver" android:exported="false">
            <intent-filter>
                <action android:name="*your.app.package.name*.LOCALZ_BLE_SCAN_FINISH" />
            </intent-filter>
        </receiver>

        <receiver android:name="com.localz.spotz.sdk.OnGeofenceEnterBroadcastReceiver" android:exported="false">
            <intent-filter>
                <action android:name="*your.app.package.name*.LOCALZ_GEOFENCE_TRANSITION_ENTER" />
            </intent-filter>
        </receiver>

        <receiver android:name="com.localz.spotz.sdk.OnGeofenceExitBroadcastReceiver" android:exported="false">
            <intent-filter>
                <action android:name="*your.app.package.name*.LOCALZ_GEOFENCE_TRANSITION_EXIT" />
            </intent-filter>
        </receiver>

        <receiver android:name="com.localz.spotz.sdk.OnNfcFoundReceiver" android:exported="false">
            <intent-filter>
                <action android:name="*your.app.package.name*.LOCALZ_NFC_ENTER" />
            </intent-filter>
        </receiver>

    3.2.These broadcast receivers must be implemented in the application (assuming *com.foo.app* is a package name of your application and *com.foo.app.receivers* is a java package of your receivers).
        They will be invoked if a device enters or exits a spot.
        Example implementation can be found in this sample application. A typical implementation will create a notification.
        
        <receiver android:name="com.foo.app.receivers.OnEnteredSpotBroadcastReceiver" android:exported="false" >
            <intent-filter>
                <action android:name="com.foo.app.SPOTZ_ON_SPOT_ENTER" />
            </intent-filter>
        </receiver>

        <receiver android:name="com.foo.app.receivers.OnExitedSpotBroadcastReceiver" android:exported="false">
            <intent-filter>
                <action android:name="com.foo.app.SPOTZ_ON_SPOT_EXIT" />
            </intent-filter>
        </receiver>

        <receiver android:name="com.foo.app.receivers.OnSpotDistanceUpdatedBroadcastReceiver" android:exported="false">
            <intent-filter>
                <action android:name="com.foo.app.SPOT_BEACON_DISTANCE_UPDATED" />
            </intent-filter>
        </receiver>

        *Additionally, your app can register for events triggered when a device enters or exits a Site.*

        <receiver android:name="com.foo.app.receivers.OnEnteredSiteBroadcastReceiver" android:exported="false" >
            <intent-filter>
                <action android:name="com.foo.app.SPOTZ_ON_SITE_ENTER" />
            </intent-filter>
        </receiver>

        <receiver android:name="com.foo.app.receivers.OnExitedSiteBroadcastReceiver" android:exported="false">
            <intent-filter>
                <action android:name="com.foo.app.SPOTZ_ON_SITE_EXIT" />
            </intent-filter>
        </receiver>
        
    3.3.This receiver is only required if you integrated your Spotz Application with a 3rd party system. The receiver will be invoked when a reply is received from a 3rd party system. See section "Integration with 3rd party systems" below:
    
        <receiver android:name="com.foo.app.receivers.OnIntegrationRespondedBroadcastReceiver" android:exported="false">
            <intent-filter>
                <action android:name="com.foo.app.SPOTZ_ON_INTEGRATION_RESPONDED" />
            </intent-filter>
        </receiver>

    3.4.This receiver will be invoked when a phone is rebooted. Register this receiver only if you are required to restart monitoring after reboot.
   
        <receiver android:name="com.localz.spotz.sdk.OnRebootReceiver" android:exported="false">
            <intent-filter>  
                <action android:name="android.intent.action.BOOT_COMPLETED" />  
            </intent-filter>  
        </receiver>
        
  4. Initialize the SDK by providing your application ID and application key (as shown on Spotz console):

    4.1.You can provide optional spot tags and attributes to limit the spots you are interested in:

        Spotz.getInstance().setSpotTags(tags); // SDK will only request spots with matching tags

        Spotz.getInstance().setSpotAttributes(spotAttributes); // SDK will only request spots with matching attributes

    4.2.Initialize the SDK

        Spotz.getInstance().initialize(context, // Your context
                "your-application-id",          // Your application ID goes here
                "your-application-key",         // Your application key goes here
                new InitializationListenerAdapter() {
                    @Override
                    public void onInitialized() {
                        // Now that we're initialized, we can start scanning for Spotz here 
                    }
                },
                true    // you can set this flag to 'true' and the SDK will start scanning automatically after initialisation
        );

The SDK will communicate with Spotz servers, authenticate, and register a device. It will download all spots you registered on the [Spotz console](https://console.localz.io). The SDK will handle any changes you've made to your server configuration.
  
Your project is now ready to start using the Spotz SDK!

---

###Scanning for Spots

  To start scanning for spots, use one of these methods:
  
        // Start scanning for spots in the foreground. These calls should only be used when the app is in the foreground.
        // When any 'start***Scanning' method is called - SDK will start to continuously scan for Geofences and NFC triggers.
        // The difference between various 'start***Scanning' methods is only in the way BLE beacons are scanned.

        // This will schedule SDK to 'wake up' every 'scanIntervalMs' milliseconds and scan for beacons for a period of 'scanDurationMs' milliseconds.
        // Unlike background scanning - foreground scanning may be used with 'scanIntervalMs' less than 60000 milliseconds
        Spotz.getInstance().startForegroundScanning(context, scanIntervalMs, scanDurationMs);

        // Similar to the above, but 'scanIntervalMs' and 'scanDurationMs' are determined by provided ScanMode.
        Spotz.getInstance().startForegroundScanning(context, scanMode);

        // This call will try to use previously used parameters or defaults.
        Spotz.getInstance().startForegroundScanning(context);

        // This will schedule SDK to 'wake up' every 'scanIntervalMs' milliseconds and scan for beacons for a period of 'scanDurationMs' milliseconds.
        // 'scanIntervalMs' of less than 60000 milliseconds is unlikely to be honoured by Android runtime, and will most likely be set to 60000 milliseconds
        Spotz.getInstance().startBackgroundScanning(context, scanIntervalMs, scanDurationMs);

        // Similar to the above, but 'scanIntervalMs' and 'scanDurationMs' are determined by provided ScanMode.
        Spotz.getInstance().startBackgroundScanning(context, scanMode);

        // This call will try to use previously used parameters or defaults.
        Spotz.getInstance().startBackgroundScanning(context);

        // One time scan for beacons for a period of 'scanDurationMs' milliseconds. Does not start Geofence or NFC scanning.
        Spotz.getInstance().scanOnce(context, scanDurationMs);

        // Spotz.ScanMode.NORMAL - normal scanning - ideal for general use
        // Spotz.ScanMode.EAGER - eager scanning - when fast Spotz engagement response is required, or if devices are expected to move in and out of range in a short time
        // Spotz.ScanMode.PASSIVE - passive scanning - use if battery conservation is more important than engagement, or if devices are expected to remain in your Spotz for longer periods
  
  The SDK will scan for beacons while your app is in the background.
  
  To stop scanning for spots:
  
        Spotz.getInstance().stopScanningBeacons(context);
        
  To conserve battery, always stop scanning when not needed.

  Public utility methods:

        // Check if the SDK is initialized
        Spotz.getInstance().isInitialized(context);

        // Check if the SDK is currently scanning for spots
        Spotz.getInstance().isScanningForSpotz(context);

        // Check if Bluetooth LE is supported on this device.
        Spotz.getInstance().hasBleSupport(context);

        // Check if Bluetooth LE is enabled on this device.
        Spotz.getInstance().isBleEnabled(context);

        // Check if NFC is supported on this device.
        Spotz.getInstance().hasNfcSupport(context);

        // Check if NFC is enabled on this device.
        Spotz.getInstance().isNfcEnabled(context);

        // Check if Location service is supported on this device.
        Spotz.getInstance().hasLocationSupport(context);

        // Check if Location service is enabled on this device.
        Spotz.getInstance().isLocationEnabled(context);

        // Get a registered device id
        Spotz.getInstance().getDeviceId(context);

        // Get Project details
        Spotz.getInstance().getProject(context);

        // Get current/active Site details
        Spotz.getInstance().getCurrentSite(context);

        // Get a list of all Project Sites
        Spotz.getInstance().getAvailableSites(context);

        // Get a list of Site Spots (if a Site is not an active Site this method may return an empty list)
        Spotz.getInstance().getSiteSpots(context, siteId);

        // If set to 'true' - will output all debug logs, otherwise - not.
        Spotz.getInstance().setDebug(debug);

  Updating Device specific data:

        // Provide a user identity to be associated with a registered device
        Spotz.getInstance().setDeviceIdentity(context, userIdentity, responseListener);

        // Delete a associated user identity from a registered device
        Spotz.getInstance().deleteDeviceIdentity(context, responseListener);

        // Set device attributes
        Spotz.getInstance().setDeviceAttributes(context, attributes, responseListener);

        // Set device extension data
        Spotz.getInstance().setDeviceExtensions(context, extensions, responseListener);

---

Advanced Features
=================
#### Restarting monitoring when a phone reboots

Spotz SDK supports restarting of monitoring for spots after a phone was rebooted. Just declare a Broadcast Receiver with an intent filter: "android.intent.action.BOOT_COMPLETED" as described in section 3.4. SDK will take care of everything else!

#### Ranging

Ranging is a term originally defined by iOS interactions. There are two modes that an app can operate:

  1. Monitoring - SDK will scan with regular, reasonably infrequent interval (in minutes) and will notify application when a spot is detected. Monitoring does NOT run in your application process and your application is notified using Broadcast Receivers. Monitoring is reasonably inexpensive in terms of battery and CPU usage.

  2. Ranging - SDK will scan with the aim of getting the distance to a spot defined by BLE beacons. Ranging runs in your process and has to be scheduled by your process. Scheduling is typically very frequent (e.g. every 1 sec). Ranging is very expensive, hence consider carefully when you range and never forget to stop ranging.

In Spotz Android SDK ranging is implemented as following:

  1. Define a BLE beacon on Spotz Console as ranging (Immediate 0-1 meters, Near 0-5 meters, Far 0-50 meters). SDK monitors for spots. When a ranging BLE beacon is detected, SDK will calculate the distance and will only notify that you are in range of a Spot if the distance is less than you specify on the console.

  2. Once in range, on app open, you will need to schedule ranging, which can be achieve in many different ways. In the sample application this is done by using 'startForegroundScanning' with a 1 second interval.

 **Important!** Start scanning in onResume() and stop onPause() to avoid unnecessary battery drain.

Note: calculation of distance is based on rssi and txPower values as broadcasted by a BLE beacon. Distance accuracy with vary by environmental conditions. Greater accuracy can be derived by averaging distance over number of ranging samples. Spotz SDK uses the following formula for distance:

	double ratio = rssi * 1.0 / tx;
	if (ratio < 1.0) {
		distance = (float) Math.pow(ratio, 10);
	} else {
		distance = (float) ((0.42093) * Math.pow(ratio, 6.9476) + 0.54992);
	}
If you require greater accuracy and precison, contact the Localz team for a non-public SDK that includes advanced positioning features including dead reckoning and accelerometer augmented positioning. 

#### Monitoring a subset of spots

You might not want to monitor all spots, but a subset of spots in your application. In this case, on [Spotz console](https://console.localz.io) for the spots that you want to monitor, you can define an attribute (or attributes). Later when initialising Spotz Android SDK, you can provide a map of attribute(s) in order to monitor for matching spots only.
In this case, SDK initialization will be similar to the following:  

	Map<String, String> attributes = new HashMap<String, String>();
	attributes.put("show", "yes"); 
	attributes.put("city", "Melbourne");

	Spotz.getInstance().setSpotAttributes(attributes);

	Spotz.getInstance().initialize(context, // Your context
		"your-application-id",          // Your application ID goes here
		"your-application-key",         // Your application key goes here
		new InitializationListenerAdapter() {
			@Override
			public void onInitialized() {
			// Now that we're initialized, we can start scanning for Spotz here 
			}
		},
		false
	);

#### Integration with 3rd party systems  

[Spotz Integration guide] (https://github.com/localz/Spotz-Docs/blob/master/README.md) introduces the concept and provides details of how to add Integrations to Spotz. Spotz platform make it easy for you to re-use Application, Site, Spot and Device data in your Integration. Spotz SDK allows you to associate 3 different types of data with a Device:

 - Device Identity

 - Device attributes

 - Device Integration attributes

Sometimes you might want to provide an Identity of a user that uses your application to the system that you integrate with. This is achieved by associating a user Identity with a Spotz Device via setDeviceIdentity(...) call in Spotz SDK. E.g.:

	Spotz.getInstance().setDeviceIdentity(context, "#565589", responseListener);

The statement above will make Identity value "#565589" available to all 3rd party integration systems. To remove this user-device association (e.g. after user logout) - use deleteDeviceIdentity(...) SDK call:

    Spotz.getInstance().deleteDeviceIdentity(context, responseListener);

You can also define other attributes for a Device, not just the Identity, and they will also be available to all 3rd party integration systems:

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("deviceAttribute", "someValue");

    Spotz.getInstance().setDeviceAttributes(context, attributes, listener);

Should you wish to pass a value ONLY to one 3rd party system, use setDeviceExtensions(context, extensions, responseListener) SDK call:

    DeviceRegisterPostRequest.Extension extension = new DeviceRegisterPostRequest.Extension();

    extension.name = "extensionName";
    extension.type = "extensionType";
    extension.attributes = new HashMap<>();
    extension.attributes.put("extensionSpecificAttribute", "#565589");

    DeviceRegisterPostRequest.Extension[] extensions = new DeviceRegisterPostRequest.Extension[] {extension};

    Spotz.getInstance().setDeviceExtensions(context, extensions, listener);

##### Consuming Integration response

If you would like to re-use the response from your Integrations, you will need to register a receiver for the Integration response:

    <receiver android:name="com.foo.app.receivers.OnIntegrationRespondedBroadcastReceiver" android:exported="false">
        <intent-filter>
            <action android:name="com.foo.app.SPOTZ_ON_INTEGRATION_RESPONDED" />
        </intent-filter>
    </receiver>

Then in your broadcast receiver you can access a "raw" Integration response:

    String integrationResponse = (String) intent.getSerializableExtra(Spotz.EXTRA_INTEGRATION);

Where "integrationResponse" is a String representation of a JSON response and has the following structure:

    {
        "123qwe123qwe": {                   // Id of a Spot which triggered the event (there may be more than one Spot in response)
            "control4localz": {             // Name of an Integration (there may be more than one Integration per Spot)
                "body": "...",              // Response body - whatever response is received from a 3rd party system
                "statusCode": 200,          // Response status code
                "headers": {                // Response headers
                    "content-length": "0",
                    "expires": "-1",
                    "x-powered-by": "ASP.NET",
                    "set-cookie": [
                        "someCookie=f7a7d929498d905a47e47168825efb28c644772e20587fe4f0525d493a8bf452;Path=/;Domain=some-domain.net"
                    ],
                    "date": "Mon, 07 Sep 2015 01:36:54 GMT"
                }
            }
        },
        "date": "2015-09-07T01:36:55.116Z"  // Integration event date and time
    }

Contribution
============

For bugs, feature requests, or other questions, [file an issue](https://github.com/localz/Spotz3-Android-SDK/issues/new).

License
=======

Copyright 2015 [Localz Pty Ltd](http://localz.co/)
