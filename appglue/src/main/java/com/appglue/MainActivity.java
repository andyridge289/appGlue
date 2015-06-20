package com.appglue;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.appglue.engine.OrchestrationService;
import com.appglue.engine.model.CompositeService;
import com.appglue.layout.FragmentAccounts;
import com.appglue.layout.FragmentComponents;
import com.appglue.layout.FragmentComposites;
import com.appglue.layout.FragmentLog;
import com.appglue.layout.FragmentSchedule;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import hugo.weaving.DebugLog;

import static com.appglue.Constants.DATA;
import static com.appglue.Constants.DURATION;
import static com.appglue.Constants.INDEX;
import static com.appglue.Constants.IS_LIST;
import static com.appglue.library.AppGlueConstants.COMPOSITE_ID;
import static com.appglue.library.AppGlueConstants.EDIT_EXISTING;
import static com.appglue.library.AppGlueConstants.PREFS_HIDDEN;
import static com.appglue.library.AppGlueConstants.P_DISCLAIMER;
import static com.appglue.library.AppGlueConstants.TEST;

public class MainActivity extends AppGlueActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private CompositeService scheduledComposite;

    public enum Page {
        HOME(0, "My Glued Apps"),
        COMPONENTS(1, "Component List"),
        SCHEDULE(2, "Schedule"),
        LOG(3, "Log"),
        ACCOUNTS(4, "Connect accounts");

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
    private static final String COMPOSITE_MODE = "composite_mode";
    private static final String COMPONENT_MODE = "component_mode";

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private FragmentComposites homeFragment;
    private FragmentComponents componentFragment;
    private FragmentSchedule scheduleFragment;
    private FragmentLog logFragment;
    private FragmentAccounts accountFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private int currentPage;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_app_glue);

        toolbar = (Toolbar) findViewById(R.id.my_awesome_toolbar);
        setSupportActionBar(toolbar);

        /*
      Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
        mNavigationDrawerFragment = (NavigationDrawerFragment)
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

            Intent intent = new Intent(this, TutorialActivity.class);
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
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.tutorial) {
            Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
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
                scheduleFragment = FragmentSchedule.create(scheduledComposite.getID());
                scheduledComposite = null;
            } else {
                scheduleFragment = FragmentSchedule.create(-1);
            }
            f = scheduleFragment;

        } else if (position == Page.LOG.index) {
            background = R.color.material_green;
            logFragment = FragmentLog.create();
            f = logFragment;
        } else if (position == Page.ACCOUNTS.index) {
            background = R.color.black;
            accountFragment = FragmentAccounts.create();
            f = accountFragment;
        }

        currentPage = position;

        if (toolbar != null) {
            toolbar.setBackgroundResource(background);
        }

        fragmentManager.beginTransaction().replace(R.id.container, f).commit();
        invalidateOptionsMenu();
    }

    // TODO Sort the menus out
    // TODO Test rotations on every page
    // TOdO Look up that error about doing fragment things after saved instance state

    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen()) {
            mNavigationDrawerFragment.close();
            return;
        }

        if (currentPage == Page.HOME.index) {
            if(homeFragment.onBackPressed()) {
                invalidateOptionsMenu();
                return;
            }
        } else if (currentPage == Page.COMPONENTS.index) {

            if (componentFragment.onBackPressed()) {
                invalidateOptionsMenu();
                return;
            }

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
        @NonNull
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
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mTitle);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.activity_app_glue, menu);

        int background = R.color.settings;
        String title = "";

        if (currentPage == Page.HOME.index) {
            background = R.color.composite;
            title = homeFragment.onCreateOptionsMenu(menu);
        } else if (currentPage == Page.COMPONENTS.index) {
            background = R.color.component;
            title = componentFragment.onCreateOptionsMenu(menu);
        } else if (currentPage == Page.SCHEDULE.index) {
            background = R.color.schedule;
            title = scheduleFragment.onCreateOptionsMenu(menu);
        } else if (currentPage == Page.LOG.index) {
            background = R.color.log;
            title = logFragment.onCreateOptionsMenu(menu);
        } else if (currentPage == Page.ACCOUNTS.index) {
            background = R.color.settings;
            title = accountFragment.onCreateOptionsMenu(menu);
        }

        if (toolbar != null) {
            toolbar.setBackgroundResource(background);
            toolbar.setTitle(title);
            toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        }

        return super.onCreateOptionsMenu(menu);
    }

    public void run(CompositeService cs) {
        if (cs == null) {
            Toast.makeText(this, "Error when trying to run composite", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent serviceIntent = new Intent(this, OrchestrationService.class);
        ArrayList<Bundle> intentData = new ArrayList<>();
        Bundle b = new Bundle();

        b.putLong(COMPOSITE_ID, cs.getID());
        b.putInt(INDEX, 0);
        b.putBoolean(IS_LIST, false);
        b.putInt(DURATION, 0);
        b.putBoolean(TEST, false);

        Logger.w("Trying to run " + cs.getID() + " : " + cs.getName());

        intentData.add(b);
        serviceIntent.putParcelableArrayListExtra(DATA, intentData);
        this.startService(serviceIntent);
    }

    public void schedule(final CompositeService cs) {
        this.scheduledComposite = cs;
        onNavigationDrawerItemSelected(Page.SCHEDULE.index);
    }

    public void edit(CompositeService cs) {
        Intent intent = new Intent(this, WiringActivity.class);
        intent.putExtra(COMPOSITE_ID, cs.getID());
        intent.putExtra(EDIT_EXISTING, true);
        startActivity(intent);
    }

    public void createShortcut(CompositeService cs) {
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