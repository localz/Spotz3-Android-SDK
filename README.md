<a href="http://www.localz.co/"><img alt="Localz logo" align="right" width="50" height="50" src="http://www.localz.co/assets/images/logo-round.png" /></a> Spotz Android SDK
=================

[Spotz](http://spotz.localz.co/) is a user engagement platform that utilizes Bluetooth Low Energy. You can create any 'Spot' you want - an exhibit, a room, an event, or even an entire street. 

The Spotz Android SDK allows your Android app to detect when it is in range of your Spotz and receive payload data - e.g. detailed information about an exhibit, promotional offers, media, it can be anything!

Changelog
=========

**1.3.1**
* Fixed triggering on Spotz that did not exist for the application.

**1.3.0**
* Initial public release.

What does the sample app do?
============================

The app simply tells you if you are in range, or out of range of a Spot.

When a Spot is in range, the app will also display any data associated with that Spot.

How to run the sample app
=========================

The sample app requires devices running Android 4.3 or newer.

  1. Clone the repository:
  
        git clone git@github.com:localz/spotz-sdk-android.git

  2. Import the project:
    
    If you're using **Android Studio**, simply 'Open' the project.

    If you're using **Eclipse ADT**, in your workspace do File -> Import -> General -> Existing Projects into Workspace.
    
    *The project targets Android 4.4 (API level 19) so check you have this version in your Android SDK.*
    
  3. Define a Spot using the [Spotz console](http://spotz.localz.com). Don't forget to add a beacon to your Spot. If you don't have a real beacon, don't worry, you can use the Beacon Toolkit app:
  
    <a href="https://itunes.apple.com/us/app/beacon-toolkit/id838735159?ls=1&mt=8">
    <img alt="Beacon Toolkit on App Store" width="100" height="33"
         src="http://localz.co/blog/wp-content/uploads/2014/03/app-store-300x102.jpg" />
  </a>
    
  Hopefully when Android supports peripheral mode in Android L, we will also have an Android Beacon Toolkit!

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
        compile 'com.localz.spotz.sdk:spotz-sdk-android:2.0.2@aar'
        compile 'com.localz.spotz.sdk:spotz-sdk-api:1.2.2'
        compile 'com.localz.proximity.ble:ble-sdk-android:1.1.2@aar'
        compile 'com.google.android.gms:play-services:6.5.+'
        ...
    }

If you're a **Maven** user you can include the library in your pom.xml:

    ...
    <dependency>
      <groupId>com.localz.spotz.sdk</groupId>
      <artifactId>spotz-sdk-android</artifactId>
      <version>2.0.2</version>
      <type>aar</type>
    </dependency>
    
    <dependency>
      <groupId>com.localz.spotz.sdk</groupId>
      <artifactId>spotz-sdk-api</artifactId>
      <version>1.2.2</version>
    </dependency>
    
    <dependency>
      <groupId>com.localz.proximity.ble</groupId>
      <artifactId>ble-sdk-android</artifactId>
      <version>1.1.2</version>
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

- ble-sdk-android-1.1.2.jar
- google-http-client-1.19.0.jar
- google-http-client-gson-1.19.0.jar
- gson-2.3.jar
- spotz-sdk-android-2.0.2.jar
- spotz-sdk-api-1.2.2.jar

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

  2. Define the following service in your AndroidManifest.xml:

        <service android:name="com.localz.proximity.ble.services.BleHeartbeat" />
        <service android:name="com.localz.spotz.sdk.geofence.GeofenceTransitionsIntentService"/>
        
  3. Initialize the SDK by providing your application ID and client key (as shown on Spotz console):
  
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
  
Your project is now ready to start using the Spotz SDK!

---

###Scan for Spotz

  To start scanning for Spotz, use one of these:
  
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

  To listen for when the device enters a Spot, define a <code>BroadcastReceiver</code> that filters for action <code>\<your package\>.SPOTZ_ON_SPOT_ENTER</code> e.g. <code>com.foo.myapp.SPOTZ_ON_SPOT_ENTER</code>
    You can find your package name in AndroidManifest.xml file:

        <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="com.foo.myapp" >
        
  Here's an example (don't forget to unregister your BroadcastReceiver when no longer needed):

        BroadcastReceiver enteredSpotBroadcastReceiver = new OnEnteredSpotBroadcastReceiver();
        registerReceiver(enteredSpotBroadcastReceiver,
                new IntentFilter(getPackageName() + ".SPOTZ_ON_SPOT_ENTER"));

        ...

        public class OnEnteredSpotBroadcastReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                Spot spot = (Spot) intent.getSerializableExtra(Spotz.EXTRA_SPOTZ);
                
                // Do something with the Spot here!    
            }
        }

  Or if you prefer, you can define your BroadcastReceiver in AndroidManifest.xml:

        <receiver android:name="com.foo.app.services.OnEnteredSpotBroadcastReceiver" >
            <intent-filter>
                <action android:name="com.foo.app.SPOTZ_ON_SPOT_ENTER" />
            </intent-filter>
        </receiver>
        
#### On Spot Exit
        
  To listen for when the device exits a Spot, define a <code>BroadcastReceiver</code> that filters for action <code>\<your package\>.SPOTZ_ON_SPOT_EXIT</code> e.g. <code>com.foo.myapp.SPOTZ_ON_SPOT_EXIT</code>

  Here's an example (don't forget to unregister your BroadcastReceiver when no longer needed):

        BroadcastReceiver exitedSpotBroadcastReceiver = new OnExitedSpotBroadcastReceiver();
        registerReceiver(exitedSpotBroadcastReceiver,
                new IntentFilter(getPackageName() + ".SPOTZ_ON_SPOT_EXIT"));

        ...

        public class OnExitedSpotBroadcastReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                Spot spot = (Spot) intent.getSerializableExtra(Spotz.EXTRA_SPOTZ);
    
                // Do something with the Spot here!
            }
        }

  Or if you prefer, here is the AndroidManifest.xml version:

        <receiver android:name="com.foo.app.services.OnExitedSpotBroadcastReceiver" >
            <intent-filter>
                <action android:name="com.foo.app.SPOTZ_ON_SPOT_EXIT" />
            </intent-filter>
        </receiver>
 
 #### On 3rd party Integration response received

  To listen for when response received from third party integration systems, define a <code>BroadcastReceiver</code> that filters for action <code>\<your package\>.SPOTZ_ON_INTEGRATION_RESPONDED</code> e.g. <code>com.foo.myapp.SPOTZ_ON_INTEGRATION_RESPONDED</code>
    You can find your package name in AndroidManifest.xml file:

        <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="com.foo.myapp" >
        
  Here's an example (don't forget to unregister your BroadcastReceiver when no longer needed):

        BroadcastReceiver enteredSpotBroadcastReceiver = new OnEnteredSpotBroadcastReceiver();
        registerReceiver(enteredSpotBroadcastReceiver,
                new IntentFilter(getPackageName() + ".SPOTZ_ON_INTEGRATION_RESPONDED"));

        ...

        public class OnIntegrationRespondedBroadcastReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                String integrationResponse = (String) intent.getSerializableExtra(Spotz.EXTRA_INTEGRATION);
                
                // Do something with the response from 3rd party systems!
                // As response specific to the Response is specific to the 
            }
        }

  Or if you prefer, you can define your BroadcastReceiver in AndroidManifest.xml:

        <receiver android:name="com.foo.app.services.OnIntegrationRespondedBroadcastReceiver" >
            <intent-filter>
                <action android:name="com.foo.app.SPOTZ_ON_INTEGRATION_RESPONDED" />
            </intent-filter>
        </receiver>
        


Advanced Features
=================
#### Monitoring subset of spotz

You might want to monitor not all Spotz but subset of spotz in your application. In this case, on [Spotz console](http://spotz.localz.com) for the spotz that you want to monitor, you can set an attribute (or few attributes) with the value. Later when initialising Spotz android SDK, you can pass attribute(s) name and value(s) to only monitor for the matching spotz. 
In this case, SDK initialization will be similar to the following:
<pre>
        <b>Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("show", "yes"); 
        attributes.put("city", "Melbourne");</b>     
        Spotz.getInstance().initialize(context, // Your context
                "your-application-id",          // Your application ID goes here
                "your-client-key",              // Your client key goes here
                <b>attributes,</b>
                null,
                new InitializationListenerAdapter() {
                    @Override
                    public void onInitialized() {
                        // Now that we're initialized, we can start scanning for Spotz here 
                    }
                }
        );
</pre>

#### Integration with 3rd party systems

[Spotz integration guide] (http://spotz.localz.com) introduces the concept and provides details of how to add integration to spotz. Sometimes you might want to provide indentity of the user that uses your application to the system that you integrate with. This is achieve by provide the identity attributes to Spotz when initialising Spotz SDK. E.g.:
<pre>
        <b>final DeviceUpdateIdsPutRequest updateIdsRequest = new DeviceUpdateIdsPutRequest();
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
                <b>updateIdsRequest</b>,
                new InitializationListenerAdapter() {
                    @Override
                    public void onInitialized() {
                        // Now that we're initialized, we can start scanning for Spotz here 
                    }
                }
        );
</pre>

Contribution
============

For bugs, feature requests, or other questions, [file an issue](https://github.com/localz/spotz-sdk-android/issues/new).

License
=======

Copyright 2015 [Localz Pty Ltd](http://www.localz.com/)

 
