package com.appglue.services.triggers;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

public class BatteryTrigger extends GenericTrigger
{
	public static final String STATE = "state";
    public static final int STATE_CHANGED = 0;
    public static final int STATE_OKAY = 1;
    public static final int STATE_LOW = 2;

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Bundle data = new Bundle();
        String command = intent.getAction().intern();

        if(command.equals(Intent.ACTION_BATTERY_CHANGED)) {
            data.putInt(STATE, STATE_CHANGED);
        } else if (command.equals(Intent.ACTION_BATTERY_OKAY)) {
            data.putInt(STATE, STATE_OKAY);
        } else if (command.equals(Intent.ACTION_BATTERY_LOW)) {
            data.putInt(STATE, STATE_LOW);
        } else {
            super.fail(context, "Not really a battery state change");
            return;
        }

        super.trigger(context, this.getClass().getCanonicalName(), data, false, 0);
	}
}
