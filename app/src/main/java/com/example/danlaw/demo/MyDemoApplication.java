package com.example.danlaw.demo;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.multidex.MultiDexApplication;
import android.util.Log;
import android.widget.Toast;

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
import com.example.danlaw.demo.activity.ConnectedActivity;
import com.example.danlaw.demo.activity.MainActivity;
import com.example.danlaw.demo.events.AutoConnectingEvent;
import com.example.danlaw.demo.events.BasicDataReceivedEvent;
import com.example.danlaw.demo.events.ConnectionStatusChangeEvent;
import com.example.danlaw.demo.events.DPidDataReceivedEvent;
import com.example.danlaw.demo.events.DPidRegisteredEvent;
import com.example.danlaw.demo.events.DPidUnregisteredEvent;
import com.example.danlaw.demo.events.EPidDataReceivedEvent;
import com.example.danlaw.demo.events.EPidRegisteredEvent;
import com.example.danlaw.demo.events.OBDDevicesFoundEvent;
import com.example.danlaw.demo.events.ScanStoppedEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;


public class MyDemoApplication extends MultiDexApplication implements AutoConnectApp, IAuthCallback {

    private static final String DEFAULT_API_KEY = "043a1b36163fa53cba313b6d92101035f545d6a0082935134cbcf3398569882647733a57e08189d8514277ef0f13522f";
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
            DPidRegisteredEvent event = new DPidRegisteredEvent();
            event.responseCode = responseCode;
            event.dPid = DPid;
            EventBus.getDefault().post(event);
        }

        @Override
        public void onDataPidUnregistered(int responseCode, int DPid) {
            DPidUnregisteredEvent event = new DPidUnregisteredEvent();
            event.responseCode = responseCode;
            event.dPid = DPid;
            EventBus.getDefault().post(event);
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
            EPidRegisteredEvent event = new EPidRegisteredEvent();
            event.responseCode = responseCode;
            EventBus.getDefault().post(event);
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
                Log.v("BLE Callback - ", "ble enabled status: " + String.valueOf(enabled));
        }
    };
    public Boolean isAppInForeground = false;

    @Override
    public void onCreate() {
        super.onCreate();
        AuthInterface.validateToken(this, DEFAULT_API_KEY, this);
    }

    public BluetoothInterface getBluetoothInterface() throws BleNotSupportedException, SdkNotAuthenticatedException {
        return BluetoothInterface.getInstance(this, iBluetoothCallback);
    }

    public DataLoggerInterface getDataLoggerInterface() throws BleNotSupportedException, SdkNotAuthenticatedException {
        return DataLoggerInterface.getInstance(this, getBluetoothInterface(), iDataLoggerCallback);
    }

    @Override
    public String getNotificationTitle() {
        return "Connected to Datalogger";
    }

    @Override
    public String getNotificationText() {
        return "Click to open Smart Connect";
    }

    @Override
    public int getNotificationIcon() {
        return R.mipmap.ic_launcher;
    }

    @Override
    public PendingIntent getNotificationPendingIntent(String deviceName, String deviceAddress) {
        Intent resultIntent = new Intent(getApplicationContext(), ConnectedActivity.class);
        resultIntent.putExtra("deviceName", deviceName);
        resultIntent.putExtra("deviceAddress", deviceAddress);
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        return PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public boolean isAppInForeground() {
        return isAppInForeground;
    }

    @Override
    public void onAuthenticationResult(int i, String s) {

    }
}
