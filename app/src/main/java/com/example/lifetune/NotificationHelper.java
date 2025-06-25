package com.example.lifetune;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    private static final String CHANNEL_ID = "goal_deletion_channel";
    private static final String CHANNEL_NAME = "Goal Deletion Notifications";
    private static final String CHANNEL_DESCRIPTION = "Notifications for goal deletion operations";
    
    private final Context context;
    private final NotificationManager notificationManager;
    
    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.setLightColor(Color.parseColor("#FF5252"));
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 100});
            
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    public void showGoalDeletedNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_delete_notification)
                .setContentTitle("Цель удалена")
                .setContentText("Цель была успешно удалена")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setColor(Color.parseColor("#111113"))
                .setColorized(true)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Ваша цель была успешно удалена из списка"))
                .setVibrate(new long[]{100, 200, 100});
        
        notificationManager.notify(1001, builder.build());
    }
    
    public void showAllGoalsDeletedNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_delete_notification)
                .setContentTitle("Все цели удалены")
                .setContentText("Все цели были успешно удалены")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setColor(Color.parseColor("#111113"))
                .setColorized(true)
                .setDefaults(NotificationCompat.DEFAULT_LIGHTS)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Все ваши цели были удалены из списка. Вы можете добавить новые цели в любое время"))
                .setVibrate(new long[]{100, 200, 100, 200, 100});
        
        notificationManager.notify(1002, builder.build());
    }
}
