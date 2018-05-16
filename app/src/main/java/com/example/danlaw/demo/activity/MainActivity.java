package com.example.danlaw.demo.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.danlaw.mobilegateway.bluetooth.BluetoothInterface;
import com.danlaw.mobilegateway.bluetooth.IBluetoothCallback;
import com.danlaw.mobilegateway.datalogger.DataLoggerInterface;
import com.danlaw.mobilegateway.datalogger.IDataLoggerCallback;
import com.danlaw.mobilegateway.datalogger.model.Message;
import com.danlaw.mobilegateway.exception.BleNotSupportedException;
import com.danlaw.mobilegateway.exception.SdkNotAuthenticatedException;
import com.example.danlaw.demo.R;
import com.example.danlaw.demo.adapter.DeviceListAdapter;
import com.example.danlaw.demo.model.DataLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    DataLoggerInterface dataLoggerInterface;
    ArrayList<DataLogger> devices = new ArrayList<>();
    boolean tryingToConnect = false;
    ListView listView;
    int selectedDevice = -1;
    Button scanButton;
    LottieAnimationView lottieAnimationView;
    DeviceListAdapter devicesAdapter;


    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final List<String> missingPermissions = new ArrayList<>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(getApplicationContext(), permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            requestPermissions(permissions, REQUEST_CODE_ASK_PERMISSIONS);
        }
        scanButton = (Button) findViewById(R.id.scanButton);
        lottieAnimationView = (LottieAnimationView) findViewById(R.id.animation_view);
        lottieAnimationView.setAnimation("ripple_loading_animation.json");
        listView = (ListView) findViewById(R.id.devicesList);
        devicesAdapter = new DeviceListAdapter(getApplicationContext(), devices);
        listView.setAdapter(devicesAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!tryingToConnect) {
                    selectedDevice = position;
                    dataLoggerInterface.connect(devices.get(position).getAddress());
                    view.setBackgroundColor(Color.DKGRAY);
                    Toast.makeText(MainActivity.this, "Initiating connection", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(MainActivity.this, "Connection in progress", Toast.LENGTH_SHORT).show();
            }
        });


        IDataLoggerCallback iDataLoggerCallback = new IDataLoggerCallback() {
            @Override
            public void onOBDDeviceFound(String s, String s1) {
                dataLoggerInterface.scanForDataLoggers(false);
                DataLogger datalogger = new DataLogger(s, s1);
                // TODO: 8/11/2017 add in something like on scan complete;
                devices.add(datalogger);
                lottieAnimationView.cancelAnimation();
                lottieAnimationView.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                devicesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onConnectionStatusChange(int i, int i1) {
                if (i1 == DataLoggerInterface.STATE_CONNECTED)
                    listView.getChildAt(selectedDevice).setBackgroundColor(Color.GREEN);
                else if (i1 == DataLoggerInterface.STATE_CONNECTING)
                    listView.getChildAt(selectedDevice).setBackgroundColor(Color.DKGRAY);
                else
                    listView.getChildAt(selectedDevice).setBackgroundColor(Color.RED);
            }

            @Override
            public void onAutoConnecting(String s, String s1) {

            }

            @Override
            public void onPasswordChange(int i) {

            }

            @Override
            public void onBasicDataReceived(int i, int i1, Object o) {

            }

            @Override
            public void onDataPidRegistered(int i, int i1) {

            }

            @Override
            public void onDataPidUnregistered(int i, int i1) {

            }

            @Override
            public void onDataPidDataReceived(int i, int i1, HashMap<Integer, Object> hashMap) {

            }

            @Override
            public void onEventPidRegistered(int i) {

            }

            @Override
            public void onEventPidUnregistered(int i) {

            }

            @Override
            public void onEventPidDataReceived(int i, int i1, Object o) {

            }

            @Override
            public void onDataTransfer(int i, Message message) {

            }

            @Override
            public void onDataTransfer(int i, byte[] bytes) {

            }

            @Override
            public void onConnectedToBackOffice(boolean b, int i) {

            }

            @Override
            public void onScanStopped(boolean b) {

            }

            @Override
            public void onWifiAdded(boolean b) {

            }

            @Override
            public void onWifiDeleted(boolean b) {

            }

            @Override
            public void onWifiList(ArrayList<String> arrayList) {

            }
        };

        IBluetoothCallback bc = new IBluetoothCallback() {
            @Override
            public void onBluetoothEnabled(boolean b) {
                if (b)
                    Toast.makeText(MainActivity.this, "ble enabled", Toast.LENGTH_SHORT).show();
            }
        };

        BluetoothInterface bi = null;
        try {
            bi = BluetoothInterface.getInstance(getApplicationContext(), bc);
        } catch (BleNotSupportedException e) {
            e.printStackTrace();
        } catch (SdkNotAuthenticatedException e) {
            e.printStackTrace();
        }

        try {
            dataLoggerInterface = DataLoggerInterface.getInstance(getApplicationContext(), bi, iDataLoggerCallback);
        } catch (SdkNotAuthenticatedException e) {
            e.printStackTrace();
        }
        dataLoggerInterface.setScanTime(5000);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                devices.clear();
                listView.setVisibility(View.INVISIBLE);
                lottieAnimationView.setVisibility(View.VISIBLE);
                lottieAnimationView.playAnimation();
                dataLoggerInterface.scanForDataLoggers(true);
            }
        });
    }
}
