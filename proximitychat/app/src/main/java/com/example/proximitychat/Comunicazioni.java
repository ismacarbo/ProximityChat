package com.example.proximitychat;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;


public class Comunicazioni {

    private static final String TAG = "BluetoothConnectionServ";

    private static final String nome = "proximityChat";

    private static final UUID UUID_mio =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private final BluetoothAdapter bluetoothAdapter;
    private Context context;

    private AccettaThread accettaThread;
    private ConnettiThread connettiThread;
    private UUID uuidDispositivo;
    private ProgressDialog dialog;
    private BluetoothDevice dispositivo;


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
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(nome, UUID_mio);

                Log.d(TAG, "Connessione accettata, codice: " + UUID_mio);
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

        public void cancel() {

            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "serverSocket: " + e.getMessage() );
            }
        }


    }

    private class ConnettiThread extends Thread{

        private BluetoothSocket socket;

        public ConnettiThread(BluetoothDevice disp, UUID uuid) {
            Log.d(TAG, "connessione inizio");
            dispositivo = disp;
            uuidDispositivo = uuid;
        }


        /**
         * Crea la connesione con il dispositivo selezionato
         */
        public void run(){
            BluetoothSocket temp = null;

            try {

                temp = dispositivo.createRfcommSocketToServiceRecord(uuidDispositivo);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }

            socket = temp;

            //cancello la ricerca perchè rallenta la connessione
            bluetoothAdapter.cancelDiscovery();

            //connessione al socket bluetooth

            try {
                //connessione bloccante (ci arriva solo se non ci sono errori)
                socket.connect();

                Log.d(TAG, "connesso");
            } catch (IOException e) {
                //chiusura socket
                try {
                    socket.close();
                } catch (IOException e1) {
                    Log.e(TAG, "Eccezione " + e1.getMessage());
                }
                Log.d(TAG, "Eccezione uuid: " + UUID_mio);
            }

            //connetti(socket,mmDevice);
        }
        public void cancel() {
            try {
                Log.d(TAG, "chiusura connessione client");
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "chiusura fallita, eccezione: " + e.getMessage());
            }
        }
    }

}

