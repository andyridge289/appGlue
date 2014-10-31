package com.appglue.services.triggers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class StartupTrigger extends GenericTrigger {

    public static final String STATE = "state";

    public static final int STARTUP = 0;
    public static final int SHUTDOWN = 1;
    public static final int REBOOT = 2;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data = new Bundle();

        if (intent.getAction().intern().equals(Intent.ACTION_BOOT_COMPLETED)) {
            data.putInt(STATE, STARTUP);
        } else if (intent.getAction().intern().equals(Intent.ACTION_SHUTDOWN)) {
            data.putInt(STATE, SHUTDOWN);
        } else if (intent.getAction().intern().equals(Intent.ACTION_REBOOT)) {
            data.putInt(STATE, REBOOT);
        } else {
            super.fail(context, "Not really a startup, shutdown or reboot");
            return;
        }

        super.trigger(context, this.getClass().getCanonicalName(), data, false, 0);
    }
}
