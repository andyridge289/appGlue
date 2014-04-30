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

		if(bt == null)
        {
            // Component failure - Bluetooth (This is a trigger, it might have to be handled differently to everything else)
            super.fail(context, "BluetoothTrigger fail, couldn't get Bluetooth adapter");
            return;
        }
		
		// I think this is the only one we want really
	    if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
	    {
	    	int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
	    	

	    	if(state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.STATE_ON)
            {
                data.putInt(STATE, state);
            }

            Log.w(TAG, "Bluetooth on/off triggered " + System.currentTimeMillis());
            super.trigger(context, this.getClass().getCanonicalName(), data, false, 0);

            // XXX Do we need to implement other bluetooth states?
            // android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED
            // android.bluetooth.adapter.action.DISCOVERY_FINISHED
            // android.bluetooth.adapter.action.DISCOVERY_STARTED
            // android.bluetooth.adapter.action.SCAN_MODE_CHANGED
	    }
	}
}
