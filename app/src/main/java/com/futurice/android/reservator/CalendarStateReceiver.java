package com.futurice.android.reservator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CalendarStateReceiver extends BroadcastReceiver {

    public static String CALENDAR_CHANGED = "CALENDAR_CHANGED";

    public CalendarStateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent(CALENDAR_CHANGED));
    }
}