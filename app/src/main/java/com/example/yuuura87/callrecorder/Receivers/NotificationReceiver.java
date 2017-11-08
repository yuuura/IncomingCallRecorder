package com.example.yuuura87.callrecorder.Receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.example.yuuura87.callrecorder.MainActivity;
import com.example.yuuura87.callrecorder.R;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent arg1) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,100,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        String notificationTitle = "Calls and messages";
        String notificationText = "You have calls or messages that waiting for your reply.";

        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle(notificationTitle)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_notification_black_24dp)
                .setAutoCancel(true)
                .setCategory(Notification.CATEGORY_REMINDER)
                .setContentText(notificationText)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setOngoing(false)
                .build();

        notificationManager.notify(100, notification);
    }
}
