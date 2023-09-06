package com.example.proximitychat;

import android.app.AlertDialog;
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

    private static final UUID UUID_mio =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private final BluetoothAdapter bluetoothAdapter;
    private final Context context;

    private AccettaThread thread1;
    private ConnettiThread connettiThread;
    private UUID uuidDispositivo;
    private ProgressDialog dialog;
    private BluetoothDevice dispositivo;
    private ThreadConnesso threadConnesso;


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
                connetti(socket, dispositivo);
            }
            
        }

        public void cancel() {

            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "serverSocket: " + e.getMessage());
            }
        }


    }

    private class ConnettiThread extends Thread {

        private BluetoothSocket socket;

        public ConnettiThread(BluetoothDevice disp, UUID uuid) {
            Log.d(TAG, "connessione inizio");
            dispositivo = disp;
            uuidDispositivo = uuid;
        }


        /**
         * Crea la connesione con il dispositivo selezionato
         */
        public void run() {
            BluetoothSocket temp = null;

            try {

                temp = dispositivo.createRfcommSocketToServiceRecord(uuidDispositivo);
            } catch (IOException e) {
                Log.e(TAG, "connessione fallita" + e.getMessage());
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

            connetti(socket,dispositivo);
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

    /**
     * avvia la chat (crea l'oggetto)
     */
    public synchronized void start() {
        Log.d(TAG, "inizio");


        if (connettiThread != null) {
            connettiThread.cancel();
            connettiThread = null;
        }
        if (thread1 == null) {
            thread1 = new AccettaThread();
            thread1.start();
        }
    }

    /**
     * si mette in ascolto
     * il thread connessione prova a fare una connessione con l'oggetto AcettaThread sull'altro dispositivo
     **/

    public void startClient(BluetoothDevice dispositivo1, UUID uuid) {


        dialog = ProgressDialog.show(context, "Connessione bluetooth..."
                , "Attendere prego", true);

        connettiThread = new ConnettiThread(dispositivo1, uuid);
        connettiThread.start();
    }

    /**
     * classe per la connessione
     **/
    private class ThreadConnesso extends Thread {

        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;


        public ThreadConnesso(BluetoothSocket socket) {
            this.socket = socket;
            InputStream tempInput = null;
            OutputStream tempoOutput = null;
            try {
                tempInput = socket.getInputStream();
                tempoOutput = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }


            //tolgo il dialog ormai la connessione c'è
            dialog.dismiss();


            inputStream = tempInput;
            outputStream = tempoOutput;
        }


        public void run() {
            byte[] buffer = new byte[1024];

            int bytes; //in ritorno dal thread

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    String messaggio = new String(buffer, 0, bytes);
                    Log.d(TAG, "IN ARRIVO: " + messaggio);
                } catch (IOException e) {
                    Log.e(TAG, "Eccezione: " + e.getMessage());
                    break;
                }
            }
        }


        //chiama dal main per scrivere
        public void write(byte[] bytes) {
            String messaggio = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "IN USCITA: " + messaggio);
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Eccezione: " + e.getMessage());
            }
        }

        //chiama dal main per chiudere
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }


    private void connetti(BluetoothSocket socket, BluetoothDevice mmDevice) {

        //inizio del thread
        threadConnesso = new ThreadConnesso(socket);
        threadConnesso.start();
    }

    /**
     * scrive in background quando arriva un messaggio
     */
    public void write(byte[] out) {
        ThreadConnesso tmp;
        threadConnesso.write(out);
    }

}