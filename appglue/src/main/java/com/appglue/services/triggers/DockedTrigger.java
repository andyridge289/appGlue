package com.appglue.services.triggers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class DockedTrigger extends GenericTrigger {

    public static final String STATE = "state";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle data = new Bundle();

        if (!action.equals(Intent.ACTION_DOCK_EVENT)) {
            super.fail(context, "Not really a dock event trigger");
            return;
        }

        int dockState = intent.getIntExtra(Intent.EXTRA_DOCK_STATE, -1);
        if (dockState == -1) {
            super.fail(context, "Unrecognised dock state");
            return;
        }

        if (dockState == Intent.EXTRA_DOCK_STATE_DESK ||
                dockState == Intent.EXTRA_DOCK_STATE_HE_DESK ||
                dockState == Intent.EXTRA_DOCK_STATE_LE_DESK) {
            data.putInt(STATE, Intent.EXTRA_DOCK_STATE_DESK);
        } else {
            data.putInt(STATE, dockState);
        }

        super.trigger(context, this.getClass().getCanonicalName(), data, false, 0);
    }

}

