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
import java.util.Set;
import java.util.UUID;


public class MainActivity extends Activity implements AdapterView.OnItemClickListener {
    private static final int REQUEST_ENABLE_BT = 0;
    private static final String TAG = "MainActivity";
    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> deviceSet;
    private BluetoothConnectionService mBluetoothConnection;


    /**
     * A set of paired devices.
     */
    private ArrayList<BluetoothDevice> devices;

    /**
     * The name of each paired device in the set.
     */
    private ListView pairedListView;
    private ListView unpairedListView;

    private DeviceListAdapter deviceListAdapter;
    private ArrayList<String> bondedDevices;
    private BluetoothDevice btDevice;


    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        unregisterReceiver(makeUnpairedList);
        unregisterReceiver(checkBondStateChange);
        unregisterReceiver(actionFoundReceiver);
        unregisterReceiver(actionFoundReceiver);
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
        final TextView pairedDevicesText = findViewById(R.id.pairedDevices);
        final TextView discoverableDevicesText = findViewById(R.id.discoverable_devices);


        not_supported.setVisibility(View.INVISIBLE);
        final Button discover = findViewById(R.id.discover);
        pairedListView = findViewById(R.id.pairedListView);
        unpairedListView = findViewById(R.id.unpairedListView);
        pairedListView.setVisibility(View.INVISIBLE);
        unpairedListView.setVisibility(View.INVISIBLE);
        pairedDevicesText.setVisibility(View.INVISIBLE);
        discoverableDevicesText.setVisibility(View.INVISIBLE);


        final Button pairedDevicesButton = findViewById(R.id.display_paired_devices);


        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(checkBondStateChange, filter);



        if (bluetoothAdapter == null) {
            discover.setEnabled(false);
            toggleSwitch.setEnabled(false);
            toggleSwitch.setVisibility(View.INVISIBLE);
            not_supported.setVisibility(View.VISIBLE);
            pairedDevicesButton.setVisibility(View.INVISIBLE);

        }
        toggleSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bluetoothAdapter.isEnabled()) {
                    discover.setEnabled(true);
                    pairedDevicesButton.setEnabled(true);
                    Log.d(TAG, "Turning bluetooth on");
                    toggleSwitch.setActivated(true);
                    bluetoothAdapter.enable();
                    Context context = getApplicationContext();
                    CharSequence text = "TURNING ON BLUETOOTH";
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                } else {
                    discover.setEnabled(false);
                    pairedDevicesButton.setEnabled(false);
                    pairedListView.setVisibility(View.INVISIBLE);
                    unpairedListView.setVisibility(View.INVISIBLE);
                    pairedDevicesText.setVisibility(View.INVISIBLE);
                    discoverableDevicesText.setVisibility(View.INVISIBLE);
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
        pairedDevicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                pairedListView.setVisibility(View.VISIBLE);
                pairedDevicesText.setVisibility(View.VISIBLE);
                unpairedListView.setVisibility(View.INVISIBLE);
                discoverableDevicesText.setVisibility(View.INVISIBLE);
                if (!bluetoothAdapter.isDiscovering() && bluetoothAdapter != null) {
                    YoYo.with(Techniques.Tada).duration(700).repeat(1).playOn(pairedDevicesButton);
                    setPairedDevices(pairedListView);
                    pairedListView.setOnItemClickListener(MainActivity.this);
                }
                if (bluetoothAdapter.isDiscovering() && bluetoothAdapter != null) {
                    bluetoothAdapter.cancelDiscovery();
                    Log.d(TAG, "btnDiscover: Canceling discovery.");
                    bluetoothAdapter.startDiscovery();
                    pairedListView.setOnItemClickListener(MainActivity.this);
                }
            }
        });
        discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairedListView.setVisibility(View.INVISIBLE);
                pairedDevicesText.setVisibility(View.INVISIBLE);
                unpairedListView.setVisibility(View.VISIBLE);
                unpairedListView.setAdapter(null);
                discoverableDevicesText.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.Tada).duration(700).repeat(1).playOn(discover);
                if (bluetoothAdapter != null ) {
                    unpairedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            bluetoothAdapter.cancelDiscovery();

                            Log.d(TAG, "onItemClick: You Clicked on a device.");
                            String deviceName = devices.get(position).getName();
                            String deviceAddress = devices.get(position).getAddress();

                            Log.d(TAG, "onItemClick: deviceName = " + deviceName);
                            Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

                            //create the bond.
                            //NOTE: Requires API 17+? I think this is JellyBean
                            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {

                                Log.d(TAG, "Trying to pair with " + deviceName);
                                devices.get(position).createBond();
                                btDevice = devices.get(position);
                                mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
                            }
                        }
                    });
                    discover(unpairedListView);
                }
            }
        });

    }
    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");

        mBluetoothConnection.startClient(device,uuid);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    private BroadcastReceiver makeUnpairedList = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                devices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                deviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, devices);
                unpairedListView.setAdapter(deviceListAdapter);
            }
        }
    };

    private final BroadcastReceiver checkBondStateChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case 1:
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOUND_BONDED.");
                    btDevice = device;
                }
                //case 2:
                if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOUND_BONDING.");
                }
                //case 3:
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOUND_NONE.");
                }
            }
        }
    };

    /**
     * Broadcast receiver for action found
     */
    private final BroadcastReceiver actionFoundReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(bluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };
    public void discover(View view) {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            //check BT permissions in manifest
            checkBTPermissions();

            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(makeUnpairedList, discoverDevicesIntent);
        }
        if(!bluetoothAdapter.isDiscovering()){

            //check BT permissions in manifest
            checkBTPermissions();

            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(makeUnpairedList, discoverDevicesIntent);
        }
    }
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }




    public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
        bluetoothAdapter.cancelDiscovery();
        String item = (String) pairedListView.getItemAtPosition(position);
        Toast.makeText(this,"Pairing with " + item,Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onItemClick: You Clicked on a device.");

    }

    public void setPairedDevices(View v) {
        Toast.makeText(getApplicationContext(), "Showing Paired Devices", Toast.LENGTH_SHORT).show();
        deviceSet = bluetoothAdapter.getBondedDevices();
        bondedDevices = new ArrayList<>();
        for (BluetoothDevice bt : deviceSet) {
            if (bt.getType() == BluetoothDevice.DEVICE_TYPE_CLASSIC) {
                bondedDevices.add(bt.getName());
            } else {
                deviceSet.remove(bt);
            }
        }
        if (bondedDevices.size() == 0) {
            Toast.makeText(getApplicationContext(),
                    "No devices found. Please make sure you're within range of a device.",
                    Toast.LENGTH_LONG).show();
        }
        final ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_activated_1, bondedDevices);
        pairedListView.setAdapter(adapter);
    }
}