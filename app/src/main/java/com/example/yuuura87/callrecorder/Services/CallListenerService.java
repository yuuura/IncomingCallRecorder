package com.example.yuuura87.callrecorder.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.yuuura87.callrecorder.Constants.AppConstants;
import com.example.yuuura87.callrecorder.DataBase.DatabaseAdapter;
import com.example.yuuura87.callrecorder.DataBase.ParentRecordingItem;
import com.example.yuuura87.callrecorder.MainActivity;
import com.example.yuuura87.callrecorder.R;
import com.example.yuuura87.callrecorder.Receivers.CallReceiver;
import com.example.yuuura87.callrecorder.Receivers.SmsReceiver;
import com.example.yuuura87.callrecorder.Utils.Utils;

public class CallListenerService extends Service {

    public static final String TAG = "CallListenerService";

    public static final String STOP_SERVICE_ACTION = "notify callRecorder service to stop running";
    public static final String STOP_SERVICE_BROADCAST_KEY = "stop callRecorder service intent key";
    public static final int STOP_SERVICE_REQUEST = 1;

    private NotifyStopServiceReceiver mNotifyStopServiceReceiver;
    private CallReceiver mCallReceiver;
    private SmsReceiver mSmsReceiver;

    private long mParentRecordingId;

    public class NotifyStopServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int rqs = intent.getIntExtra(STOP_SERVICE_BROADCAST_KEY, 0);

            if (rqs == STOP_SERVICE_REQUEST){
                Log.d(TAG, "on receive stop command");
                stopSelf();
                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
            }
        }
    }

    @Override
    public void onCreate() {

        super.onCreate();
        Log.d(TAG, "service onCreate");

        // create new parent recording
        mParentRecordingId = createNewParentRecording();

        // update sharedPreferences that is recording
        updateSharedRecording(true, mParentRecordingId);

        // set stop service receiver
        mNotifyStopServiceReceiver = new NotifyStopServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(STOP_SERVICE_ACTION);
        registerReceiver(mNotifyStopServiceReceiver, intentFilter);

        // Send Notification that service is running to notification bar
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        final int NOTIFICATION_IDENTIFIER = 0;
        final int PENDING_INTENT_IDENTIFIER = (int) System.currentTimeMillis();

        Intent myIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, PENDING_INTENT_IDENTIFIER, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String notificationTitle = "Notification title";
        String notificationText = "Notification text";

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(notificationTitle)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_hearing_white_24dp)
                .setAutoCancel(false)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentText(notificationText)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setOngoing(true)
                .build();

        notificationManager.notify(NOTIFICATION_IDENTIFIER, notification);

        //register CallReceiver
        mCallReceiver = new CallReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(android.telephony.TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(mCallReceiver, filter);

        //register SmsReceiver
        mSmsReceiver = new SmsReceiver();
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(SmsReceiver.ACTION_SMS_RECEIVED);
        registerReceiver(mSmsReceiver, filter2);
    }

    private long createNewParentRecording() {
        long id = -1;
        DatabaseAdapter dbAdapter = new DatabaseAdapter(this);
        ParentRecordingItem parent = new ParentRecordingItem();
        parent.setStartTime(Utils.getDateTime(System.currentTimeMillis()));
        parent.setNumTrashedChildren(0);
        parent.setRecordName("");
        parent.setNumActiveChildren(0);
        parent.setIsClosed(false);
        parent.setIsTrashed(false);
        id = dbAdapter.insertParentToDatabase(parent);
        return id;
    }

    private void updateSharedRecording(boolean isRecording, long parentRecordingId) {
        SharedPreferences sharedPreferences = getSharedPreferences(AppConstants.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(AppConstants.SHARED_PREFS_IS_RECORDING_KEY, isRecording);
        editor.putLong(AppConstants.SHARED_PREFS_CURRENT_PARENT_RECORDING_KEY, parentRecordingId);
        editor.commit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "on start command");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        updateSharedRecording(false, mParentRecordingId);
        closeParentRecordingInDatabase(mParentRecordingId);
        unregisterReceiver(mNotifyStopServiceReceiver);
        unregisterReceiver(mCallReceiver);
        unregisterReceiver(mSmsReceiver);
    }

    private void closeParentRecordingInDatabase(long parentRecordingId) {
        Intent intent = new Intent(this, DatabaseService.class);
        intent.setAction(DatabaseService.ACTION_CLOSE_PARENT);
        intent.putExtra(DatabaseService.EXTRA_PARENT_ID, parentRecordingId);
        startService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}