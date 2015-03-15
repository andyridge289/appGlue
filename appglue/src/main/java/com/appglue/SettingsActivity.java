package com.appglue;

import android.os.Bundle;

import com.appglue.layout.FragmentSettings;

public class SettingsActivity extends AppGlueActivity {
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.fragment_settings);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new FragmentSettings()).commit();
    }
}
