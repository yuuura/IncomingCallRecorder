package com.example.yuuura87.callrecorder.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import com.example.yuuura87.callrecorder.Services.RecordFlowIntentService;

public class SmsReceiver extends BroadcastReceiver {

    private String TAG = SmsReceiver.class.getSimpleName();

    public static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    public SmsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String str = "";
        String form = "";
        long when = 0;

        if (intent.getAction().equals(ACTION_SMS_RECEIVED)) {

            SmsMessage[] allMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent);

            for (int i = 0; i < allMessages.length; i++) {
                SmsMessage sms = allMessages[i];
                when =  sms.getTimestampMillis();
                form = sms.getOriginatingAddress();
                str += sms.getMessageBody();
            }

            Log.d(TAG, "An SMS from " + form + " At " + Long.toString(when));
            Intent smsIntent = new Intent(context, RecordFlowIntentService.class);
            smsIntent.setAction(RecordFlowIntentService.ACTION_INCOMING_SMS);
            smsIntent.putExtra(RecordFlowIntentService.EXTRA_CALLER, form);
            smsIntent.putExtra(RecordFlowIntentService.EXTRA_INCOMING_TIME, when);
            smsIntent.putExtra(RecordFlowIntentService.EXTRA_MESSAGE, str);
            context.startService(smsIntent);
        }
    }
}