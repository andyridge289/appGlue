package com.appglue;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static com.appglue.Constants.INDEX;

public class FragmentTutorial extends Fragment {
    private int index = -1;
    private Activity activity;

    public static FragmentTutorial create(int index) {
        FragmentTutorial ft = new FragmentTutorial();
        Bundle b = new Bundle();
        b.putInt(INDEX, index);
        ft.setArguments(b);
        return ft;
    }

    public FragmentTutorial() {

    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if (getArguments() != null) {
            this.index = getArguments().getInt(INDEX);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {

        View v = inflater.inflate(R.layout.fragment_tutorial, container, false);

        TextView tv = (TextView) v.findViewById(R.id.test);
        tv.setText("" + index);

        return v;
    }
}