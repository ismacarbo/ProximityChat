package com.example.proximitychat;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;


public class Comunicazioni {

    private static final String TAG = "BluetoothConnectionServ";

    private static final String nome = "proximityChat";

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private final BluetoothAdapter bluetoothAdapter;
    Context context;


    public Comunicazioni(Context context) {
        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    /**
     * ascolta per nuove connessioni
     */
    private class AccettaThread extends Thread {


        private final BluetoothServerSocket serverSocket;

        public AccettaThread() {
            BluetoothServerSocket tmp = null;

            //server socket che ascolta
            try {
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(nome, MY_UUID_INSECURE);

                Log.d(TAG, "Connessione accettata, codice: " + MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "Eccezione " + e.getMessage());
            }
            serverSocket = tmp;
        }

        public void run() {

            BluetoothSocket socket = null;

            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                Log.e(TAG, "Eccezione " + e.getMessage());
            }


            if (socket != null) {
                //connetti(socket, dispositivo);
            }

            Log.i(TAG, "END mAcceptThread ");
        }


    }
}

