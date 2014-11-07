package com.appglue;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.appglue.description.ServiceDescription;
import com.appglue.engine.description.ComponentService;

import static com.appglue.Constants.CLASSNAME;
import static com.appglue.Constants.PACKAGENAME;
import static com.appglue.library.AppGlueConstants.JUST_A_LIST;

public class FragmentComponents extends Fragment implements AppGlueFragment {

    private int mode;
    public static final int MODE_LIST = 0;
    public static final int MODE_COMPONENT = 1;
    public static final int MODE_APP = 2;

    FragmentComponentListPager listFragment;
    private FragmentComponent componentFragment;
    private FragmentApp appFragment;


    private String className = "";
    private String packageName = "";

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

        if (getArguments() != null) {
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
        redraw();
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

    public boolean onBackPressed() {

        if (mode == MODE_COMPONENT) {
            setMode(MODE_LIST);
            redraw();
            return true;
        } else if(mode == MODE_APP) {
            setMode(MODE_COMPONENT);
            redraw();
            return true;
        } else {
            return listFragment.onBackPressed();
        }
    }

    @Override
    public String onCreateOptionsMenu(Menu menu) {
        String title = "Components";

        if (mode == FragmentComponents.MODE_COMPONENT) {
            title = componentFragment.onCreateOptionsMenu(menu);
        } else if (mode == FragmentComponents.MODE_LIST) {
            title = listFragment.onCreateOptionsMenu(menu);
        } else if (mode == FragmentComponents.MODE_APP) {
            title = appFragment.onCreateOptionsMenu(menu);
        }

        return title;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    void redraw() {
        Fragment active;

        switch (mode) {
            case MODE_COMPONENT:
                componentFragment = (FragmentComponent) FragmentComponent.create();
                active = componentFragment;
                if (!className.equals("")) {
                    Bundle args = new Bundle();
                    args.putString(CLASSNAME, className);
                    componentFragment.setArguments(args);
                    className = "";
                }
                break;

            case MODE_APP:
                appFragment = (FragmentApp) FragmentApp.create();
                active = appFragment;
                if (!packageName.equals("")) {
                    Bundle args = new Bundle();
                    args.putString(PACKAGENAME, packageName);
                    appFragment.setArguments(args);
                    packageName = "";
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
    }

    public int getMode() {
        return mode;
    }

    public void showApp(String packageName) {
        if (appFragment != null) {
            appFragment.setData(packageName);
            this.packageName = "";
        } else {
            this.packageName = packageName;
        }

        setMode(MODE_APP);
        redraw();
    }

    public void showServiceDescription(String className) {
        if (componentFragment != null) {
            componentFragment.setData(className);
            this.className = "";
        } else {
            this.className = className;
        }

        setMode(MODE_COMPONENT);
        redraw();
    }

    public String getName() {
        if (mode == FragmentComponents.MODE_COMPONENT) {
            // Ask the component fragment what the name of the component is
            return componentFragment.getName();
        } else {
            return ActivityAppGlue.Page.COMPONENTS.name;
        }
    }

    public ServiceDescription getComponent() {
        if (componentFragment != null) {
            return componentFragment.getComponent();
        }
        return null;
    }
}
