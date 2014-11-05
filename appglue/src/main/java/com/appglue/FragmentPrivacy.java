package com.appglue;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentPrivacy extends Fragment {
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

        return inflater.inflate(R.layout.fragment_privacy, container, false);
    }
}