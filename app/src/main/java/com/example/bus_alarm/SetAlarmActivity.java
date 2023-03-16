package com.example.bus_alarm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class SetAlarmActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap map;

    private String address;
    private double lat, lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_alarm);

        Bundle data = getIntent().getExtras();

        if(data != null){
            TextView addressLine = findViewById(R.id.textView);
            addressLine.setText(data.getString("address"));

            lat = data.getDouble("lat");
            lon = data.getDouble("lon");
        }else{
            Toast.makeText(this, "Problem", Toast.LENGTH_SHORT).show();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;

        MarkerOptions marker = new MarkerOptions()
                                    .position(new LatLng(lat, lon))
                                    .draggable(false)
                                    .visible(true);
        marker.rotation(20);

        map.addMarker(marker);

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 15);
        map.animateCamera(update);
    }
}