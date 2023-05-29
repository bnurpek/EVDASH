package com.example.myapplication.ui.screens;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private GoogleMap mMap;
    private Geocoder geocoder;
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onMapClick(LatLng latLng) {
        // Tıklanan konumu alın
        double latitude = latLng.latitude;
        double longitude = latLng.longitude;

        try {
            // LatLng'yi adreslere dönüştür
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);

                // Adres bilgilerini kullanıcıya göstermek veya seçtirmek için işlemleri gerçekleştirin
                String selectedAddress = address.getAddressLine(0);
                Toast.makeText(this, "Seçilen Adres: " + selectedAddress, Toast.LENGTH_SHORT).show();

                // İstediğiniz şekilde adresi kullanabilirsiniz
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

