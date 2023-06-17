package com.example.shin_utsurundesu;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.UUID;

public class ConnectBT extends AsyncTask<Void, Void, Void> {

    private static final String DEVICE_ADDRESS = "SET BLUETOOTH ADDRESS FOR RASPBERRY PI HERE";
    private static final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");  // Raspberry Pi側も同じにしてね
    private Activity mParentActivity;
    private BluetoothAdapter mAdapter;
    private BluetoothSocket mSocket;
    private boolean isConnected = true;
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
    protected Void doInBackground(Void... params) {
        try {
            if (mSocket == null || !isConnected) {
                BluetoothDevice device = mAdapter.getRemoteDevice(DEVICE_ADDRESS);
                if (ActivityCompat.checkSelfPermission(mParentActivity, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return null;
                }
                mSocket = device.createInsecureRfcommSocketToServiceRecord(PORT_UUID);
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                mSocket.connect();
            }
        } catch (IOException e) {
            isConnected = false;
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        if (!isConnected) {
            Toast.makeText(mParentActivity, "Bluetooth接続エラー", Toast.LENGTH_LONG).show();
            mParentActivity.finish();
        } else {
            Toast.makeText(mParentActivity, "Bluetooth接続成功", Toast.LENGTH_LONG).show();
        }
    }
}
