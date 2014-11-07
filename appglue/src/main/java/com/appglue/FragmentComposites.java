package com.appglue;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.appglue.engine.description.CompositeService;

import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.COMPOSITE_ID;
import static com.appglue.library.AppGlueConstants.MODE;

public class FragmentComposites extends Fragment implements AppGlueFragment {

    private int mode = -1;
    public static final int MODE_LIST = 0;
    public static final int MODE_COMPOSITE = 1;

    private FragmentCompositeList listFragment;
    private FragmentComposite compositeFragment;

    private long compositeId = -1;

    public static Fragment create() {
        return new FragmentComposites();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if (getArguments() != null) {
            int mode = getArguments().getInt(MODE);
            Log.d(TAG, "Got mode (args): " + (mode == MODE_COMPOSITE ? "COMPOSITE" : "LIST"));
            setMode(mode);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        return inflater.inflate(R.layout.fragment_composites, container, false);
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

    @Override
    public boolean onBackPressed() {

        if (mode == FragmentComposites.MODE_COMPOSITE) {
            setMode(FragmentComposites.MODE_LIST);
            redraw();
            return true;
        }

        return false;
    }

    @Override
    public String onCreateOptionsMenu(Menu menu) {

        String title = "appGlue";

        if (mode == FragmentComposites.MODE_COMPOSITE) {
            title = compositeFragment.onCreateOptionsMenu(menu);
        } else if(mode == FragmentComposites.MODE_LIST) {
            title = listFragment.onCreateOptionsMenu(menu);
        }

        return title;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    void redraw() {

        Fragment active;
        int slideOut;
        int slideIn;

        if (listFragment == null) {
            listFragment = (FragmentCompositeList) FragmentCompositeList.create();
            compositeFragment = (FragmentComposite) FragmentComposite.create();
        }

        switch (mode) {

            case MODE_COMPOSITE:
                active = compositeFragment;
                slideOut = R.anim.slide_out_left;
                slideIn = R.anim.slide_in_right;
                if (compositeId != -1) {
                    Bundle args = new Bundle();
                    args.putLong(COMPOSITE_ID, compositeId);
                    compositeFragment.setArguments(args);
                    compositeId = -1;
                }
                break;

            case MODE_LIST:
            default:
                active = listFragment;
                listFragment.redraw();
                slideOut = R.anim.slide_out_right;
                slideIn = R.anim.slide_in_left;
                break;
        }

        FragmentManager fm = getChildFragmentManager();
        fm.beginTransaction().setCustomAnimations(slideIn, slideOut)
                .replace(R.id.container, active).commit();

        getActivity().invalidateOptionsMenu();
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
        if (compositeFragment != null) {
            compositeFragment.setData(id);
            compositeId = -1;
        } else {
            compositeId = id;
        }

        setMode(MODE_COMPOSITE);
        redraw();
    }

    public CompositeService getComposite() {
        if (compositeFragment != null) {
           return compositeFragment.getComposite();
        }
        return null;
    }

    public void setCompositeMode(boolean normalMode) {
        if (compositeFragment != null) {
            compositeFragment.setMode(normalMode);
        }
    }

    public boolean isEditingComposite() {
        if (compositeFragment != null) {
            return compositeFragment.isEditingComposite();
        }
        return false;
    }

    // TODO need to put the on back pressed stuff in here too I guess
}
