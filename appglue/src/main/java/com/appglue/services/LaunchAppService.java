package com.appglue.services;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.appglue.ComposableService;

public class LaunchAppService extends ComposableService 
{
	public static final String APP_PACKAGE = "app_package";
	
	@Override
	public ArrayList<Bundle> performService(Bundle o)
	{
		if(LOG) Log.d(TAG, "Launching app");
		 
		String packageName = o.getString(APP_PACKAGE);
		
		try 
		{
		    Intent i = getPackageManager().getLaunchIntentForPackage(packageName);
		    startActivity(i);
		}
		catch (Exception e) 
		{
			Log.e(TAG, "Tried and failed to launch app to launch app? " + e.getMessage());
		}
		
		return null;
	}

	@Override
	public ArrayList<Bundle> performList(ArrayList<Bundle> os)
	{
		// You're only getting the first one...
		performService(os.get(0));
		return null;
	}

}
