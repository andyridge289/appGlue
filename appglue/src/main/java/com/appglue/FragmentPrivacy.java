package com.appglue;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class FragmentPrivacy extends Fragment
{
    private Activity activity;

    public static FragmentPrivacy create() {
        return new FragmentPrivacy();
    }

    public FragmentPrivacy() {

    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }
}