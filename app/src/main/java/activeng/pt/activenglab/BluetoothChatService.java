/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package activeng.pt.activenglab;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.SyncStateContract;

import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
//import com.example.android.common.logger.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import activeng.pt.activenglab.data.TemperatureContract;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothChatService {
    // Debugging
    private static final String TAG = "BluetoothChatService";

    // to make sure we only unregister the receiver if it was registered before
    private boolean registered = false;

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";

    // Unique UUID for this application
    // Não vou precisar disto
    // O Android vai participar como client bluetooth

    //OK
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //    private static final UUID MY_UUID_SECURE =
    //            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    //    private static final UUID MY_UUID_INSECURE =
    //            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    // Member fields
    private final BluetoothAdapter mAdapter;
     //private final Handler mHandler;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private int mStateProtocol = Constants.STATE_PROTOCOL_NONE;
    private Context mContext;

    private BroadcastReceiver connectionUpdates;

    private String deviceName;
    private String deviceAddress;

    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param context The UI Activity Context
     */
    //public BluetoothChatService(Context context, Handler handler) {
    public BluetoothChatService(Context context) {
        mContext = context;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = Constants.STATE_NONE;
        //mHandler = handler;

        connectionUpdates = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                Log.d("ActivEng", "BluetoothChatService --> onReceive");
                if (extras != null) {
                    String message = extras.getString(Intent.EXTRA_TEXT);
                    Log.d("ActivEng", message);
                    write(message + "\n");
                }
            }
        };

    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        // mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();

        Intent intent = new Intent(Constants.MESSAGE_BT_STATE_CHANGE).putExtra(Intent.EXTRA_TEXT, state);
        //mContext.sendBroadcast(intent);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
        manager.sendBroadcast(intent);
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(Constants.STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == Constants.STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        setState(Constants.STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        deviceName = device.getName();
        deviceAddress = device.getAddress();

        // Send the name of the connected device back to the UI Activity

        //Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        //Bundle bundle = new Bundle();
        //bundle.putString(Constants.DEVICE_NAME, device.getName());
        //bundle.putString(Constants.DEVICE_ADRESS, device.getAddress());
        //msg.setData(bundle);
        //mHandler.sendMessage(msg);

        //Intent intent = new Intent(Constants.MESSAGE_BT_NAME).putExtra(Intent.EXTRA_TEXT, device.getName());
        //mContext.sendBroadcast(intent);

        Log.d("ActivEng", "BluetoothChatService BroadcastReceiver versus LocalBroadcastReceiver");
        //mContext.getApplicationContext().registerReceiver(this.connectionUpdates, new IntentFilter(Constants.MESSAGE_TO_ARDUINO));
        //
        //LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
        //LocalBroadcastManager.getInstance(mContext.getApplicationContext()).registerReceiver(this.connectionUpdates,
        //        new IntentFilter(Constants.MESSAGE_TO_ARDUINO));
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
        IntentFilter filter = new IntentFilter(Constants.MESSAGE_TO_ARDUINO);
        manager.registerReceiver(this.connectionUpdates, filter);
        registered = true;
        setState(Constants.STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        deviceName = "";
        deviceAddress = "";
        setState(Constants.STATE_NONE);

        Log.d("ActivEng", "BluetoothChatService unregisterReceiver stop()");
        if (registered) {
            //mContext.getApplicationContext().unregisterReceiver(this.connectionUpdates);
            //LocalBroadcastManager.getInstance(mContext.getApplicationContext()).unregisterReceiver(this.connectionUpdates);
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);
            manager.unregisterReceiver(this.connectionUpdates);
            registered = false;
        }

    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(String out) {
        //Log.d("ActivEng", "----> Perform the write unsynchronized");
        byte[] send = out.getBytes();
        //for ( int j = 0; j < send.length; j++ ) {
        //    int v = send[j];
        //    Log.d("ActivEng", "send[" + j + "]=" + v);
        //}
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != Constants.STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        // Log.d("ActivEng", out);
        r.write(send);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {

        /*
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        */

        Intent intent = new Intent(Constants.MESSAGE_BT_FAIL).putExtra(Intent.EXTRA_TEXT, "Unable to connect device");
        //mContext.sendBroadcast(intent);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

        // Start the service over to restart listening mode
        BluetoothChatService.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {

        /*
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        */

        Intent intent = new Intent(Constants.MESSAGE_BT_FAIL).putExtra(Intent.EXTRA_TEXT, "Device connection was lost");
        //mContext.sendBroadcast(intent);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

        // Start the service over to restart listening mode
        BluetoothChatService.this.start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Create a new listening server socket
            try {
                if (secure) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                            MY_UUID_SECURE);
                } else {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                            NAME_INSECURE, MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "Socket Type: " + mSocketType +
                    "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != Constants.STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothChatService.this) {
                        switch (mState) {
                            case Constants.STATE_LISTEN:
                            case Constants.STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice(),
                                        mSocketType);
                                break;
                            case Constants.STATE_NONE:
                            case Constants.STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
            Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothChatService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[8192];
            int bytes, pos;
            StringBuilder queue = new StringBuilder("");
            String temp;
            Intent intent;
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    if (bytes > 0) {
                        String readMessage = new String(buffer, 0, bytes);
                        queue.append(readMessage);
                        // Log.d("Temperatura", "read() #" + Integer.toString(bytes) + " -> " + queue.substring(0, 40));
                        pos = queue.indexOf("\n");
                        while (pos >= 0) {
                            // Log.d("Temperatura", "indexOf() #" + pos);
                            temp = queue.substring(0, pos - 1);
                            queue.delete(0, pos + 1);
                            // Log.d("Temperatura", "limpa() #" + " -> " + temp);
                            // mHandler.obtainMessage(Constants.MESSAGE_READ, 0, 0, temp).sendToTarget();
                            Log.d("ActivEng", "sendBroadcast " + temp);
                            if (temp.length()>1) {
                                switch (temp.charAt(0)) {
                                    case 'R':
                                        Temperature tempObj = UtilitySingleton.getInstance().processAndSaveMessage(mContext, temp, deviceAddress, mStateProtocol);

                                        intent = new Intent(Constants.MESSAGE_TEMPERATURE);
                                        intent.putExtra(Constants.EXTRA_MSG_TEMP, tempObj.getRead());
                                        intent.putExtra(Constants.EXTRA_MSG_TEMP_STR, tempObj.getReadString());
                                        intent.putExtra(Constants.EXTRA_MSG_TEMP_SENSOR, tempObj.getReadSensor());
                                        intent.putExtra(Constants.EXTRA_MSG_TEMP_MILLIS, tempObj.getReadMillis());

                                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                                        break;
                                    case 'M':
                                        mStateProtocol = Constants.STATE_PROTOCOL_METADATA;
                                        intent = new Intent(Constants.MESSAGE_METADATA).putExtra(Intent.EXTRA_TEXT, temp);
                                        //mContext.sendBroadcast(intent);
                                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                                        break;
                                    case 'S':
                                        intent = new Intent(Constants.MESSAGE_SENSORMETADATA).putExtra(Intent.EXTRA_TEXT, temp);
                                        //mContext.sendBroadcast(intent);
                                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                                        break;
                                    case 'T':
                                        mStateProtocol = Constants.STATE_PROTOCOL_TIME;
                                        intent = new Intent(Constants.MESSAGE_CLOCK).putExtra(Intent.EXTRA_TEXT, temp);
                                        //mContext.sendBroadcast(intent);
                                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                                        break;
                                    default:
                                        break;
                                }
                            }
                            pos = queue.indexOf("\n");
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    // Start the service over to restart listening mode
                    BluetoothChatService.this.start();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                // mmOutStream.write(buffer);
                mmOutStream.write(buffer, 0, buffer.length);
                mmOutStream.flush();
                // Share the sent message back to the UI Activity
                // mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
                Log.d("ActivEng", "----> WRITE " + buffer.length + " Done!");
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
