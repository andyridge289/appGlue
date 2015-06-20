package com.appglue.services;

import android.content.Intent;
import android.os.Bundle;

import com.appglue.ComposableService;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;

public class LaunchAppService extends ComposableService 
{
	public static final String APP_PACKAGE = "app_package";
	
	@Override
	public ArrayList<Bundle> performService(Bundle o)
	{
		Logger.d("Launching app");
		 
		String packageName = o.getString(APP_PACKAGE);
		
		try 
		{
		    Intent i = getPackageManager().getLaunchIntentForPackage(packageName);
		    startActivity(i);
		}
		catch (Exception e) 
		{
			Logger.e("Tried and failed to launch app to launch app? " + e.getMessage());
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
