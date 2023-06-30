package com.example.shin_utsurundesu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends AppCompatActivity {
    private static final int DURATION = 1000;
    private final Handler handler = new Handler();
    private Runnable to;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplication(), LandingActivity.class);
                startActivity(intent);
                finish();
            }
        }, DURATION);
    }
}