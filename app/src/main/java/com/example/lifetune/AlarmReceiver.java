package com.example.lifetune;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !"wakeup".equals(intent.getStringExtra("alarm_type"))) {
            return; // Игнорируем другие интенты
        }
        // Создаем полноценный будильник
        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(alarmIntent);
    }
}