package com.appglue.services.triggers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ScreenStateTrigger extends GenericTrigger {

    public static final String STATE = "state";
    public static final int SCREEN_ON = 0;
    public static final int SCREEN_OFF = 1;

    // TODO Check that all of the services fail properly

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        Bundle data = new Bundle();

        if (action.equals(Intent.ACTION_SCREEN_ON)) {
            data.putInt(STATE, SCREEN_ON);
        } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
            data.putInt(STATE, SCREEN_OFF);
        } else {
            super.fail(context, "Unrecognised screen state");
            return;
        }

        super.trigger(context, this.getClass().getCanonicalName(), data, false, 0);
    }

}
