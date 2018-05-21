package com.example.danlaw.demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.danlaw.demo.R;
import com.example.danlaw.demo.model.DataLogger;

import java.util.ArrayList;

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
            convertView = LayoutInflater.from(context).inflate(R.layout.devices_list_item,null);

        TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
        text1.setText(devices.get(position).getName());
        return convertView;
    }
}
