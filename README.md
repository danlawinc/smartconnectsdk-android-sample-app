# smartconnectsdk-android-sample-app
This sample app acts as a template to kickstart your android app development. 

To build the project, just **copy the SDK (.aar file) to libs folder** of your project and **replace API_KEY with your key** that was issued to you by Danlaw in ```MyDemoApplication.java``` file.

# Features
- [x] Scan Devices
- [x] Enable Bluetooth
- [x] Connect/Disconnect to Devices
- [x] Set Favorite/Forget Devices
- [x] Autoconnect to Favorite Device
- [x] Firmware Update
- [x] Request data pids 
- [x] Register/unregister data pids for continuous updates
- [x] Register/unregister for event updates

# Requirements
- Minimum Android API level 19
- Android Studio

# Step 1: Installation
1. Copy the SDK (.aar file) to libs folder of your project. 
2. Add the following to **top level build.gradle file under repositories**.
```
repositories {
    // your other repos here
    flatDir {
        dirs 'libs'
    }
}
```
3. Add these necessary dependencies to your **app's build.gradle file** and sync your project.
```
implementation fileTree(include: ['*.jar'], dir: 'libs')
api '(name: 'smart-connect-sdk-release', ext: 'aar')'
implementation 'org.apache.commons:commons-lang3:3.8.1'
implementation 'commons-net:commons-net:3.6'
implementation 'org.greenrobot:eventbus:3.1.1'
implementation 'com.android.volley:volley:1.1.0'
implementation 'com.google.android.gms:play-services-location:17.0.0'
implementation 'com.google.guava:guava:25.1-android'
implementation 'org.slf4j:slf4j-api:1.7.25'
```

# Step 2: Authenticating
After installing the SDK, **you MUST authenticate it before you can use all the interfaces**. 
To authenticate the SDK you will your android app's ```context```, ```API_KEY``` issued by Danlaw and an implementaion of ```IAuthCallback```. 

```
AuthInterface.validateToken(context, API_KEY, iAuthCallback);
```
Once you get response code 200 in the callback, you are ready to use the 

# Step 3: Connecting
1. Get an instance:

```
DataLoggerInterface interface = DataLoggerInterface.getInstance(this, getBluetoothInterface(), iDataLoggerCallback);
``` 

2. Scan for devices, scan results are delivered by the ```onOBDDeviceFound``` callback:

```interface.scanForDataLoggers(true);```

3. Connect to device using the address returned in the above step:

```interface.connect(address);```


# Component Library
- AuthInterface: This class AuthInterface provides the entry point for the SDK. The hosting application should call the interface method validateToken to get the SDK authenticated. If the SDK is not authenticated, then none of the services that are offered by the SDK will be available.
- DataLoggerInterface: This class handles communication between the app and the SDK. It provides the outward facing methods for interacting with the Danlaw Android SDK.
- IDataLoggerCallback: This class defines the callback methods that needs to be implemented by the hosting application to receive updates delivered by `DataLoggerInterface`.

# FAQ
- **Could not find :smart-connect-sdk**  
    
    Make sure the sdk is added to the 'libs' folder of the project, and is added as a dependecy in the build.gradle file.
    
- **Auto connect doesn't work**
    
    Please make sure battery optimization is disabled in the system settings for the app.


# Credits
SmartConnect sample app and SmartConnectSDK is owned by Danlaw Inc. A valid license is required to use Danlaw’s Smart Connect products. Licenses are issued by Danlaw on an annual basis for a rolling twelve-month effective time period. License fees established by Danlaw are comprised of a baseline minimum fee, plus a per device fee for each active device at the time of the annual Smart Connect license renewal. Please contact mobile@danlawinc.com for the Key and Licensing information.
