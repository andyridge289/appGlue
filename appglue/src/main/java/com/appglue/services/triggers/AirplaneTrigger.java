package com.appglue.services.triggers;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

public class AirplaneTrigger extends GenericTrigger
{
	public static final String STATE = "state";

	@Override
    @SuppressWarnings("deprecation")
    public void onReceive(Context context, Intent intent)
	{
		Bundle data = new Bundle();

        if (!intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
            super.fail(context, "Not really an airplane mode change");
            return;
        }

        boolean mode;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // only for gingerbread and newer versions
            mode = Settings.System.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            mode = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        }

        data.putBoolean(STATE, mode);
        super.trigger(context, this.getClass().getCanonicalName(), data, false, 0);
	}
}
