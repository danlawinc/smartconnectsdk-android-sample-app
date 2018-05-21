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
import com.example.danlaw.demo.events.ConnectionStatusChangeEvent;
import com.example.danlaw.demo.events.OBDDevicesFoundEvent;
import com.example.danlaw.demo.model.DataLogger;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
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
    BluetoothInterface bluetoothInterface;

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, REQUEST_CODE_ASK_PERMISSIONS);
            }
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

        try {
            dataLoggerInterface = ((MyDemoApplication) getApplication()).getDataLoggerInterface();
            bluetoothInterface = ((MyDemoApplication) getApplication()).getBluetoothInterface();
        } catch (BleNotSupportedException e) {
            e.printStackTrace();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOBDDevicesFoundEvent(OBDDevicesFoundEvent event) {
        // TODO: 5/18/2018 maybe add scan complete callback
        dataLoggerInterface.scanForDataLoggers(false);
        DataLogger datalogger = new DataLogger(event.deviceName, event.deviceAddress);
        devices.add(datalogger);
        lottieAnimationView.cancelAnimation();
        lottieAnimationView.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        devicesAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectionStatusChangeEvent(ConnectionStatusChangeEvent event) {
        if (event.connectionStatus == DataLoggerInterface.STATE_CONNECTED) {
            listView.getChildAt(selectedDevice).setBackgroundColor(Color.GREEN);
            Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(MainActivity.this,ConnectedActivity.class));
                }
            }, 500);
        } else if (event.connectionStatus == DataLoggerInterface.STATE_CONNECTING)
            listView.getChildAt(selectedDevice).setBackgroundColor(Color.DKGRAY);
        else
            listView.getChildAt(selectedDevice).setBackgroundColor(Color.RED);
    }


    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Do you want to disconnect from DataLogger?")
                .setPositiveButton("Disconnect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dataLoggerInterface.disconnect();
                        finish();
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

}
