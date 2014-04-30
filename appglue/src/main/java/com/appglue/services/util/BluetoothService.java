package com.appglue.services.util;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.util.Log;

import com.appglue.ComposableService;

import java.util.ArrayList;

import static com.appglue.Constants.TAG;
import static com.appglue.Constants.LOG;

public class BluetoothService extends ComposableService 
{
	public static final String BLUETOOTH_STATE = "bluetooth_state";
	
	@Override
	public ArrayList<Bundle> performService(Bundle input, ArrayList<Bundle> parameters) 
	{
		BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
		
		if(bt == null)
		{
			// Component failure - Bluetooth
            super.fail("Bluetooth failure, couldn't get default adapter");
			return null;
		}
		
		boolean newState = input.getBoolean(BLUETOOTH_STATE);
		boolean worked;
		
		if(newState)
			worked = bt.enable();
		else
			worked = bt.disable();


        if(worked)
		{
			// Component success - Bluetooth
            if(LOG) Log.d(TAG, "Succeeded at Bluetooth-ing");
		}
		else
		{
            super.fail("Bluetooth failure, unable to change state of adapter");
            return null;
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