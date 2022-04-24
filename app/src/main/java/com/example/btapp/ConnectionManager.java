package com.example.btapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

class AcceptThread extends Thread {
    private BluetoothAdapter BA;
    private final BluetoothServerSocket mmServerSocket;
    private String APP_NAME = "BTApp";
    private UUID APP_UUID = UUID.fromString("e5cd4a22-c3ba-11ec-9d64-0242ac120002");

    public AcceptThread(BluetoothAdapter b) {
        BA = b;
        BluetoothServerSocket tmp = null;
        try {
            tmp = BA.listenUsingRfcommWithServiceRecord(APP_NAME, APP_UUID);
        } catch (IOException e) {
            Log.e("btsocket", "Socket's listen() method failed", e);
        }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                Log.e("btsocket", "Socket's accept() method failed", e);
                break;
            }

            if (socket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e("btsocket", "Could not close the connect socket", e);
        }
    }
}

class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private BluetoothAdapter BA;
    private UUID APP_UUID = UUID.fromString("e5cd4a22-c3ba-11ec-9d64-0242ac120002");

    public ConnectThread(BluetoothDevice device, BluetoothAdapter b) {
        BA = b;
        BluetoothSocket tmp = null;
        mmDevice = device;

        try {
            tmp = device.createRfcommSocketToServiceRecord(APP_UUID);
        } catch (IOException e) {
            Log.e("btsocketcli", "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        BA.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e("btsocketcli", "Could not close the client socket", closeException);
            }
            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e("btsocketcli", "Could not close the client socket", e);
        }
    }
}

