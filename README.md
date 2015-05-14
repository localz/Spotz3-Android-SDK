<a href="http://www.localz.co/"><img alt="Localz logo" align="right" width="50" height="50" src="http://www.localz.co/assets/images/logo-round.png" /></a> Spotz Android SDK
=================

[Spotz](http://spotz.localz.co/) is a user engagement platform that utilizes Bluetooth Low Energy. You can create any 'Spot' you want - an exhibit, a room, an event, or even an entire street. 

The Spotz Android SDK allows your Android app to detect when it is in range of your Spotz and receive payload data - e.g. detailed information about an exhibit, promotional offers, media, it can be anything!

Changelog
=========

**3.0.0**	
* Added running on background support.  
* Added ranging support.  

**2.0.2**	
* Added geofence support.	
* Added monitoring of subset of spotz.  
* Added integration with 3rd party systems support.
*  
 
**1.3.1**	
* Fixed triggering on Spotz that did not exist for the application.
*

**1.3.0**	
* Initial public release.
*

What does the sample app do?
============================

The app simply tells you if you are in the proximity of a Spot. 

If you are in the proximity of a Spot, you will receive notification. If you open the app, you will also be able to see any data associated with that Spot. Further, if you define a Spot as "ranging", you will also see distance to the closest beacon in the spot. 

Monitoring will continue even if activity is exited, or even phone rebooted. 

How to run the sample app
=========================

The sample app requires devices running Android 4.3 or newer.

  1. Clone the repository:
  
        git clone git@github.com:localz/spotz-sdk-android.git

  2. Import the project:
    
    If you're using **Android Studio**, simply 'Open' the project.

    If you're using **Eclipse ADT**, in your workspace do File -> Import -> General -> Existing Projects into Workspace first for google-play-services-lib library project and then for the main project.
    
    *The project targets Android 4.4 (API level 19) so check you have this version in your Android SDK.*
    
  3. Define a Spot using the [Spotz console](http://spotz.localz.com). Don't forget to add a beacon to your Spot. If you don't have a real beacon, don't worry, you can use the Beacon Toolkit app:
  
    <a href="https://itunes.apple.com/us/app/beacon-toolkit/id838735159?ls=1&mt=8">
    <img alt="Beacon Toolkit on App Store" width="100" height="33"
         src="http://localz.co/blog/wp-content/uploads/2014/03/app-store-300x102.jpg" />
    </a>    
    As Android L now supports peripheral, we will have version of Android Beacon Toolkit sometime soon!

  4. Insert your Spotz application ID and client key into MainActivity.java - these can be found in the Spotz console under your application. Be sure to use the *Android* client key:

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

If you're a **Gradle** user you can easily include the library by specifying it as
a dependency in your build.gradle script:

    allprojects {
        repositories {
            maven { url "http://localz.github.io/mvn-repo" }
            ...
        }
    }
    ...
    dependencies {
        compile 'com.localz.spotz.sdk:spotz-sdk-android:2.0.6@aar'
        compile 'com.localz.spotz.sdk:spotz-sdk-api:1.3.2'
        compile 'com.localz.proximity.ble:ble-smart-sdk-android:1.0.1@aar'
        compile 'com.google.android.gms:play-services:6.5.+'
        ...
    }

If you're a **Maven** user you can include the library in your pom.xml:

    ...
    <dependency>
      <groupId>com.localz.spotz.sdk</groupId>
      <artifactId>spotz-sdk-android</artifactId>
      <version>2.0.6</version>
      <type>aar</type>
    </dependency>
    
    <dependency>
      <groupId>com.localz.spotz.sdk</groupId>
      <artifactId>spotz-sdk-api</artifactId>
      <version>1.3.2</version>
    </dependency>
    
    <dependency>
      <groupId>com.localz.proximity.ble</groupId>
      <artifactId>ble-smart-sdk-android</artifactId>
      <version>1.0.1</version>
      <type>aar</type>
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
    
You will also need add dependency to google play services. Google play services is not available via public maven repositories. You will need to create package (apklib or aar), load to your local maven repository and then use it as reference in your pom.xml. The following tool should help: [https://github.com/simpligility/maven-android-sdk-deployer/](https://github.com/simpligility/maven-android-sdk-deployer/). 

Otherwise, if you are old school, you can manually copy all the JARs in the libs folder and add them to your project's dependencies. Your libs folder will have at least the following JARs:

- ble-smart-sdk-android-1.0.1.jar
- google-http-client-1.19.0.jar
- google-http-client-gson-1.19.0.jar
- gson-2.3.jar
- spotz-sdk-android-2.0.6.jar
- spotz-sdk-api-1.3.2.jar

and also add "google play services lib" library project to your project. For instructions refer to [http://developer.android.com/google/play-services/setup.html](http://developer.android.com/google/play-services/setup.html). Select "Using Eclipse with ADT". 

How to use the SDK
==================

**Currently only devices that support Bluetooth Low Energy (generally Android 4.3 API level 18 or newer) are able to make use of the Spotz SDK**. You can still include the SDK on devices that don't support Bluetooth Low Energy, but calling any scan methods will throw an exception - see footer note in [Scan for Spotz](#scan-for-spotz) on how to deal with this.

There are only 3 actions to implement - **initialize, scan, and listen!**

*Refer to the sample app code for a working implementation of the SDK.*

###Initialize the SDK

  1. Ensure your AndroidManifest.xml has these permissions:

        <uses-permission android:name="android.permission.INTERNET" />
        <uses-permission android:name="android.permission.BLUETOOTH" />
        <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
        <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/> 
        <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>  
        <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/> 
Note: `android.permission.RECEIVE_BOOT_COMPLETED` permission only required if you want to restart monitoring after phone reboot.

  2. Define the following service in your AndroidManifest.xml:

        <service android:name="com.localz.proximity.ble.services.BleHeartbeat" />
        <service android:name="com.localz.spotz.sdk.geofence.GeofenceTransitionsIntentService"/>  
        
  3. Define the following broadcast receivers in your AndroidManifest.xml:  
    3.1.These broadcast receivers are used internally in Spotz SDK. They must be registered in AndroidManifest file:
    
        <receiver android:name="com.localz.spotz.sdk.OnBeaconDiscoveryFoundReceiver" android:exported="false">
            <intent-filter>
                <action android:name="com.localz.spotz.sdk.LOCALZ_BLE_SCAN_FOUND" />
            </intent-filter>
        </receiver>

        <receiver android:name="com.localz.spotz.sdk.OnBeaconDiscoveryFinishedReceiver"  android:exported="false">
            <intent-filter>
                <action android:name="com.localz.spotz.sdk.LOCALZ_BLE_SCAN_FINISH" />
            </intent-filter>
        </receiver>
        <receiver android:name="com.localz.spotz.sdk.OnGeofenceEnterBroadcastReceiver"  android:exported="false">
            <intent-filter>
                <action android:name="com.localz.spotz.sdk.LOCALZ_BLE_SCAN_FOUND" />
            </intent-filter>
        </receiver>
        <receiver android:name="com.localz.spotz.sdk.OnGeofenceExitBroadcastReceiver" android:exported="false">
            <intent-filter>
                <action android:name="com.localz.spotz.sdk.LOCALZ_BLE_SCAN_FINISH" />
            </intent-filter>
        </receiver>
    3.2.These broadcast receivers are need to be implemented in the application(assuming com.foo.app is a package name of your application com.foo.app.receivers is a java package of your receivers).  
        They will be invoked if device enters or exit a Spot. 
        Example implementation can be found in this sample application. Typical implementation will create a notification.  
        
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
        
    3.3.This receiver only required if you integrated Spotz platform with 3rd party system.The receiver will be invoked when reply is received from 3rd party system. See section "Integration with 3rd party systems" below:
    
        <receiver android:name="com.foo.app.receivers.OnIntegrationRespondedBroadcastReceiver" android:exported="false">
            <intent-filter>
                <action android:name="com.foo.app.SPOTZ_ON_INTEGRATION_RESPONDED" />
            </intent-filter>
        </receiver>

    3.4.This receiver will be invoked when phone rebooted. Register this received only if you required to restart monitoring after reboot.  
   
        <receiver android:name="com.localz.spotz.sdk.OnRebootReceiver" android:exported="false">
            <intent-filter>  
                <action android:name="android.intent.action.BOOT_COMPLETED" />  
            </intent-filter>  
        </receiver>
        
  4. Initialize the SDK by providing your application ID and client key (as shown on Spotz console):
  
        Spotz.getInstance().initialize(context, // Your context
                "your-application-id",          // Your application ID goes here
                "your-client-key",              // Your client key goes here
                null,
                null,
                new InitializationListenerAdapter() {
                    @Override
                    public void onInitialized() {
                        // Now that we're initialized, we can start scanning for Spotz here 
                    }
                }
        );

The SDK will communicate with Spotz server, authenticate, and register device. Then it will download the spotz that you registered on the [Spotz console](http://spotz.localz.com). If you ever change spotz details, you will need to call this method again. 
  
Your project is now ready to start using the Spotz SDK!

---

###Scan for Spotz

  To start scanning for Spotz, use one of these:
  
        // Smart scanning - intensity of scanning will get ballance between battery life and responsivness to beacons 
        Spotz.getInstance().startScanningForSpotz(context, Spotz.ScanMode.SMART);
        
        // Normal scanning - ideal for general use 
        Spotz.getInstance().startScanningForSpotz(context, Spotz.ScanMode.NORMAL);

        // Eager scanning - for when fast Spotz engagement response is required, or if devices are expected to move in and out of range in short time
        Spotz.getInstance().startScanningForSpotz(context, Spotz.ScanMode.EAGER);
        
        // Passive scanning - use if battery conservation is more important than engagement, or if devices are expected to remain in your Spotz for longer periods
        Spotz.getInstance().startScanningForSpotz(context, Spotz.ScanMode.PASSIVE);
        
        // Customize your own scan parameters
        // scanIntervalMs - millisecs between each scan
        // scanDurationMs - millisecs to scan for
        Spotz.getInstance().startScanningForSpotz(context, scanIntervalMs, scanDurationMs);
  
  **Important!** It might be tempting to have very short scanIntervalMs so that your application will be more responsive to beacons. However, in Android 5.1 the change is introduces where intervals less than 60 sec is unlikely to be honoured. You might see the following errors in the adb log: "Suspiciously short interval 30000 millis; expanding to 60 seconds". To support up to second updates when app is in foreground use ranging as described in the Advanced Features section below. 
  
  The SDK will scan for beacons while your app is in the background.
  
  To stop scanning for Spotz:
  
        Spotz.getInstance().stopScanningBeacons(context);
        
   To conserve battery, always stop scanning when not needed. 

  **Important!** Devices that don't support Bluetooth Low Energy will throw unchecked exception <code>DeviceNotSupportedException</code> when calling any of the scan methods. Ensure that the device is supported by using:
  
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // Do scanning!
        }
        else {
            // No BLE support
        }

---

###Listen for Events
  
#### On Spot Enter

  To listen for when the device enters a Spot, define a <code>BroadcastReceiver</code> that filters for action <code>\<your package\>.SPOTZ\_ON\_SPOT_ENTER</code> in AndroidManifest.xml as described in section 3.2.
  
#### On Spot Exit
        
  To listen for when the device exits a Spot, define a <code>BroadcastReceiver</code> that filters for action <code>\<your package\>.SPOTZ\_ON\_SPOT_EXIT</code> in the AndroidManifest.xml as described in section 3.2.  
  
#### On 3rd party Integration response received

  To listen for when response received from third party integration systems, define a <code>BroadcastReceiver</code> that filters for action <code>\<your package\>.SPOTZ\_ON\_INTEGRATION\_RESPONDED</code> in AndroidManifest.xml as described in section 3.2.
        


Advanced Features
=================
#### Restarting monitoring when phone reboots
Spotz SDK support restarting of monitoring for spotz after phone was rebooted. Just declare Broadcast Receiver with intent filter: "android.intent.action.BOOT_COMPLETED" as described in section 3.4. SDK will take care of everything else!

#### Ranging
<<<<<<< HEAD
Ranging is an iOS term. There are two modes that app can be interested in points of interests (Spotz):   
1. Monitoring - SDK will look for spotz with regular, resonably infrequent interval (in minutes) and will notify application when spot is detected. Monitoring does NOT run in your application process and your application notified using Brodcast Receivers. Monitoring is reasonably inexpensive in terms of battery and CPU usage.   
2. Ranging - SDK will scan with the aim of get distance to the beacons in spot. Ranging runs in your process and has to be scheduled by your process. Scheduling is typically very frequent (e.g. every 1 sec). Ranging is very expesive, hence consider carefully when you range and never forget to stop ranging. 
In Spotz Android SDK ranging implemented as following:    
1. You define a beacon on Spotz Console as ranging (Immediate 0-1 meters, Near 0-5 meters, Far 0-50 meters). SDK monitor spotz. When ranging beacon is detected, SDK will calculate the distangeand will only notify that you in range of the Spot if distance is less than you specify on the console.   
2. Once you in range, if you open the app, you will need to schedule ranging, which can be achieve in many different ways. In the sample application this is by having handler scheduling runnable ever 1 sec to range.  
=======
Ranging is an iOS term. There are two ways that application can modes that app can monitor spotz:  
1. Region Monitoring - SDK will look for spotz with regular, reasonably infrequent interval (in minutes) and will notify application when spot is detected. Monitoring does NOT run in your application process and your application notified using Brodcast Receivers. Monitoring is reasonably inexpensive in terms of battery and CPU usage.  
2. Ranging - SDK will return distance to the previously discovered spotz. Ranging runs in your process and has to be scheduled by your process and typically very frequent (e.g. every few sec). Ranging is very expesive, hence consider carefully when you range and never forget to stop ranging.  
In Spotz Android SDK ranging implemented as following:  
1. You define a beacon on Spotz Console as ranging (Immediate 0-1 meters, Near 0-5 meters, Far 0-50 meters). SDK monitor spotz. When ranging beacon is detected, SDK will calculate the distance and will only notify that you in range of the Spot if distance is less than you specify on the console.  
2. Once you in range, if you open the app, you will need to schedule ranging, which can be achieve in many different ways. In the sample application handler schedules runnable ever 1 sec:  

	Handler rangingHandler = new Handler();
	Runnable rangingRunnable = new Runnable() {
		public void run() {
			rangeIfRequired();
		}
	};

and rangeIfRequired() method has actual ranging call:   

	Spotz.getInstance().range(context, new RangingListener() {  
		@Override  
		public void onRangeIterationCompleted(HashMap<String, Double> spotIdsAndDistances) {  
			// process spotIdsAndDistances <key, value> pairs.  
		}  
	});  

 **Important!** Start scanning in onResume() and stop onPause() to avoid unnecessary battery drain.  
Note: calculation of distance is based on rssi and txPower values as broadcasted by beacon. Distance is not exactly scientifically accurate. More accurate value could be derived by averaging distance over number of ranging samples. Spotz SDK uses the following formula for distance:  

	double ratio = rssi * 1.0 / tx;
	if (ratio < 1.0) {
		distance = (float) Math.pow(ratio, 10);
	} else {
		distance = (float) ((0.42093) * Math.pow(ratio, 6.9476) + 0.54992);
	}

#### Monitoring subset of spotz

You might want to monitor not all Spotz but subset of spotz in your application. In this case, on [Spotz console](http://spotz.localz.com) for the spotz that you want to monitor, you can set an attribute (or few attributes) with the value. Later when initialising Spotz android SDK, you can pass attribute(s) name and value(s) to only monitor for the matching spotz. 
In this case, SDK initialization will be similar to the following:  

	Map<String, String> attributes = new HashMap<String, String>();
	attributes.put("show", "yes"); 
	attributes.put("city", "Melbourne");</b>     
	Spotz.getInstance().initialize(context, // Your context
		"your-application-id",          // Your application ID goes here
		"your-client-key",              // Your client key goes here
		attributes,
		null,
		new InitializationListenerAdapter() {
			@Override
			public void onInitialized() {
			// Now that we're initialized, we can start scanning for Spotz here 
			}
		}
	);

#### Integration with 3rd party systems  

[Spotz integration guide] (https://github.com/localz/Spotz-Docs/blob/master/README.md) introduces the concept and provides details of how to add integration to spotz. Sometimes you might want to provide indentity of the user that uses your application to the system that you integrate with. This is achieve by provide the identity attributes to Spotz when initialising Spotz SDK. E.g.:  

	final DeviceUpdateIdsPutRequest updateIdsRequest = new DeviceUpdateIdsPutRequest();
	updateIdsRequest.ids = new DeviceUpdateIdsPutRequest.Ids();
	updateIdsRequest.ids.payload = new HashMap<String, String>();
	updateIdsRequest.ids.payload.put("customerAccount", "user123");
	// the statement above will make customerAccount value "user123" 
	// available to all 3rd party integration systems. 
	// Should you wish to pass the value ONLY to a one 3rd party system, 
	// the syntax is "integrationName.idName", e.g.
	updateIdsRequest.ids.payload.put("zapierWebhook.privateUserId", "#565589"); </b>     
	Spotz.getInstance().initialize(context, // Your context
		"your-application-id",          // Your application ID goes here
		"your-client-key",              // Your client key goes here
		null,
		updateIdsRequest,
		new InitializationListenerAdapter() {
			@Override
			public void onInitialized() {
			// Now that we're initialized, we can start scanning for Spotz here 
			}
		}
	);


Contribution
============

For bugs, feature requests, or other questions, [file an issue](https://github.com/localz/spotz-sdk-android/issues/new).

License
=======

Copyright 2015 [Localz Pty Ltd](http://www.localz.com/)
