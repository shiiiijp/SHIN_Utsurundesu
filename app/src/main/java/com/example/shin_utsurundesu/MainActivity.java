package com.example.shin_utsurundesu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String LOG = "BluetoothApp";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private int REQUEST_ENABLE_BT = 1;

    private Button connectButton;
    private Button previewButton;
    private Button shootButton;
    private ImageView previewImageView;
    private FrameLayout previewFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectButton = findViewById(R.id.connectButton);
        previewButton = findViewById(R.id.previewButton);
        shootButton = findViewById(R.id.shootButton);
        previewImageView = findViewById(R.id.previewImageView);
        previewFrameLayout = findViewById(R.id.previewFrameLayout);

        connectButton.setOnClickListener(this);
        previewButton.setOnClickListener(this);
        shootButton.setOnClickListener(this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(MainActivity.this, "Bluetoothはこのデバイスでサポートされていません", Toast.LENGTH_LONG).show();
            finish();
        } else if (!bluetoothAdapter.isEnabled()) {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            startActivityForResult(enableAdapter, REQUEST_ENABLE_BT);
        }
    }

    @Override
    public void onClick(View view) {
        Log.d(LOG, "blog onClick()");
        if (view.getId() == R.id.connectButton) {
            new ConnectBT(MainActivity.this, bluetoothAdapter, socket){
                @Override
                protected void onPostExecute(BluetoothSocket socket) {
                    super.onPostExecute(socket);
                    MainActivity.this.socket = socket;
                }
            }.execute();
        }
        if (view.getId() == R.id.previewButton) {
            if (socket != null && socket.isConnected()) {
                sendPreviewRequest(socket);

                long startTime = System.currentTimeMillis();
                long endTime = startTime + 10000;
                while (System.currentTimeMillis() < endTime) {
                    receivePhoto(socket);
                }
            } else {
                Toast.makeText(MainActivity.this, "Bluetoothに接続されていません", Toast.LENGTH_SHORT).show();
            }
        }
        if (view.getId() == R.id.shootButton) {
            if (socket != null && socket.isConnected()) {
                sendShootRequest(socket);
            } else {
                Toast.makeText(MainActivity.this, "Bluetoothに接続されていません", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendPreviewRequest(BluetoothSocket socket) {
        try {
            String request = "previewRequest";  // Raspberry Pi側と揃えてね

            OutputStream outputStream = socket.getOutputStream();
            byte[] requestData = request.getBytes();

            outputStream.write(requestData);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receivePhoto(BluetoothSocket socket) {
        try {
            InputStream inputStream = socket.getInputStream();

            byte[] buffer = new byte[1024];
            int bytesRead;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            byte[] photoData = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();

            Bitmap photoBitmap = BitmapFactory.decodeByteArray(photoData, 0, photoData.length);
            previewImageView.setImageBitmap(photoBitmap);
            previewFrameLayout.addView(previewImageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendShootRequest(BluetoothSocket socket) {
        try {
            String request = "shootRequest";  // Raspberry Pi側と揃えてね

            OutputStream outputStream = socket.getOutputStream();
            byte[] requestData = request.getBytes();

            outputStream.write(requestData);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}