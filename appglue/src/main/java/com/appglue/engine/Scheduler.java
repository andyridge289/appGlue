package com.appglue.engine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.appglue.engine.description.CompositeService;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.appglue.Constants.DATA;
import static com.appglue.Constants.DURATION;
import static com.appglue.Constants.ID;
import static com.appglue.Constants.INDEX;
import static com.appglue.Constants.IS_LIST;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.COMPOSITE_ID;
import static com.appglue.library.AppGlueConstants.TEST;

public class Scheduler extends BroadcastReceiver {

    private static Scheduler instance;

    private Registry registry;
    private Context context;

    private Scheduler(Context context) {
        this.context = context;
        registry = Registry.getInstance(context);
    }

    public static Scheduler getInstance(Context context) {
        if (instance == null) {
            instance = new Scheduler(context);
        }

        return instance;
    }

    public void schedule(Schedule s) {
        Intent intent = new Intent(context, Scheduler.class);
        intent.putExtra(COMPOSITE_ID, s.getComposite().getID());
        intent.putExtra(ID, s.getID());
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (s.getScheduleType() == Schedule.ScheduleType.INTERVAL) {

            // Work out what the difference is is
            int interval = s.getInterval().value;
            interval *= 1000; // Turn it into milliseconds
            interval *= s.getNumeral();

            s.setNextExecute(s.getLastExecuted() + interval);

        } else {
            Calendar cal = new GregorianCalendar();
            cal.setTimeInMillis(System.currentTimeMillis());

            // We need to find the next time that meets the required condition

            // If it's hour, move to the next appropriate minute
            int minute = cal.get(Calendar.MINUTE);
            if (minute >= s.getMinute()) {
                // Move forward an hour
                cal.add(Calendar.HOUR, 1);
            }

            // Then move to the right minute
            cal.set(Calendar.MINUTE, s.getMinute());

            if (s.getTimePeriod().index > Schedule.TimePeriod.HOUR.index) {

                // If it's day, Move to the next appropriate minute, then hour
                int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
                if (hourOfDay > s.getHour()) {
                    // Move forward a day
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }

                cal.set(Calendar.HOUR_OF_DAY, s.getHour());

                if (s.getTimePeriod().equals(Schedule.TimePeriod.WEEK)) {

                    // If it's week, Move to the next appropriate minute, then hour, then day of week
                    int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                    if (dayOfWeek > s.getDayOfWeek()) {
                        // Move forward a week
                        cal.add(Calendar.WEEK_OF_YEAR, 1);
                    }

                    cal.set(Calendar.DAY_OF_WEEK, s.getDayOfWeek());

                } else if (s.getTimePeriod().equals(Schedule.TimePeriod.MONTH)) {

                    // If it's month Move to the next appropraite minute, then hour, then day of month
                    int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
                    if (dayOfMonth > s.getDayOfMonth()) {
                        // Move forward a month
                        cal.add(Calendar.WEEK_OF_MONTH, 1);
                    }

                    cal.set(Calendar.DAY_OF_MONTH, s.getDayOfMonth());
                }

                s.setNextExecute(cal.getTimeInMillis());
            }
        }

        registry.update(s);

        // At this point the calendar should contain all the information about the time it needs to be scheduled for
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.RTC_WAKEUP, s.getNextExecute(), alarmIntent);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, s.getNextExecute(), alarmIntent);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Scheduler sch = Scheduler.getInstance(context);

        // Check if we should execute this
        Schedule s = registry.getSchedule(intent.getLongExtra(ID, -1));
        if (s != null && s.isEnabled()) {

            CompositeService composite = s.getComposite();

            if (composite != null) {
                long diff = System.currentTimeMillis() - s.getNextExecute();
                if (diff < 0) {
                    // It needs to be executed at some point in the future
                }

                // Execute whatever we're meant to execute
                Intent serviceIntent = new Intent(context, OrchestrationService.class);
                ArrayList<Bundle> intentData = new ArrayList<Bundle>();
                Bundle b = new Bundle();

                b.putLong(COMPOSITE_ID, composite.getID());
                b.putInt(INDEX, 0);
                b.putBoolean(IS_LIST, false);
                b.putInt(DURATION, 0);
                b.putBoolean(TEST, false);

                if (LOG)
                    Log.w(TAG, "Trying to run from scheduler " + composite.getID() + " : " + composite.getName());

                intentData.add(b);
                serviceIntent.putParcelableArrayListExtra(DATA, intentData);
                context.startService(serviceIntent);

                // Update the next execute time of this one to show that we've executed it
                sch.schedule(s);
            }
        }

        // Schedule that one
        sch.schedule(registry.getNextScheduledItem());
    }
}
