package com.appglue.services.util;

import static com.appglue.Constants.TAG;

import java.util.ArrayList;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import com.appglue.ComposableService;

public class WifiService extends ComposableService 
{
	public static final String WIFI_STATE = "wifi_state";
	
	@Override
	public ArrayList<Bundle> performService(Bundle input, ArrayList<Bundle> parameters) 
	{
		WifiManager manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		
		boolean newState = input.getBoolean(WIFI_STATE);
		
		boolean worked = manager.setWifiEnabled(newState);
		
		if(worked)
		{
			// TODO Component success - WIFI
            Log.d(TAG, "Did something to WiFi successfully.");
		}
		else
		{
			// TODO Component failure - WIFI
            Log.d(TAG, "Failed at doing something to WiFi");
		}
		
		return null;
	}

	@Override
	public ArrayList<Bundle> performList(ArrayList<Bundle> inputs, ArrayList<Bundle> parameters) 
	{
		if(inputs.size() > 0)
			return performService(inputs.get(0), parameters);
		else
			return null;
	}
}