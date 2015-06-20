package com.appglue.services.util;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;

import com.appglue.ComposableService;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;

public class BluetoothService extends ComposableService 
{
	public static final String BLUETOOTH_STATE = "bluetooth_state";
	
	@Override
	public ArrayList<Bundle> performService(Bundle input)
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
            Logger.d("Succeeded at Bluetooth-ing");
		}
		else
		{
            super.fail("Bluetooth failure, unable to change state of adapter");
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