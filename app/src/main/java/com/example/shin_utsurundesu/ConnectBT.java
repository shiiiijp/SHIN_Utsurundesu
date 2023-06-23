// ConnectBT.java と置き換えてください
package com.example.shin_utsurundesu;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class ConnectBT extends AsyncTask<Void, Void, BluetoothSocket> {
    static final int REQUEST_CODE = 1;
    private static final String DEVICE_ADDRESS = "B8:27:EB:D7:92:D5";
//    private static final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");  // Raspberry Pi側も同じにしてね
//    private static final UUID SERVICE_UUID = UUID.fromString("e288efca-8008-4105-99f0-072c8e9e51a3"); // 任意のUUID
    private static final UUID SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String TAG = "CBT";
    private boolean isConnected = false;

    private Activity mParentActivity;
    private BluetoothAdapter mAdapter;
    private BluetoothSocket mSocket;
    private ConnectBTCallback mCallback;


    public ConnectBT(Activity parentActivity, BluetoothAdapter bluetoothAdapter, ConnectBTCallback callback) {
        this.mParentActivity = parentActivity;
        this.mAdapter = bluetoothAdapter;
        this.mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Toast.makeText(mParentActivity, "Bluetooth接続中...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected BluetoothSocket doInBackground(Void... params) {

        if (Build.VERSION.SDK_INT >= 31) {
            if (ActivityCompat.checkSelfPermission(mParentActivity, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                //todo  ActivityCompat.checkSelfPermission
            }
        }else{
            if (ActivityCompat.checkSelfPermission(mParentActivity, android.Manifest.permission.BLUETOOTH)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mParentActivity, new String[]{
                        android.Manifest.permission.BLUETOOTH
                }, REQUEST_CODE);
            }
            if (ActivityCompat.checkSelfPermission(mParentActivity, android.Manifest.permission.BLUETOOTH_ADMIN)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mParentActivity, new String[]{
                        android.Manifest.permission.BLUETOOTH_ADMIN
                }, REQUEST_CODE);
            }
        }
        if (!isConnected) {
            try {
                Set<BluetoothDevice> pairedDevices = this.mAdapter.getBondedDevices();
                Log.d("App", String.valueOf(pairedDevices.size()));
                if (pairedDevices.size() > 0) {
                    /*
                    事前にペアリング済みのデバイスを全て取得
                    ペアリング対象の DEVICE_ADDRESS(MAC address) or deviceName をUIから選択できるようにすれば
                    任意のデバイスとのペアリングができます
                    */
                    for (BluetoothDevice device : pairedDevices) {
                        String deviceName = device.getName();
                        String deviceHardwareAddress = device.getAddress(); // MAC address
                        Log.d("App", deviceName);
                        Log.d("App", deviceHardwareAddress);
                        Log.d("App", DEVICE_ADDRESS);
                        if (deviceHardwareAddress.equals(DEVICE_ADDRESS)) {
                            Log.d("App", "Device Found! (" + DEVICE_ADDRESS + ")");
                            mSocket = device.createRfcommSocketToServiceRecord(SERVICE_UUID);
                            Log.d("App", "Got BluetoothSocket");
                            if (mAdapter.isDiscovering()) {
                                mAdapter.cancelDiscovery();
                            }
                            try {
                                mSocket.connect();
                                isConnected = true;
                                Log.d("App", "Connected");
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
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
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