package com.example.wishwash;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.overlay.Marker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1000;
    private static final String CLIENT_ID = "pppo35lowg";
    private static final String CLIENT_SECRET = "Fjc09cPrMhjKQlQLzmByyd9G46O1WTfL7g18IEZx";

    private FusedLocationProviderClient fusedLocationClient;
    private com.naver.maps.map.MapView mapView;

    private final Marker currentLocationMarker = new Marker();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapView = findViewById(R.id.map_view);

        checkLocationPermission();

        Button searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> searchNearbyLaundries());
    }

    private boolean isLocationServiceEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return isGPSEnabled || isNetworkEnabled;
    }

    private void checkLocationPermission() {
        if (!isLocationServiceEnabled()) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            Toast.makeText(this, "위치 서비스를 활성화해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
        } else {
            loadMapWithCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
        mapView.getMapAsync(naverMap -> {
            naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                try {
                    fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentLocationMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                            currentLocationMarker.setMap(naverMap);
                        } else {
                            Toast.makeText(this, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (SecurityException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void searchNearbyLaundries() {
        String query = "세탁방";

        NaverSearchAPI api = new Retrofit.Builder()
                .baseUrl("https://naveropenapi.apigw.ntruss.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NaverSearchAPI.class);

        api.searchPlace(CLIENT_ID, CLIENT_SECRET, query).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(@NonNull Call<SearchResponse> call, @NonNull Response<SearchResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getItems().isEmpty()) {
                    mapView.getMapAsync(naverMap -> {
                        for (SearchResponse.SearchItem item : response.body().getItems()) {
                            LatLng location = new LatLng(item.getX(), item.getY());
                            Marker marker = new Marker();
                            marker.setPosition(location);
                            marker.setMap(naverMap);
                        }
                    });
                } else {
                    Toast.makeText(MapActivity.this, "세탁방 검색 결과를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SearchResponse> call, @NonNull Throwable t) {
                Log.e("Search Error", t.getMessage());
                Toast.makeText(MapActivity.this, "세탁방 검색 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}