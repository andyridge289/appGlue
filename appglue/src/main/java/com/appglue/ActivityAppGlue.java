package com.appglue;

import android.app.Activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;


public class ActivityAppGlue extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static final int PAGE_HOME = 0;
    public static final int PAGE_COMPONENTS = 1;
    public static final int PAGE_LOG = 2;
    public static final int PAGE_GPLUS = 3;
    public static final int PAGE_SETTINGS = 4;
    public static final int PAGE_PRIVACY = 5;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private int currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_app_glue);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment f = null;

        // FIXME The menus need to be re-drawn depending on what's showing

        switch(position) {
            case PAGE_HOME:
                f = FragmentCompositeList.create();
                break;

            case PAGE_COMPONENTS:
                f = FragmentComponentListPager.create(true);
                break;

            case PAGE_LOG:
                f = FragmentLog.create();
                break;

            case PAGE_SETTINGS:
                f = FragmentSettings.create();
                break;

            case PAGE_PRIVACY:
                f = FragmentPrivacy.create();
                break;

            case PAGE_GPLUS:
                googlePlusLogin();
                return;
        }

        currentPage = position;

        fragmentManager.beginTransaction()
            .replace(R.id.container, f)
            .commit();
    }

    @Override
    public void onBackPressed() {
        if (currentPage != PAGE_HOME) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, FragmentCompositeList.create())
                    .commit();
            // Show the home page
            return;
        }

        super.onBackPressed();
    }

    private void googlePlusLogin() {
        Toast.makeText(this, "Google+ log in", Toast.LENGTH_SHORT).show();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case PAGE_HOME:
                mTitle = getString(R.string.page_home);
                break;
            case PAGE_COMPONENTS:
                mTitle = getString(R.string.page_components);
                break;
            case PAGE_LOG:
                mTitle = getString(R.string.page_log);
                break;
            case PAGE_GPLUS:
                mTitle = getString(R.string.page_gplus);
                break;
            case PAGE_SETTINGS:
                mTitle = getString(R.string.page_settings);
                break;
            case PAGE_PRIVACY:
                mTitle = getString(R.string.page_privacy);
                break;
            default:
                mTitle = getString(R.string.application_name);
                break;
        }

        restoreActionBar();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        if (!mNavigationDrawerFragment.isDrawerOpen()) {
//            // Only show items in the action bar relevant to this screen
//            // if the drawer is not showing. Otherwise, let the drawer
//            // decide what to show in the action bar.
//            getMenuInflater().inflate(R.menu.activity_app_glue, menu);
//            restoreActionBar();
//            return true;
//        }
//        return super.onCreateOptionsMenu(menu);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_activity_app_glue, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((ActivityAppGlue) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
