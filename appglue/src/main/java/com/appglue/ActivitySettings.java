package com.appglue;

import android.app.Activity;
import android.os.Bundle;

public class ActivitySettings extends Activity {
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        setContentView(R.layout.fragment_settings);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new FragmentSettings()).commit();
    }
}
