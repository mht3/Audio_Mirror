package com.example.shareaudio;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.ParcelUuid;
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
import java.util.UUID;


/**
 * Main Class for share audio.
 * Created by Matt Taylor, Eric Monson, and Joe Brown
 *
 * Helpful sources:
 * Bluetooth API Documentation
 *https://developer.android.com/guide/topics/connectivity/bluetooth
 *
 * Great intro tutorial on the Bluetooth API
 * https://www.c-sharpcorner.com/article/create-bluetooth-android-application-using-android-studio/
 *
 * Mitch Tabian Bluetooth Tutorial -
 * https://www.youtube.com/watch?v=YJ0JQXcNNTA
 */
public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    /**
     * The integer value that relates to requesting to enable the bluetooth.
     */
    private static final int REQUEST_ENABLE_BT = 0;

    /**
     * Our main activity tag used in Logs for testing
     */
    private static final String TAG = "MainActivity";

    /**
     * The bluetooth adapter from the Bluetooth API
     */
    private BluetoothAdapter bluetoothAdapter;

    /**
     * set of bonded bluetooth devices.
     */
    private Set<BluetoothDevice> deviceSet;

    /**
     * Call to the Bluetoth connection service class.
     * This is where a socket is made and the device is physically connected.
     */
    private BluetoothConnectionService bluetoothConnection;

    /**
     * A List of paired bluetooth devices received from the bluetooth set.
     */
    private ArrayList<BluetoothDevice> devices;

    /**
     * The name of each paired device in the set. Accessed by pressing the paired devices button.
     */
    private ListView pairedListView;

    /**
     * List view for the unpaired devices. Accessed by pressing the discover button.
     */
    private ListView unpairedListView;

    /**
     * Devicelist adapter used for unpaired listview Display.
     */
    private DeviceListAdapter deviceListAdapter;

    /**
     * A list of strings of the bonded bluetooth devices.
     */
    private ArrayList<String> bondedDevices;


    /**
     * Array of Parcel Universally Unique Identifiers for a device that is used to create a connection.
     */
    private ParcelUuid[] mDeviceUUIDs;


    /**
     * Unregisters the broadcast receivers once the app is closed.
     */
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        unregisterReceiver(makeUnpairedList);
        unregisterReceiver(checkBondStateChange);
        super.onDestroy();
    }

    /**
     * onCreate method used to set up the display, register receivers, and the bluetooth adapter.
     * @param savedInstanceState the savedInstanceState
     */
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
            /**
             * On click listener for the Bluetooth on/off toggle switch.
             * @param v the current view
             */
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
            /**
             * On click listener for the paired devices button. This displays the list of paired devices.
             * @param arg0 the current view.
             */
            @Override
            public void onClick(View arg0) {
                pairedListView.setVisibility(View.VISIBLE);
                pairedDevicesText.setVisibility(View.VISIBLE);
                unpairedListView.setVisibility(View.INVISIBLE);
                discoverableDevicesText.setVisibility(View.INVISIBLE);
                if (bluetoothAdapter != null) {
                    if (bluetoothAdapter.isDiscovering()) {
                        bluetoothAdapter.cancelDiscovery();
                    }
                    YoYo.with(Techniques.Tada).duration(700).repeat(1).playOn(pairedDevicesButton);
                    setPairedDevices(pairedListView);
                    pairedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        /**
                         * Tries to connect each bluetooth device that is clicked.
                         * @param parent parent view for the paired list view.
                         * @param view the current view from the item.
                         * @param position the position of the item that was clicked from the list view.
                         * @param id the id in
                         */
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String currentName = bondedDevices.get(position);
                            BluetoothDevice currentDevice = null;
                            for (BluetoothDevice bt : deviceSet) {
                                if (bt.getName().equals(currentName)) {
                                    currentDevice = bt;
                                }
                            }
                            CharSequence text = "Connecting with " + currentName;
                            Log.d(TAG, "Connecting with " + currentName);
                            int duration = Toast.LENGTH_LONG;
                            Toast toast = Toast.makeText(pairedListView.getContext(), text, duration);
                            toast.show();
                            bluetoothAdapter.cancelDiscovery();
                            mDeviceUUIDs = currentDevice.getUuids();
                            List<UUID> uuidList = new ArrayList<>();
                            for(ParcelUuid u : mDeviceUUIDs) {
                                uuidList.add(u.getUuid());
                            }
                            try {

                                bluetoothConnection = new BluetoothConnectionService(MainActivity.this, bluetoothAdapter);
                                bluetoothConnection.startClient(currentDevice, mDeviceUUIDs);
                            } catch (NullPointerException e) {
                                Log.d(TAG, "Failed due to NP exception at index " + position);

                                CharSequence text2 = "Unable to connect";
                                int duration2 = Toast.LENGTH_LONG;
                                Toast toast2 = Toast.makeText(pairedListView.getContext(), text2, duration2);
                                toast2.show();
                            }

                        }
                    });
                }
            }
        });
        discover.setOnClickListener(new View.OnClickListener() {
            /**
             * On click listener for the discover button. An animation is used and the unpaired list
             * view is made visible. Priority goes as follows: If the name of the bluetooth device
             * is available, device_adapter_view.xml will display the devices NAME.
             * If the deviceName is unavailable, the device ADDRESS will be displayed.
             * @param v the view for the unpairedListView
             */
            @Override
            public void onClick(View v) {
                pairedListView.setVisibility(View.INVISIBLE);
                pairedDevicesText.setVisibility(View.INVISIBLE);
                unpairedListView.setVisibility(View.VISIBLE);
                unpairedListView.setAdapter(null);
                devices = new ArrayList<>();
                discoverableDevicesText.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.Tada).duration(700).repeat(1).playOn(discover);
                if (bluetoothAdapter != null ) {
                    unpairedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        /**
                         *
                         * @param parent unpairedListView parent
                         * @param view current view from the item clicked.
                         * @param position the position of the item clicked as an integer.
                         * @param id the id of the item in the form of a long
                         */
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            bluetoothAdapter.cancelDiscovery();

                            Log.d(TAG, "onItemClick: You Clicked on a device.");
                            String deviceName = devices.get(position).getName();
                            String deviceAddress = devices.get(position).getAddress();

                            Log.d(TAG, "onItemClick: deviceName = " + deviceName);
                            Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

                            //create the bond.
                            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                Log.d(TAG, "Trying to pair with " + deviceName);
                                devices.get(position).createBond();
                                bluetoothConnection = new BluetoothConnectionService(MainActivity.this, bluetoothAdapter);
                            }
                        }
                    });
                    discover(unpairedListView);
                }
            }
        });

    }


    /**
     * Inflation options for our settings.
     * Note that the menu is not dispayed. Hopefully this functionality will come in the near future.
     * @param menu the menu as seen in the res folder.
     * @return returns true that the menu was in fact inflated.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Broadcast receiver is used to create the UnpairedList using the Bluetooth API's preset final variables.
     */
    private BroadcastReceiver makeUnpairedList = new BroadcastReceiver() {

        /**
         * Once the action is received, the broadcast receiver will initialize the deviceListAdapter so that
         * the dislay can be inflated.
         * @param context the current context received by the broadcast receiver.
         * @param intent the intent received about the action.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND ");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                devices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                deviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, devices);
                unpairedListView.setAdapter(deviceListAdapter);
            }
        }
    };

    /**
     * The broadcast receiver for when the action found through the bluetooth adapter is changed.
     * Note that this is currently unused, but will be necessary for further implementations.
     */
    private final BroadcastReceiver actionStateChange = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(bluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE IS OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "actionStateChange: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "actionStateChange: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "actionStateChange: STATE IS TURNING ON");
                        break;
                }
            }
        }
    };

    /**
     * Broadcast receiver for when the bond state of the unpaired device changes.
     */
    private final BroadcastReceiver checkBondStateChange = new BroadcastReceiver() {
        /**
         * Once received logs are created that show how the bond is changing and when it is bonded.
         * @param context the current context (checkBondStateChange)
         * @param intent the intent filter for ACTION_BOND_STATE_CHANGED
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case 1:
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "checkBondStateChange: DEVICE IS BONDED.");
                }
                //case 2:
                if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "checkBondStateChange: DEVICE IS BONDING.");
                }
                //case 3:
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "checkBondStateChange: BOND_NONE.");
                }
            }
        }
    };


    /**
     * called when the discover button is pressed.
     * starts discovery and begins the pairing process by registering the UnpairedList receiver.
     * @param view the view of the unpaired devices listview
     */
    public void discover(View view) {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            //checks Bluetooth permissions in manifest
            checkBluetoothPermissions();

            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(makeUnpairedList, discoverDevicesIntent);
        }
        if(!bluetoothAdapter.isDiscovering()){

            //checks Bluetoothpermissions in manifest
            checkBluetoothPermissions();

            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(makeUnpairedList, discoverDevicesIntent);
        }
    }

    /**
     * Permissions have to be checked for android devices older than Lollipop.
     */
    private void checkBluetoothPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBluetoothPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }


    /**
     * Unused item click listener. Present because we implement in in Main Activity.
     * @param parent N/A
     * @param view N/A
     * @param position N/A
     * @param id N/A
     */
    public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
        bluetoothAdapter.cancelDiscovery();
        String item = (String) pairedListView.getItemAtPosition(position);
        Toast.makeText(this,"Pairing with " + item,Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onItemClick: You Clicked on a device.");

    }

    /**
     * The bonded devices are received from the bluetooth set.
     * When the Paired Devices button is clicked, this method is called and displays
     * a list of paired devices using the pairedListView.
     * @param v The current view for the pairedListView
     */
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
