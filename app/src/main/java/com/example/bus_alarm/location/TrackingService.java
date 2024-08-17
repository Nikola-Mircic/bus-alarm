package com.example.bus_alarm.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import com.example.bus_alarm.AlarmActivity;
import com.example.bus_alarm.PlayerService;
import com.example.bus_alarm.SetAlarmActivity;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;

import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.AlarmClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.bus_alarm.MapsActivity;
import com.example.bus_alarm.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.security.Permission;
import java.security.Permissions;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TrackingService extends Service {
    public static final String TAG = "TrackingService";

    private String CHANNEL_ID = "WakeMeUpWhenIt'sAllOver";

    private NotificationManager notificationManager;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("TrackingService", "Started service");
        Toast.makeText(this, "Service started!", Toast.LENGTH_LONG).show();

        int distance = intent.getIntExtra("minDistance", 5000);
        double lat = intent.getDoubleExtra("lat", 0);
        double lon = intent.getDoubleExtra("lon", 0);

        Location destination = new Location("Destination");
        destination.setLatitude(lat);
        destination.setLongitude(lon);

        createChannel();

        startForeground(165, getServiceNotification());

        getLocation(destination, distance);

        return START_NOT_STICKY;
    }

    @SuppressLint("MissingPermission")
    private void getLocation(Location destination, int distance){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            locationRequest = new LocationRequest.Builder(15000)
                    .setGranularity(Granularity.GRANULARITY_FINE)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setMinUpdateDistanceMeters(500)
                    .build();

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    super.onLocationResult(locationResult);

                    Location location = locationResult.getLocations().get(0);

                    if(location == null)
                        return;

                    double currentDistance = destination.distanceTo(location);

                    if(currentDistance < distance){
                        Log.d("TrackingService", "Current distance: " + currentDistance);

                        Intent playerIntent = new Intent(TrackingService.this, PlayerService.class);
                        playerIntent.addCategory(PlayerService.TAG);
                        startService(playerIntent);

                        Intent stopIntent = new Intent(TrackingService.this, AlarmActivity.class);

                        stopIntent.putExtra("lat", destination.getLatitude());
                        stopIntent.putExtra("lng", destination.getLongitude());

                        showNotification(TrackingService.this,
                                "Wake up!",
                                "You arrived!",
                                stopIntent,
                                165);

                        stopLocationUpdates();
                    }else{
                        /*float newDistanceUpdate = (float) currentDistance * 0.03f;

                        locationRequest = new LocationRequest.Builder(15000)
                                .setGranularity(Granularity.GRANULARITY_FINE)
                                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                                .setMinUpdateDistanceMeters(newDistanceUpdate)
                                .build();

                        requestLocationUpdates();*/

                        Log.d("TrackingService", "Current distance: " + currentDistance);
                    }
                }
            };

            requestLocationUpdates();
        }
    }

    private void requestLocationUpdates(){
        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);

        settingsClient.checkLocationSettings(locationSettingsRequest).addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                if(task.isSuccessful()){
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                }else{
                    task.getException().printStackTrace();
                }
            }
        });

        Log.d("TrackingService", "Updated location update request");
    }

    private void stopLocationUpdates(){
        Log.d("TrackingService", "stopLocationUpdates: Location updates stoped");
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private Notification getServiceNotification(){
        Intent intent = new Intent(this, AlarmActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 165, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_twotone_alarm_24)
                .setContentTitle("Bus alarm")
                .setContentText("Taking care of you!")
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationManager.IMPORTANCE_HIGH);

        return notificationBuilder.build();
    }

    public void createChannel(){
        notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, "Channel name", importance);
        mChannel.setVibrationPattern(new long[]{1,0,1,0,1,1,1,0,0,1,1});

        notificationManager.createNotificationChannel(mChannel);
    }

    public void showNotification(Context context, String title, String message, Intent intent, int reqCode) {

        PendingIntent pendingIntent = PendingIntent.getActivity(context, reqCode, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_twotone_alarm_24)
                .setContentTitle(title)
                .setContentText(message)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .addAction(R.drawable.alarm_off, "Ugasi alarm", pendingIntent)
                .setPriority(NotificationManager.IMPORTANCE_HIGH);

        notificationManager.notify(reqCode, notificationBuilder.build());

        Log.d("showNotification", "showNotification: " + reqCode);
    }

    @Override
    public void onDestroy() {
        Log.d("TrackingService", "onDestroy: TrackingService stopped!");
        stopLocationUpdates();
        super.onDestroy();
    }
}
