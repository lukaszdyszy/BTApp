package com.example.btapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Button grantPermissonsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        grantPermissonsBtn = (Button) findViewById(R.id.grantPermissionsBtn);
        grantPermissonsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handlePermissions();
            }
        });

        handlePermissions();
    }


    // ------------------------------------------------
    // Permissions
    // ================================================
    private final String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private final int permRequestCode = 1;

    private void handlePermissions() {
        if(!areWeOk()) {
            permissonLauncher.launch(permissions);
        } else {
            startHomeScreenActivity();
        }
    }

    private boolean areWeOk(){
        int rejected = 0;
        for(String perm : permissions){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(), perm) == PackageManager.PERMISSION_DENIED) rejected++;
        }
        return rejected == 0;
    }

    private ActivityResultLauncher permissonLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onActivityResult(Map<String, Boolean> result) {
            if(!result.containsValue(false)){
                startHomeScreenActivity();
            }
        }
    });

    private void startHomeScreenActivity(){
        Intent homeScreenIntent = new Intent(this.getApplicationContext(), HomeScreenActivity.class);
        startActivity(homeScreenIntent);
    }
}