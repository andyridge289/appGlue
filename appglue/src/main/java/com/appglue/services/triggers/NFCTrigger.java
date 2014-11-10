package com.appglue.services.triggers;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;

public class NFCTrigger extends GenericTrigger {

    public static final String STATE = "state";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle data = new Bundle();

        // The second condition should never be untrue, but you never know
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION) && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

            if (action.equals(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)) {
                @SuppressLint("InlinedApi") final int state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, NfcAdapter.STATE_OFF);
                switch (state) {
                    case NfcAdapter.STATE_OFF:
                        data.putBoolean(STATE, false);
                        break;
                    case NfcAdapter.STATE_ON:
                        data.putBoolean(STATE, true);
                        break;
                }
            }

            super.trigger(context, this.getClass().getCanonicalName(), data, false, 0);
        }
    }
}
