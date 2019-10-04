# smartconnectsdk-android-sample-app
This sample app is quick guide on how to start using SmartConnectSDK in your app.

# Feature
- [x] Scan Devices
- [x] Bluetooth Enable/Disable
- [x] Autoconnect Devices
- [x] Connect/Disconnect Devices
- [x] Set Favorite/Forget Devices
- [x] Firmware Update
- [x] Register/Unregister Pids
- [x] Create/Remove Notifications
- [x] Configure/Remove wi-fi



# Requirements
- Minimum Android API level 19
- Android Studio


# Installation
- Copy the SDK (.aar file) to libs folder of your project. 
- Replace ApiKey issued by Danlaw in 'MyDemoApplication.java' file.


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
