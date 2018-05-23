package com.example.danlaw.demo.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.danlaw.mobilegateway.bluetooth.BluetoothInterface;
import com.danlaw.mobilegateway.datalogger.DataLoggerInterface;
import com.danlaw.mobilegateway.exception.BleNotSupportedException;
import com.danlaw.mobilegateway.exception.SdkNotAuthenticatedException;
import com.example.danlaw.demo.MyDemoApplication;
import com.example.danlaw.demo.R;
import com.example.danlaw.demo.adapter.DeviceListAdapter;
import com.example.danlaw.demo.events.AutoConnectingEvent;
import com.example.danlaw.demo.events.ConnectionStatusChangeEvent;
import com.example.danlaw.demo.events.OBDDevicesFoundEvent;
import com.example.danlaw.demo.model.DataLogger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    DataLoggerInterface dataLoggerInterface;
    ArrayList<DataLogger> devices = new ArrayList<>();
    boolean tryingToConnect = false;
    ListView listView;
    int selectedDevicePositionInList = -1;
    DataLogger selectedDatalogger = null;
    Button scanButton;
    LottieAnimationView lottieAnimationView;
    DeviceListAdapter devicesAdapter;
    BluetoothInterface bluetoothInterface;

    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION};
    final private String TAG = MainActivity.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final List<String> missingPermissions = new ArrayList<>();
        // bluetooth requires ACCESS_FINE_LOCATION permission to scan for bluetooth devices. Checking for permission
        // on create in case the user revoked permission in setting while the app was closed.
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, REQUEST_CODE_ASK_PERMISSIONS);
            }
        }
        // setting up views
        scanButton = (Button) findViewById(R.id.scanButton);
        lottieAnimationView = (LottieAnimationView) findViewById(R.id.animation_view);
        lottieAnimationView.setAnimation("ripple_loading_animation.json");
        listView = (ListView) findViewById(R.id.devicesList);
        // setting device adapter to show list of devices found during scanning
        devicesAdapter = new DeviceListAdapter(getApplicationContext(), devices);
        listView.setAdapter(devicesAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // update UI and try connecting to a device if any connection is not in progress.
                if (!tryingToConnect) {
                    selectedDevicePositionInList = position;
                    selectedDatalogger = new DataLogger(devices.get(position).getName(), devices.get(position).getAddress());
                    // calling connect method from datalogger interface to initiate connection. SDK takes care of handling bluetooth connection in the background.
                    dataLoggerInterface.connect(devices.get(position).getAddress());
                    view.setBackgroundColor(Color.LTGRAY);
                    Toast.makeText(MainActivity.this, "Initiating connection", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(MainActivity.this, "Connection in progress", Toast.LENGTH_SHORT).show();
            }
        });

        try {
            // getting the singleton datalogger interface to communicate with datalogger
            dataLoggerInterface = ((MyDemoApplication) getApplication()).getDataLoggerInterface();
            // TODO: 5/23/2018 describe bluetooth callback?
            bluetoothInterface = ((MyDemoApplication) getApplication()).getBluetoothInterface();
        } catch (BleNotSupportedException e) {
            Log.d(TAG, "bluetooth not supported");
            e.printStackTrace();
        } catch (SdkNotAuthenticatedException e) {
            Log.d(TAG, "SDK not authenticated");
            e.printStackTrace();
        }
        // setting the scanning duration ( in milliseconds)
        dataLoggerInterface.setScanTime(5000);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                devices.clear();
                selectedDatalogger = null;
                listView.setVisibility(View.INVISIBLE);
                lottieAnimationView.setVisibility(View.VISIBLE);
                lottieAnimationView.playAnimation();
                dataLoggerInterface.scanForDataLoggers(true);
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOBDDevicesFoundEvent(OBDDevicesFoundEvent event) {
        // adding the device to list anytime a new device is found and updating ui
        // TODO: 5/18/2018 maybe add scan complete callback
        dataLoggerInterface.scanForDataLoggers(false);
        selectedDatalogger = new DataLogger(event.deviceName, event.deviceAddress);
        devices.add(selectedDatalogger);
        lottieAnimationView.cancelAnimation();
        lottieAnimationView.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        devicesAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectionStatusChangeEvent(ConnectionStatusChangeEvent event) {
        // updating the UI based on the connection status
        if (event.connectionStatus == DataLoggerInterface.STATE_CONNECTED) {
            listView.getChildAt(selectedDevicePositionInList).setBackgroundColor(Color.GREEN);
            Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent resultIntent = new Intent(getApplicationContext(), ConnectedActivity.class);
                    resultIntent.putExtra("deviceName", selectedDatalogger.getName());
                    resultIntent.putExtra("deviceAddress", selectedDatalogger.getAddress());
                    startActivity(resultIntent);
                }
            }, 150);
        } else if (event.connectionStatus == DataLoggerInterface.STATE_CONNECTING)
            listView.getChildAt(selectedDevicePositionInList).setBackgroundColor(Color.LTGRAY);
        else if (event.connectionStatus == DataLoggerInterface.STATE_DISCONNECTED) {
            Toast.makeText(MainActivity.this, "Failed to connect", Toast.LENGTH_SHORT).show();
            listView.getChildAt(selectedDevicePositionInList).setBackgroundColor(Color.RED);
        }
    }


    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Do you want to close the app?")
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // good practice to disconnect from the datalogger when exiting
                        dataLoggerInterface.disconnect();
                        finish();
                        // NOTE: System.exit should be called anytime the app closes as it initiates
                        // garbage collection and clears up the singleton instances.
                        System.exit(0);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setCancelable(true)
                .show();
    }

    @Override
    public void onStart() {
        super.onStart();
        // registering event bus to get event notifications
        EventBus.getDefault().register(this);
        // changing the foreground flag for autoconnect to process in the background.
        ((MyDemoApplication) getApplication()).isAppInForeground = true;
        init();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        // updating the flag so that the
        ((MyDemoApplication) getApplication()).isAppInForeground = false;
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAutoConnectingEvent(AutoConnectingEvent event) {
        selectedDatalogger = new DataLogger(event.deviceName, event.deviceAddress);
        Toast.makeText(MainActivity.this, "Favorite device found! Please wait, trying to auto connect.", Toast.LENGTH_SHORT).show();
    }

    public void init() {
        devices.clear();
        selectedDatalogger = null;
        listView.setVisibility(View.INVISIBLE);
        lottieAnimationView.setVisibility(View.INVISIBLE);
    }

}
