package com.appglue;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import static com.appglue.library.AppGlueConstants.HAS_INPUTS;
import static com.appglue.library.AppGlueConstants.HAS_OUTPUTS;
import static com.appglue.library.AppGlueConstants.JUST_A_LIST;

public class FragmentComponentListPager extends Fragment {

    private PagerAdapter adapter;
    private ViewPager viewPager;

    private boolean justList = false;

    ArrayList<FragmentComponentList> fragments;

    public static Fragment create(boolean justList) {
        FragmentComponentListPager f = new FragmentComponentListPager();
        Bundle args = new Bundle();
        args.putBoolean(JUST_A_LIST, justList);
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
        View root = inflater.inflate(R.layout.activity_component_list, container, false);

        fragments = new ArrayList<FragmentComponentList>();

        FragmentComponentListSearch searchFragment = new FragmentComponentListSearch();
        searchFragment.setName("SEARCH");
        fragments.add(searchFragment);

        Bundle noInputArgs = new Bundle();
        noInputArgs.putBoolean(HAS_INPUTS, false);
        noInputArgs.putBoolean(HAS_OUTPUTS, true);
        FragmentComponentListLocal noInput = new FragmentComponentListLocal();
        noInput.setArguments(noInputArgs);
        noInput.setName("OUTPUT ONLY");
        fragments.add(noInput);

        Bundle noOutputArgs = new Bundle();
        noOutputArgs.putBoolean(HAS_INPUTS, true);
        noOutputArgs.putBoolean(HAS_OUTPUTS, false);
        FragmentComponentListLocal noOutput = new FragmentComponentListLocal();
        noOutput.setArguments(noOutputArgs);
        noOutput.setName("INPUT ONLY");
        fragments.add(noOutput);

//        if (showMatching) {
//            Bundle matchingArgs = new Bundle();
//            matchingArgs.putBoolean(MATCHING, true);
//            matchingArgs.putInt(POSITION, position);
//            FragmentComponentListLocal matching = new FragmentComponentListLocal();
//            matching.setArguments(matchingArgs);
//            matching.setName("MATCHING COMPONENTS");
//            fragments.add(matching);
//        }

        Bundle allArgs = new Bundle();
        allArgs.putBoolean(HAS_INPUTS, true);
        allArgs.putBoolean(HAS_OUTPUTS, true);
        FragmentComponentListLocal all = new FragmentComponentListLocal();
        all.setArguments(allArgs);
        all.setName("ALL COMPONENTS");
        fragments.add(all);

        adapter = new PagerAdapter(getChildFragmentManager());
        viewPager = (ViewPager) root.findViewById(R.id.pager);
        viewPager.setAdapter(adapter);

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

//    private boolean justAList;
//    private int position;
//
//    private ArrayList<FragmentComponentList> fragments;
//
//    public void onCreate(Bundle icicle) {
//        super.onCreate(icicle);
///
//        Intent intent = this.getIntent();
//
//        // This is the stuff for story mode
//        boolean triggersOnly = intent.getBooleanExtra(TRIGGERS_ONLY, false);
//        position = intent.getIntExtra(POSITION, -1);
//        Registry registry = Registry.getInstance(this);
//
//        boolean showMatching = false;
//
//        if (registry.getService() != null) {
//            SparseArray<ComponentService> components = registry.getService().getComponents();
//            showMatching = components.size() != 0;
//        }
//

//
//        if (triggersOnly) {
//            Bundle args = new Bundle();
//            args.putBoolean(TRIGGERS_ONLY, true);
//            FragmentComponentListLocal triggers = new FragmentComponentListLocal();
//            triggers.setArguments(args);
//            triggers.setName("TRIGGERS");
//            fragments.add(triggers);
//        } else {
//
//        viewPager.setCurrentItem(1);

    public void showServiceDescription(String className) {
        ((FragmentComponents) getParentFragment()).showServiceDescription(className);
    }

    public Fragment getCurrentFragment() {
        return adapter.getItem(viewPager.getCurrentItem());
    }

    public boolean isJustList() {
        return justList;
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (fragments.get(i) == null) {
                return fragments.get(0);
            } else return fragments.get(i);

        }

        @Override
        public int getCount() {
            if(fragments == null)
                return 0;

            return fragments.size();
        }

        public CharSequence getPageTitle(int position) {
            return fragments.get(position).getName();
        }

    }
}
