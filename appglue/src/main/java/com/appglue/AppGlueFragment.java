package com.appglue;

import android.view.Menu;

public interface AppGlueFragment {

    public abstract boolean onBackPressed();
    public abstract String onCreateOptionsMenu(Menu menu);
}
