package com.appglue.services.triggers;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;

public class RingerTrigger extends GenericTrigger {

    // TODO Need to create a flag for uses storage one
    // TODO No schedule menu item if the composite contains a trigger
    // TODO Need to sort out the logic for composition if it contains a trigger
    // TODO Editing composite details

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle data = new Bundle();

        if (action.equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
            int extra = intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1);
        } else {
            // TODO Fail
        }

//        super.trigger(context, this.getClass().getCanonicalName(), data, false, 0);
    }
}
