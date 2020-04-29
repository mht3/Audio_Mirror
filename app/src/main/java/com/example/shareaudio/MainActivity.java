package com.example.shareaudio;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.opengl.Matrix;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVERABLE_BT = 0;

    private BluetoothAdapter bluetoothAdapter;

    /**
     * A set of paired devices.
     */
    private Set<BluetoothDevice> deviceSet;

    /**
     * The name of each paired device in the set.
     */
    private List<String> pairedDevices;

    private ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        final ImageView logo = findViewById(R.id.logo);
        final Button toggleSwitch = findViewById(R.id.on_off);
        final TextView not_supported = findViewById(R.id.not_supported);
        not_supported.setVisibility(View.INVISIBLE);
        final Button coverage = findViewById(R.id.coverage_button);
        listView = findViewById(R.id.listView);
        setPairedDevices(listView);
        if (bluetoothAdapter == null) {
            coverage.setEnabled(false);
            toggleSwitch.setEnabled(false);
            toggleSwitch.setVisibility(View.INVISIBLE);
            not_supported.setVisibility(View.VISIBLE);
        }
        toggleSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.enable();
                    Context context = getApplicationContext();
                    CharSequence text = "TURNING ON BLUETOOTH";
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                } else {
                    bluetoothAdapter.disable();
                    Context context = getApplicationContext();
                    CharSequence text = "TURNING OFF BLUETOOTH";
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        });
        coverage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!bluetoothAdapter.isDiscovering()) {
                    Context context = getApplicationContext();
                    CharSequence text = "MAKING YOUR DEVICE DISCOVERABLE";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(enableBtIntent, REQUEST_DISCOVERABLE_BT);
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

    public void setPairedDevices(View v) {
        deviceSet = bluetoothAdapter.getBondedDevices();
        pairedDevices = new ArrayList();

        for (BluetoothDevice bt : deviceSet) {
            pairedDevices.add(bt.getName());
            Toast.makeText(getApplicationContext(), "Showing Paired Devices", Toast.LENGTH_SHORT).show();
            final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, pairedDevices);
            listView.setAdapter(adapter);
        }
    }

}