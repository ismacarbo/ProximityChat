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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private BluetoothAdapter bluetoothAdapter;
    public Set<BluetoothDevice> dispositiviTrovati = new HashSet<>();
    public listaDispositivi DeviceListAdapter;
    private ListView dispositivi;
    private Comunicazioni comunicazioni;
    private Button iniziaConnessione;
    private Button invia;
    private EditText editText;
    private BluetoothDevice dispositivo;
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dispositivi = (ListView) findViewById(R.id.nuoviDisp);
        dispositiviTrovati = new HashSet<>();
        bluetoothAdapter = bluetoothAdapter.getDefaultAdapter();
        editText=findViewById(R.id.editText);
        invia=findViewById(R.id.invia);
        iniziaConnessione=findViewById(R.id.iniziaConnessione);

        invia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iniziaConnessione();
            }
        });
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
                DeviceListAdapter = new listaDispositivi(context, R.layout.device_adapter_view, trovati);
                dispositivi.setAdapter(DeviceListAdapter);
            }
        }
    };



    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: chiamato");
        super.onDestroy();
    }

    public void cerca(View view) {
        Log.d(TAG, "controllo di dispositivi non associati");

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
                return;
            }
        }

        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: cancellato");


            controllaPermessi();

            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if(!bluetoothAdapter.isDiscovering()){

            controllaPermessi();

            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }


    public void iniziaConnessione(){
        iniziaConessioneBT(dispositivo,MY_UUID_INSECURE);
    }

    public void iniziaConessioneBT(BluetoothDevice dispositivo, UUID uuid){
        comunicazioni.startClient(dispositivo,uuid);
    }


    /**
     * controlla i permessi nel manifest.xml per il bluetooth API+23
     */
    private void controllaPermessi() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            }
            if (permissionCheck != 0) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
                }
            }
        }else{
            Log.d(TAG, "non serve controllare. SDK version < LOLLIPOP.");
        }
    }
}