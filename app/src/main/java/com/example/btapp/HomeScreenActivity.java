package com.example.btapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeScreenActivity extends AppCompatActivity {
    // BT Adapter
    BluetoothAdapter BA;

    // Discovered devices
    Set<BluetoothDevice> discoveredDevices;

    // UI
    EditText nameInput;
    ImageView btSwitch;
    TextView btNotSupportedLabel;
    TextView btOffLabel;
    TextView devicesListLabel;
    ListView devicesList;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        // set up BluetoothAdapter
        BA = BluetoothAdapter.getDefaultAdapter();
        if (BA == null) {
            btNotSupportedLabel.setVisibility(View.VISIBLE);
        } else {
            discoveredDevices = new HashSet<BluetoothDevice>();
            setUpUi();
            switchBluetoothCallback();
        }
    }

    private void setUpUi() {
        nameInput = (EditText) findViewById(R.id.nameInput);
        nameInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                return nameInputSubmit(view, i, keyEvent);
            }
        });
        btNotSupportedLabel = (TextView) findViewById(R.id.btNotSupportedLabel);
        btOffLabel = (TextView) findViewById(R.id.btOffLabel);
        devicesListLabel = (TextView) findViewById(R.id.devicesListLabel);
        btSwitch = (ImageView) findViewById(R.id.btSwitch);
        btSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchBluetooth();
            }
        });
        devicesList = (ListView) findViewById(R.id.devicesList);
        devicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                sendToDevice((Device)adapterView.getItemAtPosition(i));
            }
        });
    }

    private void sendToDevice(Device D) {
        BA.cancelDiscovery();
        Log.d("itemclicked", D.getDevice().getAddress());

        if(D.getDevice().getBondState() == BluetoothDevice.BOND_BONDED){
            Log.d("itemclicked", "bonded");
        } else {
            Log.d("itemclicked", "Not bonded");
        }
    }


    // -----------------------------------------
    // Bluetooth actions
    // ========================================
    private ActivityResultLauncher btSwitchLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onActivityResult(ActivityResult result) {
            Log.d("btenable", String.valueOf(result.describeContents()));
            if (result != null && result.getResultCode() == RESULT_OK) {
                switchBluetoothCallback();
            }
            Log.d("btenable", "state changed");
        }
    });

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void switchBluetooth() {
        Log.d("btenable", "en click");
        if (!BA.isEnabled()) {
            Intent btEnableIntent = new Intent(BA.ACTION_REQUEST_ENABLE);
            btSwitchLauncher.launch(btEnableIntent);
        } else {
            Intent btDisableIntent = new Intent("android.bluetooth.adapter.action.REQUEST_DISABLE");
            btSwitchLauncher.launch(btDisableIntent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void switchBluetoothCallback() {
        if(BA.isEnabled()){
            btSwitch.setColorFilter(this.getColor(android.R.color.holo_blue_bright));
            nameInput.setEnabled(true);
            nameInput.setText(BA.getName());
            btOffLabel.setVisibility(View.INVISIBLE);
            devicesList.setVisibility(View.VISIBLE);
            devicesListLabel.setVisibility(View.VISIBLE);
            getPairedDevices();
            Intent discoverableIntent = new Intent("android.bluetooth.adapter.action.REQUEST_DISCOVERABLE");
            discoverableIntent.putExtra( BA.EXTRA_DISCOVERABLE_DURATION, 0);
            startActivity(discoverableIntent);
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(receiver, filter);
            BA.startDiscovery();
        } else {
            Log.d("btenable", "disabled");
            btSwitch.setColorFilter(this.getColor(android.R.color.black));
            nameInput.setEnabled(false);
            btOffLabel.setVisibility(View.VISIBLE);
            devicesList.setVisibility(View.INVISIBLE);
            devicesListLabel.setVisibility(View.INVISIBLE);
            discoveredDevices.clear();
        }
    }

    private void getPairedDevices(){
//        Set<BluetoothDevice> paired = BA.getBondedDevices();
//        ArrayList<Device> pairedList = new ArrayList<Device>();
//        for(BluetoothDevice p : paired){
//            pairedList.add(new Device(p));
//        }
//        ArrayAdapter<Device> adapter = new ArrayAdapter<Device>(this,android.R.layout.simple_list_item_1, pairedList);
//        devicesList.setAdapter(adapter);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                discoveredDevices.add(device);
                Log.d("dfound", discoveredDevices.toString());
                updateDevicesList();
            }
        }
    };

    // -----------------------------------
    // Name change
    private boolean nameInputSubmit(View v, int keyCode, KeyEvent event){
        Log.d("key", String.valueOf(keyCode));
        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
            InputMethodManager im = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(v.getWindowToken(), 0);
            BA.setName(String.valueOf(nameInput.getText()));
            nameInput.clearFocus();
            return true;
        }
        return false;
    }

    // update list
    private void updateDevicesList() {
        ArrayList<Device> ds = new ArrayList<Device>();
        for(BluetoothDevice d : discoveredDevices){
            ds.add(new Device(d));
        }
        ArrayAdapter<Device> dAdapter = new ArrayAdapter<Device>(this,android.R.layout.simple_list_item_1, ds);
        devicesList.setAdapter(dAdapter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
    }
}