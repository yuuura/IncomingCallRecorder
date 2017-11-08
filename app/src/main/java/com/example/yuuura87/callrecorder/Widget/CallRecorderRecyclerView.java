package com.example.yuuura87.callrecorder.Widget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.yuuura87.callrecorder.Constants.AppConstants;
import com.example.yuuura87.callrecorder.Services.NotificationService;
import com.example.yuuura87.callrecorder.Utils.Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CallRecorderRecyclerView extends RecyclerView {

    public static final String TAG = "Recycler";

    private List<View> mNonEmptyStateViews = Collections.emptyList();
    private List<View> mEmptyStateViews = Collections.emptyList();
    private boolean mIsRecording = false;
    private boolean mIsNotificationsEnabled = true;
    private int[] mRemindTimeSet;

    private AdapterDataObserver mObserver = new AdapterDataObserver() {

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            Log.d(TAG, "onItemRangeRemoved");
            toggleViews();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            Log.d(TAG, "onItemRangeMoved");
            toggleViews();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            Log.d(TAG, "onItemRangeInserted");
            toggleViews();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            super.onItemRangeChanged(positionStart, itemCount, payload);
            Log.d(TAG, "onItemRangeChanged");
            toggleViews();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            Log.d(TAG, "onItemRangeChanged");
            toggleViews();
        }

        @Override
        public void onChanged() {
            Log.d(TAG, "onChanged");
            toggleViews();
        }
    };

    public CallRecorderRecyclerView(Context context) {
        super(context);
    }

    public CallRecorderRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CallRecorderRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void toggleViews() {

        Log.d(TAG, "toggling");

        if (getAdapter() != null && !mNonEmptyStateViews.isEmpty() && !mEmptyStateViews.isEmpty()) {

            if (getAdapter().getItemCount() == 0) {
                // if recycler view is in empty state

                // remove the non empty state views
                Utils.hideViews(mNonEmptyStateViews);

                // add the empty state views
                Utils.showViews(mEmptyStateViews);

                Intent intent = new Intent(getContext(), NotificationService.class);
                intent.setAction(NotificationService.STOP_ACTION_NOTIF);
                getContext().startService(intent);

            } else {
                // if recycler view is in not in empty state

                // add the non empty state views
                Utils.showViews(mNonEmptyStateViews);

                // remove the empty state views
                Utils.hideViews(mEmptyStateViews);

                /*if(mIsNotificationsEnabled && !mIsRecording) {
                    mRemindTimeSet = loadTimeFromSharedPref();
                    Intent intent1 = new Intent(getContext(), NotificationService.class);
                    intent1.setAction(NotificationService.START_ACTION_NOTIF);
                    intent1.putExtra(NotificationService.TIME_KEY, mRemindTimeSet);
                    getContext().startService(intent1);
                }*/
            }
        }
    }

    private int[] loadTimeFromSharedPref() {
        int time[] = new int[2];
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(AppConstants.SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
        time[0] = sharedPreferences.getInt(AppConstants.SHARED_PREFS_NOTIF_TIME_HOUR_KEY, -1);
        time[1] = sharedPreferences.getInt(AppConstants.SHARED_PREFS_NOTIF_TIME_MINUTE_KEY, -1);
        return time;
    }



    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(mObserver);
        }
        mObserver.onChanged();
    }

    public void hideIfEmpty(View...views) {
        mNonEmptyStateViews = Arrays.asList(views);
    }

    public void showIfEmpty(View...views) {
        mEmptyStateViews = Arrays.asList(views);
    }

    public void setIsRecording(boolean mIsRecording) {
        this.mIsRecording = mIsRecording;

    }

    public void setIsNotificationEnabled(boolean mIsNotificationsEnabled) {
        this.mIsNotificationsEnabled = mIsNotificationsEnabled;
    }
}

