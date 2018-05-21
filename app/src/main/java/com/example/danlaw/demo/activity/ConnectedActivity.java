package com.example.danlaw.demo.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.danlaw.mobilegateway.bluetooth.BluetoothInterface;
import com.danlaw.mobilegateway.datalogger.DataLoggerInterface;
import com.danlaw.mobilegateway.datalogger.model.FuelLevel;
import com.danlaw.mobilegateway.exception.BleNotSupportedException;
import com.danlaw.mobilegateway.exception.SdkNotAuthenticatedException;
import com.example.danlaw.demo.MyDemoApplication;
import com.example.danlaw.demo.R;
import com.example.danlaw.demo.events.BasicDataReceivedEvent;
import com.example.danlaw.demo.events.ConnectionStatusChangeEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class ConnectedActivity extends AppCompatActivity {

    boolean connected = true;
    DataLoggerInterface dataLoggerInterface;
    BluetoothInterface bluetoothInterface;
    TextView fuelLevel;
    final int DPID_COLLECTION = 1;
    ArrayList<Integer> dpidList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);
        EventBus.getDefault().register(this);
        try {
            dataLoggerInterface = ((MyDemoApplication) getApplication()).getDataLoggerInterface();
            bluetoothInterface = ((MyDemoApplication) getApplication()).getBluetoothInterface();
        } catch (BleNotSupportedException e) {
            e.printStackTrace();
        } catch (SdkNotAuthenticatedException e) {
            e.printStackTrace();
        }
        fuelLevel = (TextView) findViewById(R.id.fuelLevelValue);
        dpidList = new ArrayList<>();
        dpidList.add(DataLoggerInterface.PID_VEHICLE_SPEED);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectionStatusChangeEvent(ConnectionStatusChangeEvent event) {
        switch (event.connectionStatus) {
            case DataLoggerInterface.STATE_DISCONNECTED:
                connected = false;
                updateUI();
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBasicDataReceivedEvent(BasicDataReceivedEvent event) {
        switch (event.pid) {
            case DataLoggerInterface.PID_FUEL_LEVEL:
                String fuel = ((FuelLevel) event.data).currentFuelLevel + ((FuelLevel) event.data).unit;
                fuelLevel.setText(fuel);
                break;
            default:
                break;
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

        dont know about backstack because could be autoconnect so jsut clear and start a fresh task
        in intent
    }

    public void onDisconnectClicked(View view) {
        dataLoggerInterface.disconnect();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onBasicRequestClicked(View view){
        Toast.makeText(this, "Requesting current fuel level", Toast.LENGTH_SHORT).show();
        dataLoggerInterface.readBasicPidData(DataLoggerInterface.PID_FUEL_LEVEL);
    }

    public void onRequestAdvancedPid(View view){
        Toast.makeText(this, "Registering Speed For Continuous Updates", Toast.LENGTH_LONG).show();
        dataLoggerInterface.registerDataPid(DPID_COLLECTION,dpidList);
    }
}
