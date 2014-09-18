package com.appglue;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appglue.serviceregistry.Registry;

import static com.appglue.library.AppGlueConstants.COMPOSITE_ID;
import static com.appglue.library.AppGlueConstants.MODE;
import static com.appglue.library.AppGlueConstants.PLAY_SERVICES;

import static com.appglue.Constants.TAG;

public class FragmentComposites extends Fragment {

    private int mode = -1;
    public static final int MODE_LIST = 0;
    public static final int MODE_COMPOSITE = 1;

    private FragmentCompositeList listFragment;
    private FragmentComposite compositeFragment;

    private long compositeId = -1;

    public static Fragment create() {
        FragmentComposites f = new FragmentComposites();
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if(getArguments() != null) {
            int mode = getArguments().getInt(MODE);
            Log.d(TAG, "Got mode (args): " + (mode == MODE_COMPOSITE ? "COMPOSITE" : "LIST"));
            setMode(mode);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        View root = inflater.inflate(R.layout.fragment_composites, container, false);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);
    }
    @Override
    public void onStart() {
        super.onStart();
    }
    @Override
    public void onResume() {
        redraw();
        super.onResume();
    }
    @Override
    public void onViewStateRestored(Bundle in) {
        super.onViewStateRestored(in);
    }
    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
    }
    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onStop() {
        super.onStop();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    @Override
    public void onDetach() {
        super.onDetach();
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    private void redraw() {
        if(mode == MODE_LIST) {
            Log.d(TAG, "Set mode LIST");
        } else if(mode == MODE_COMPOSITE) {
            Log.d(TAG, "Set mode COMPOSITE");
        }

        Fragment active;
        int slideOut;
        int slideIn;

        if(listFragment == null) {
            listFragment = (FragmentCompositeList) FragmentCompositeList.create();
            compositeFragment = (FragmentComposite) FragmentComposite.create();
        }

        switch(mode) {

            case MODE_COMPOSITE:
                active = compositeFragment;
                slideOut = R.anim.slide_out_left;
                slideIn = R.anim.slide_in_right;
                if(compositeId != -1) {
                    Bundle args = new Bundle();
                    args.putLong(COMPOSITE_ID, compositeId);
                    compositeFragment.setArguments(args);
                    compositeId = -1;
                }
                break;

            case MODE_LIST:
            default:
                active = listFragment;
                slideOut = R.anim.slide_out_right;
                slideIn = R.anim.slide_in_left;
                break;
        }

        FragmentManager fm = getChildFragmentManager();
        fm.beginTransaction().setCustomAnimations(slideIn, slideOut)
                .replace(R.id.container, active).commit();


    }

    public void setViewMode() {
        if(listFragment != null) {
            listFragment.setViewMode();
        }
    }

    public String getName() {
        if (mode == FragmentComposites.MODE_COMPOSITE) {
            // Ask the fragment what the name of the composite is
            return compositeFragment.getName();
        } else {
            return ActivityAppGlue.Page.HOME.name;
        }
    }

    public void viewComposite(long id) {
        if(compositeFragment != null) {
            compositeFragment.setData(id);
            compositeId = -1;
        } else {
            compositeId = id;
        }

        setMode(MODE_COMPOSITE);
        redraw();
    }
}