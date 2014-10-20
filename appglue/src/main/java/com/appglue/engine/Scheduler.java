package com.appglue.engine;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.appglue.serviceregistry.Registry;

public class Scheduler extends BroadcastReceiver {

    private static Scheduler instance;

    private Registry registry;
    private AlarmManager manager;
    private Context context;

    private Scheduler(Context context) {

        this.context = context;
        registry = Registry.getInstance(context);
        manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

    }

    public static Scheduler getInstance(Context context) {
        if (instance == null) {
            instance = new Scheduler(context);
        }

        return instance;
    }

    private void getNext() {

    }

    private void scheduleNext() {
//        Intent intent = new Intent(context, AlarmReceiver.class);
//        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
//
//        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                SystemClock.elapsedRealtime() +
//                        60 * 1000, alarmIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        // TODO Execute whatever we're meant to execute

        // TODO Find out from the registry what the next one is

        // TODO Schedule that one

        Scheduler sch = Scheduler.getInstance(context);
    }
}
