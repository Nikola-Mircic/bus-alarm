package com.example.bus_alarm.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class LocationFinder {
    public interface OnLocationFoundListener{
        public void handleLocation(LatLng location);
    }

    private Context context;

    private LatLng userLocation;
    private LocationManager locationManager;
    private LocationListener locationCallback;

    public LocationFinder(Context context) {
        this.context = context;
    }

    public void findUserLocation(OnLocationFoundListener listener){
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationCallback = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            }
        };

        checkPermission(listener);
    }

    private void checkPermission(OnLocationFoundListener listener) {
        Dexter.withContext(context)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @SuppressLint("MissingPermission")
                    @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                            return;
                        }
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0,locationCallback);

                        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        userLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());

                        listener.handleLocation(userLocation);

                        /**/
                    }
                    @Override public void onPermissionDenied(PermissionDeniedResponse response) {

                    }
                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }

}