package com.appglue.engine;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.appglue.engine.model.CompositeService;
import com.appglue.serviceregistry.Registry;
import com.orhanobut.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.appglue.Constants.DATA;
import static com.appglue.Constants.DURATION;
import static com.appglue.Constants.ID;
import static com.appglue.Constants.INDEX;
import static com.appglue.Constants.IS_LIST;
import static com.appglue.library.AppGlueConstants.COMPOSITE_ID;
import static com.appglue.library.AppGlueConstants.EXECUTION_NUM;
import static com.appglue.library.AppGlueConstants.TEST;

public class Scheduler extends BroadcastReceiver {


    private Registry registry;
    private Context context;

    @SuppressWarnings("UnusedDeclaration")
    public Scheduler() {

    }

    public Scheduler(Context context) {
        this.context = context;
        registry = Registry.getInstance(context);
    }

    public void schedule(Schedule s) {
        Intent intent = new Intent(context, Scheduler.class);

        intent.setAction("com.appglue.schedule");
        intent.putExtra(COMPOSITE_ID, s.getComposite().getID());
        intent.putExtra(ID, s.getID());
        int eNum = s.getExecutionNum();
        intent.putExtra(EXECUTION_NUM, eNum);

        Logger.d("Putting: (" + intent.hashCode() + ")" + intent.getLongExtra(COMPOSITE_ID, -1) + ", " + intent.getLongExtra(ID, -1) + ", " +
                intent.getIntExtra(EXECUTION_NUM, -1));

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // At this point the calendar should contain all the information about the time it needs to be scheduled for
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.RTC_WAKEUP, s.getNextExecute(), alarmIntent);
            Calendar cal = new GregorianCalendar();
            cal.setTimeInMillis(s.getNextExecute());
            SimpleDateFormat sdf = new SimpleDateFormat("cccc d MMMM yyyy   HH:mm:ss");
            Logger.d("Scheduled " + s.getComposite().getName() + " for " + sdf.format(cal.getTime()));
            s.setScheduled(true);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, s.getNextExecute(), alarmIntent);
            Calendar cal = new GregorianCalendar();
            cal.setTimeInMillis(s.getNextExecute());
            SimpleDateFormat sdf = new SimpleDateFormat("cccc d MMMM yyyy   HH:mm:ss");
            Logger.d("Scheduled " + s.getComposite().getName() + " for " + sdf.format(cal.getTime()));
            s.setScheduled(true);
        }

        registry.update(s);
    }

    private void run(CompositeService composite) {
        Intent serviceIntent = new Intent(context, OrchestrationService.class);
        ArrayList<Bundle> intentData = new ArrayList<>();
        Bundle b = new Bundle();

        b.putLong(COMPOSITE_ID, composite.getID());
        b.putInt(INDEX, 0);
        b.putBoolean(IS_LIST, false);
        b.putInt(DURATION, 0);
        b.putBoolean(TEST, false);

        intentData.add(b);
        serviceIntent.putParcelableArrayListExtra(DATA, intentData);
        context.startService(serviceIntent);
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        Logger.d("Scheduler onReceive ");
        Logger.d("Getting: (" + intent.hashCode() + ")" + intent.getLongExtra(COMPOSITE_ID, -1) + ", " +
                intent.getLongExtra(ID, -1) + ", " + intent.getIntExtra(EXECUTION_NUM, -1));

        this.context = context;
        registry = Registry.getInstance(context);
        Schedule s = registry.getSchedule(intent.getLongExtra(ID, -1));

        // Check if we should execute this
        int eNum = intent.getIntExtra(EXECUTION_NUM, -1);
        if (s != null && s.isEnabled()) {

            CompositeService composite = s.getComposite();
            if (composite != null && composite.isEnabled() &&
                    s.getExecutionNum() == eNum) {

                // Execute whatever we're meant to execute
                run(composite);
                s.setLastExecuteTime(System.currentTimeMillis());

                // Update the next execute time of this one to show that we've executed it
                s.setExecutionNum(s.getExecutionNum() + 1);
                s.calculateNextExecute(System.currentTimeMillis());
                registry.update(s);

                Schedule t = registry.getSchedule(s.getID());
                Logger.d(String.format("old %d new %d", s.getExecutionNum(), t.getExecutionNum()));

                schedule(s);

            } else if (s.getExecutionNum() != eNum) {

                Logger.w("Wrong execution num: expected " + eNum + ", got " + s.getExecutionNum());
                // We shouldn't have to do anything because we should have already re-scheduled it
            }
        }
    }
}
