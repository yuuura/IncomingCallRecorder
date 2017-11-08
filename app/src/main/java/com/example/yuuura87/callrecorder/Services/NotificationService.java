package com.example.yuuura87.callrecorder.Services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.yuuura87.callrecorder.Receivers.NotificationReceiver;

import java.util.Calendar;

public class NotificationService extends IntentService {

    public static final String TAG ="alarmService";
    public final static String START_ACTION_NOTIF = "com.example.yuuura87.callrecorder.service.action.START_ACTION_NOTIF";
    public final static String STOP_ACTION_NOTIF = "com.example.yuuura87.callrecorder.service.action.STOP_ACTION_NOTIF";
    public static final String UPDATE_ACTION_TIME = "notify about new time notification";

    public static final String TIME_KEY = "hours and minutes";

    public static final String CHILD_KEY = "child key";


    private int[] mRemindTimeSet = new int[2];

    private int mHours, mMinutes;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    public NotificationService() {
        super("NotificationService");
        Log.d(TAG , "CREATING SERVICE");
    }

    public class UpdateNotificationData extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent != null) {
                final String action = intent.getAction();
                if (action.equals(UPDATE_ACTION_TIME)) {
                    Bundle extras = intent.getExtras();
                    mRemindTimeSet = extras.getIntArray(NotificationService.TIME_KEY);
                    startNotif();
                }
            }
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {
            final String action = intent.getAction();
            if (action.equals(START_ACTION_NOTIF)) {
                Log.d(TAG , "START notifications");

                Bundle extras = intent.getExtras();
                mRemindTimeSet = extras.getIntArray(NotificationService.TIME_KEY);

               // notificationActivity = (NotificationActivity) intent.getSerializableExtra(TIME_KEY);
                mHours = mRemindTimeSet[0];
                mMinutes = mRemindTimeSet[1];
                startNotif();

            } else if (action.equals(STOP_ACTION_NOTIF)) {
                stopNotif();
                Log.d(TAG , "STOP notif");
            }
        }
    }

    public void stopNotif() {
        Intent notifyIntent = new Intent(getApplicationContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 100, notifyIntent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public void startNotif() {

        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, mHours);
        time.set(Calendar.MINUTE, mMinutes);
        time.set(Calendar.SECOND, 0);

        Intent notifyIntent = new Intent(getApplicationContext(), NotificationReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 100, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT); //PendingIntent.FLAG_UPDATE_CURRENT
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), 1000 * 20, pendingIntent);        // alarmManager.INTERVAL_DAY

    }

}