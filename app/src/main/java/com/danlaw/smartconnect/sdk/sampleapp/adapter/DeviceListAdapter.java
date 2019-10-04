package com.danlaw.smartconnect.sdk.sampleapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.danlaw.smartconnect.sdk.sampleapp.R;
import com.danlaw.smartconnect.sdk.sampleapp.model.DataLogger;

import java.util.ArrayList;

/**
 * adapter for device list
 */
public class DeviceListAdapter extends BaseAdapter {
    private ArrayList<DataLogger> devices = new ArrayList<>();
    private Context context;

    public DeviceListAdapter(Context context, ArrayList<DataLogger> devices) {
        this.context = context;
        this.devices = devices;
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.devices_list_item, null);

        TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
        text1.setText(devices.get(position).getName());
        return convertView;
    }

    public int getIndexByProperty(String deviceName) {
        for (int i = 0; i < devices.size(); i++) {
            DataLogger dl = devices.get(i);
            if (dl != null && dl.getName().equals(deviceName)) {
                return i;
            }
        }
        return -1;// not there in list
    }
}
