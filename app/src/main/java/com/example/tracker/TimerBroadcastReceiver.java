package com.example.tracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class TimerBroadcastReceiver extends BroadcastReceiver {

    private final String NOTIFICATION_CHANNEL_ID = "channel_id";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("BROADCAST_RECEIVER", "onReceive");

        long[] pattern = {0, 300, 300, 300};
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(VibrationEffect.createWaveform(pattern, -1), new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Tracker")
                .setContentText("Timer abgelaufen.");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // This is required for the notification to show from the background
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notification channel", NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(channel);
        builder.setChannelId(NOTIFICATION_CHANNEL_ID);

        notificationManager.notify(0, builder.build());
    }
}
