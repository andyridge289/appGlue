package com.appglue;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.Map;

import static com.appglue.library.AppGlueConstants.PREFS;

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