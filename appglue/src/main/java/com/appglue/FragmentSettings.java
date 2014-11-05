package com.appglue;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class FragmentSettings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static FragmentSettings create() {
        return new FragmentSettings();
    }

    public FragmentSettings() {

    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference p = findPreference(key);

        if (p instanceof CheckBoxPreference) {
            ((CheckBoxPreference) p).setChecked(sharedPreferences.getBoolean(key, false));
        }
    }
}