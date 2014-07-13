package com.appglue;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import com.appglue.description.ServiceDescription;

import static com.appglue.Constants.INDEX;

public abstract class FragmentVW extends Fragment {
    // TODO See how much of the other fragment stuff can be moved into here
    private int position;

    private ServiceDescription pre;
    private ServiceDescription component;
    private ServiceDescription post;

    public static Fragment create(int position, boolean wiring) {
        FragmentVW fragment = wiring ? new FragmentWiring() : new FragmentValue();
        Bundle args = new Bundle();
        args.putInt(INDEX, position);
        fragment.setArguments(args);
        return fragment;
    }

    public abstract void redraw();

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

    }
}