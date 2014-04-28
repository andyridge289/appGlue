package com.appglue.serviceregistry;


import static com.appglue.Constants.ICON;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.JSON_APP;
import static com.appglue.Constants.JSON_SERVICE;
import static com.appglue.Constants.TAG;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.appglue.Constants.ProcessType;
import com.appglue.description.AppDescription;
import com.appglue.description.ServiceDescription;
import com.appglue.library.LocalStorage;
import com.appglue.services.triggers.HeadphoneTrigger;

/***
 * This is the background service that is started when the app starts which listens for ComposableServices
 * within apps and then adds them to our registry of services
 * 
 * @author andyridge
 */
public class RegistryService extends Service
{
	private Receiver receiver;
	private Registry registry = null;

	@Override
	public IBinder onBind(Intent arg0) 
	{
		return null;
	}
	
	public void onCreate()
	{
		super.onCreate();
		
		if(registry == null)
			registry = Registry.getInstance(this.getApplicationContext());
		
		IntentFilter filter = new IntentFilter("com.appglue.IM_A_COMPOSABLE_SERVICE");
        receiver = new Receiver();
        this.registerReceiver(receiver, filter);
        
        HeadphoneTrigger h = new HeadphoneTrigger();
        IntentFilter headphoneFilter = new IntentFilter();
        headphoneFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        this.registerReceiver(h, headphoneFilter);
	}
	
	/**
	 * This class receives broadcasts from other apps on the device that contain composite services
	 * 
	 * @author andyridge
	 *
	 */
	private class Receiver extends BroadcastReceiver
    {
		@Override
		public void onReceive(Context context, Intent intent) 
		{	
			if(LOG) Log.d(TAG, "Received broadcast");
			String json = intent.getStringExtra(JSON_SERVICE);			
			String icon = intent.getStringExtra(ICON);
			
			ArrayList<ServiceDescription> services = new ArrayList<ServiceDescription>();
			try 
			{
				
				JSONObject jsonApp = new JSONObject(json).getJSONObject(JSON_APP);
				AppDescription app = AppDescription.parseFromJSON(jsonApp);
				
				LocalStorage storage = LocalStorage.getInstance();
				try 
				{
					String filename = storage.writeIcon(app.getPackageName(), icon);
					app.setIconLocation(filename);
				}
				catch (FileNotFoundException e) 
				{
					Log.e(TAG, "File not found!!!!");
					e.printStackTrace();
				}
				catch (IOException e) 
				{
					Log.e(TAG, "File not found!!!!");
					e.printStackTrace();
				}
				
				services = ServiceDescription.parseServices(json, RegistryService.this, app);
				
			}
			catch (JSONException e) 
			{
				e.printStackTrace();
				Log.e(TAG, "Broadcast failed - bad parsing: " + e.getLocalizedMessage());
				return;
			}
			
			for(int i = 0; i < services.size(); i++)
			{
				ServiceDescription sd = services.get(i);
				
				long atomicId = registry.addServiceFromBroadcast(sd);
				
				// -1 = Fail
				// 0 = Already there, do nothing
				// Otherwise its just the ID
				if(atomicId == -1)
				{
					Toast.makeText(RegistryService.this, String.format("Failed to add device service: %s", sd.getName()), Toast.LENGTH_SHORT).show();
				}
				else if(atomicId != 0)
				{
					Toast.makeText(RegistryService.this, String.format("Added new device service: %s", sd.getName()), Toast.LENGTH_SHORT).show();
				}
				
				if(sd.getProcessType() == ProcessType.CONVERTER)
					continue;
				
//				ExternalConnection conn = ExternalConnection.getInstance();
//				conn.getExternalService(sd);
			}
		}
    }
}
