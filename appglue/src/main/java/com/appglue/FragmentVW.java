package com.appglue;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import static com.appglue.Constants.INDEX;

public abstract class FragmentVW extends Fragment {

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