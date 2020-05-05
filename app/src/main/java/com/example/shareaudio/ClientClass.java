package com.example.shareaudio;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;


/**
 * ClientClass was the class responsible for creating and connecting the phone to a Bluetooth socket.
 * However, we could not seem to establish a stable connection, so this class became obsolete once we
 * implemented BluetoothConnectionService.java. We left it in the app to demonstrate our progress as
 * we worked, thought it can be deleted with no consequence to performance.
 */
public class ClientClass {

    /**
     * Here, socket is created, but not initialized. It will be set in the constructor. The socket is
     * the connection between the Bluetooth device and the phone itself.
     */
    private BluetoothSocket socket;

    /**
     * Here, device is created, but not initialized. It will be set in the constructor. The device is
     * the reference to the Bluetooth device the phone is trying to connect to.
     */
    private BluetoothDevice device;

    /**
     * Here, adapter is created, but not initialized. It will be set in the constructor. The adapter is
     * the physical Bluetooth adapter on the phone, and in a sense represents the phone itself in this class.
     */
    private BluetoothAdapter adapter;

    /**
     * This is the constructor for the class. It takes the Bluetooth device and the phone's Bluetooth
     * adapter and creates a Bluetooth socket between them. This socket is what allows information to
     * be passed over the Bluetooth connection. However, the constructor does not activate or maintain
     * the connection, that is done later. We included a try / catch feature to prevent app crashes.
     * @param newDevice - The Bluetooth device that the phone is attempting to connect to.
     * @param newAdapter - the actual Bluetooth adapter of the phone that is sending/receiving signal.
     */
    public ClientClass(BluetoothDevice newDevice, BluetoothAdapter newAdapter) {
        device = newDevice;
        adapter = newAdapter;
        try {
            socket = device.createRfcommSocketToServiceRecord(UUID.randomUUID());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This function is responsible for actually creating the connection over the Bluetooth socket using
     * socket.connect() function provided by the android Bluetooth API. Again, we included a try / catch
     * feature to help prevent crashes and provide us with information when the app would break.
     */
    public void run() {
        adapter.cancelDiscovery();
        try {
            socket.connect();
            Log.d("CLIENT CLASS: ", "Client received");
        } catch (IOException e) {
            Log.d("CLIENT CLASS: ", "Client failed");
            e.printStackTrace();
        }
    }
}
