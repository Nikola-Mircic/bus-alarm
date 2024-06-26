package com.example.bus_alarm;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.bus_alarm.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private SearchView searchView;
    private Button selectBtn;

    private List<Address> addresses;
    private Address selected = null;

    private String CHANNEL_ID = "WakeMeUpWhenIt'sAllOver";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        createNotificationChannel();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        zoomToLastLocation();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                MarkerOptions marker = new MarkerOptions().position(latLng).title("Your destination");

                marker.rotation(20);

                mMap.addMarker(marker);

                Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());

                try {
                    selected = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                marker.remove();

                return false;
            }
        });

        final Activity activity = this;

        searchView = findViewById(R.id.addr_search);
        LinearLayout options = findViewById(R.id.addr_options);

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                options.removeAllViews();
            }
        });

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Geocoder coder = new Geocoder(activity, Locale.getDefault());

                try {
                    options.removeAllViews();
                    addresses = coder.getFromLocationName(s, 7);

                    ArrayList<String> results = new ArrayList<>();
                    for(Address address : addresses){
                        options.addView(makeTextView(address));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }

                return false;
            }
        });

        selectBtn = findViewById(R.id.select_btn);

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selected == null){
                    Toast.makeText(MapsActivity.this, "selected = null", Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(v.getContext(), SetAlarmActivity.class);

                intent.putExtra("address", selected.getAddressLine(0));
                intent.putExtra("lat", selected.getLatitude());
                intent.putExtra("lon", selected.getLongitude());

                startActivity(intent);
            }
        });
    }

    private TextView makeTextView(Address address){
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1);

        TextView tv = new TextView(this);

        tv.setText(address.getAddressLine(0));

        tv.setGravity(Gravity.CENTER);
        tv.setPadding(5, 5, 5, 5);
        tv.setLayoutParams(params);

        tv.setBackgroundColor(0xffffffff);
        tv.setTextColor(0xff000000);

        tv.setOnClickListener(view -> {
            selected = address;

            Toast.makeText(MapsActivity.this, address.getAddressLine(0), Toast.LENGTH_LONG).show();

            mMap.clear();

            LatLng latLng = new LatLng(selected.getLatitude(), selected.getLongitude());

            MarkerOptions marker = new MarkerOptions();
            marker.position(latLng);
            marker.rotation(20);
            mMap.addMarker(marker);

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
            mMap.animateCamera(cameraUpdate);

            searchView.setQuery("", false);
            searchView.clearFocus();
        });

        return tv;
    }

    @SuppressLint("MissingPermission")
    private void zoomToLastLocation(){
        String[] permissions = {};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.POST_NOTIFICATIONS};
        }else{
            permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        }

        if(!checkPermission(permissions))
            return;

        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

        client.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();

                CameraPosition cam = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongitude()))
                        .zoom(12.5f)
                        .build();
                CameraUpdate update = CameraUpdateFactory.newCameraPosition(cam);
                mMap.moveCamera(update);
            }
        });
    }

    private boolean checkPermission(String[] permissions){
        for(String permission : permissions) {
            if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(permissions, 100);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED)
                return;
        }

        zoomToLastLocation();

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Example Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}