package com.appglue.services.triggers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.appglue.Constants.ProcessType;
import com.appglue.engine.CompositeService;
import com.appglue.engine.OrchestrationService;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

import static com.appglue.Constants.COMPOSITE_ID;
import static com.appglue.Constants.DATA;
import static com.appglue.Constants.DURATION;
import static com.appglue.Constants.INDEX;
import static com.appglue.Constants.IS_LIST;
import static com.appglue.Constants.TAG;

public abstract class GenericTrigger extends BroadcastReceiver 
{
	public static ProcessType processType = ProcessType.TRIGGER;

    private boolean fail = false;
    private String error = "";

    protected void fail(Context context, String message)
    {
        fail = true;
        error = message;
        trigger(context, null, null, false, 0);
    }

	@Override
	public abstract void onReceive(Context context, Intent arg1);

	// I don't know whether it's more work to do this or to farm off the information to a worker thread. The DB Lookup could slow us down, but I might have to do that from the main thread anyway..
	public void trigger(Context context, String className, Bundle data, boolean isList, int duration)
	{
        Registry registry = Registry.getInstance(context);

        if(fail)
        {
            // There has been a failure in executing the trigger, so don't start. Just record it to the log
            // The compositeId is -1 because nothing actually started.
            registry.fail(-1L, this.getClass().getCanonicalName(), error);
            return;
        }

		ArrayList<CompositeService> services = registry.atomicAtPosition(className, 0);
		
		if(services == null)
			return;
		
		if(services.size() == 0)
			return;
		
		Intent serviceIntent = new Intent(context, OrchestrationService.class);
		ArrayList<Bundle> intentData = new ArrayList<Bundle>();

        for (CompositeService service : services) {
            if (service == null)
                continue;

            if (!service.isShouldBeRunning())
                continue;

            Bundle b = new Bundle();

            b.putLong(COMPOSITE_ID, service.getId());
            b.putInt(INDEX, 1);

            b.putBoolean(IS_LIST, isList);
            b.putInt(DURATION, duration);

            b.putBundle(DATA, data);

            intentData.add(b);
        }
		
//		Log.w(TAG, "Started service " + services.get(i).getName() + " " + System.currentTimeMillis());
		serviceIntent.putParcelableArrayListExtra(DATA, intentData);
		
		context.startService(serviceIntent);
		
		Log.w(TAG, "Done triggering " + this.getClass().getCanonicalName() + " " + System.currentTimeMillis());
	}
}
