package com.appglue;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.appglue.Constants.Interval;
import com.appglue.Constants.ProcessType;
import com.appglue.description.AppDescription;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.engine.OrchestrationService;
import com.appglue.library.LocalStorage;
import com.appglue.serviceregistry.Registry;
import com.appglue.serviceregistry.RegistryService;
import com.appglue.services.ServiceFactory;

import org.json.JSONException;

import java.util.ArrayList;

import static com.appglue.library.AppGlueConstants.COMPOSITE_ID;
import static com.appglue.Constants.DATA;
import static com.appglue.Constants.DURATION;
import static com.appglue.Constants.INDEX;
import static com.appglue.Constants.IS_LIST;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.RUN_NOW;
import static com.appglue.Constants.TAG;
import static com.appglue.Constants.TEST;
import static com.appglue.library.AppGlueConstants.CREATE_NEW;
import static com.appglue.library.AppGlueConstants.EDIT_PARAMS;
import static com.appglue.library.AppGlueConstants.PLAY_SERVICES;
import static com.appglue.library.AppGlueConstants.PRE_EXEC_PARAMS;

public class ActivityCompositeList extends Activity {
    private GridView loadGrid;
    private ImageView loader;

    private ArrayList<CompositeService> composites;

    private Registry registry;
    private LocalStorage localStorage;

    private CompositeGridAdapter adapter;

    private int tempCount = 0;

    private ArrayList<Integer> selected = new ArrayList<Integer>();

    // TODO Let them choose custom icons

    private ActionMode actionMode;
    private ActionMode.Callback actionCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.composite_list_context_menu, menu);

            mode.setTitle("Choose action:");

            if (selected.size() > 1)
                mode.setSubtitle(selected.size() + " selected.");
            else
                mode.setSubtitle(composites.get(selected.get(0)).getName() + " selected.");

            if (selected.size() > 1) {
                menu.setGroupVisible(R.id.comp_context_rungroup, false);
                menu.setGroupVisible(R.id.comp_context_singlegroup, false);
            } else if (composites.get(selected.get(0)).getComponents().get(0).getDescription().getProcessType() == ProcessType.TRIGGER) {
                menu.setGroupVisible(R.id.comp_context_rungroup, false);
                menu.setGroupVisible(R.id.comp_context_singlegroup, true);
            } else {
                menu.setGroupVisible(R.id.comp_context_rungroup, true);
                menu.setGroupVisible(R.id.comp_context_singlegroup, true);
            }

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // Don't really know why I'm returning false, but this is what the example does...
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // Just clear all of them, bugger it
            for (int i = 0; i < loadGrid.getChildCount(); i++) {
                loadGrid.getChildAt(i).setBackgroundResource(0);
            }

            actionMode = null;
            selected.clear();
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.comp_context_run:
                    run(composites.get(selected.get(0)));
                    break;

                case R.id.comp_context_timer:
                    createTimerDialog(composites.get((selected.get(0))));
                    break;

				case R.id.comp_context_view:
					view(composites.get((selected.get(0))));
					break;

                case R.id.comp_context_edit:
                    edit(composites.get((selected.get(0))));
                    break;

//				case R.getID.comp_context_editstory:
//					editStory(composites.get((selected.get(0))));
//					break;

                case R.id.comp_context_shortcut:
                    createShortcut(composites.get((selected.get(0))));
                    break;

                case R.id.comp_context_delete:
                    ArrayList<CompositeService> killList = new ArrayList<CompositeService>();
                    for (Integer aSelected : selected) {
                        killList.add(composites.get(aSelected));
                    }
                    delete(killList);
                    break;
            }

            mode.finish();

            return false;
        }
    };

    // Make it give you a notification of what apps have changed since the last time it was loaded.
    // Make it execute on long press
    // Make it log in with Google+
    // Why is the icon the old icon the default?
    // http://developers.google.com/drive/android-quickstart
    // If others broadcast, this should bind

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.activity_composite_list);

        registry = Registry.getInstance(this);
        localStorage = LocalStorage.getInstance();
        loadGrid = (GridView) findViewById(R.id.loadList);
        loader = (ImageView) findViewById(R.id.loading_spinner);

//		mainContainer = (LinearLayout) findViewById(R.getID.comp_list_main);

        //Intent intent = new Intent(ActivityCompositeList.this, AlarmService.class);
        //startService(intent);

        // Google plus stuff
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.composite_list_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;

//		if(item.getItemId() == R.getID.comp_list_simple)
//		{
//			intent = new Intent(ActivityCompositeList.this, ActivityComponentList.class);
//			intent.putExtra(JUST_A_LIST, true);
//		}
//		else
        if (item.getItemId() == R.id.comp_list_new) {
            intent = new Intent(ActivityCompositeList.this, ActivityWiring.class);
        } else if (item.getItemId() == R.id.comp_list_log) {
            intent = new Intent(ActivityCompositeList.this, ActivityLog.class);
        }
//		else if(item.getItemId() == R.getID.composite_list_gplus_login)
//		{
//			GooglePlus gPlus = GooglePlus.getInstance(this);
//			String[] accounts = gPlus.getAccountNames(this);
//			
//			if(accounts.length == 1)
//			{
//				gPlus.signIn(accounts[0]);
//			}
//			else
//			{
//				// Make them choose which account if there is more than one account 
//			}
//
//			return true;
//		}
//		else if(item.getItemId() == R.getID.comp_list_running)
//		{
//			intent = new Intent(ActivityCompositeList.this, ActivityRunning.class);
//		}
//		else if(item.getItemId() == R.getID.comp_list_story)
//		{
//			intent = new Intent(ActivityCompositeList.this, ActivityStory.class);
//		}
        else if (item.getItemId() == R.id.comp_list_settings) {
            intent = new Intent(ActivityCompositeList.this, ActivitySettings.class);
        }

        startActivity(intent);
        return true;
    }

    public void onPause() {
        super.onPause();
    }

    public void onBackPressed() {
        if (actionMode != null) {
            actionMode.finish();
            Log.d(TAG, "Back pressed during action mode");
        }

        super.onBackPressed();
    }

    public void onResume() {
        super.onResume();

        BackgroundCompositeLoader bcl = new BackgroundCompositeLoader();
        bcl.execute();

//		int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
//		
//		switch(result)
//		{
//			case ConnectionResult.SUCCESS:
//				// This is fine
//				break;
//				
//			case ConnectionResult.SERVICE_MISSING:
//			case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
//			case ConnectionResult.SERVICE_DISABLED:
//				Dialog dialog = GooglePlayServicesUtil.getErrorDialog(result, this, PLAY_SERVICES);
//				dialog.show();
//				break;
//		}
    }

    protected void onSaveInstanceState(@NonNull Bundle icicle) {
        icicle.describeContents();
        // Not sure we need to save anything here
    }

    protected void onRestoreInstanceState(@NonNull Bundle icicle) {
        icicle.describeContents();
        // So we probably don't need to restore anything back
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == PRE_EXEC_PARAMS && resultCode == Activity.RESULT_OK) {
            long compositeId = intent.getLongExtra(COMPOSITE_ID, -1);

            Intent serviceIntent = new Intent(ActivityCompositeList.this, OrchestrationService.class);
            serviceIntent.putExtra(COMPOSITE_ID, compositeId);
            serviceIntent.putExtra(DURATION, intent.getIntExtra(DURATION, 0));
            startService(serviceIntent);
        } else if (requestCode == EDIT_PARAMS && resultCode == Activity.RESULT_OK) {
            // Not sure we actually need to do anything do we?
            if (LOG) Log.d(TAG, "Edit params OKAY result!");
        } else if (requestCode == PLAY_SERVICES) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "Play services return okay!! ");
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "You need to install Google Play service to use all the features of appGlue", Toast.LENGTH_LONG).show();
            } else {
                Log.e(TAG, "Play Services Return derp  " + resultCode);
            }
        } else {
            Log.w(TAG, "Play services FAIL");
            // It didn't go okay?
        }
    }

    private void run(CompositeService cs) {
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
        startService(serviceIntent);
    }

    private void createTimerDialog(final CompositeService cs) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.MyDialog));
        builder.setTitle("Set timer duration");

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_timer, null);
        builder.setView(layout);

        if (layout == null)
            return;

        final EditText numeralEdit = (EditText) layout.findViewById(R.id.timer_edit_numerals);
        final CheckBox runNowCheck = (CheckBox) layout.findViewById(R.id.timer_run_now);

        if (numeralEdit == null || runNowCheck == null)
            return;

        final Spinner intervalSpinner = (Spinner) layout.findViewById(R.id.timer_spinner_intervals);
        ArrayAdapter<CharSequence> intervalAdapter = ArrayAdapter.createFromResource(this, R.array.time_array, R.layout.dialog_spinner_dropdown);
        intervalAdapter.setDropDownViewResource(R.layout.dialog_spinner_dropdown);
        intervalSpinner.setAdapter(intervalAdapter);

        Dialog.OnClickListener okayClick = new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the things of each of the spinners and work out the duration
                long numeral = Integer.parseInt(numeralEdit.getText().toString());
                int intervalIndex = intervalSpinner.getSelectedItemPosition();
                Interval interval;

                if (intervalIndex == Interval.SECONDS.index) {
                    interval = Interval.SECONDS;
                } else if (intervalIndex == Interval.MINUTES.index) {
                    interval = Interval.MINUTES;
                } else if (intervalIndex == Interval.HOURS.index) {
                    interval = Interval.HOURS;
                } else {
                    interval = Interval.DAYS;
                }

                runOnTimer(cs, numeral * interval.value, runNowCheck.isChecked());
            }
        };

        builder.setPositiveButton("Okay", okayClick);
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void runOnTimer(CompositeService cs, long duration, boolean runNow) {
        Intent intent = new Intent(ActivityCompositeList.this, OrchestrationService.class);
        intent.putExtra(COMPOSITE_ID, cs.getID());
        intent.putExtra(DURATION, duration);
        intent.putExtra(RUN_NOW, runNow);
        startService(intent);
    }

    private void view(CompositeService cs) {
        Intent intent = new Intent(ActivityCompositeList.this, ActivityComposite.class);
        intent.putExtra(COMPOSITE_ID, cs.getID());
        startActivity(intent);
    }

    private void edit(CompositeService cs) {
        Intent intent = new Intent(ActivityCompositeList.this, ActivityWiring.class);
        if (LOG) Log.d(TAG, "Putting id for edit " + cs.getID());
        intent.putExtra(COMPOSITE_ID, cs.getID());
        startActivity(intent);
    }

    private void editStory(CompositeService cs) {
        Intent intent = new Intent(ActivityCompositeList.this, ActivityStory.class);
        if (LOG) Log.d(TAG, "Putting id for edit " + cs.getID());
        intent.putExtra(COMPOSITE_ID, cs.getID());
        startActivity(intent);
    }

    private void delete(final ArrayList<CompositeService> csList) {
        final CompositeService cs = csList.get(0);

        new AlertDialog.Builder(ActivityCompositeList.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Delete")
                .setMessage(String.format("Are you sure you want to delete %s?", csList.size() == 1 ? cs.getName() : csList.size() + " services"))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean fail = false;
                        for (CompositeService aCsList : csList) {
                            if (!registry.deleteComposite(aCsList)) {
                                fail = true;
                            }
                        }

                        if (fail)
                            Toast.makeText(ActivityCompositeList.this, String.format("Failed to delete \"%s\"", cs.getName()), Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(ActivityCompositeList.this, String.format("\"%s\" deleted successfully", cs.getName()), Toast.LENGTH_SHORT).show();

                        // This only works when you click on something else?
                        composites = registry.getComposites(false);
                        composites.add(CompositeService.makePlaceholder());

                        if (adapter != null) {
                            // This might need to be sorted?
                            adapter = new CompositeGridAdapter(ActivityCompositeList.this, R.layout.list_item_app_selector, composites);
                            loadGrid.setAdapter(adapter);
                        } else {
                            adapter = new CompositeGridAdapter(ActivityCompositeList.this, R.layout.list_item_app_selector, composites);
                            loadGrid.setAdapter(adapter);
                        }

                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    private void createShortcut(CompositeService cs) {
        Intent shortcutIntent = new Intent();
        shortcutIntent.setComponent(new ComponentName(ActivityCompositeList.this.getPackageName(), ShortcutActivity.class.getName()));

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
        putShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(ActivityCompositeList.this, R.drawable.icon));
        putShortCutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        sendBroadcast(putShortCutIntent);
    }

    private class CompositeGridAdapter extends ArrayAdapter<CompositeService> {
        public CompositeGridAdapter(Context context, int textViewResourceId, ArrayList<CompositeService> items) {
            super(context, textViewResourceId, items);
        }

        @SuppressLint("InflateParams")
        public View getView(final int position, View convertView, final ViewGroup parent) {
            View v = convertView;
            final CompositeService cs = composites.get(position);

            if (cs.getID() == CompositeService.NEW_COMPOSITE_PLACEHOLDER) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.composite_list_item_new, null);

                if (v != null) {
                    v.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ActivityCompositeList.this, ActivityWiring.class);
                            intent.putExtra(CREATE_NEW, true);
                            startActivity(intent);
                        }
                    });
                }

                return v;
            }

            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_item_app_selector, null);
            }

            if (v == null)
                return null;

            TextView nameText = (TextView) v.findViewById(R.id.load_list_name);
            if (nameText == null) // This way it doesn't die, but this way of fixing it doesn't seem to be a problem...
                return v;

            if (cs.getName().equals(""))
                nameText.setText("Temp " + tempCount++);
            else
                nameText.setText(cs.getName());

            ImageView icon = (ImageView) v.findViewById(R.id.service_icon);
            SparseArray<ComponentService> components = cs.getComponents();

//            Log.d(TAG, cs.name() + ": " + components.size());
//            for (int i = 0; i < components.size(); i++) {
//                Log.d(TAG, components.keyAt(i) + ": alive");
//            }

            AppDescription app = components.get(0).getDescription().app();


            if (app == null || app.iconLocation() == null) {
                // FIXME Work out how to do the below
//                icon.setBackground(getResources().getDrawable(R.drawable.icon));
            } else {
                String iconLocation = app.iconLocation();
                Bitmap b = localStorage.readIcon(iconLocation);
                icon.setImageBitmap(b);
            }

            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selected.contains(position)) {
                        // Remove it
                        selected.remove(selected.indexOf(position));
                        v.setBackgroundResource(0);

                        // And set the background of the removed one to be blank
                    }

                    selected.add(position);
//					oldSelectedIndex = selectedIndex;
//					selectedIndex = position;

//					if(oldSelectedIndex != -1)
//						parent.getChildAt(oldSelectedIndex).setBackgroundResource(0);

                    // Set the background colour of this one.
                    v.setBackgroundColor(getResources().getColor(R.color.android_blue_very));

                    if (actionMode != null) {
                        actionMode.setSubtitle(selected.size() + " selected."); //cs.name());

                        if (selected.size() == 1) {
                            if (cs.getComponents().get(0).getDescription().getProcessType() == ProcessType.TRIGGER) {
                                actionMode.getMenu().setGroupVisible(R.id.comp_context_rungroup, false);
                                actionMode.getMenu().setGroupVisible(R.id.comp_context_singlegroup, true);
                            } else {
                                actionMode.getMenu().setGroupVisible(R.id.comp_context_rungroup, true);
                                actionMode.getMenu().setGroupVisible(R.id.comp_context_singlegroup, true);
                            }
                        } else if (selected.size() > 1) {
                            // Hide the edit ones
                            actionMode.getMenu().setGroupVisible(R.id.comp_context_rungroup, false);
                            actionMode.getMenu().setGroupVisible(R.id.comp_context_singlegroup, false);
                        } else {
                            // Dno, it's probably zero now
                            if (LOG) Log.d(TAG, "There's nothing left in the action mode");
                        }

                        // The menu needs to have different things depending on how many are selected
                    } else {
                        actionMode = ActivityCompositeList.this.startActionMode(actionCallback);
                    }
                }
            });
            return v;
        }
    }

    private class BackgroundCompositeLoader extends AsyncTask<Void, Void, ArrayList<CompositeService>> {

        @Override
        protected ArrayList<CompositeService> doInBackground(Void... arg0) {
            try {
                ServiceFactory sf = ServiceFactory.getInstance(registry, ActivityCompositeList.this);
                sf.setupServices();

            } catch (JSONException e) {
                Log.e(TAG, "JSONException - Failed to create services (CompositeListActivity) " + e.getMessage());
            }

            ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

            if (manager != null) {
                boolean registryRunning = false;

                for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                    if (RegistryService.class.getCanonicalName().equals(service.service.getClassName())) {
                        registryRunning = true;
                    }
                }

                if (!registryRunning) {
                    Intent registryIntent = new Intent(ActivityCompositeList.this, RegistryService.class);
                    startService(registryIntent);
                }
            }

            ArrayList<CompositeService> composites = registry.getComposites(false);
            composites.add(CompositeService.makePlaceholder());

            return composites;
        }

        protected void onPostExecute(ArrayList<CompositeService> compositeList) {
            // Maybe set a flag or something?
            composites = compositeList;

            CompositeGridAdapter adapter = new CompositeGridAdapter(ActivityCompositeList.this, R.layout.composition_list_item, composites);
            loadGrid.setAdapter(adapter);

            loader.setVisibility(View.GONE);
            loadGrid.setVisibility(View.VISIBLE);
        }
    }
}
