package com.appglue;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

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

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {

        WebView web = (WebView) inflater.inflate(R.layout.fragment_privacy, container, false);
        web.getSettings().setAllowContentAccess(true);
        web.loadUrl("file:///android_res/xml/privacy.html");

        return web;
    }
}