package com.appglue;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static com.appglue.Constants.CLASSNAME;
import static com.appglue.library.AppGlueConstants.JUST_A_LIST;

import static com.appglue.Constants.TAG;

public class FragmentComponents extends Fragment {

    private int mode;
    public static final int MODE_LIST = 0;
    public static final int MODE_COMPONENT = 1;

    private FragmentComponentListPager listFragment;
    private FragmentComponent componentFragment;

    private String className = "";

    private boolean justList;

    public static Fragment create(boolean justList) {
        Bundle args = new Bundle();
        args.putBoolean(JUST_A_LIST, justList);
        FragmentComponents f = new FragmentComponents();
        f.setArguments(args);
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
            justList = getArguments().getBoolean(JUST_A_LIST);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        View root = inflater.inflate(R.layout.fragment_components, container, false);

        setMode(MODE_LIST);
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
        super.onResume();
    }

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

    public void setMode(int mode) {

        Fragment active;

        switch(mode) {
            case MODE_COMPONENT:
                componentFragment = (FragmentComponent) FragmentComponent.create();
                active = componentFragment;
                if(!className.equals("")) {
                    Bundle args = new Bundle();
                    args.putString(CLASSNAME, className);
                    componentFragment.setArguments(args);
                    className = "";
                }
                break;

            case MODE_LIST:
            default:
                listFragment = (FragmentComponentListPager) FragmentComponentListPager.create(justList);
                active = listFragment;
                break;
        }

        FragmentManager fm = getChildFragmentManager();
        fm.beginTransaction().replace(R.id.container, active)
                             .setCustomAnimations(R.anim.fade_out, R.anim.fade_in).commit();

        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public void showServiceDescription(String className) {
        if(componentFragment != null) {
            componentFragment.setData(className);
            this.className = "";
        } else {
            this.className = className;
        }

        setMode(MODE_COMPONENT);
    }

    public String getName() {
        if(mode == FragmentComponents.MODE_COMPONENT) {
            // Ask the component fragment what the name of the component is
            return componentFragment.getName();
        } else {
            return ActivityAppGlue.Page.COMPONENTS.name;
        }
    }
}
