package com.example.shin_utsurundesu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

public class ConnectBTActivity extends AppCompatActivity implements ConnectBT.ConnectBTCallback {
    private final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_btactivity);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(ConnectBTActivity.this, "Bluetoothはこのデバイスでサポートされていません", Toast.LENGTH_LONG).show();
            finish();
        } else if (!bluetoothAdapter.isEnabled()) {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            startActivityForResult(enableAdapter, REQUEST_ENABLE_BT);
        }

        FrameLayout button_connect_bt = findViewById(R.id.button_shoot);
        button_connect_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ConnectBT(ConnectBTActivity.this, bluetoothAdapter, ConnectBTActivity.this).execute();
            }
        });
    }

    public void onConnectBTResult(BluetoothSocket socket) {
        if (socket != null && socket.isConnected()) {
            BluetoothSocketHolder.setSocket(socket);
            Toast.makeText(ConnectBTActivity.this, "Bluetooth接続成功", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplication(), ShootingActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(ConnectBTActivity.this, "Bluetooth接続エラー", Toast.LENGTH_SHORT).show();
        }
    }
}