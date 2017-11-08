package com.example.yuuura87.callrecorder;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.yuuura87.callrecorder.Constants.AppConstants;
import com.example.yuuura87.callrecorder.Services.CallListenerService;
import com.example.yuuura87.callrecorder.Services.NotificationService;


public class NotificationActivity extends AppCompatActivity {

    private Button btnSetTime;
    private final int DIALOG_ID = 0;
    public int mHour = 12, mMinute = 0;
    private Switch swEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        btnSetTime = (Button) findViewById(R.id.btnTime);
        swEnable = (Switch) findViewById(R.id.switchNotif);
        swEnable.setChecked(loadIsEnabledSwitchFromSharedPref());
        swEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSharedRecordingFilePermission(isChecked);
            }
        });
        setTimePickerDialog();
    }

    public void setTimePickerDialog() {
        btnSetTime.setOnClickListener(new View.OnClickListener(){
             @Override
             public void onClick(View view) {
                 showDialog(DIALOG_ID);
             }
         });
    }

    private boolean loadIsEnabledSwitchFromSharedPref() {
        SharedPreferences sharedPreferences = getSharedPreferences(AppConstants.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(AppConstants.SHARED_PREFS_NOTIF_IS_ENABLED, false);
    }

    private void updateSharedRecordingFilePermission(boolean isEabled) {
        SharedPreferences sharedPreferences = getSharedPreferences(AppConstants.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(AppConstants.SHARED_PREFS_NOTIF_IS_ENABLED, isEabled);
        editor.commit();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if(id == DIALOG_ID)
            return new TimePickerDialog(NotificationActivity.this, kTimePickerListener, mHour, mMinute, true);
        return null;
    }

    protected TimePickerDialog.OnTimeSetListener kTimePickerListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
            mHour = hourOfDay;
            mMinute = minute;

            SharedPreferences sharedPreferences = getSharedPreferences(AppConstants.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(AppConstants.SHARED_PREFS_NOTIF_TIME_HOUR_KEY, mHour);
            editor.putInt(AppConstants.SHARED_PREFS_NOTIF_TIME_MINUTE_KEY, mMinute);
            editor.commit();

            int time[] = {mHour, mMinute};
            Intent intent = new Intent();
            intent.setAction(NotificationService.UPDATE_ACTION_TIME);
            intent.putExtra(NotificationService.TIME_KEY, time);
            sendBroadcast(intent);
           // Toast.makeText(NotificationActivity.this, hour_x + ":" + minute_x, Toast.LENGTH_LONG).show();
        }
    };
}
