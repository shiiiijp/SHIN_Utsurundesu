package com.example.shin_utsurundesu;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
    private static final String DEVICE_ADDRESS = "B8:27:EB:D7:92:D5";
    private static final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");  // Raspberry Pi側も同じにしてね
    private boolean isConnected = false;

    private Activity mParentActivity;
    private BluetoothAdapter mAdapter;
    private BluetoothSocket mSocket;
    private BluetoothDevice mDevice;

    public ConnectBT(Activity parentActivity, BluetoothAdapter bluetoothAdapter, BluetoothSocket socket) {
        this.mParentActivity = parentActivity;
        this.mAdapter = bluetoothAdapter;
        this.mSocket = socket;
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

            //接続が確立するまで少し待つ
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                mSocket = mDevice.createInsecureRfcommSocketToServiceRecord(PORT_UUID);
                if (BluetoothAdapter.getDefaultAdapter().isDiscovering()) {
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                }
                mSocket.connect();
                isConnected = true;
            } catch (IOException e) {
                isConnected = false;
                try {
                    mSocket.close();
                } catch (IOException closeException) {
                    e.printStackTrace();
                }
            }
            return mSocket;
        }
        return null;
    }

    @Override
    protected void onPostExecute(BluetoothSocket socket) {
        super.onPostExecute(socket);

        if (!isConnected) {
            Toast.makeText(mParentActivity, "Bluetooth接続エラー", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mParentActivity, "Bluetooth接続成功", Toast.LENGTH_LONG).show();
        }
    }
}
