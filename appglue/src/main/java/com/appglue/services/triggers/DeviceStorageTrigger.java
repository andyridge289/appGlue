package com.appglue.services.triggers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class DeviceStorageTrigger extends GenericTrigger {

    public static final String STATE = "state";

    public static final int LOW_STORAGE = 0;
    public static final int OKAY_STORAGE = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle data = new Bundle();

        if (action.equals(Intent.ACTION_DEVICE_STORAGE_LOW)) {
            data.putInt(STATE, LOW_STORAGE);
        } else if (!action.equals(Intent.ACTION_DEVICE_STORAGE_OK)) {
            data.putInt(STATE, OKAY_STORAGE);
        } else {
            super.fail(context, "Not really a device storage trigger");
            return;
        }

        super.trigger(context, this.getClass().getCanonicalName(), data, false, 0);
    }

}

