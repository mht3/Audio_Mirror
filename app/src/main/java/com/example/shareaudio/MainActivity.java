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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.Inflater;


public class MainActivity extends Activity implements AdapterView.OnItemClickListener {
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVERABLE_BT = 0;
    private static final String TAG = "MainActivity";


    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> deviceSet;

    /**
     * A set of paired devices.
     */
    private ArrayList<BluetoothDevice> devices;

    /**
     * The name of each paired device in the set.
     */
    private ListView listView;
    private DeviceListAdapter deviceListAdapter;
    private ArrayList<String> availableDevices;


    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        devices = new ArrayList<>();
        setContentView(R.layout.activity_main);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final Intent enableBtIntent = new Intent(bluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        final ImageView logo = findViewById(R.id.logo);
        final Button toggleSwitch = findViewById(R.id.on_off);
        final TextView not_supported = findViewById(R.id.not_supported);
        not_supported.setVisibility(View.INVISIBLE);
        final Button discover = findViewById(R.id.discover);
        listView = findViewById(R.id.listView);

        //Broadcasts when band state changes (ie:pairing)
        //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        //registerReceiver(broadcastReceiver2, filter);



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
                if (!bluetoothAdapter.isDiscovering() && bluetoothAdapter != null) {
                    YoYo.with(Techniques.Tada).duration(700).repeat(1).playOn(discover);
                    setPairedDevices(listView);
                    listView.setOnItemClickListener(MainActivity.this);
                }
                if (bluetoothAdapter.isDiscovering() && bluetoothAdapter != null) {
                    bluetoothAdapter.cancelDiscovery();

                    Log.d(TAG, "btnDiscover: Canceling discovery.");
                    //check BT permissions in manifest

                    bluetoothAdapter.startDiscovery();
                    listView.setOnItemClickListener(MainActivity.this);
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
//    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//            Log.d(TAG, "onReceive: ACTION FOUND.");
//
//            if (action.equals(BluetoothDevice.ACTION_FOUND)){
//                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
//                devices.add(device);
//                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
//                deviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, devices);
//                listView.setAdapter(deviceListAdapter);
//            }
//        }
//    };

//    private final BroadcastReceiver broadcastReceiver2 = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//
//            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
//                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                //3 cases:
//                //case 1:
//                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
//                    Log.d(TAG, "BroadcastReceiver: BOUND_BONDED.");
//                }
//                //case 2:
//                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
//                    Log.d(TAG, "BroadcastReceiver: BOUND_BONDING.");
//                }
//                //case 3:
//                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
//                    Log.d(TAG, "BroadcastReceiver: BOUND_NONE.");
//                }
//            }
//        }
//    };


    public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
        bluetoothAdapter.cancelDiscovery();
        String item = (String) listView.getItemAtPosition(position);
        Toast.makeText(this,"Pairing with : " + item,Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onItemClick: You Clicked on a device.");
        //devices.get(position).createBond();

    }

    public void setPairedDevices(View v) {
        deviceSet = bluetoothAdapter.getBondedDevices();
        availableDevices = new ArrayList<>();
        for (BluetoothDevice bt : deviceSet) {
            availableDevices.add(bt.getName());
            Toast.makeText(getApplicationContext(), "Showing Paired Devices", Toast.LENGTH_SHORT).show();
            final ArrayAdapter adapter = new ArrayAdapter(this,
                    android.R.layout.simple_list_item_activated_1, availableDevices);
            listView.setAdapter(adapter);
        }
    }


}