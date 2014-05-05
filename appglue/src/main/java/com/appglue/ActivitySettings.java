package com.appglue;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ActivitySettings extends PreferenceActivity
{
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new FragmentSettings()).commit();
    }

//    @Override
//    public void onBuildHeaders(List<Header> target)
//    {
//        loadHeadersFromResource(R.xml.prefs_headers, target);
//    }

    protected boolean isValidFragment(String fragmentName)
    {
        return true;
    }




}
