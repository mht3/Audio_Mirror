package com.example.shareaudio;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    private LayoutInflater mLayoutInflater;
    private ArrayList<BluetoothDevice> mDevices;
    private int  mViewResourceId;

    public DeviceListAdapter(Context context, int tvResourceId, ArrayList<BluetoothDevice> devices){
        super(context, tvResourceId,devices);
        this.mDevices = devices;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = tvResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        BluetoothDevice device = mDevices.get(position);
        if (vi == null) {
            vi = mLayoutInflater.inflate(R.layout.device_adapter_view, null);
        }
        TextView deviceName = vi.findViewById(R.id.deviceName);
        if (!(device.getName().length() == 0)) {
            deviceName.setText(device.getName());
        }
        return vi;
    }
}
