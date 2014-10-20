package com.appglue;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.appglue.engine.OrchestrationService;
import com.appglue.engine.description.CompositeService;
import com.appglue.serviceregistry.Registry;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
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
import static com.appglue.library.AppGlueConstants.TEST;

public class ActivityAppGlue extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public enum Page {
        HOME(0, "My Glued Apps"),
        COMPONENTS(1, "Component List"),
        SCHEDULE(2, "Schedule"),
        LOG(3, "Log"),
        ACCOUNTS(4, "Connect accounts"),
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
        } else if (position == Page.ACCOUNTS.index) {
            f = FragmentAccounts.create();
        }

        currentPage = position;

        fragmentManager.beginTransaction().replace(R.id.container, f).commit();
        invalidateOptionsMenu();
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

        invalidateOptionsMenu();
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST :

                switch (resultCode) {
                    case Activity.RESULT_OK :
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
        if(actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (currentPage == Page.HOME.index) {

            if (homeFragment.getMode() == FragmentComposites.MODE_LIST) {
                getMenuInflater().inflate(R.menu.activity_app_glue, menu);
            } else {
                getMenuInflater().inflate(R.menu.composite_menu, menu);
            }
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
        if(cs == null) {
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
        // TODO Go to the schedule fragment and indicate that we want to create a new one
        onNavigationDrawerItemSelected(Page.SCHEDULE.index);


//        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.MyDialog));
//        builder.setTitle("Set timer duration");
//
//        LayoutInflater inflater = getLayoutInflater();
//        View layout = inflater.inflate(R.layout.dialog_timer, null);
//        builder.setView(layout);
//
//        if (layout == null)
//            return;
//
//        final EditText numeralEdit = (EditText) layout.findViewById(R.id.timer_edit_numerals);
//        final CheckBox runNowCheck = (CheckBox) layout.findViewById(R.id.timer_run_now);
//
//        if (numeralEdit == null || runNowCheck == null)
//            return;
//
//        final Spinner intervalSpinner = (Spinner) layout.findViewById(R.id.timer_spinner_intervals);
//        ArrayAdapter<CharSequence> intervalAdapter = ArrayAdapter.createFromResource(this, R.array.time_array, R.layout.dialog_spinner_dropdown);
//        intervalAdapter.setDropDownViewResource(R.layout.dialog_spinner_dropdown);
//        intervalSpinner.setAdapter(intervalAdapter);
//
//        Dialog.OnClickListener okayClick = new Dialog.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                // Get the things of each of the spinners and work out the duration
//                long numeral = Integer.parseInt(numeralEdit.getText().toString());
//                int intervalIndex = intervalSpinner.getSelectedItemPosition();
//                Interval interval;
//
//                if (intervalIndex == Interval.SECONDS.index) {
//                    interval = Interval.SECONDS;
//                } else if (intervalIndex == Interval.MINUTE.index) {
//                    interval = Interval.MINUTE;
//                } else if (intervalIndex == Interval.HOUR.index) {
//                    interval = Interval.HOUR;
//                } else {
//                    interval = Interval.DAY_OF_WEEK;
//                }
//
//                runOnTimer(cs, numeral * interval.value, runNowCheck.isChecked());
//            }
//        };
//
//        builder.setPositiveButton("Okay", okayClick);
//        builder.setNegativeButton("Cancel", null);
//
//        AlertDialog dialog = builder.create();
//        dialog.show();
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

    void delete(final CompositeService cs) {

        final Registry registry = Registry.getInstance(this);

        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Delete")
                .setMessage(String.format("Are you sure you want to delete %s?", cs.getName()))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (registry.deleteComposite(cs)) {
                            Toast.makeText(ActivityAppGlue.this, String.format("\"%s\" deleted successfully", cs.getName()), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ActivityAppGlue.this, String.format("Failed to delete \"%s\"", cs.getName()), Toast.LENGTH_SHORT).show();
                        }

                        // Tell the page to refresh
                        if (homeFragment == null) {
                            onNavigationDrawerItemSelected(Page.HOME.index);
                        } else {
                            homeFragment.setMode(FragmentComposites.MODE_LIST);
                            homeFragment.redraw();
                        }
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

}
