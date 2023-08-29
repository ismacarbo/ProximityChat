package com.example.proximitychat;

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
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    BluetoothAdapter BluetoothAdapter;
    public Set<BluetoothDevice> dispositiviTrovati = new HashSet<>();
    public DeviceListAdapter DeviceListAdapter;
    ListView dispositivi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dispositivi = (ListView) findViewById(R.id.nuoviDisp);
        dispositiviTrovati = new HashSet<>();
        BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: inizio");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                dispositiviTrovati.add(device);
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                        return;
                    }
                }
                Log.d(TAG, "Trovati: " + device.getName() + ": " + device.getAddress());
                ArrayList<BluetoothDevice> trovati=new ArrayList<>();
                trovati.addAll(dispositiviTrovati);
                DeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, trovati);
                dispositivi.setAdapter(DeviceListAdapter);
            }
        }
    };



    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: chiamato");
        super.onDestroy();
    }

   
}