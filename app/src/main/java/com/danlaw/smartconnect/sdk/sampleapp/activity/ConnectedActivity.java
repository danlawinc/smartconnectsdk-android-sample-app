package com.danlaw.smartconnect.sdk.sampleapp.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.danlaw.smartconnectsdk.bluetooth.BluetoothInterface;
import com.danlaw.smartconnectsdk.datalogger.DataLoggerInterface;
import com.danlaw.smartconnectsdk.datalogger.model.EngineRPM;
import com.danlaw.smartconnectsdk.datalogger.model.FuelLevel;
import com.danlaw.smartconnectsdk.datalogger.model.HardAccelerationData;
import com.danlaw.smartconnectsdk.datalogger.model.HardBrakingData;
import com.danlaw.smartconnectsdk.datalogger.model.VehicleSpeed;
import com.danlaw.smartconnectsdk.exception.BleNotSupportedException;
import com.danlaw.smartconnectsdk.exception.SdkNotAuthenticatedException;
import com.danlaw.smartconnect.sdk.sampleapp.MyDemoApplication;
import com.danlaw.smartconnect.sdk.sampleapp.R;
import com.danlaw.smartconnect.sdk.sampleapp.events.BasicDataReceivedEvent;
import com.danlaw.smartconnect.sdk.sampleapp.events.ConnectionStatusChangeEvent;
import com.danlaw.smartconnect.sdk.sampleapp.events.DPidDataReceivedEvent;
import com.danlaw.smartconnect.sdk.sampleapp.events.EPidDataReceivedEvent;
import com.danlaw.smartconnect.sdk.sampleapp.model.DataLogger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import static com.danlaw.smartconnectsdk.datalogger.DataLoggerInterface.PID_ENGINE_RPM;
import static com.danlaw.smartconnectsdk.datalogger.DataLoggerInterface.PID_VEHICLE_SPEED;
import static com.danlaw.smartconnectsdk.datalogger.IDataLoggerCallback.RESPONSE_SUCCESS;

public class ConnectedActivity extends AppCompatActivity {

    boolean connected = true;
    DataLoggerInterface dataLoggerInterface;
    BluetoothInterface bluetoothInterface;
    final int DPID_SPEED_ID = 1;
    final int DPID_RPM_ID = 2;
    ArrayList<Integer> dpidListSpeed;
    ArrayList<Integer> dpidListRPM;
    ArrayList<Integer> eventPids;
    TextView fuelTextView;
    TextView speedTextView;
    TextView rpmTextView;
    TextView eventDescriptionView;
    Button registerAdvancedButton;
    Button unRegisterAdvancedButton;
    Switch favSwitch;
    DataLogger dataLogger;
    final private String TAG = ConnectedActivity.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);
        try {
            //getting the singleton interfaces
            dataLoggerInterface = ((MyDemoApplication) getApplication()).getDataLoggerInterface();
            bluetoothInterface = ((MyDemoApplication) getApplication()).getBluetoothInterface();
        } catch (BleNotSupportedException e) {
            Log.d(TAG, "bluetooth not supported");
            e.printStackTrace();
        } catch (SdkNotAuthenticatedException e) {
            Log.d(TAG, "SDK not authenticated");
            e.printStackTrace();
        }

        String name = getIntent().getStringExtra("deviceName");
        String address = getIntent().getStringExtra("deviceAddress");
        dataLogger = new DataLogger(name, address);
        dpidListSpeed = new ArrayList<>();
        dpidListRPM = new ArrayList<>();
        eventPids = new ArrayList<>();
        dpidListSpeed.add(DataLoggerInterface.PID_VEHICLE_SPEED);
        dpidListRPM.add(DataLoggerInterface.PID_ENGINE_RPM);
        fuelTextView = (TextView) findViewById(R.id.fuelLevelValue);
        speedTextView = (TextView) findViewById(R.id.speedTextView);
        rpmTextView = (TextView) findViewById(R.id.rpmTextView);
        favSwitch = (Switch) findViewById(R.id.favSwitch);
        eventDescriptionView = (TextView) findViewById(R.id.eventDescriptionTextView);
        registerAdvancedButton = (Button) findViewById(R.id.registerAdvancedButton);
        unRegisterAdvancedButton = (Button) findViewById(R.id.unRegisterAdvancedButton);
        favSwitch = (Switch) findViewById(R.id.favSwitch);
        favSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    // setting the device as favorite. Setting a device as favorite, enables auto connect
                    dataLoggerInterface.setFavoriteDevice(dataLogger.getName(), dataLogger.getAddress());
                    favSwitch.setText("Auto connect turned on for device: " + dataLogger.getName());
                } else {

                    // removing the device as favorite - disabling auto connect
                    dataLoggerInterface.forgetDevice();
                    favSwitch.setText("Auto connect not enabled for the connected device");
                }
            }
        });
        // checking if the current device is a favorite device
        boolean isFav = dataLoggerInterface.isFavDevice(dataLogger.getName(), dataLogger.getAddress());
        if (isFav) {
            favSwitch.setText("Auto connect turned on for device: " + dataLogger.getName());
        } else {
            favSwitch.setText("Auto connect not enabled for the connected device");
        }
        favSwitch.setChecked(isFav);
        eventPids.add(DataLoggerInterface.PID_EVENT_HARD_BRAKING);
        eventPids.add(DataLoggerInterface.PID_EVENT_HARD_ACCEL);
        // same pid should not be registered multiple times to avoid overwhelming datalogger

        // pressing this multiple times might cause app to behave unexpectedly
        registerAdvancedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Registering PIDs For Continuous Updates", Toast.LENGTH_LONG).show();

                // registering PIDs with their IDs
                boolean registerSpeed = dataLoggerInterface.registerDataPid(DPID_SPEED_ID, dpidListSpeed);
                boolean registerRPM = dataLoggerInterface.registerDataPid(DPID_RPM_ID, dpidListRPM);

                if (registerSpeed && registerRPM) {
                    Log.d(TAG, " speed and rpm registered successfully");
                } else {
                    Log.d(TAG, " speed and rpm registration failed");
                }
            }
        });

        unRegisterAdvancedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "UnRegistering PIDs", Toast.LENGTH_LONG).show();

                // unregistering PIDs with their IDs
                dataLoggerInterface.unregisterDataPid(DPID_SPEED_ID);
                dataLoggerInterface.unregisterDataPid(DPID_RPM_ID);
                rpmTextView.setText("RPM: --");
                speedTextView.setText("Speed: --");
            }
        });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectionStatusChangeEvent(ConnectionStatusChangeEvent event) {
        switch (event.connectionStatus) {
            // listening for change in connection status
            case DataLoggerInterface.STATE_DISCONNECTED:
                connected = false;
                Toast.makeText(ConnectedActivity.this, "Connection lost", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBasicDataReceivedEvent(BasicDataReceivedEvent event) {
        switch (event.pid) {

            // getting basic channel data and retrieving the value
            case DataLoggerInterface.PID_FUEL_LEVEL:
                FuelLevel fuelData = (FuelLevel) event.data;
                String fuel = "FUEL: " + fuelData.currentFuelLevel + fuelData.unit;
                fuelTextView.setText(fuel);
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAdvancedDataReceivedEvent(DPidDataReceivedEvent event) {
        // getting ODB PID data from advanced data channel
        if (event.responseCode == RESPONSE_SUCCESS) {
            switch (event.DPid) {
                case DPID_SPEED_ID:
                    VehicleSpeed speed = (VehicleSpeed) event.PID_Data.get(PID_VEHICLE_SPEED);
                    String speedText = "Speed: " + String.valueOf(speed.value) + speed.unit;
                    speedTextView.setText(speedText);
                    break;
                case DPID_RPM_ID:
                    EngineRPM engineRPM = (EngineRPM) event.PID_Data.get(PID_ENGINE_RPM);
                    String rpmText = "RPM: " + String.valueOf(engineRPM.value) + engineRPM.unit;
                    rpmTextView.setText(rpmText);
                    break;
                default:
                    break;
            }
        } else {
            Log.v(TAG, "response code error:" + event.responseCode);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventDataReceived(EPidDataReceivedEvent event) {
        // getting event data from advanced channel
        String eventDescription = "--";
        switch (event.EPid) {
            case DataLoggerInterface.PID_EVENT_HARD_BRAKING:
                Integer breakValue = ((HardBrakingData) event.data).maxBraking;
                eventDescription = "Hard Break: " + String.valueOf(breakValue);
                break;
            case DataLoggerInterface.PID_EVENT_HARD_ACCEL:
                Integer accelValue = ((HardAccelerationData) event.data).maxAcceleration;
                eventDescription = "Hard Accel: " + String.valueOf(accelValue);
                break;
            default:
                break;
        }
        eventDescriptionView.setText(eventDescription);
    }

    @Override
    public void onBackPressed() {
        // alerting the user before finishing activity
        if (connected) {
            new AlertDialog.Builder(ConnectedActivity.this)
                    .setTitle("Do you want to disconnect from DataLogger?")
                    .setPositiveButton("Disconnect", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dataLoggerInterface.disconnect();
                            finish();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).setCancelable(true)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    public void onDisconnectClicked(View view) {
        // disconnecting from datalogger
        dataLoggerInterface.disconnect();
        finish();
    }

    @Override
    public void onStart() {
        // registering event bus and updating flag so that auto connect doesn't get triggered in the background
        super.onStart();
        EventBus.getDefault().register(this);
        ((MyDemoApplication) getApplication()).isAppInForeground = true;
    }

    @Override
    public void onStop() {
        // un-registering event bus and updating flag so that auto connect gets triggered in the background
        EventBus.getDefault().unregister(this);
        ((MyDemoApplication) getApplication()).isAppInForeground = false;
        super.onStop();
    }

    public void onBasicRequestClicked(View view) {
        Toast.makeText(this, "Requesting current fuel level", Toast.LENGTH_SHORT).show();

        // requesting pid through basic channel.
        // datalogger does not provide continuous updates for pids provided through basic data channel
        dataLoggerInterface.readBasicPidData(DataLoggerInterface.PID_FUEL_LEVEL);
    }

    public void onEventPid(View view) {
        // registering event.
        // once registered, datalogger will send updates in real-time as they happen unless unregistered
        // same events should not be registered twice as it can overwhelm the datalogger
        boolean registerEventPid = dataLoggerInterface.registerEventPid(eventPids);
        Toast.makeText(this, "Event pid registration result: " + String.valueOf(registerEventPid), Toast.LENGTH_SHORT).show();
    }

    public void onUnregisterEventPid(View view) {
        // un-registering events
        boolean unregisterEventPid = dataLoggerInterface.unregisterEventPid(eventPids);
        eventDescriptionView.setText("--");
        Toast.makeText(this, "Event pid unregister result: " + String.valueOf(unregisterEventPid), Toast.LENGTH_SHORT).show();
    }

    // click handlers for more info
    public void onMoreInfoClicked(View view) {
        switch (view.getId()) {
            case R.id.basicChannelDataPidInfo:
                new AlertDialog.Builder(ConnectedActivity.this)
                        .setTitle(R.string.basic_header)
                        .setMessage(R.string.basic_info)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setCancelable(true)
                        .show();
                break;
            case R.id.advancedChannelDataPidInfo:
                new AlertDialog.Builder(ConnectedActivity.this)
                        .setTitle(R.string.advanced_header_data)
                        .setMessage(R.string.advanced_info_data)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setCancelable(true)
                        .show();
                break;
            case R.id.advancedChannelEventPidInfo:
                new AlertDialog.Builder(ConnectedActivity.this)
                        .setTitle(R.string.advanced_header_events)
                        .setMessage(R.string.advanced_info_events)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setCancelable(true)
                        .show();
                break;
            case R.id.autoConnectInfo:
                new AlertDialog.Builder(ConnectedActivity.this)
                        .setTitle(R.string.autoconnect_header)
                        .setMessage(R.string.autoconnect_info)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setCancelable(true)
                        .show();
                break;
            default:
                break;
        }
    }
}
