package com.example.shin_utsurundesu;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ShootingActivity extends AppCompatActivity implements View.OnClickListener {
    private BluetoothSocket socket;
    private FrameLayout previewButton;
    private FrameLayout shootButton;
    private ImageView previewImageView;
    private FrameLayout previewFrameLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shooting);

        socket = BluetoothSocketHolder.getSocket();

        previewButton = findViewById(R.id.button_preview);
        shootButton = findViewById(R.id.button_shoot);
        previewImageView = findViewById(R.id.imageview_preview);
        previewFrameLayout = findViewById(R.id.framelayout_preview);

        previewButton.setOnClickListener(this);
        shootButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_preview) {
            if (socket != null && socket.isConnected()) {
                sendPreviewRequest(socket);
                new ReceivePhotoTask().execute(socket);
            } else {
                Toast.makeText(ShootingActivity.this, "Bluetoothに接続されていません", Toast.LENGTH_SHORT).show();
            }
        } else if (view.getId() == R.id.button_shoot) {
            if (socket != null && socket.isConnected()) {
                sendShootRequest(socket);
            } else {
                Toast.makeText(ShootingActivity.this, "Bluetoothに接続されていません", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendPreviewRequest(BluetoothSocket mysocket) {
        try {
            String request = "previewRequest";  // Raspberry Pi側と揃えてね

            OutputStream outputStream = mysocket.getOutputStream();
            byte[] requestData = request.getBytes();

            outputStream.write(requestData);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
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
                Toast.makeText(ShootingActivity.this, "プレビューを受信できませんでした", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendShootRequest(BluetoothSocket mysocket) {
        try {
            String request = "shootRequest";  // Raspberry Pi側と揃えてね

            OutputStream outputStream = mysocket.getOutputStream();
            byte[] requestData = request.getBytes();

            outputStream.write(requestData);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
