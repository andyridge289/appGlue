package com.appglue;

import android.view.Menu;

public interface AppGlueFragment {

    boolean onBackPressed();
    String onCreateOptionsMenu(Menu menu);
}
