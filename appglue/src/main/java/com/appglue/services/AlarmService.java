package com.appglue.services;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Bundle;

import com.appglue.ComposableService;
import com.appglue.description.IOType;

import java.util.ArrayList;
import java.util.Calendar;

public class AlarmService extends ComposableService
{
    public static final String HOUR = "hour";
    public static final String MINUTE = "minute";
    public static final String SECOND = "second";

    @Override
    public ArrayList<Bundle> performService(Bundle o) {

        IOType number = IOType.Factory.getType(IOType.Factory.NUMBER);

        int hour = (Integer) number.getFromBundle(o, HOUR, 8);
        int minute = (Integer) number.getFromBundle(o, MINUTE, 0);
        int second = (Integer) number.getFromBundle(o, SECOND, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);

        AlarmManager am = (AlarmManager) super.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), null);

        return null;
    }

    @Override
    public ArrayList<Bundle> performList(ArrayList<Bundle> os) {
        return null;
    }
}