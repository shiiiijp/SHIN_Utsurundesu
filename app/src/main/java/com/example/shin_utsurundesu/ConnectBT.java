package com.example.shin_utsurundesu;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class ConnectBT extends AsyncTask<Void, Void, BluetoothSocket> {
//    private static final String DEVICE_ADDRESS = "B8:27:EB:D7:92:D5";
    private static final String DEVICE_ADDRESS = "40:5B:D8:A3:C8:2A";
    private static final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");  // Raspberry Pi側も同じにしてね
    private static final String TAG = "CBT";
    private boolean isConnected = false;

    private Activity mParentActivity;
    private BluetoothAdapter mAdapter;
    private BluetoothSocket mSocket;
    private BluetoothDevice mDevice;
    private ConnectBTCallback mCallback;


    public ConnectBT(Activity parentActivity, BluetoothAdapter bluetoothAdapter, ConnectBTCallback callback) {
        this.mParentActivity = parentActivity;
        this.mAdapter = bluetoothAdapter;
        this.mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Toast.makeText(mParentActivity, "Bluetooth接続中...", Toast.LENGTH_LONG).show();
    }

    @Override
    protected BluetoothSocket doInBackground(Void... params) {
        if (ActivityCompat.checkSelfPermission(mParentActivity, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        if (!isConnected) {
            mDevice = mAdapter.getRemoteDevice(DEVICE_ADDRESS);

            try {
                mSocket = mDevice.createInsecureRfcommSocketToServiceRecord(PORT_UUID);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            if (BluetoothAdapter.getDefaultAdapter().isDiscovering()) {
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            }

            try {
                mSocket.connect();
                isConnected = true;
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Unable to connect.");
                try {
                    mSocket.close();
                    Log.d(TAG, "Unable to close socket.");
                } catch (IOException closeException) {
                    e.printStackTrace();
                }
            }
        }
        return mSocket;
    }

    @Override
    protected void onPostExecute(BluetoothSocket socket) {
        super.onPostExecute(socket);
        if (mCallback != null) {
            mCallback.onConnectBTResult(socket);
        }
    }

    public interface ConnectBTCallback {
        void onConnectBTResult(BluetoothSocket socket);
    }
}