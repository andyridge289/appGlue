package com.appglue.services.triggers;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import static com.appglue.Constants.TAG;

public class MobileConnectionTrigger extends GenericTrigger {

    public static final String STATE = "state";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle data = new Bundle();

        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

            String info = intent.getStringExtra(ConnectivityManager.EXTRA_EXTRA_INFO);
            boolean fail = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);
            boolean none = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

            ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo net = conn.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (net == null) {
                data.putBoolean(STATE, false);
            } else if (net.isConnected()) {
                data.putBoolean(STATE, true);
            } else {
                data.putBoolean(STATE, false);
            }

            super.trigger(context, this.getClass().getCanonicalName(), data, false, 0);
        }
    }
}
