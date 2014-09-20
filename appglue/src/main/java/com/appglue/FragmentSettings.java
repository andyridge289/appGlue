package com.appglue;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;


import java.util.Map;

import static com.appglue.library.AppGlueConstants.PREFS;

public class FragmentSettings extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private Activity activity;

    public static FragmentSettings create() {
        return new FragmentSettings();
    }

    public FragmentSettings() {

    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Map<String, ?> prefs = sharedPreferences.getAll();

        // This needs to show a clear database button

        // Sorts out the showing notifications preference.
//        boolean notifications = prefs.getBoolean(getResources().getString(R.string.prefs_notifications), false);
//        CheckBoxPreference n = (CheckBoxPreference) findPreference(getResources().getString(R.string.prefs_notifications));
//        n.setChecked(notifications);
//
//        n.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                Boolean checked = Boolean.valueOf(newValue.toString());
//
//                SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
//                SharedPreferences.Editor e = prefs.edit();
//                e.putBoolean(getResources().getString(R.string.prefs_notifications), checked);
//                e.apply();
//
//                return false;
//            }
//        });

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        Preference p = findPreference(key);
//        if(p instanceof CheckBoxPreference)
//        {
//            ((CheckBoxPreference) p).setChecked(sharedPreferences.getBoolean(key, false));
//        }
    }
}