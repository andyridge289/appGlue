package com.appglue.services.triggers;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import static com.appglue.Constants.TAG;

public class WifiTrigger extends GenericTrigger {

    public static final String STATE = "state";
    public static final String NETWORK_SSID = "network_ssid";

    public static final int WIFI_OFF = 0;
    public static final int WIFI_ON = 1;
    public static final int WIFI_DISCONNECTED = 2;
    public static final int WIFI_CONNECTED = 3;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle data = new Bundle();

        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wm == null) {
            // Component failure - Bluetooth (This is a trigger, it might have to be handled differently to everything else)
            super.fail(context, "WiFiTrigger fail, couldn't get WiFi adapter");
            return;
        }

        if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);

            if (state == WifiManager.WIFI_STATE_DISABLED) {
                data.putInt(STATE, WIFI_OFF);
                data.putString(NETWORK_SSID, "");
            } else if (state == WifiManager.WIFI_STATE_ENABLED) {
                data.putInt(STATE, WIFI_ON);
                data.putString(NETWORK_SSID, "");
            }
        } else if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
            boolean connected = intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);
            if (connected) {
                data.putInt(STATE, WIFI_CONNECTED);
                WifiInfo wi = wm.getConnectionInfo();
                if (wi != null) {
                    String name = wi.getSSID();
                    data.putString(NETWORK_SSID, name);
                } else {
                    data.putString(NETWORK_SSID, "");
                }

            } else {
                data.putInt(STATE, WIFI_DISCONNECTED);
                data.putString(NETWORK_SSID, "");
            }
        } else {
            super.fail(context, "WiFiTrigger fail, wrong wifi state");
            return;
        }

        Log.w(TAG, "Wifi on/off triggered " + System.currentTimeMillis());
        super.trigger(context, this.getClass().getCanonicalName(), data, false, 0);
    }
}
