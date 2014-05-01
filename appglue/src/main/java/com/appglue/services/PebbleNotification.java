package com.appglue.services;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import static com.appglue.Constants.TAG;
import static com.appglue.Constants.LOG;

import com.appglue.ComposableService;

public class PebbleNotification extends ComposableService 
{
	public static final String PEBBLE_TITLE = "pebble_title";
	public static final String PEBBLE_NOTIFICATION = "pebble_notification";
	
	@Override
	public ArrayList<Bundle> performService(Bundle o, ArrayList<Bundle> parameters) 
	{
		if(LOG) Log.d(TAG, "Performing Pebble!!!!");
		final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");
		 
		String title = o.getString(PEBBLE_TITLE);
		String message = o.getString(PEBBLE_NOTIFICATION);
		
		final HashMap<String, String> data = new HashMap<String, String>();
		data.put("title", title);
		data.put("body", message);
		
		final JSONObject jsonData = new JSONObject(data);
		final String notificationData = new JSONArray().put(jsonData).toString();
		i.putExtra("messageType", "PEBBLE_ALERT");
		i.putExtra("sender", "Composer");
		i.putExtra("notificationData", notificationData);
		Log.e(TAG, String.format("Broadcasting to pebble: %s :: %s", title, message));
		sendBroadcast(i);

		return null;
	}

	@Override
	public ArrayList<Bundle> performList(ArrayList<Bundle> os, ArrayList<Bundle> parameters) 
	{
        for (Bundle o : os) {
            performService(o, parameters);
        }
		
		return null;
	}

}
