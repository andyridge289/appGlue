package com.appglue.services.util;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.util.Log;

import com.appglue.ComposableService;

import java.util.ArrayList;

import static com.appglue.Constants.TAG;

public class BluetoothService extends ComposableService 
{
	public static final String BLUETOOTH_STATE = "bluetooth_state";
	
	@Override
	public ArrayList<Bundle> performService(Bundle input, ArrayList<Bundle> parameters) 
	{
		BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
		
		if(bt == null)
		{
			// TODO Component failure - Bluetooth
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
			// TODO Component success - Bluetooth
            Log.d(TAG, "Succeeded at Bluetooth-ing");
		}
		else
		{
			// TODO Component failure - Bluetooth
            Log.d(TAG, "Failed at Bluetooth-ing");
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