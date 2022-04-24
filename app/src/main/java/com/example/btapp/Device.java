package com.example.btapp;

import android.bluetooth.BluetoothDevice;

public class Device {
    private BluetoothDevice device;

    public Device(BluetoothDevice d){
        device = d;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    @Override
    public String toString() {
        return device.getName();
    }
}
