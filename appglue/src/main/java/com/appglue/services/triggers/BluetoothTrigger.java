package com.appglue.services.triggers;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import static com.appglue.Constants.TAG;

public class BluetoothTrigger extends GenericTrigger
{
	public static final String STATE = "state";

	@Override
	public void onReceive(Context context, Intent intent) 
	{
		
		
		String action = intent.getAction();
		Bundle data = new Bundle();
		
		BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
		if(bt == null) 	// TODO Component failure - Bluetooth
			return;
		
		// I think this is the only one we want really
	    if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
	    {
	    	int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
	    	
//	    	state == BluetoothAdapter.STATE_ON || 
	    	
	    	if(state == BluetoothAdapter.STATE_OFF)
	    	{
	    		data.putInt(STATE, state);
	    	}
	    	else
	    	{
	    		return;
	    		// TODO Component failure - Bluetooth
	    	}
	    }
		
		// android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED
		// android.bluetooth.adapter.action.DISCOVERY_FINISHED
		// android.bluetooth.adapter.action.DISCOVERY_STARTED
		// android.bluetooth.adapter.action.SCAN_MODE_CHANGED
	    
	    Log.w(TAG, "Bluetooth off triggered " + System.currentTimeMillis());
		
		super.trigger(context, this.getClass().getCanonicalName(), data, false, 0);
	}

}
