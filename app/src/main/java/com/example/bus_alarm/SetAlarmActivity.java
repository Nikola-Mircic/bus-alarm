package com.example.bus_alarm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bus_alarm.location.TrackingService;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.security.Permission;

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

        Button startBtn = findViewById(R.id.startBtn);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Dexter.withContext(SetAlarmActivity.this)
                            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                            .withListener(new PermissionListener(){
                                @Override
                                public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                    Toast.makeText(SetAlarmActivity.this, "Pokrenut service", Toast.LENGTH_LONG).show();

                                    Intent service = new Intent(SetAlarmActivity.this, TrackingService.class);

                                    service.putExtra("minDistance", getMinDistance());
                                    service.putExtra("lat", lat);
                                    service.putExtra("lon", lon);

                                    SetAlarmActivity.this.startService(service);
                                }

                                @Override
                                public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {}

                                @Override
                                public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {}
                            }).check();
            }
        });
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

    private int getMinDistance(){
        CheckBox cb_5km = findViewById(R.id.cb_5km);
        CheckBox cb_10km = findViewById(R.id.cb_10km);
        CheckBox cb_15km = findViewById(R.id.cb_15km);

        if(cb_5km.isChecked()) return 5000;
        if(cb_10km.isChecked()) return 10000;
        if(cb_15km.isChecked()) return 15000;

        EditText customDist = findViewById(R.id.drugaRaz);

        return (int) ( 1000 * Double.parseDouble( customDist.getText().toString() ) );
    }
}