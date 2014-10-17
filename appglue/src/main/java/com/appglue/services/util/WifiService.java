package com.appglue.services.util;

import static com.appglue.Constants.TAG;
import static com.appglue.Constants.LOG;

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
	public ArrayList<Bundle> performService(Bundle input)
	{
		WifiManager manager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		
		boolean newState = input.getBoolean(WIFI_STATE);
		
		boolean worked = manager.setWifiEnabled(newState);
		
		if(worked)
		{
			// Component success - WIFI
            if(LOG) Log.d(TAG, "Did something to WiFi successfully.");
		}
		else
		{
			// Component failure - WIFI
            super.fail("WiFi failure, couldn't change state of adapter");
            return null;
		}
		
		return null;
	}

	@Override
	public ArrayList<Bundle> performList(ArrayList<Bundle> inputs)
	{
		if(inputs.size() > 0)
			return performService(inputs.get(0));
		else
			return null;
	}
}