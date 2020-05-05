package com.example.shareaudio;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.util.UUID;

public class ClientClass {
    private BluetoothSocket socket;
    private BluetoothDevice device;
    private BluetoothAdapter adapter;

    public ClientClass(BluetoothDevice newDevice, BluetoothAdapter newAdapter) {
        device = newDevice;
        adapter = newAdapter;
        try {
            socket = device.createRfcommSocketToServiceRecord(UUID.randomUUID());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        adapter.cancelDiscovery();
        try {
            socket.connect();
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException p) {
                p.printStackTrace();
            }
            e.printStackTrace();
        }
    }
}
