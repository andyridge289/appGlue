package com.appglue;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class ActivityAppGlue extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public enum Page {
        HOME(0, "My Glued Apps"),
        COMPONENTS(1, "Component List"),
        SCHEDULE(2, "Schedule"),
        LOG(3, "Log"),
        GPLUS(4, "Google+ Login"),
        SETTINGS(5, "Settings"),
        PRIVACY(6, "Privacy");

        public int index;
        public String name;

        Page(int index, String name)
        {
            this.index = index;
            this.name = name;
        }
    }

    private static final String PAGE = "page";
    private static final String COMPOSITE_PAGE_VIEW = "composite_page_view";
    private static final String COMPOSITE_MODE = "composite_mode";
    private static final String COMPONENT_MODE = "component_mode";


    private int view;
    public static final int VIEW_GRID = 0;
    public static final int VIEW_LIST = 1;

    private FragmentComposites homeFragment;
    private FragmentComponents componentFragment;

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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.view = prefs.getInt(COMPOSITE_PAGE_VIEW, VIEW_GRID);

        if(savedInstanceState == null) {

            // Generate default values for all of the things
            this.currentPage = Page.HOME.index;
            if (homeFragment != null) {
                homeFragment.setMode(FragmentComposites.MODE_LIST);
            }

            if (componentFragment != null) {
                componentFragment.setMode(FragmentComponents.MODE_LIST);
            }

        } else {
            // Restore the values of the things that we have

            this.currentPage = savedInstanceState.getInt(PAGE, Page.HOME.index);

            int compositeMode = savedInstanceState.getInt(COMPOSITE_MODE, -1);
            int componentMode = savedInstanceState.getInt(COMPONENT_MODE, -1);

            if(homeFragment != null) {
                homeFragment.setMode(compositeMode);
            }

            if(componentFragment != null) {
                componentFragment.setMode(componentMode);
            }
        }

    }

    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);

        out.putInt(PAGE, this.currentPage);
        out.putInt(COMPOSITE_PAGE_VIEW, this.view);
        out.putInt(COMPOSITE_MODE, homeFragment == null ? -1 : homeFragment.getMode());
        out.putInt(COMPONENT_MODE, componentFragment == null ? -1 : componentFragment.getMode());

        // This mode
        // This view
        // Components mode
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();

        Fragment f = null;

        if (position == Page.HOME.index) {
            homeFragment = (FragmentComposites) FragmentComposites.create();
            f = homeFragment;
        } else if (position == Page.COMPONENTS.index) {
            componentFragment = (FragmentComponents) FragmentComponents.create(true);
            f = componentFragment;
        } else if (position == Page.SCHEDULE.index) {
            f = FragmentSchedule.create();
        } else if (position == Page.LOG.index) {
            f = FragmentLog.create();
        } else if (position == Page.SETTINGS.index) {
            f = FragmentSettings.create();
        } else if (position == Page.PRIVACY.index) {
            f = FragmentPrivacy.create();
        } else if (position == Page.GPLUS.index) {
            googlePlusLogin();
        }

        currentPage = position;

        fragmentManager.beginTransaction().replace(R.id.container, f).commit();
    }

    @Override
    public void onBackPressed() {
        if (currentPage == Page.HOME.index) {
            if (homeFragment.getMode() == FragmentComposites.MODE_COMPOSITE) {
                homeFragment.setMode(FragmentComposites.MODE_LIST);
                homeFragment.redraw();
                return;
            }
        } else {
            if (currentPage == Page.COMPONENTS.index) {
                componentFragment.setMode(FragmentComponents.MODE_LIST);
                componentFragment.redraw();
                return;
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, FragmentCompositeList.create())
                    .commit();
        }

        super.onBackPressed();
    }

    private void googlePlusLogin() {
        Toast.makeText(this, "Google+ log in", Toast.LENGTH_SHORT).show();
    }

    public void onSectionAttached(Page page) {

        if (page == Page.HOME) {
            mTitle = homeFragment.getName();
        } else if (page == Page.COMPONENTS) {
            mTitle = componentFragment.getName();
        } else {
            mTitle = page.name;
        }
        restoreActionBar();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_app_glue, menu);

        if (currentPage == Page.HOME.index) {
            if (view == VIEW_LIST) {
                menu.setGroupVisible(R.id.group_grid, false);
                menu.setGroupVisible(R.id.group_list, true);
            } else {
                menu.setGroupVisible(R.id.group_grid, true);
                menu.setGroupVisible(R.id.group_list, false);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

        @Override
    public boolean onOptionsItemSelected(MenuItem item) {

            switch(item.getItemId()) {
                case R.id.change_view_grid:
                    setViewMode(VIEW_GRID);
                    break;

                case R.id.change_view_list:
                    setViewMode(VIEW_LIST);
                    break;
            }

        return super.onOptionsItemSelected(item);
    }

    public int getViewMode() {
        return view;
    }
    public void setViewMode(int view) {
        this.view = view;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putInt(COMPOSITE_PAGE_VIEW, view).commit();

        if (homeFragment != null) {
            homeFragment.setViewMode();
        }

        invalidateOptionsMenu();
    }

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
                    Page.values()[getArguments().getInt(ARG_SECTION_NUMBER)]);
        }
    }

}
