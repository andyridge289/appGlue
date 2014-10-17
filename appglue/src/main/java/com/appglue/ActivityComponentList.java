package com.appglue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.appglue.Constants.ServiceType;
import com.appglue.engine.description.ComponentService;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

import static com.appglue.Constants.CLASSNAME;
import static com.appglue.Constants.INDEX;
import static com.appglue.Constants.POSITION;
import static com.appglue.Constants.RESULT;
import static com.appglue.Constants.SERVICE_TYPE;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.EDIT_EXISTING;
import static com.appglue.library.AppGlueConstants.CREATE_NEW;
import static com.appglue.library.AppGlueConstants.HAS_INPUTS;
import static com.appglue.library.AppGlueConstants.HAS_OUTPUTS;
import static com.appglue.library.AppGlueConstants.JUST_A_LIST;
import static com.appglue.library.AppGlueConstants.MARKET_LOOKUP;
import static com.appglue.library.AppGlueConstants.MATCHING;
import static com.appglue.library.AppGlueConstants.NOT_SET;
import static com.appglue.library.AppGlueConstants.SUCCESS;
import static com.appglue.library.AppGlueConstants.TRIGGERS_ONLY;

public class ActivityComponentList extends ActionBarActivity {
    private PagerAdapter adapter;
    private ViewPager viewPager;

    private boolean justAList;
    private int position;

    private ArrayList<FragmentComponentList> fragments;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_component_list);

        Intent intent = this.getIntent();

        // This is the stuff for story mode
        boolean triggersOnly = intent.getBooleanExtra(TRIGGERS_ONLY, false);

        justAList = intent.getBooleanExtra(JUST_A_LIST, false);

        position = intent.getIntExtra(POSITION, -1);
        Registry registry = Registry.getInstance(this);

        boolean showMatching = false;

        if (registry.getCurrent() != null) {
            SparseArray<ComponentService> components = registry.getCurrent().getComponents();
            showMatching = components.size() != 0;
        }

        fragments = new ArrayList<FragmentComponentList>();

        if (triggersOnly) {
            Bundle args = new Bundle();
            args.putBoolean(TRIGGERS_ONLY, true);
            FragmentComponentListLocal triggers = new FragmentComponentListLocal();
            triggers.setArguments(args);
            triggers.setName("TRIGGERS");
            fragments.add(triggers);
        } else {
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

            if (showMatching) {
                Bundle matchingArgs = new Bundle();
                matchingArgs.putBoolean(MATCHING, true);
                matchingArgs.putInt(POSITION, position);
                FragmentComponentListLocal matching = new FragmentComponentListLocal();
                matching.setArguments(matchingArgs);
                matching.setName("MATCHING COMPONENTS");
                fragments.add(matching);
            }

            Bundle args = new Bundle();
            args.putBoolean(HAS_INPUTS, true);
            args.putBoolean(HAS_OUTPUTS, true);
            FragmentComponentListLocal all = new FragmentComponentListLocal();
            all.setArguments(args);
            all.setName("ALL COMPONENTS");
            fragments.add(all);
        }

        adapter = new PagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(adapter);

        viewPager.setCurrentItem(1);

        ActionBar actionBar = getSupportActionBar();
        boolean editExisting = intent.getBooleanExtra(EDIT_EXISTING, false);
        boolean createNew = intent.getBooleanExtra(CREATE_NEW, false);

        // TODO Work out what to put here
//        if (actionBar != null) {
//            if (!createNew)
//                actionBar.setTitle("Available components");
//            else
//                actionBar.setTitle("Choose first component");

//            actionBar.setHomeButtonEnabled(true);
//        }
    }

    public void onBackPressed() {
        // Put something in bundle that says they pressed back instead of choosing a component.
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public Fragment getCurrentFragment() {
        return adapter.getItem(viewPager.getCurrentItem());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean justAList() {
        return justAList;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (intent == null)
            return;

        int result = intent.getIntExtra(RESULT, NOT_SET);

        if (justAList) {
//			localFragment.update(registry.getAllDeviceServices());
//			remoteFragment.update(new ArrayList<ServiceDescription>());

            return;
        }


        switch (result) {
            case MARKET_LOOKUP: {
                // Make the lists do a refresh
//				localFragment.update(registry.getAllDeviceServices());
//				remoteFragment.update(new ArrayList<ServiceDescription>());
                break;
            }

            case SUCCESS:
                // Don't think we need to do anything here
                break;

            case NOT_SET:
                // They haven't selected anything, so don't do anything
                return;
        }

        String className = intent.getStringExtra(CLASSNAME);
        int serviceType = intent.getIntExtra(SERVICE_TYPE, -1);

        Intent i = new Intent();
        i.putExtra(CLASSNAME, className);
        i.putExtra(SERVICE_TYPE, serviceType);
        i.putExtra(POSITION, position);

        Log.d(TAG, "ACL.java: sending back " + position);

        if (getParent() == null) {
            setResult(Activity.RESULT_OK, i);
        } else {
            getParent().setResult(Activity.RESULT_OK, i);
        }

        Log.e(TAG, "Finishing component list");
        finish();
    }

    public void chosenItem(String className) {
        Intent i = new Intent();
        i.putExtra(CLASSNAME, className);
        i.putExtra(SERVICE_TYPE, ServiceType.DEVICE.index);
        i.putExtra(INDEX, position);

        Log.w(TAG, "Putting before I finish: " + className + " " + position);

        if (getParent() == null) {
            setResult(Activity.RESULT_OK, i);
        } else {
            getParent().setResult(Activity.RESULT_OK, i);
        }

        finish();
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
            return fragments.size();
        }

        public CharSequence getPageTitle(int position) {
            return fragments.get(position).getName();
        }

    }
}
