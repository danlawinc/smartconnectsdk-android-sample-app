# smartconnectsdk-android-sample-app
This sample app acts as a template to kickstart your android app development. 

To build the project, just **copy the SDK (.aar file) to libs folder** of your project and **replace API_KEY with your key** that was issued to you by Danlaw in ```MyDemoApplication.java``` file.

# Table of Contents  

1. [Features](#features)
2. [Requirements](#requirements)
3. [Installation](#installation)
4. [Authentication](#authentication)
5. [Connecting to Datalogger](#connecting-to-datalogger)
6. [Auto-Connect](#auto-connect)
7. [Basic PIDs](#basic-pids)
8. [Advanced PIDs](#advanced-pids)
9. [Data PIDs](#data-pids)
10. [UDP Events](#udp-events)
11. [FAQ](#faq)
12. [Credits](#credits)


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

# Installation
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

# Authentication
After installing the SDK, **you MUST authenticate it before you can use all the interfaces**. 
To authenticate the SDK you will your android app's ```context```, ```API_KEY``` issued by Danlaw and an implementaion of ```IAuthCallback```. 

```
AuthInterface.validateToken(context, API_KEY, iAuthCallback);
```
Once you get response code 200 in the callback, your app will be ready to access all the features of the SDK

# Connecting to Datalogger
1. Get an instance:

```
DataLoggerInterface interface = DataLoggerInterface.getInstance(this, getBluetoothInterface(), iDataLoggerCallback);
``` 

2. Scan for devices, scan results are delivered by the ```onOBDDeviceFound``` callback:

```interface.scanForDataLoggers(true);```

3. Connect to device using the address returned in the above step:

```interface.connect(address);```

# Auto-Connect
// add this

# Basic PIDs
Requesting and reading PID data from basic channel
```
//requesting
interface.readBasicPidData(DataLoggerInterface.PID_FUEL_LEVEL);

//reading 
@Override
public void onBasicDataReceived (int responseCode, int pid, Object data) {
    if (responseCode == IDataLoggerCallback.RESPONSE_SUCCESS) {
        switch (pid) {
            case DataLoggerInterface.PID_FUEL_LEVEL:
                FuelLevel fuelLevel = (FuelLevel) data; // casting the object to type FuelLevel
                Log.v(TAG,"FUEL: " + String.valueOf(fuelLevel.currentFuelLevel));
                break;

            //Add your case for other pids
            default:
                Log.v(TAG, " basic data received");
                break;
            }
        }
    }
```

# Advanced PIDs
```
//requesting
int requestID = 1;
                    
ArrayList<Integer> list = new ArrayList<>();
list.add(DataLoggerInterface.PID_VEHICLE_SPEED);
list.add(DataLoggerInterface.PID_ENGINE_RPM);
                    
interface.registerDataPid(requestID,list);

//reading
@Override
public void onDataPidDataReceived(int responseCode, int DPid,HashMap<Integer, Object> PID_Data) {
    if (responseCode == RESPONSE_SUCCESS) {
        switch (DPid) {
            case requestID: // int value used to register dpid
                VehicleSpeed speed = (VehicleSpeed) PID_Data.get(DataLoggerInterface.PID_VEHICLE_SPEED);
                EngineRPM engineRPM = (EngineRPM) PID_Data.get(DataLoggerInterface.PID_ENGINE_RPM);
                Log.v(TAG,"Speed: " + String.valueOf(speed.value));
                Log.v(TAG,"RPM: " + String.valueOf(engineRPM.value));
                break;
            default:
                Log.v(TAG,"data received");
                break;
        }
    } else {
        Log.v(TAG, " check response code");
    }
}
```
# Data PIDs
// add this
# UDP Events
// add this


# FAQ
- **Could not find :smart-connect-sdk**  
    
    Make sure the sdk is added to the 'libs' folder of the project, and is added as a dependecy in the build.gradle file.
    
- **Auto connect doesn't work**
    
    Please make sure battery optimization is disabled in the system settings for the app.
    
- **What is the difference between basic and advanced channel?**
- **Advanced PID request failed**


# Credits
SmartConnect sample app and SmartConnectSDK is owned by Danlaw Inc. A valid license is required to use Danlaw’s Smart Connect products. Licenses are issued by Danlaw on an annual basis for a rolling twelve-month effective time period. License fees established by Danlaw are comprised of a baseline minimum fee, plus a per device fee for each active device at the time of the annual Smart Connect license renewal. Please contact mobile@danlawinc.com for the Key and Licensing information.
