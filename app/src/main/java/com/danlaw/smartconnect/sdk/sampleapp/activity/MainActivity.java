package com.danlaw.smartconnect.sdk.sampleapp.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.danlaw.smartconnect.sdk.sampleapp.events.AuthEvent;
import com.danlaw.smartconnectsdk.bluetooth.BluetoothInterface;
import com.danlaw.smartconnectsdk.datalogger.DataLoggerInterface;
import com.danlaw.smartconnectsdk.exception.BleNotSupportedException;
import com.danlaw.smartconnectsdk.exception.SdkNotAuthenticatedException;
import com.danlaw.smartconnect.sdk.sampleapp.MyDemoApplication;
import com.danlaw.smartconnect.sdk.sampleapp.R;
import com.danlaw.smartconnect.sdk.sampleapp.adapter.DeviceListAdapter;
import com.danlaw.smartconnect.sdk.sampleapp.events.AutoConnectingEvent;
import com.danlaw.smartconnect.sdk.sampleapp.events.BluetoothEnabledEvent;
import com.danlaw.smartconnect.sdk.sampleapp.events.ConnectionStatusChangeEvent;
import com.danlaw.smartconnect.sdk.sampleapp.events.OBDDevicesFoundEvent;
import com.danlaw.smartconnect.sdk.sampleapp.events.ScanStoppedEvent;
import com.danlaw.smartconnect.sdk.sampleapp.model.DataLogger;

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
    ProgressBar scanningAnimation;
    DeviceListAdapter devicesAdapter;
    BluetoothInterface bluetoothInterface;

    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION};
    final private String TAG = MainActivity.class.getCanonicalName();
    private boolean setupComplete = false;

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
        scanningAnimation = (ProgressBar) findViewById(R.id.animation_view);
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
                    // trying to connect to a device if scanning is in progress will also stop scanning
                    dataLoggerInterface.connect(devices.get(position).getAddress());
                    scanningAnimation.setVisibility(View.GONE);
                    view.setBackgroundColor(Color.LTGRAY);
                    Toast.makeText(MainActivity.this, "Initiating connection", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(MainActivity.this, "Connection in progress", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void setup() {
        try {

            // getting the singleton datalogger interface to communicate with datalogger
            dataLoggerInterface = ((MyDemoApplication) getApplication()).getDataLoggerInterface();

            // getting the singleton bluetooth interface to communicate with mobile's bluetooth ( detect if on/ turn on)
            bluetoothInterface = ((MyDemoApplication) getApplication()).getBluetoothInterface();

            // turning off the auto acknowledgement for UDP message for  bleap devices
           dataLoggerInterface.setBleapAutoAcknowledgement(false);
        } catch (BleNotSupportedException e) {
            Log.d(TAG, "bluetooth not supported");
            e.printStackTrace();
        } catch (SdkNotAuthenticatedException e) {
            Log.d(TAG, "SDK not authenticated");
            e.printStackTrace();
        }

        // checking if bluetooth not enabled display a dialog asking the user to turn on bluetooth
        if (!bluetoothInterface.isBluetoothEnabled()) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Bluetooth needs to be turned on to scan for devices")
                    .setPositiveButton("Turn on", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // this method turns on the bluetooth for the user
                            bluetoothInterface.enableBluetooth();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).setCancelable(true)
                    .show();
            Toast.makeText(this, "Bluetooth not turned on", Toast.LENGTH_LONG).show();
        }

        // setting the scanning duration ( in milliseconds)
        dataLoggerInterface.setScanTime(10000); // set to scan for 10 seconds

        // changing flag for onStart to update ui
        setupComplete = true;
        init();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOBDDevicesFoundEvent(OBDDevicesFoundEvent event) {

        // adding the device to list anytime a new device is found and updating ui
        devices.add(new DataLogger(event.deviceName, event.deviceAddress));
        devicesAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectionStatusChangeEvent(ConnectionStatusChangeEvent event) {

        // updating the UI based on the connection status
        switch (event.connectionStatus) {
            case DataLoggerInterface.STATE_CONNECTED:
                listView.getChildAt(selectedDevicePositionInList).setBackgroundColor(Color.GREEN);
                Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();

                // taking user to the next screen
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent resultIntent = new Intent(getApplicationContext(), ConnectedActivity.class);
                        resultIntent.putExtra("deviceName", selectedDatalogger.getName());
                        resultIntent.putExtra("deviceAddress", selectedDatalogger.getAddress());
                        startActivity(resultIntent);
                    }
                }, 500);
                break;
            case DataLoggerInterface.STATE_DISCONNECTED:
            case DataLoggerInterface.STATE_CONNECTION_FAILURE:
            case DataLoggerInterface.STATE_CHECKING_HEALTH_STATUS_FAILED:
            case DataLoggerInterface.STATE_AUTHENTICATION_FAILED:
                Toast.makeText(MainActivity.this, "Failed to connect", Toast.LENGTH_SHORT).show();
                listView.getChildAt(selectedDevicePositionInList).setBackgroundColor(Color.RED);
                scanButton.setVisibility(View.VISIBLE);
                break;
            default:
                listView.getChildAt(selectedDevicePositionInList).setBackgroundColor(Color.LTGRAY);
                break;
        }
    }


    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Do you want to close the app?")
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // as a good practice always call disconnect before exiting the app, this closes all the connections
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

        // checking flag to make sure app has references to all the views before updating them.
        if (setupComplete)
            init();
    }

    @Override
    public void onStop() {
        // un-registering event bus in this activity so that this activity does not continue to get updates once the user has navigated away from this screen.
        EventBus.getDefault().unregister(this);
        // updating the flag so that if needed, autoconnect can process in the background.
        ((MyDemoApplication) getApplication()).isAppInForeground = false;
        super.onStop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAutoConnectingEvent(AutoConnectingEvent event) {

        // called when sdk tries to auto connect to a device set as favorite
        selectedDatalogger = new DataLogger(event.deviceName, event.deviceAddress);

        selectedDevicePositionInList = devicesAdapter.getIndexByProperty(selectedDatalogger.getName());
        Toast.makeText(MainActivity.this, "Favorite device found! Please wait, trying to auto connect.", Toast.LENGTH_SHORT).show();
    }

    public void init() {

        // initializing the activity
        devices.clear();
        selectedDatalogger = null;
        scanningAnimation.setVisibility(View.GONE);
        devicesAdapter.notifyDataSetChanged();
        scanningAnimation.setVisibility(View.GONE);
        scanButton.setVisibility(View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothEnabledEvent(BluetoothEnabledEvent event) {

        // called when bluetooth interface enables phone's bluetooth
        if (event.isEnabled) {
            Toast.makeText(MainActivity.this, "Bluetooth turned on", Toast.LENGTH_SHORT).show();
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onScanStoppedEvent(ScanStoppedEvent event) {
        // called when the sdk stops scanning for datalogger.
        // true is returned when scan timed out
        // false when a connection is tried to be established before the scan duration timed out.
        if (event.scanTimeOut) {
            Toast.makeText(MainActivity.this, "Scan complete", Toast.LENGTH_SHORT).show();
            scanButton.setVisibility(View.VISIBLE);
            scanningAnimation.setVisibility(View.GONE);
        } else {
            Log.d("Scan stopped:", "an attempt to connect to a device was made before scan duration timed out");
        }
    }

    public void onScanButtonClicked(View view) {
        // resetting everything before starting scan
        Toast.makeText(MainActivity.this, "Scanning for devices", Toast.LENGTH_SHORT).show();
        devices.clear();
        devicesAdapter.notifyDataSetChanged();
        selectedDatalogger = null;
        scanningAnimation.setVisibility(View.VISIBLE);
        dataLoggerInterface.scanForDataLoggers(true);
        scanButton.setVisibility(View.INVISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAuthEvent(AuthEvent event) {
        // called when the sdk gets results for auth.
        // status 200 means auth was successful
        if (event.code == 200) {
            setup();
        } else {
            Log.d("Auth: ", event.message);
        }
    }
}
