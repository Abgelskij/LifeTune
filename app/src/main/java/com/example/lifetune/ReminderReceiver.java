package com.example.lifetune;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager manager = (NotificationManager)
                context.getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "sleep_channel")
                .setContentTitle("Пора спать!")
                .setContentText("Ваше время сна приближается")
                .setSmallIcon(R.drawable.ic_bedtime);

        manager.notify(1, builder.build());
    }
}