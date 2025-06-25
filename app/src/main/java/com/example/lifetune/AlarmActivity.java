package com.example.lifetune;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class AlarmActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        findViewById(R.id.stop_button).setOnClickListener(v -> {
            mediaPlayer.stop();
            finish();
        });
    }
}