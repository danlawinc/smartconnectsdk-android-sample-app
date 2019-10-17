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
7. [Get PID data (Basic Channel)](#get-pid-data-basic-channel)
8. [Register PID Data for Continuous Updates (Advanced Channel)](#register-pid-data-for-continuous-updates-advanced-channel)
9. [Realtime Events (Advanced Channel)](#realtime-events-advanced-channel)
10. [UDP Events (BLEAP)](#udp-events-bleap)
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
To authenticate the SDK:
1. Pass your android app's ```context```, ```API_KEY``` issued by Danlaw and an implementaion of ```IAuthCallback```.
```
AuthInterface.validateToken(context, API_KEY, iAuthCallback);
```

2. Make sure you get response code 200 in the callback before you try to use the features of the SDK
```
@Override
    public void onAuthenticationResult(int code, String message) {
        if(code == 200){
            Log.d(TAG, "Auth success, sdk is ready to be used");
        } else {
            Log.d(TAG, "Auth Results:" + code + " Message:" + message);
        }
    }

```

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
Smart Connect SDK offers an Auto Connect feature which will make the app connect to the DataLogger automatically.
For this to work, a device must be set as favorite, the connection is triggered automatically everytime the device is discovered during scan.
Auto connect also works in background, when the SDK detects when the mobile device is in the vehicle (Using Android's Activity Recognition) and tries to connect silently in the background.

To enable this feature, follow these steps:
1. Set a device as favorite
```
interface.setFavoriteDevice(deviceName, deviceAddress);
```

2. Create a class that extends from the Application class and implement the ```AutoConnectApp``` callback. Refer to https://developer.android.com/reference/android/app/Application.html for more details.


3. Disable battery optimization for your app in the system settings. (Needed for Android 6.0 and above). Refer to https://developer.android.com/training/monitoring-device-state/doze-standby.html#support_for_other_use_cases for more details.

To disable auto connect:
```
interface.forgetDevice();
```

Check out https://github.com/danlawinc/smartconnectsdk-android-sample-app/blob/master/app/src/main/java/com/danlaw/smartconnect/sdk/sampleapp/MyDemoApplication.java for example.

# Get PID data (Basic Channel)
The request can be made as often as needed, and the data will be returned once for every request.

Data that can be requested:
 - Standard PIDs (id: 0-255)
 - Danlaw's Custom PIDs (id: 256 and over)
 - Please refer the documentation for a complete list of the request IDs and their corresponding return object types.

Here's an example to request fuel level data:
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

# Register PID Data for Continuous Updates (Advanced Channel)
Registering for PID allows to receive data continuously until the request is unregistered. 

A max of 5 PIDs can be registered in a single request.
Data that can be requested:
 - Only Standard PIDs (id: 0-255) are supported for continuous updates.
 - Please refer the documentation for a complete list of the request IDs and their corresponding return object types. 

An example to get continuous updates for the PIDs speed and engine rpm 
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

To unregister, pass the requestID:
```
boolean unregisterResult = interface.unregisterDataPid(requestID);
```

# Realtime Events (Advanced Channel)
Registering for events allows to receive data in real-time when an event such as hard break, hard acceleration, cornering etc., is detected by the datalogger while the vehicle is being driven. 

Realtime events can only be received if the mobile is connected to the Datalogger when the event occurred. 
If the datalogger is not connected to a mobile device, event is delivered as a part of UDP Event the next time a connection is established.

Data that can be requested:
 - Custom events pre defined by Danlaw's communication protocol. 
 - Please refer the documentation for a complete list of the request IDs and their corresponding return object types. 

A max of 5 event PIDs can be registered in a single request.

Here's an example to register hard break and hard acceleration events:
```
ArrayList<Integer> eventPids = new ArrayList<>();
eventPids.add(DataLoggerInterface.PID_EVENT_HARD_BRAKING);
eventPids.add(DataLoggerInterface.PID_EVENT_HARD_ACCEL);
boolean registerationSuccessful = interface.registerEventPid(eventPids);
```

To unregister pass the IDs as a part of arraylist:
```
ArrayList<Integer> eventPids = new ArrayList<>();
eventPids.add(DataLoggerInterface.PID_EVENT_HARD_BRAKING);
boolean unregisterationResult = interface.unregisterEventPid(eventPids);
```


# UDP Events (BLEAP)
Every realtime event that is detected by the datalogger is delivered as an UDP Event by the datalogger, regardless of the datalogger was connected to a mobile when the event occurred or not.

No registeration is required in order to get these events.

The events are erased from the datalogger's memory once it receives a confirmation that the event was received by the client.

Data that can be received:
 - Custom events pre defined by Danlaw's communication protocol. 
 - Please refer the documentation for a complete list of the request IDs and their corresponding return object types. 

By default, the UDP events are delivered to Danlaw Servers, but for dataloggers with the **BLEAP** configuration, the events are delivered to mobile device instead.

Follow these steps in order to receive the UDP Events on your mobile device:
1. Check if the connected device is a BLEAP or not by accessing the static member ```isBleap``` of ```DataLoggerInterface``` class.
 ```
 boolean isBleap = DataLoggerInterface.isBleap;
 ```
 2. **(Optional)** Turn auto acknowledgement off (on by default).
 ```
 bleapInterface.setBleapAutoAcknowledgement(false);
 ```
 
 3. Parse incoming data
 ```
 @Override
 public void onBleapFormattedUDPData(ArrayList<HashMap<Integer, Object>> arrayList, byte ack){
 //process the data
    for (int i = 0; i < arrayList.size(); i++) {
        int id = (int) Objects.requireNonNull(arrayList.get(i).keySet().toArray())[0];
        Object data = arrayList.get(i).get(id);
         switch (id) {
            case PID_EVENT_HARD_BRAKING:
                HardBrakingData hardBrakingData = (HardBrakingData) data;
                Log.v(TAG, "Hard Break Value: " + String.valueOf(hardBrakingData.maxBraking));
                break;
             // handle other cases
        }
    }
    // bleapInterface.sendBleapAcknowledgement(ackByte); // only needed if acknowledgement is turned off in step 2.
 }
 ```

# FAQ
- **Could not find :smart-connect-sdk**  
    
    Make sure the sdk is added to the 'libs' folder of the project, and is added as a dependecy in the build.gradle file.
    
- **Auto connect doesn't work**
    
    Please make sure that:
    * Application is registered under ```android:name=""``` for the ```application``` tag in ```AndroidManifest.xml```
    * Battery optimization is disabled in the system settings for the app
    * Try to authenticate the sdk in the ```onCreate()``` method of the class that extends from ```Application``` to make sure token
    is valid before the app tries to connect in the backgroud.
    * Permission for physical activity ```android.permission.ACTIVITY_RECOGNITION``` is granted to the app (Android 10 and above)
    
- **Continuous Updates/realtime events request failed**

    Although you can register upto 5 PIDs per request, if any of the PIDs is not supported by the vehicle or if data is not available,
    the entire request fails.
    
    Try registering the PIDs/events individually to see which request fails.
    
    For example, instead of registering speed and rpm together in a single request, break it into 2 requests.
    ```
    int id1 = 1;
    ArrayList<Integer> list1 = new ArrayList<>();
    list1.add(DataLoggerInterface.PID_VEHICLE_SPEED);
    
    int id2 = 2
    ArrayList<Integer> list2 = new ArrayList<>();
    list2.add(DataLoggerInterface.PID_ENGINE_RPM);
    
    interface.registerDataPid(id1,list1);
    
    // running the other request after 500ms to avoid getting data logger busy error
    new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               interface.registerDataPid(id2,list2);         
            }
        }, 500);
    
    ```
    
    **NOTE:** Only Standard PIDs(id: 0-255) are supported for continuous updates. Danlaw's Custom PIDs (id: 256 and over) must be
    requested everytime a new value is needed.
  
 - **Same UDP Event is getting received again and again**
 
    Try turning ```setBleapAutoAcknowledgement``` off and call ```sendBleapAcknowledgement``` for every UDP event message you get.
  
    

# Credits
SmartConnect sample app and SmartConnectSDK is owned by Danlaw Inc. A valid license is required to use Danlaw’s Smart Connect products. Licenses are issued by Danlaw on an annual basis for a rolling twelve-month effective time period. License fees established by Danlaw are comprised of a baseline minimum fee, plus a per device fee for each active device at the time of the annual Smart Connect license renewal. Please contact mobile@danlawinc.com for the Key and Licensing information.
