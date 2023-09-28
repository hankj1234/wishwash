package com.example.wishwash;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.naver.maps.map.MapFragment;

public class MapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.map_fragment, MapFragment.newInstance())
                    .commit();
        }
    }
}
