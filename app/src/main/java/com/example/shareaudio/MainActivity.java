package com.example.shareaudio;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVERABLE_BT = 0;
    private static final String TAG = "MainActivity";


    private BluetoothAdapter bluetoothAdapter;

    /**
     * A set of paired devices.
     */
    private ArrayList<BluetoothDevice> devices;

    /**
     * The name of each paired device in the set.
     */
    private ListView listView;
    private DeviceListAdapter deviceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        devices = new ArrayList<>();
        setContentView(R.layout.activity_main);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        final ImageView logo = findViewById(R.id.logo);
        final Button toggleSwitch = findViewById(R.id.on_off);
        final TextView not_supported = findViewById(R.id.not_supported);
        not_supported.setVisibility(View.INVISIBLE);
        final Button discover = findViewById(R.id.discover);
        listView = findViewById(R.id.listView);
        //setPairedDevices(listView);
        if (bluetoothAdapter == null) {
            discover.setEnabled(false);
            toggleSwitch.setEnabled(false);
            toggleSwitch.setVisibility(View.INVISIBLE);
            not_supported.setVisibility(View.VISIBLE);
        }
        toggleSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bluetoothAdapter.isEnabled()) {
                    Log.d(TAG, "Turning bluetooth on");
                    toggleSwitch.setActivated(true);
                    bluetoothAdapter.enable();
                    Context context = getApplicationContext();
                    CharSequence text = "TURNING ON BLUETOOTH";
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                } else {
                    bluetoothAdapter.disable();
                    Context context = getApplicationContext();
                    Log.d(TAG, "Turning bluetooth off");

                    CharSequence text = "TURNING OFF BLUETOOTH";
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        });
        discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!bluetoothAdapter.isDiscovering() && bluetoothAdapter !=null) {
                    YoYo.with(Techniques.Tada).duration(700).repeat(1).playOn(discover);
                    discover(discover);
//                    Context context = getApplicationContext();
//                    CharSequence text = "MAKING YOUR DEVICE DISCOVERABLE";
//                    int duration = Toast.LENGTH_SHORT;
//                    Toast toast = Toast.makeText(context, text, duration);
//                    toast.show();
//                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//                    startActivityForResult(enableBtIntent, REQUEST_DISCOVERABLE_BT);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                devices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                deviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, devices);
//                final ArrayAdapter adapter = new ArrayAdapter(this,
//                    android.R.layout.simple_list_item_activated_1, pairedDevices.keySet().toArray());
                listView.setAdapter(deviceListAdapter);
            }
        }
    };

    public void discover(View v) {
        Toast.makeText(getApplicationContext(), "Searching", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Searching");

        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();

            Log.d(TAG, "btnDiscover: Canceling discovery.");
            //check BT permissions in manifest

            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(broadcastReceiver, discoverDevicesIntent);
        }
        if(!bluetoothAdapter.isDiscovering()){
            //check BT permissions in manifest
            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(broadcastReceiver, discoverDevicesIntent);
        }
    }
//    public void setPairedDevices(View v) {
//        deviceSet = bluetoothAdapter.getBondedDevices();
//        pairedDevices = new HashMap<>();
//        for (BluetoothDevice bt : deviceSet) {
//            pairedDevices.put(bt.getName(), bt);
//            Toast.makeText(getApplicationContext(), "Showing Paired Devices", Toast.LENGTH_SHORT).show();
//            final ArrayAdapter adapter = new ArrayAdapter(this,
//                    android.R.layout.simple_list_item_activated_1, pairedDevices.keySet().toArray());
//            listView.setAdapter(adapter);
//        }
//    }
//

}