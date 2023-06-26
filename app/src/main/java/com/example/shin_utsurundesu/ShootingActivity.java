package com.example.shin_utsurundesu;

import static android.content.ContentValues.TAG;

import static java.lang.Math.ceil;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;

public class ShootingActivity extends AppCompatActivity implements View.OnClickListener {
    private BluetoothSocket socket;
    private FrameLayout previewButton;
    private FrameLayout shootButton;
    private ImageView previewImageView;
    private FrameLayout previewFrameLayout;
    private TextView shootTextView;
    private CountDownTimer shootRequestTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shooting);

        socket = BluetoothSocketHolder.getSocket();

        previewButton = findViewById(R.id.button_preview);
        shootButton = findViewById(R.id.button_shoot);
        previewImageView = findViewById(R.id.imageview_preview);
        previewFrameLayout = findViewById(R.id.framelayout_preview);
        shootTextView = findViewById(R.id.textview_shoot);

        previewButton.setOnClickListener(this);
        shootButton.setOnClickListener(this);

        shootRequestTimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) ceil(millisUntilFinished / 1000.0);
                shootTextView.setText(String.valueOf(seconds));
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "shoot button");
                sendShootRequest(socket);
                Toast.makeText(ShootingActivity.this, "撮影しました！", Toast.LENGTH_SHORT).show();
                shootTextView.setText(String.valueOf("撮影する"));
            }
        };
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_preview) {
            if (socket != null && socket.isConnected()) {
                Toast.makeText(ShootingActivity.this, "プレビューを取得中です...", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "preview button");
                sendPreviewRequest(socket);
                Log.d(TAG, "start receiving photo...");
                new ReceivePhotoTask().execute(socket);
            } else {
                Toast.makeText(ShootingActivity.this, "Bluetoothに接続されていません", Toast.LENGTH_SHORT).show();
            }
        } else if (view.getId() == R.id.button_shoot) {
            if (socket == null || !socket.isConnected()) {
                Toast.makeText(ShootingActivity.this, "Bluetoothに接続されていません", Toast.LENGTH_SHORT).show();
            } else {
                shootRequestTimer.start();
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
            Log.d(TAG, "socket received");
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
                if(previewImageView.getParent() != null) {
                    ((ViewGroup)previewImageView.getParent()).removeView(previewImageView);
                }
                Log.d(TAG, "adding view...");
                previewImageView.setImageBitmap(result);
                Log.d(TAG, "set ImageView");
                previewFrameLayout.addView(previewImageView);
                Log.d(TAG, "add view!");
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
