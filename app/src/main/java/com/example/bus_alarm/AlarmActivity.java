package com.example.bus_alarm;

import android.app.NotificationManager;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bus_alarm.location.TrackingService;

public class AlarmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alarm);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(165);

        Intent trackingIntent = new Intent(this, TrackingService.class);
        trackingIntent.addCategory(TrackingService.TAG);
        stopService(trackingIntent);

        Button btn = findViewById(R.id.ugasiAlarmBtn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent playerIntent = new Intent(AlarmActivity.this, PlayerService.class);
                playerIntent.addCategory(PlayerService.TAG);
                stopService(playerIntent);

                Intent mainActivityIntent = new Intent(AlarmActivity.this, MapsActivity.class);
                startActivity(mainActivityIntent);
            }
        });
    }
}