package com.example.danlaw.demo.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.danlaw.mobilegateway.bluetooth.BluetoothInterface;
import com.danlaw.mobilegateway.datalogger.DataLoggerInterface;
import com.danlaw.mobilegateway.datalogger.model.EngineRPM;
import com.danlaw.mobilegateway.datalogger.model.FuelLevel;
import com.danlaw.mobilegateway.datalogger.model.VehicleSpeed;
import com.danlaw.mobilegateway.exception.BleNotSupportedException;
import com.danlaw.mobilegateway.exception.SdkNotAuthenticatedException;
import com.example.danlaw.demo.MyDemoApplication;
import com.example.danlaw.demo.R;
import com.example.danlaw.demo.events.BasicDataReceivedEvent;
import com.example.danlaw.demo.events.ConnectionStatusChangeEvent;
import com.example.danlaw.demo.events.DPidDataReceivedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import static com.danlaw.mobilegateway.datalogger.DataLoggerInterface.PID_ENGINE_RPM;
import static com.danlaw.mobilegateway.datalogger.DataLoggerInterface.PID_VEHICLE_SPEED;
import static com.danlaw.mobilegateway.datalogger.IDataLoggerCallback.RESPONSE_SUCCESS;

public class ConnectedActivity extends AppCompatActivity {

    boolean connected = true;
    DataLoggerInterface dataLoggerInterface;
    BluetoothInterface bluetoothInterface;
    final int DPID_SPEED = 1;
    final int DPID_RPM = 2;
    ArrayList<Integer> dpidListSpeed;
    ArrayList<Integer> dpidListRPM;
    TextView fuelTextView;
    TextView speedTextView;
    TextView rpmTextView;
    Button registerAdvancedButton;
    Button unRegisterAdvancedButton;
    final private String TAG = ConnectedActivity.class.getCanonicalName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);
        try {
            dataLoggerInterface = ((MyDemoApplication) getApplication()).getDataLoggerInterface();
            bluetoothInterface = ((MyDemoApplication) getApplication()).getBluetoothInterface();
        } catch (BleNotSupportedException e) {
            e.printStackTrace();
        } catch (SdkNotAuthenticatedException e) {
            e.printStackTrace();
        }
        dpidListSpeed = new ArrayList<>();
        dpidListRPM = new ArrayList<>();
        dpidListSpeed.add(DataLoggerInterface.PID_VEHICLE_SPEED);
        dpidListRPM.add(DataLoggerInterface.PID_ENGINE_RPM);
        fuelTextView = (TextView) findViewById(R.id.fuelLevelValue);
        speedTextView = (TextView) findViewById(R.id.speedTextView);
        rpmTextView = (TextView) findViewById(R.id.rpmTextView);
        registerAdvancedButton = (Button) findViewById(R.id.registerAdvancedButton);
        unRegisterAdvancedButton = (Button) findViewById(R.id.unRegisterAdvancedButton);

        // same pid should not be registered multiple times to avoid overwhelming datalogger
        // pressing multiple times might cause app to behave unexpectedly
        registerAdvancedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Registering PIDs For Continuous Updates", Toast.LENGTH_LONG).show();
                dataLoggerInterface.registerDataPid(DPID_SPEED, dpidListSpeed);
                dataLoggerInterface.registerDataPid(DPID_RPM, dpidListRPM);
            }
        });

        unRegisterAdvancedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "UnRegistering PIDs", Toast.LENGTH_LONG).show();
                dataLoggerInterface.unregisterDataPid(DPID_SPEED);
                dataLoggerInterface.unregisterDataPid(DPID_RPM);
            }
        });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectionStatusChangeEvent(ConnectionStatusChangeEvent event) {
        switch (event.connectionStatus) {
            case DataLoggerInterface.STATE_DISCONNECTED:
                connected = false;
//                updateUI();
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBasicDataReceivedEvent(BasicDataReceivedEvent event) {
        switch (event.pid) {
            case DataLoggerInterface.PID_FUEL_LEVEL:
                String fuel = "FUEL: " + ((FuelLevel) event.data).currentFuelLevel + ((FuelLevel) event.data).unit;
                fuelTextView.setText(fuel);
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAdvancedDataReceivedEvent(DPidDataReceivedEvent event) {
        if (event.responseCode == RESPONSE_SUCCESS) {
            switch (event.DPid) {
                case DPID_SPEED:
                    VehicleSpeed speed = (VehicleSpeed) event.PID_Data.get(PID_VEHICLE_SPEED);
                    String speedText = "Speed: " + String.valueOf(speed.value) + speed.unit;
                    speedTextView.setText(speedText);
                    break;
                case DPID_RPM:
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

    @Override
    public void onBackPressed() {
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

//        dont know about backstack because could be autoconnect so jsut clear and start a fresh task
//        in intent
    }

    public void onDisconnectClicked(View view) {
        dataLoggerInterface.disconnect();
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    public void onBasicRequestClicked(View view) {
        Toast.makeText(this, "Requesting current fuel level", Toast.LENGTH_SHORT).show();
        dataLoggerInterface.readBasicPidData(DataLoggerInterface.PID_FUEL_LEVEL);
    }
}
