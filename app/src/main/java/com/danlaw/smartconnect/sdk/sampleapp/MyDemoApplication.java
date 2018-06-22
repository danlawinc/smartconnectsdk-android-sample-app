package com.danlaw.smartconnect.sdk.sampleapp;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.TaskStackBuilder;

import com.danlaw.mobilegateway.auth.AuthInterface;
import com.danlaw.mobilegateway.auth.IAuthCallback;
import com.danlaw.mobilegateway.bluetooth.BluetoothInterface;
import com.danlaw.mobilegateway.bluetooth.IBluetoothCallback;
import com.danlaw.mobilegateway.datalogger.AutoConnectApp;
import com.danlaw.mobilegateway.datalogger.DataLoggerInterface;
import com.danlaw.mobilegateway.datalogger.IDataLoggerCallback;
import com.danlaw.mobilegateway.datalogger.model.Message;
import com.danlaw.mobilegateway.exception.BleNotSupportedException;
import com.danlaw.mobilegateway.exception.SdkNotAuthenticatedException;
import com.danlaw.smartconnect.sdk.sampleapp.activity.ConnectedActivity;
import com.danlaw.smartconnect.sdk.sampleapp.events.AutoConnectingEvent;
import com.danlaw.smartconnect.sdk.sampleapp.events.BasicDataReceivedEvent;
import com.danlaw.smartconnect.sdk.sampleapp.events.BluetoothEnabledEvent;
import com.danlaw.smartconnect.sdk.sampleapp.events.ConnectionStatusChangeEvent;
import com.danlaw.smartconnect.sdk.sampleapp.events.DPidDataReceivedEvent;
import com.danlaw.smartconnect.sdk.sampleapp.events.EPidDataReceivedEvent;
import com.danlaw.smartconnect.sdk.sampleapp.events.OBDDevicesFoundEvent;
import com.danlaw.smartconnect.sdk.sampleapp.events.ScanStoppedEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * If an app wants to implement autoconnect, it must create
 * an application class that extends from android's application class and implement autoconnect
 */
public class MyDemoApplication extends MultiDexApplication implements AutoConnectApp, IAuthCallback {

    private static final String DEFAULT_API_KEY = "043a1b36163fa53cba313b6d92101035f545d6a0082935134cbcf3398569882647733a57e08189d8514277ef0f13522f";
    public boolean isAppInForeground = false;

    @Override
    public void onCreate() {
        super.onCreate();

        // Validate token must be the first call to the sdk.
        AuthInterface.validateToken(this, DEFAULT_API_KEY, this);
    }

    private IDataLoggerCallback iDataLoggerCallback = new IDataLoggerCallback() {
        @Override
        public void onOBDDeviceFound(String deviceName, String deviceAddress) {
            OBDDevicesFoundEvent obdDevicesFoundEvent = new OBDDevicesFoundEvent();
            obdDevicesFoundEvent.deviceName = deviceName;
            obdDevicesFoundEvent.deviceAddress = deviceAddress;
            EventBus.getDefault().post(obdDevicesFoundEvent);
        }

        @Override
        public void onConnectionStatusChange(int responseCode, int connectionStatus) {
            ConnectionStatusChangeEvent event = new ConnectionStatusChangeEvent();
            event.responseCode = responseCode;
            event.connectionStatus = connectionStatus;
            EventBus.getDefault().post(event);
        }

        @Override
        public void onAutoConnecting(String deviceName, String deviceAddress) {
            AutoConnectingEvent event = new AutoConnectingEvent();
            event.deviceName = deviceName;
            event.deviceAddress = deviceAddress;
            EventBus.getDefault().post(event);
        }

        @Override
        public void onPasswordChange(int responseCode) {
        }

        @Override
        public void onBasicDataReceived(int responseCode, int pid, Object data) {
            BasicDataReceivedEvent event = new BasicDataReceivedEvent();
            event.responseCode = responseCode;
            event.pid = pid;
            event.data = data;
            EventBus.getDefault().post(event);
        }

        @Override
        public void onDataPidRegistered(int responseCode, int DPid) {
        }

        @Override
        public void onDataPidUnregistered(int responseCode, int DPid) {
        }

        @Override
        public void onDataPidDataReceived(int responseCode, int DPid, HashMap<Integer, Object> PID_Data) {
            DPidDataReceivedEvent event = new DPidDataReceivedEvent();
            event.responseCode = responseCode;
            event.DPid = DPid;
            event.PID_Data = PID_Data;
            EventBus.getDefault().post(event);
        }

        @Override
        public void onEventPidRegistered(int responseCode) {
        }

        @Override
        public void onEventPidUnregistered(int responseCode) {
        }

        @Override
        public void onEventPidDataReceived(int responseCode, int EPid, Object data) {
            EPidDataReceivedEvent event = new EPidDataReceivedEvent();
            event.responseCode = responseCode;
            event.EPid = EPid;
            event.data = data;
            EventBus.getDefault().post(event);
        }

        @Override
        public void onDataTransfer(int type, Message message) {
        }

        @Override
        public void onDataTransfer(int type, byte[] rawBytes) {
        }

        @Override
        public void onConnectedToBackOffice(boolean b, int i) {
        }

        @Override
        public void onScanStopped(boolean scanTimeOut) {
            ScanStoppedEvent event = new ScanStoppedEvent();
            event.scanTimeOut = scanTimeOut;
            EventBus.getDefault().post(event);
        }

        @Override
        public void onWifiAdded(boolean wifiAdded) {
        }

        @Override
        public void onWifiDeleted(boolean wifiDeleted) {
        }

        @Override
        public void onWifiList(ArrayList<String> SSIDs) {
        }
    };
    IBluetoothCallback iBluetoothCallback = new IBluetoothCallback() {
        @Override
        public void onBluetoothEnabled(boolean enabled) {
            BluetoothEnabledEvent event = new BluetoothEnabledEvent();
            event.isEnabled = enabled;
            EventBus.getDefault().post(event);
        }
    };

    public BluetoothInterface getBluetoothInterface() throws BleNotSupportedException, SdkNotAuthenticatedException {
        return BluetoothInterface.getInstance(this, iBluetoothCallback);
    }

    public DataLoggerInterface getDataLoggerInterface() throws BleNotSupportedException, SdkNotAuthenticatedException {
        return DataLoggerInterface.getInstance(this, getBluetoothInterface(), iDataLoggerCallback);
    }

    @Override
    public String getNotificationTitle() {
        return "Connected to DataLogger";
    }

    @Override
    public String getNotificationText() {
        return "Click to open the app";
    }

    @Override
    public int getNotificationIcon() {
        return R.mipmap.ic_launcher;
    }

    @Override
    public PendingIntent getNotificationPendingIntent(String deviceName, String deviceAddress) {
        // Construct the PendingIntent for your Notification
        Intent resultIntent = new Intent(getApplicationContext(), ConnectedActivity.class);
        resultIntent.putExtra("deviceName", deviceName);
        resultIntent.putExtra("deviceAddress", deviceAddress);

        // clicking on notification will skip main activity and directly take the user to connected activity screen.
        // clicking back will take the user straight to launcher which is not a good user experience.
        // task stack builder, helps to synthetically creates a back stack and enables consistent user experience
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // This uses android:parentActivityName and
        // android.support.PARENT_ACTIVITY meta-data by default
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public boolean isAppInForeground() {
        return isAppInForeground;
    }

    @Override
    public void onAuthenticationResult(int i, String s) {

    }
}
