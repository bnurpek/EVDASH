package com.example.myapplication.ui.screens;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SelectAddress extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private GoogleMap map;
    private EditText address;
    private static final String TAG = SelectAddress.class.getSimpleName();
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private final LatLng defaultLocation = new LatLng(41.028424044715415, 28.890713922958838);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;
    private Location lastKnownLocation;
    private LatLng selected;
    private Boolean click;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_address);

        Places.initialize(getApplicationContext(), getString(R.string.maps_api_key));
        placesClient = Places.createClient(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        address = findViewById(R.id.et_address);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        click = false;
        selected = defaultLocation;
        address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String newAddress = s.toString();
                if(click){
                    Log.d("ADDRESS", selected.latitude+" "+selected.longitude);
                    map.clear();
                    MarkerOptions markerOptions = new MarkerOptions().position(selected);
                    map.addMarker(markerOptions);
                    map.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(selected, DEFAULT_ZOOM));
                    click = false;
                    return;
                }
                Geocoder geocoder = new Geocoder(SelectAddress.this);
                try {
                    List<Address> addresses = geocoder.getFromLocationName(newAddress, 1);
                    if(!addresses.isEmpty()){
                        Address location = addresses.get(0);
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        LatLng latLng = new LatLng(latitude, longitude);
                        selected = latLng;
                        Log.d("ADDRESS", latitude+" "+longitude);
                        map.clear();
                        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
                        map.addMarker(markerOptions);
                        map.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(latLng, DEFAULT_ZOOM));
                    }
                    else{
                        Log.e("ADDRESS", "Address not found");
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e("GEOCODING", e.toString());
                    return;
                }
            }
        });

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("address", address.getText().toString());
                intent.putExtra("latitude", selected.latitude);
                intent.putExtra("longitude", selected.longitude);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        this.map.setOnMapClickListener(this);

        updateLocationUI();
        if(isGpsEnabled()) {
            getDeviceLocation();
        }
        else{
            turnOnGps();
        }
    }
    private void updateLocationUI() {
        if (map == null) {
            Toast.makeText(getApplicationContext(), "MAP NULL", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if (locationPermissionGranted) {
                Log.d("PERMISSION", "GRANTED");
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                Log.d("PERMISSION", "NOT");
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("SECURITY", e.getMessage());
        }
    }
    private void turnOnGps() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setTitle("Konum Bilgisi");
        builder.setMessage("Bulunduğunuz adresi görebilmek için konumunuzu açın.");
        builder.create().show();
        getDeviceLocation();
    }

    private boolean isGpsEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            Log.d("PERMISSION", "GRANTED");
            updateLocationUI();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        Log.d("PERMISSION", "NOT");
        if (requestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("PERMISSION", "GRANTED");
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        updateLocationUI();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        click = true;
        selected = latLng;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        address.setText(latlngToAdr(latLng));
    }

    private String latlngToAdr(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                if(address.getMaxAddressLineIndex() >= 0){
                    return address.getAddressLine(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}

