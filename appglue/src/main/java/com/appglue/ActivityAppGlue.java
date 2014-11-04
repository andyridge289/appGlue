package com.appglue;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.appglue.engine.OrchestrationService;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.serviceregistry.Registry;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;

import static com.appglue.Constants.DATA;
import static com.appglue.Constants.DURATION;
import static com.appglue.Constants.INDEX;
import static com.appglue.Constants.IS_LIST;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.COMPOSITE_ID;
import static com.appglue.library.AppGlueConstants.EDIT_EXISTING;
import static com.appglue.library.AppGlueConstants.PREFS_HIDDEN;
import static com.appglue.library.AppGlueConstants.P_DISCLAIMER;
import static com.appglue.library.AppGlueConstants.TEST;

public class ActivityAppGlue extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private CompositeService scheduledComposite;

    public enum Page {
        HOME(0, "My Glued Apps"),
        COMPONENTS(1, "Component List"),
        SCHEDULE(2, "Schedule"),
        LOG(3, "Log"),
        ACCOUNTS(4, "Connect accounts"),
        PRIVACY(5, "Privacy");

        public int index;
        public String name;

        Page(int index, String name) {
            this.index = index;
            this.name = name;
        }
    }

    private Toolbar toolbar;

    // Result code for the google plus stuff
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private static final String PAGE = "page";
    private static final String COMPOSITE_PAGE_VIEW = "composite_page_view";
    private static final String COMPOSITE_MODE = "composite_mode";
    private static final String COMPONENT_MODE = "component_mode";

    private FragmentComposites homeFragment;
    private FragmentComponents componentFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private int currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_app_glue);

        Registry registry = Registry.getInstance(this);

        toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);

        /*
      Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
        NavigationDrawerFragment mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


        if (savedInstanceState == null) {

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

            if (homeFragment != null) {
                homeFragment.setMode(compositeMode);
            }

            if (componentFragment != null) {
                componentFragment.setMode(componentMode);
            }
        }

        SharedPreferences hiddenPrefs = getSharedPreferences(PREFS_HIDDEN, Context.MODE_PRIVATE);
        boolean disclaimer = hiddenPrefs.getBoolean(P_DISCLAIMER, false);
        if (!disclaimer) {

            Intent intent = new Intent(this, ActivityTutorial.class);
            startActivity(intent);

            hiddenPrefs.edit().putBoolean(P_DISCLAIMER, true).commit();
        }

    }

    public void onResume() {
        // From https://developer.android.com/training/location/retrieve-current.html
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS != resultCode) {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getSupportFragmentManager(),
                        "Location Updates");
            }
        }

        super.onResume();
    }

    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);

        out.putInt(PAGE, this.currentPage);
        out.putInt(COMPOSITE_MODE, homeFragment == null ? -1 : homeFragment.getMode());
        out.putInt(COMPONENT_MODE, componentFragment == null ? -1 : componentFragment.getMode());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            Intent intent = new Intent(ActivityAppGlue.this, ActivitySettings.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.tutorial) {
            Intent intent = new Intent(ActivityAppGlue.this, ActivityTutorial.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.composite_edit) {
            // Show or hide the right pages of the composite page
            homeFragment.setCompositeMode(false);
            invalidateOptionsMenu();
        } else if (item.getItemId() == R.id.composite_done) {
            // also need a done button to commit the changes that they make
            homeFragment.setCompositeMode(true);
            invalidateOptionsMenu();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment f = null;

        int background = R.color.colorPrimaryDark;

        if (position == Page.HOME.index) {
            homeFragment = (FragmentComposites) FragmentComposites.create();
            f = homeFragment;
        } else if (position == Page.COMPONENTS.index) {
            componentFragment = (FragmentComponents) FragmentComponents.create(true);
            background = R.color.material_indigo;
            f = componentFragment;
        } else if (position == Page.SCHEDULE.index) {
            background = R.color.schedule;
            if (scheduledComposite != null) {
                f = FragmentSchedule.create(scheduledComposite.getID());
                scheduledComposite = null;
            } else {
                f = FragmentSchedule.create(-1);
            }

        } else if (position == Page.LOG.index) {
            background = R.color.material_green;
            f = FragmentLog.create();
        } else if (position == Page.PRIVACY.index) {
            background = R.color.black;
            f = FragmentPrivacy.create();
        } else if (position == Page.ACCOUNTS.index) {
            background = R.color.black;
            f = FragmentAccounts.create();
        }

        currentPage = position;

        if (toolbar != null) {
            toolbar.setBackgroundResource(background);
        }

        fragmentManager.beginTransaction().replace(R.id.container, f).commit();
        invalidateOptionsMenu();
    }

    // TODO Categories for component list
    // TODO Test rotations on every page
    // TOdO Look up that error about doing fragment things after saved instance state

    @Override
    public void onBackPressed() {
        if (currentPage == Page.HOME.index) {
            if (homeFragment.getMode() == FragmentComposites.MODE_COMPOSITE) {
                homeFragment.setMode(FragmentComposites.MODE_LIST);
                homeFragment.redraw();
                invalidateOptionsMenu();
                return;
            }
        } else {
            if (currentPage == Page.COMPONENTS.index &&
                    componentFragment.getMode() != FragmentComponents.MODE_LIST) {

                if (componentFragment.getMode() == FragmentComponents.MODE_COMPONENT) {
                    componentFragment.setMode(FragmentComponents.MODE_LIST);
                } else { // It should be the app page
                    componentFragment.setMode(FragmentComponents.MODE_COMPONENT);
                }

                componentFragment.redraw();
                invalidateOptionsMenu();
                return;
            } // If it's on the component list we just want to go back to the home page

            // TODO Find out where "ID is too small" comes from
            // TODO Find out where "JSON string to bundle Fail FUCKSTICKS" comes from

            onNavigationDrawerItemSelected(Page.HOME.index);
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:

                switch (resultCode) {
                    case Activity.RESULT_OK:
                        break;
                }
        }
    }

    /**
     * From https://developer.android.com/training/location/retrieve-current.html
     */
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
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
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
        }
    }

    // FIXME Choosing which app to view didn't work
    // FIXME Saving the phone number also didn't work
    // TODO Could do with adding an overview to the wiring page

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.activity_app_glue, menu);

        int background = R.color.settings;
        String title = "";

        if (currentPage == Page.HOME.index) {
            background = R.color.composite;
            if (homeFragment.getMode() == FragmentComposites.MODE_COMPOSITE) {
                CompositeService composite = homeFragment.getComposite();
                if (composite != null) {
                    title = composite.getName();
                } else {
                    title = "Composite";
                }

                if (homeFragment.isEditingComposite()) {
                    menu.setGroupVisible(R.id.composite_done_group, true);
                    menu.setGroupVisible(R.id.composite_edit_group, false);
                } else {
                    menu.setGroupVisible(R.id.composite_done_group, false);
                    menu.setGroupVisible(R.id.composite_edit_group, true);
                }

            } else {
                title = "appGlue";
                menu.setGroupVisible(R.id.composite_done_group, false);
                menu.setGroupVisible(R.id.composite_edit_group, false);
            }

        } else if (currentPage == Page.COMPONENTS.index) {
            background = R.color.component;
            if (componentFragment.getMode() == FragmentComponents.MODE_COMPONENT) {
                ComponentService component = componentFragment.getComponent();
                if (component != null) {
                    title = "Component: " + component.getDescription().getName();
                } else {
                    title = "Component";
                }
            } else {
                title = "Components";
            }
        } else if (currentPage == Page.SCHEDULE.index) {
            background = R.color.schedule;
            title = "Schedule";
        } else if (currentPage == Page.LOG.index) {
            background = R.color.log;
            title = "Log";
        } else if (currentPage == Page.ACCOUNTS.index) {
            background = R.color.settings;
            title = "Connect accounts";
        } else if (currentPage == Page.PRIVACY.index) {
            background = R.color.settings;
            title = "Privacy";
        }

        if (toolbar != null) {
            toolbar.setBackgroundResource(background);
            toolbar.setTitle(title);
            toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        }

        return super.onCreateOptionsMenu(menu);
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
            return inflater.inflate(R.layout.fragment_activity_app_glue, container, false);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((ActivityAppGlue) activity).onSectionAttached(
                    Page.values()[getArguments().getInt(ARG_SECTION_NUMBER)]);
        }
    }

    void run(CompositeService cs) {
        if (cs == null) {
            Toast.makeText(this, "Error when trying to run composite", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent serviceIntent = new Intent(this, OrchestrationService.class);
        ArrayList<Bundle> intentData = new ArrayList<Bundle>();
        Bundle b = new Bundle();

        b.putLong(COMPOSITE_ID, cs.getID());
        b.putInt(INDEX, 0);
        b.putBoolean(IS_LIST, false);
        b.putInt(DURATION, 0);
        b.putBoolean(TEST, false);

        if (LOG) Log.w(TAG, "Trying to run " + cs.getID() + " : " + cs.getName());

        intentData.add(b);
        serviceIntent.putParcelableArrayListExtra(DATA, intentData);
        this.startService(serviceIntent);
    }

    void schedule(final CompositeService cs) {
        this.scheduledComposite = cs;
        onNavigationDrawerItemSelected(Page.SCHEDULE.index);
    }

    void edit(CompositeService cs) {
        Intent intent = new Intent(this, ActivityWiring.class);
        intent.putExtra(COMPOSITE_ID, cs.getID());
        intent.putExtra(EDIT_EXISTING, true);
        startActivity(intent);
    }

    void createShortcut(CompositeService cs) {
        Intent shortcutIntent = new Intent();
        shortcutIntent.setComponent(new ComponentName(getPackageName(), ShortcutActivity.class.getName()));

        Bundle b = new Bundle();
        b.putLong(COMPOSITE_ID, cs.getID());
        shortcutIntent.putExtras(b);

        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final Intent putShortCutIntent = new Intent();
        putShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT,
                shortcutIntent);

        // Sets the custom shortcut's title
        putShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, cs.getName());
        putShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.drawable.icon));
        putShortCutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        sendBroadcast(putShortCutIntent);
    }
}