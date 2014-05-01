package com.appglue;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import static com.appglue.library.AppGlueConstants.PREFS;

public class FragmentSettings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        // Sorts out the showing notifications preference.
        boolean notifications = prefs.getBoolean(getResources().getString(R.string.prefs_notifications), false);
        CheckBoxPreference n = (CheckBoxPreference) findPreference(getResources().getString(R.string.prefs_notifications));
        n.setChecked(notifications);

        n.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = Boolean.valueOf(newValue.toString());

                SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
                SharedPreferences.Editor e = prefs.edit();
                e.putBoolean(getResources().getString(R.string.prefs_notifications), checked);
                e.commit();

                return false;
            }
        });

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        Preference p = findPreference(key);
        if(p instanceof CheckBoxPreference)
        {
            ((CheckBoxPreference) p).setChecked(sharedPreferences.getBoolean(key, false));
        }

    }
}