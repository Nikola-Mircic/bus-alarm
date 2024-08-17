package com.example.bus_alarm;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.bus_alarm.location.TrackingService;

public class PlayerService extends Service {
    public static final String TAG = "PlayerService";
    private Uri uri;
    private Ringtone ringtone;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("PlayerService", "onStartCommand: Playing....");

        uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        if(uri == null){
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

            if(uri == null) {
                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
        }

        ringtone = RingtoneManager.getRingtone(this, uri);

        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build();

        ringtone.setAudioAttributes(attributes);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone.setLooping(true);
        }

        ringtone.play();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if(ringtone.isPlaying())
            ringtone.stop();

        super.onDestroy();
    }
}
