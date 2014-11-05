package com.appglue.services.triggers;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;

public class RingerTrigger extends GenericTrigger {

    public static final String STATE = "state";

    // TODO Need to create a flag for uses storage one

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle data = new Bundle();

        if (action.equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
            int extra = intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1);
            if (extra == AudioManager.RINGER_MODE_NORMAL || extra == AudioManager.RINGER_MODE_SILENT ||
                                                            extra == AudioManager.RINGER_MODE_VIBRATE) {
                data.putInt(STATE, extra);
                super.trigger(context, this.getClass().getCanonicalName(), data, false, 0);
            }
        } else {
            super.fail(context, "Triggered when not in ringer mode");
        }
    }
}
