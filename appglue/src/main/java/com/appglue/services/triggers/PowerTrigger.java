package com.appglue.services.triggers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.appglue.Constants.ProcessType;
import static com.appglue.Constants.TAG;

public class PowerTrigger extends GenericTrigger
{
	public static final String CONNECTED = "connected";
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{		
		String action = intent.getAction();

		Bundle data = new Bundle();
		
	    if(action.equals(Intent.ACTION_POWER_CONNECTED)) 
	    {
	        // Do something when power connected
	    	data.putBoolean(CONNECTED, true);
	    }
	    else if(action.equals(Intent.ACTION_POWER_DISCONNECTED)) 
	    {
	        // Do something when power disconnected
	    	data.putBoolean(CONNECTED, false);
	    }
	    else
	    {
	    	// Do nothing
	    }
		
		super.trigger(context, this.getClass().getCanonicalName(), data, false, 0);
	}
	
}

