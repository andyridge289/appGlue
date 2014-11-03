package com.appglue;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.appglue.description.AppDescription;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.layout.FloatingActionButton;
import com.appglue.library.LocalStorage;
import com.appglue.serviceregistry.Registry;
import com.appglue.serviceregistry.RegistryService;
import com.appglue.services.factory.ServiceFactory;

import org.json.JSONException;

import java.util.ArrayList;

import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.CREATE_NEW;
import static com.appglue.library.AppGlueConstants.EDIT_EXISTING;

public class FragmentCompositeList extends Fragment {

    private ListView compositeList;
    private CompositeListAdapter listAdapter;

    private ImageView loader;
    private View noComposites;
    private FloatingActionButton addFab;
    private LinearLayout contextToolbar;

    private Registry registry;
    private LocalStorage localStorage;

    private ArrayList<CompositeService> composites;

    public static Fragment create() {
        return new FragmentCompositeList();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        registry = Registry.getInstance(getActivity());
        localStorage = LocalStorage.getInstance();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        View root = inflater.inflate(R.layout.fragment_composite_list, container, false);

        noComposites = root.findViewById(R.id.no_composites);
        contextToolbar = (LinearLayout) root.findViewById(R.id.context_toolbar);
        contextToolbar.setVisibility(View.GONE);
        compositeList = (ListView) root.findViewById(R.id.composite_list);

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

        final ActivityAppGlue aag = ((ActivityAppGlue) getActivity());

        View run = root.findViewById(R.id.composite_run);
        run.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                aag.run(listAdapter.getCurrentComposite());
                contextToolbar.setVisibility(View.GONE);
            }
        });

        View schedule = root.findViewById(R.id.composite_schedule);
        schedule.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                aag.schedule(listAdapter.getCurrentComposite());
                contextToolbar.setVisibility(View.GONE);
            }
        });

        View edit = root.findViewById(R.id.composite_edit);
        edit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                aag.edit(listAdapter.getCurrentComposite());
                contextToolbar.setVisibility(View.GONE);
            }
        });

        View shortcut = root.findViewById(R.id.composite_shortcut);
        shortcut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                aag.createShortcut(listAdapter.getCurrentComposite());
                contextToolbar.setVisibility(View.GONE);
            }
        });

        View delete = root.findViewById(R.id.composite_delete);
        delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final CompositeService cs = listAdapter.getCurrentComposite();
                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Delete")
                        .setMessage(String.format("Are you sure you want to delete %s?", cs.getName()))
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (registry.delete(cs)) {
                                    Toast.makeText(getActivity(), String.format("\"%s\" deleted successfully", cs.getName()), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), String.format("Failed to delete \"%s\"", cs.getName()), Toast.LENGTH_SHORT).show();
                                }

                                listAdapter.remove(cs);
                                listAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("No", null).show();
                contextToolbar.setVisibility(View.GONE);
            }
        });

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

    public void redraw() {
        if (registry == null) {
            return;
        }

        composites = registry.getComposites();

        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }

        addFab.hide(false);
        contextToolbar.setVisibility(View.GONE);
        listAdapter.selectedIndex = -1;
    }

    private class CompositeListAdapter extends ArrayAdapter<CompositeService> {

        int selectedIndex = -1;
        private Boolean[] expanded;

        public CompositeListAdapter(Context context, ArrayList<CompositeService> items) {
            // TODO java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.Object android.content.Context.getSystemService(java.lang.String)' on a null object reference
            super(context, R.layout.list_item_composite, items);
            expanded = new Boolean[items.size()];
            for (int i = 0; i < expanded.length; i++) {
                expanded[i] = false;
            }
        }

        // TODO Unhighlight the selected composite when the contextbar is hidden

        @SuppressLint("InflateParams")
        public View getView(final int position, View convertView, final ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_item_composite, null);
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

            ImageView icon = (ImageView) v.findViewById(R.id.schedule_icon);
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
                            contextToolbar.setBackgroundResource(item.getColour(true));
                        } else if (selectedIndex == position) {
                            addFab.hide(false);
                            contextToolbar.setVisibility(View.GONE);
                            selectedIndex = -1;
                        } else {
                            selectedIndex = position;
                            contextToolbar.setBackgroundResource(item.getColour(true));
                        }
                        notifyDataSetChanged();
                    } else {
                        // If they click an unselected one then take everything back
//                        selectedIndex = -1;
//                        addFab.hide(false);
                        if (getParentFragment() != null) {
                            ((FragmentComposites) getParentFragment()).viewComposite(item.getID());
                        } else {
                            // Not sure why this would happen, it seems that android might have killed it. Maybe because there's not a reference to it?
                            Log.e(TAG, "Parent fragment is null");
                        }
                    }
                }
            });

            LinearLayout componentContainer = (LinearLayout) v.findViewById(R.id.composite_components);
            componentContainer.removeAllViews();

            if (expanded[position]) {
                for (int i = 0; i < item.getComponents().size(); i++) {
                    ComponentService component = item.getComponents().get(i);
                    TextView tv = new TextView(getContext());
                    tv.setText(component.getDescription().getName());

                    // XXX In expanded mode we need to add more information about the components

                    if (item.isEnabled()) {
                        if (position == selectedIndex) {
                            tv.setTextColor(getResources().getColor(R.color.textColor));
                        } else {
                            tv.setTextColor(getResources().getColor(R.color.textColor_dim));
                        }
                    } else {
                        tv.setTextColor(getResources().getColor(R.color.textColor_dimmer));
                    }

                    componentContainer.addView(tv);
                }
            } else {
                for (int i = 0; i < item.getComponents().size(); i++) {
                    ComponentService component = item.getComponents().get(i);
                    TextView tv = new TextView(getContext());
                    tv.setText(component.getDescription().getName());

                    if (item.isEnabled()) {
                        if (position == selectedIndex) {
                            tv.setTextColor(getResources().getColor(R.color.textColor));
                        } else {
                            tv.setTextColor(getResources().getColor(R.color.textColor_dim));
                        }
                    } else {
                        tv.setTextColor(getResources().getColor(R.color.textColor_dimmer));
                    }

                    componentContainer.addView(tv);
                }
            }

            View backgroundView = v.findViewById(R.id.composite_item_bg);
            if (item.isEnabled()) {
                // The text needs to be brighter
                if (position == selectedIndex) {
                    // This is selected so it should be bright
                    backgroundView.setBackgroundResource(item.getColour(true));
                    nameText.setTextColor(getResources().getColor(R.color.textColorInverse));
                } else {
                    backgroundView.setBackgroundResource(item.getColour(false));
                    nameText.setTextColor(getResources().getColor(R.color.textColor));
                }

                // The image needs to be in colour
                icon.setColorFilter(null);
            } else {
                backgroundView.setBackgroundResource(item.getColour(false));
                nameText.setTextColor(getResources().getColor(R.color.textColorInverse_dim));

                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                icon.setColorFilter(filter);
            }

            v.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (expanded[position]) {
                        expanded[position] = false;
                    } else {
                        expanded[position] = true;
                    }

                    notifyDataSetChanged();
                    return true;
                }
            });

            return v;
        }

        private CompositeService getCurrentComposite() {
            if (selectedIndex == -1) {
                return null;
            }

            return composites.get(selectedIndex);
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
            FragmentCompositeList.this.composites = composites;
            listAdapter = new CompositeListAdapter(getActivity(), composites);
            compositeList.setAdapter(listAdapter);

            loader.setVisibility(View.GONE);

            if (composites.size() > 0) {
                compositeList.setVisibility(View.VISIBLE);
                noComposites.setVisibility(View.GONE);
            } else {
                noComposites.setVisibility(View.VISIBLE);
                compositeList.setVisibility(View.GONE);
            }
        }
    }
}