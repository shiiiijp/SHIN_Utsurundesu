// MainActivity.java と置き換えてください
package com.example.shin_utsurundesu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ConnectBT.ConnectBTCallback {
    private static final String TAG = "MAINApp";
    private final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;

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

        previewButton.setEnabled(false);
        shootButton.setEnabled(false);

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
        Log.d(TAG, "blog onClick()");
        if (view.getId() == R.id.connectButton) {
            new ConnectBT(MainActivity.this, bluetoothAdapter, MainActivity.this).execute();
        } else if (view.getId() == R.id.previewButton) {
            if (socket != null && socket.isConnected()) {
                sendPreviewRequest(socket);
                new ReceivePhotoTask().execute(socket);
            } else {
                Toast.makeText(MainActivity.this, "Bluetoothに接続されていません", Toast.LENGTH_SHORT).show();
            }
        } else if (view.getId() == R.id.shootButton) {
            if (socket != null && socket.isConnected()) {
                sendShootRequest(socket);
            } else {
                Toast.makeText(MainActivity.this, "Bluetoothに接続されていません", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onConnectBTResult(BluetoothSocket socket) {
        this.socket = socket;
        if (socket != null && socket.isConnected()) {
            Toast.makeText(MainActivity.this, "Bluetooth接続成功", Toast.LENGTH_LONG).show();
            previewButton.setEnabled(true);
            shootButton.setEnabled(true);
        } else {
            Toast.makeText(MainActivity.this, "Bluetooth接続エラー", Toast.LENGTH_LONG).show();
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

    private class ReceivePhotoTask extends AsyncTask<BluetoothSocket, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(BluetoothSocket... sockets) {
            BluetoothSocket socket = sockets[0];
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(socket.getInputStream());
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                previewImageView.setImageBitmap(result);
                if(previewImageView.getParent() != null) {
                    ((ViewGroup)previewImageView.getParent()).removeView(previewImageView);
                }
                previewFrameLayout.addView(previewImageView);
            } else {
                Log.d(TAG, "Unable to receive preview.");
                Toast.makeText(MainActivity.this, "プレビューを受信できませんでした", Toast.LENGTH_SHORT).show();
            }
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
