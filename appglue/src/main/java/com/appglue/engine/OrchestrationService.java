package com.appglue.engine;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.appglue.engine.description.CompositeService;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

import static com.appglue.Constants.COMPOSITE_ID;
import static com.appglue.Constants.DATA;
import static com.appglue.Constants.DURATION;
import static com.appglue.Constants.INDEX;
import static com.appglue.Constants.IS_LIST;
import static com.appglue.Constants.TAG;
import static com.appglue.Constants.TEST;

public class OrchestrationService extends Service
{
	private Registry registry;
//	private ArrayList<DEADExecutionPackage> queue;
	
	public void onCreate()
	{		
		registry = Registry.getInstance(this);
//		queue = new ArrayList<DEADExecutionPackage>();		
	}

    @Override
	public int onStartCommand(Intent intent, int startId, int something)
	{
		Log.w(TAG, Thread.currentThread().getName() + ": OrchestrationService.onStart() " + System.currentTimeMillis());
		
		if(intent == null)
			return -1;
		
		// We're receiving a list here now, need to do this NOW!!!!
		ArrayList<Bundle> intentData = intent.getParcelableArrayListExtra(DATA);
		
		if(intentData == null)
		{
			Log.e(TAG, "INTENT DATA IS NULL. WHAT THE HELL");
			return -1;
		}
		
		Bundle[] dataArray = new Bundle[intentData.size()];
		for(int i = 0; i < dataArray.length; i++)
		{
			dataArray[i] = intentData.get(i);
		}
				
		OrchestrateTask ot = new OrchestrateTask();
		ot.execute(dataArray);

        return 1;
	}

	@Override
	public IBinder onBind(Intent intent) 
	{
		// Don't need this, not binding this bastard
		return null;
	}
	
	private class OrchestrateTask extends AsyncTask<Bundle, Integer, Boolean>
	{

		@Override
		protected Boolean doInBackground(Bundle... params) 
		{
			Log.w(TAG, Thread.currentThread().getName() + ": Runner.run() " + System.currentTimeMillis());

            for (Bundle thing : params) {
                final long compositeId = thing.getLong(COMPOSITE_ID, -1);
                final int duration = thing.getInt(DURATION, 0);
                final int startIndex = thing.getInt(INDEX, -1);
                final boolean isList = thing.getBoolean(IS_LIST, false);
                final boolean test = thing.getBoolean(TEST, false);
                final Bundle data = thing.getBundle(DATA);

                final CompositeService cs = test ? registry.getService() : registry.getComposite(compositeId);

                if (cs == null) {
                    Log.e(TAG, "The composite is null. WHAT THE FUCK HAVE YOU DONE");
                    return false;
                }

                boolean enabled = registry.enabled(cs.getId());

                if (!test) {
                    if ((!enabled) && duration != 0) // Then it shouldn't be running
                    {
                        // So say it isn't
                        return false;
                    }
                }

                OrchestrationServiceConnection connection = new OrchestrationServiceConnection(OrchestrationService.this, cs, test);

                if (duration != 0) {
                    Log.e(TAG, "Duration duration");
                    // Z Need to make the executing work on a timer
//					if(!test)
//						{
//							if(duration != 0)
//							{
//								if(runningStatus.second) // Make sure they haven't paused it
//									h.postDelayed(this, duration * 1000);
//							}	
//						}
                    return false;
                }

                if (startIndex == -1 || startIndex == 0) {
                    Log.e(TAG, "Connection start");
                    connection.start();
                } else {
                    Log.e(TAG, "Starting at a position??? " + startIndex);
                    connection.startAtPosition(startIndex, isList, data);
                }
            }
			
			return true;
		}
	
		protected void onProgressUpdate(Integer... progress) 
		{
			// Work out what to put here
		}

		protected void onPostExecute(Boolean result) 
		{
			// Work out what to put here
		}
	}
}
