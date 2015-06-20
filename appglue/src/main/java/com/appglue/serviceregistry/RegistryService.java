package com.appglue.serviceregistry;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;

import com.appglue.description.AppDescription;
import com.appglue.description.ServiceDescription;
import com.appglue.library.LocalStorage;
import com.appglue.services.triggers.HeadphoneTrigger;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import static com.appglue.Constants.ICON;
import static com.appglue.Constants.JSON_APP;
import static com.appglue.Constants.JSON_SERVICE;

/***
 * This is the background service that is started when the app starts which listens for ComposableServices
 * within apps and then adds them to our registry of services
 * 
 * @author andyridge
 */
public class RegistryService extends Service
{
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
        Receiver receiver = new Receiver();
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
			Logger.d("Received broadcast");
			String json = intent.getStringExtra(JSON_SERVICE);			
			String icon = intent.getStringExtra(ICON);
			
			ArrayList<ServiceDescription> services;
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
				catch (IOException e)
				{
					Logger.e("File not found!!!!");
					e.printStackTrace();
				}
				
				services = ServiceDescription.parseServices(json, RegistryService.this, app);
				
			}
			catch (JSONException e) 
			{
				e.printStackTrace();
				Logger.e("Broadcast failed - bad parsing: " + e.getLocalizedMessage());
				return;
			}

            for (ServiceDescription sd : services) {
                ServiceDescription retval = registry.addServiceFromBroadcast(sd);

                 if (retval == null) {
                    Toast.makeText(RegistryService.this, String.format("Failed to add device service: %s", sd.getName()), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegistryService.this, String.format("Added new device service: %s", sd.getName()), Toast.LENGTH_SHORT).show();
                }

//				ExternalConnection conn = ExternalConnection.getInstance();
//				conn.getExternalService(sd);
            }
		}
    }
}
