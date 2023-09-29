package com.example.wishwash;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.CameraPosition; // 추가한 임포트
import retrofit2.Call; // 추가한 임포트
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1000;
    private static final String CLIENT_ID = "pppo35lowg";
    private static final String CLIENT_SECRET = "Fjc09cPrMhjKQlQLzmByyd9G46O1WTfL7g18IEZx";
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();

        Button searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchLocation();
            }
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            loadMapWithCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadMapWithCurrentLocation();
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadMapWithCurrentLocation() {
        com.naver.maps.map.MapView mapView = findViewById(R.id.map_view);
        mapView.getMapAsync(naverMap -> {
            naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                try {
                    fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                        if (location != null) {
                            Marker marker = new Marker();
                            marker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                            marker.setMap(naverMap);
                        }
                    });
                } catch (SecurityException e) {
                    Toast.makeText(this, "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void searchLocation() {
        EditText searchText = findViewById(R.id.search_edit_text);
        String query = searchText.getText().toString();

        if (!query.isEmpty()) {
            NaverSearchAPI api = new Retrofit.Builder()
                    .baseUrl("https://naveropenapi.apigw.ntruss.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(NaverSearchAPI.class);

            api.searchPlace(CLIENT_ID, CLIENT_SECRET, query).enqueue(new Callback<SearchResponse>() {
                @Override
                public void onResponse(@NonNull Call<SearchResponse> call, @NonNull Response<SearchResponse> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().getItems().isEmpty()) {
                        SearchResponse.SearchItem item = response.body().getItems().get(0);
                        LatLng location = new LatLng(item.getY(), item.getX());

                        com.naver.maps.map.MapView mapView = findViewById(R.id.map_view);
                        mapView.getMapAsync(naverMap -> {
                            Marker marker = new Marker();
                            marker.setPosition(location);
                            marker.setMap(naverMap);
                            naverMap.setCameraPosition(new CameraPosition(location, 15));
                        });
                    } else {
                        Toast.makeText(MapActivity.this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<SearchResponse> call, @NonNull Throwable t) {
                    Toast.makeText(MapActivity.this, "검색 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}