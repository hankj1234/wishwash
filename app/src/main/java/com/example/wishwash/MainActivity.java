package com.example.wishwash;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.ExperimentalGetImage;


@ExperimentalGetImage
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton bt_1 = findViewById(R.id.bt_1);
        bt_1.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Camera.class);
            startActivity(intent);
        });



        ImageButton bt_3 = findViewById(R.id.bt_3);
        bt_3.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), MapActivity.class);
            startActivity(intent);
        });

        ImageButton bt_4 = findViewById(R.id.bt_4);
        bt_4.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), BoardActivity.class);
            startActivity(intent);
        });


    }
}