package com.appglue.services.triggers;

import java.util.ArrayList;

import com.appglue.Library;
import com.appglue.Constants.ProcessType;
import com.appglue.engine.CompositeService;
import com.appglue.engine.OrchestrationService;
import com.appglue.serviceregistry.Registry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import static com.appglue.Constants.*;

public abstract class GenericTrigger extends BroadcastReceiver 
{
	public static ProcessType processType = ProcessType.TRIGGER;
	
	@Override
	public abstract void onReceive(Context context, Intent arg1);

	// I don't know whether it's more work to do this or to farm off the information to a worker thread. The DB Lookup could slow us down, but I might have to do that from the main thread anyway..
	public void trigger(Context context, String className, Bundle data, boolean isList, int duration)
	{
		Registry registry = Registry.getInstance(context);
		ArrayList<CompositeService> services = registry.atomicAtPosition(className, 0);
		
		if(services == null)
			return;
		
		if(services.size() == 0)
			return;
		
		Intent serviceIntent = new Intent(context, OrchestrationService.class);
		ArrayList<Bundle> intentData = new ArrayList<Bundle>();
		
		for(int i = 0; i < services.size(); i++)
		{
			CompositeService service = services.get(i);
			
			if(service == null)
				continue;
			
			if(!services.get(i).isShouldBeRunning())
				continue;
			
			Bundle b = new Bundle();
						
			b.putLong(COMPOSITE_ID, services.get(i).getId());
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
