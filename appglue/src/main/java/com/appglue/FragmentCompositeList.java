package com.appglue;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.appglue.description.AppDescription;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.layout.FloatingActionButton;
import com.appglue.library.AppGlueLibrary;
import com.appglue.library.LocalStorage;
import com.appglue.serviceregistry.Registry;
import com.appglue.serviceregistry.RegistryService;
import com.appglue.services.ServiceFactory;

import org.json.JSONException;

import java.util.ArrayList;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.COMPOSITE_COLOURS;
import static com.appglue.library.AppGlueConstants.COMPOSITE_COLOURS_LIGHT;
import static com.appglue.library.AppGlueConstants.COMPOSITE_ID;
import static com.appglue.library.AppGlueConstants.EDIT_EXISTING;
import static com.appglue.library.AppGlueConstants.CREATE_NEW;

// FIXME The selectionMode thing

public class FragmentCompositeList extends Fragment {

    private GridView compositeGrid;
    private ListView compositeList;

    private boolean listMode = true;

    private ImageView loader;
    private View noComposites;
    private FloatingActionButton addFab;

    private Toolbar contextToolbar;
    private float toolbarY = -1;
    private float toolbarYHidden = -1;

    private Registry registry;
    private LocalStorage localStorage;

    private ArrayList<CompositeService> composites;

    private CompositeGridAdapter gridAdapter;

    private ArrayList<Integer> selected = new ArrayList<Integer>();

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
            } else if (composites.get(selected.get(0)).getComponents().get(0).getDescription().hasFlag(ComposableService.FLAG_TRIGGER)) {
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
            // Just clear all of them - except the temp where we need to add a new one
            for (int i = 0; i < compositeGrid.getChildCount() - 1; i++) {
                compositeGrid.getChildAt(i).setBackgroundResource(0);
            }

            for (int i = 0; i < compositeList.getChildCount(); i++) {
                compositeList.getChildAt(i).setBackgroundResource(0);
            }

            actionMode = null;
            selected.clear();
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            ActivityAppGlue aag = (ActivityAppGlue) getActivity();
            switch (item.getItemId()) {
                case R.id.comp_context_run:
                    aag.run(composites.get(selected.get(0)));
                    break;

                case R.id.comp_context_timer:
                    schedule(composites.get((selected.get(0))));
                    break;

                case R.id.comp_context_view:
                    view(composites.get((selected.get(0))));
                    break;

                case R.id.comp_context_edit:
                    edit(composites.get((selected.get(0))));
                    break;

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


    public static Fragment create() {
        return new FragmentCompositeList();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        registry = Registry.getInstance(activity);
        localStorage = LocalStorage.getInstance();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        View root = inflater.inflate(R.layout.fragment_composite_list, container, false);

        compositeGrid = (GridView) root.findViewById(R.id.load_grid);
        compositeList = (ListView) root.findViewById(R.id.load_list);

        noComposites = root.findViewById(R.id.no_composites);
        contextToolbar = (Toolbar) root.findViewById(R.id.context_toolbar);

        addFab = (FloatingActionButton) root.findViewById(R.id.fab_add);
        if (addFab != null) {
            addFab.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ActivityWiring.class);
                    intent.putExtra(EDIT_EXISTING, false);
                    intent.putExtra(CREATE_NEW, true);
                    startActivity(intent);
                }
            });
        }

        loader = (ImageView) root.findViewById(R.id.loading_spinner);

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
        BackgroundCompositeLoader bcl = new BackgroundCompositeLoader();
        bcl.execute();
    }

    public void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);
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

    public void setViewMode() {
        if (getViewMode() == ActivityAppGlue.VIEW_GRID) {
            compositeGrid.setVisibility(View.VISIBLE);
            compositeList.setVisibility(View.GONE);
        } else {
            compositeGrid.setVisibility(View.GONE);
            compositeList.setVisibility(View.VISIBLE);
        }
    }

    private int getViewMode() {
        return ((ActivityAppGlue) getActivity()).getViewMode();
    }

    private void delete(final ArrayList<CompositeService> csList) {
        final CompositeService cs = csList.get(0);

        new AlertDialog.Builder(getActivity())
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
                            Toast.makeText(getActivity(), String.format("Failed to delete \"%s\"", cs.getName()), Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getActivity(), String.format("\"%s\" deleted successfully", cs.getName()), Toast.LENGTH_SHORT).show();

                        // This only works when you click on something else?
                        composites = registry.getComposites();

                        if (gridAdapter != null) {
                            // This might need to be sorted?
                            gridAdapter = new CompositeGridAdapter(getActivity(), R.layout.grid_item_app_selector, composites);
                            compositeGrid.setAdapter(gridAdapter);
                        } else {
                            gridAdapter = new CompositeGridAdapter(getActivity(), R.layout.grid_item_app_selector, composites);
                            compositeGrid.setAdapter(gridAdapter);
                        }

                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    private void schedule(final CompositeService cs) {
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
//                } else if (intervalIndex == Interval.MINUTES.index) {
//                    interval = Interval.MINUTES;
//                } else if (intervalIndex == Interval.HOURS.index) {
//                    interval = Interval.HOURS;
//                } else {
//                    interval = Interval.DAYS;
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

    private void view(CompositeService cs) {
        Intent intent = new Intent(getActivity(), ActivityComposite.class);
        intent.putExtra(COMPOSITE_ID, cs.getID());
        startActivity(intent);
    }

    private void edit(CompositeService cs) {
        Intent intent = new Intent(getActivity(), ActivityWiring.class);
        if (LOG) Log.d(TAG, "Putting id for edit " + cs.getID());
        intent.putExtra(COMPOSITE_ID, cs.getID());
        startActivity(intent);
    }

    private void createShortcut(CompositeService cs) {
        Intent shortcutIntent = new Intent();
        shortcutIntent.setComponent(new ComponentName(getActivity().getPackageName(), ShortcutActivity.class.getName()));

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
        putShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getActivity(), R.drawable.icon));
        putShortCutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        getActivity().sendBroadcast(putShortCutIntent);
    }

    private class CompositeGridAdapter extends ArrayAdapter<CompositeService> {
        public CompositeGridAdapter(Context context, int textViewResourceId, ArrayList<CompositeService> items) {
            super(context, textViewResourceId, items);
        }

        @SuppressLint("InflateParams")
        public View getView(final int position, View convertView, final ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.grid_item_app_selector, null);
            }

            final CompositeService cs = composites.get(position);

            if (v == null)
                return null;

            TextView nameText = (TextView) v.findViewById(R.id.load_list_name);
            if (nameText == null) // This way it doesn't die, but this way of fixing it doesn't seem to be a problem...
                return v;

            if (cs.getName().equals(""))
                nameText.setText("Temp ");// + tempCount++);
            else
                nameText.setText(cs.getName());

            ImageView icon = (ImageView) v.findViewById(R.id.service_icon);
            SparseArray<ComponentService> components = cs.getComponents();

            if (components.size() == 0) {
                icon.setBackgroundResource(R.drawable.icon);
            } else {

                AppDescription app = components.get(0).getDescription().getApp();

                if (app == null || app.iconLocation() == null) {
                    icon.setBackgroundResource(R.drawable.icon);
                } else {
                    String iconLocation = app.iconLocation();
                    Bitmap b = localStorage.readIcon(iconLocation);
                    icon.setImageBitmap(b);
                }
            }

            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (actionMode == null) {

                    } else {
                        // Add and remove things from the action mode
                        if (selected.contains(position)) {
                            selected.remove(selected.indexOf(position));
                            v.setBackgroundResource(0);
                        } else {
                            selected.add(position);
                            v.setBackgroundColor(getResources().getColor(R.color.android_blue_very));
                        }

                        if (selected.size() == 0) {
                            actionMode.finish();
                            actionMode = null;
                        } else {
                            if (selected.size() > 1)
                                actionMode.setSubtitle(selected.size() + " selected.");
                            else
                                actionMode.setSubtitle(composites.get(selected.get(0)).getName() + " selected.");
                        }
                    }
                }
            });

            v.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    if (actionMode == null) {
                        if (!selected.contains(position)) {
                            selected.add(position);
                            v.setBackgroundColor(getResources().getColor(R.color.android_blue_very));
                        }

                        actionMode = getActivity().startActionMode(actionCallback);
                    }

                    return true;
                }
            });

            return v;
        }
    }

    private class CompositeListAdapter extends ArrayAdapter<CompositeService> {

        int selectedIndex = -1;

        public CompositeListAdapter(Context context, int textViewResourceId, ArrayList<CompositeService> items) {
            super(context, textViewResourceId, items);
        }

        @SuppressLint("InflateParams")
        public View getView(final int position, View convertView, final ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_item_app_selector, null);
            }

            final CompositeService item = composites.get(position);

            if (v == null)
                return null;

            TextView nameText = (TextView) v.findViewById(R.id.composite_name);
            if (nameText == null) // This way it doesn't die, but this way of fixing it doesn't seem to be a problem...
                return v;

            if (item.getName().equals(""))
                nameText.setText("Temp ");
            else
                nameText.setText(item.getName());

            ImageView icon = (ImageView) v.findViewById(R.id.composite_icon);
            SparseArray<ComponentService> components = item.getComponents();

            AppDescription app = components.get(0).getDescription().getApp();
            if (app == null || app.iconLocation() == null) {
                icon.setBackgroundResource(R.drawable.icon);
            } else {
                String iconLocation = app.iconLocation();
                Bitmap b = localStorage.readIcon(iconLocation);
                if (b != null) {
                    icon.setImageBitmap(b);
                } else {
                    icon.setBackgroundResource(R.drawable.icon);
                }
            }

            v.findViewById(R.id.info_button).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getParentFragment() != null) {
                        ((FragmentComposites) getParentFragment()).viewComposite(item.getID());
                    } else {
                        // Not sure why this would happen, it seems that android might have killed it. Maybe because there's not a reference to it?
                        Log.e(TAG, "Parent fragment is null");
                    }
                }
            });

            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (item.isEnabled()) {
                        if (selectedIndex == -1) {
                            addFab.hide(true);
                            selectedIndex = position;
                            contextToolbar.setVisibility(View.VISIBLE);
                        } else if (selectedIndex == position) {
                            addFab.hide(false);
                            contextToolbar.setVisibility(View.GONE);
                            selectedIndex = -1;
                        } else {
                            selectedIndex = position;
                        }
                        notifyDataSetChanged();
                    } else {
                        // If they click an unselected one then take everything back
                        selectedIndex = -1;
                        addFab.hide(false);
                    }
                }
            });

            LinearLayout componentContainer = (LinearLayout) v.findViewById(R.id.composite_components);
            componentContainer.removeAllViews();
            for (int i = 0; i < item.getComponents().size(); i++) {
                ComponentService component = item.getComponents().get(i);
                TextView tv = new TextView(getContext());
                tv.setText(component.getDescription().getName());
                componentContainer.addView(tv);
            }

            View backgroundView = v.findViewById(R.id.composite_item_bg);
            if (item.isEnabled()) {
                if (position == selectedIndex) {
                    // This is sellected so it should be bright
                    backgroundView.setBackgroundResource(COMPOSITE_COLOURS[position % COMPOSITE_COLOURS.length]);
                    nameText.setTextColor(getResources().getColor(R.color.textColorPrimary));
                } else {
                    backgroundView.setBackgroundResource(COMPOSITE_COLOURS_LIGHT[position % COMPOSITE_COLOURS_LIGHT.length]);
                    nameText.setTextColor(getResources().getColor(R.color.textColor));
                }
                // The image needs to be in colour
                // The text needs to be brighter
                // The top bar needs to be normal
            } else {
                backgroundView.setBackgroundResource(R.color.card_disabled);
                nameText.setTextColor(getResources().getColor(R.color.dimmer_text));
                // Grey icon
                // Grey text
                // Dimmer top bar - maybe just desaturate the translucent colour
            }

            return v;
        }
    }

    private class BackgroundCompositeLoader extends AsyncTask<Void, Void, ArrayList<CompositeService>> {

        @Override
        protected ArrayList<CompositeService> doInBackground(Void... arg0) {
            try {
                ServiceFactory sf = ServiceFactory.getInstance(registry, getActivity());
                sf.setupServices();

            } catch (JSONException e) {
                Log.e(TAG, "JSONException - Failed to create services (CompositeListActivity) " + e.getMessage());
            }

            if (getActivity() == null) {
                return new ArrayList<CompositeService>();
                // TODO Probably should schedule the thing to have another look. Don't know why the activity would be null
            }

            ActivityManager manager = (ActivityManager) getActivity().getSystemService(Activity.ACTIVITY_SERVICE);

            if (manager != null) {
                boolean registryRunning = false;

                for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                    if (RegistryService.class.getCanonicalName().equals(service.service.getClassName())) {
                        registryRunning = true;
                    }
                }

                if (!registryRunning) {
                    Intent registryIntent = new Intent(getActivity(), RegistryService.class);
                    getActivity().startService(registryIntent);
                }
            }

            ArrayList<CompositeService> composites = registry.getComposites();
            return composites;
        }

        protected void onPostExecute(ArrayList<CompositeService> composites) {

            // Maybe set a flag or something?
            FragmentCompositeList.this.composites = composites;

            gridAdapter = new CompositeGridAdapter(getActivity(), R.layout.grid_item_app_selector, composites);
            compositeGrid.setAdapter(gridAdapter);

            CompositeListAdapter listAdapter = new CompositeListAdapter(getActivity(), R.layout.list_item_app_selector, composites);
            compositeList.setAdapter(listAdapter);

            loader.setVisibility(View.GONE);

            if (composites.size() > 0) {
                if (listMode) {
                    compositeList.setVisibility(View.VISIBLE);
                    compositeGrid.setVisibility(View.GONE);
                } else {
                    compositeGrid.setVisibility(View.VISIBLE);
                    compositeList.setVisibility(View.GONE);
                }

                noComposites.setVisibility(View.GONE);
            } else {
                noComposites.setVisibility(View.VISIBLE);
            }


        }
    }
}
