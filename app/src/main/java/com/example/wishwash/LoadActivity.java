package com.example.wishwash;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

@OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
public class LoadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);
        //로딩화면 시작.
        startLoading();
    }
    private void startLoading() {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }, 2000);
    }
}
