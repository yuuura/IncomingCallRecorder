package com.example.yuuura87.callrecorder.Utils;

import android.util.Log;
import android.view.View;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Utils {

    public static void showViews(List<View> views) {
        for (View view : views) {
            view.setVisibility(View.VISIBLE);
        }
    }

    public static void hideViews(List<View> views) {
        for (View view : views) {
            view.setVisibility(View.GONE);
        }
    }

    public static String getDateTime(long time) {
        Date date = new Date(time);
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss z");
        return dateFormat.format(date);
    }
}