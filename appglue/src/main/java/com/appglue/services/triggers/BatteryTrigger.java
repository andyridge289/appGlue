package com.appglue.services.triggers;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

public class BatteryTrigger extends GenericTrigger
{
	public static final String STATE = "state";

	@Override
	public void onReceive(Context context, Intent intent)
	{
		Bundle data = new Bundle();

        if (!intent.getAction().intern().equals(Intent.ACTION_BATTERY_CHANGED) &&
            !intent.getAction().intern().equals(Intent.ACTION_BATTERY_OKAY) &&
            !intent.getAction().intern().equals(Intent.ACTION_BATTERY_LOW)) {
            super.fail(context, "Not really a battery state change");
            return;
        }

        data.putString(STATE, intent.getAction().intern());
        super.trigger(context, this.getClass().getCanonicalName(), data, false, 0);
	}
}
