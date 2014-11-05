package com.appglue.services.triggers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import static com.appglue.Constants.LOG;

import static com.appglue.Constants.TAG;

public class HeadphoneTrigger extends GenericTrigger {
	public static final String STATE = "state";
	public static final String MICROPHONE = "microphone";

	@Override
	public void onReceive(Context context, Intent intent) {
		if(LOG) Log.d(TAG, "Headphones plugged/unplugged");
		
		String action = intent.getAction();
		
		if(action.equals(Intent.ACTION_HEADSET_PLUG)) {
            Bundle data = new Bundle();

            int intState = intent.getIntExtra(STATE, 1); // Default to being plugged in
            int intMic = intent.getIntExtra(MICROPHONE, 0); // Default to no microphone

            if(LOG) Log.d(TAG, String.format("Headphones! State %b, Mic %b", intState == 1, intMic == 1));

            data.putBoolean(STATE, intState == 1);
            data.putBoolean(MICROPHONE, intMic == 1);

            super.trigger(context, this.getClass().getCanonicalName(), data, false, 0);
        }
	}
}
