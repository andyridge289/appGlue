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

import static com.appglue.Constants.DATA;
import static com.appglue.Constants.DURATION;
import static com.appglue.Constants.ID;
import static com.appglue.Constants.INDEX;
import static com.appglue.Constants.IS_LIST;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.COMPOSITE_ID;
import static com.appglue.library.AppGlueConstants.EXECUTION_NUM;
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
        intent.putExtra(EXECUTION_NUM, s.getExecutionNum());
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // At this point the calendar should contain all the information about the time it needs to be scheduled for
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.RTC_WAKEUP, s.getNextExecute(), alarmIntent);
            s.setScheduled(true);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, s.getNextExecute(), alarmIntent);
            s.setScheduled(true);
        }

        registry.update(s);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Scheduler sch = Scheduler.getInstance(context);

        if (true) return;

        // Check if we should execute this
        Schedule s = registry.getSchedule(intent.getLongExtra(ID, -1));
        if (s != null && s.isEnabled()) {

            CompositeService composite = s.getComposite();

            if (composite != null && s.getExecutionNum() == intent.getIntExtra(EXECUTION_NUM, -1)) {

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
                s.nextExecutionNum();
                sch.schedule(s);

            } else if (s.getExecutionNum() == intent.getIntExtra(EXECUTION_NUM, -1)) {

                // TODO What the hell do we do now?
            }
        }
    }
}
