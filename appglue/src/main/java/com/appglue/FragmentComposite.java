package com.appglue;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.appglue.description.AppDescription;
import com.appglue.description.ServiceDescription;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.library.LocalStorage;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

import static com.appglue.library.AppGlueConstants.COMPOSITE_ID;

public class FragmentComposite extends Fragment {

    private ImageView compositeIcon;

    private TextView compositeName;
    private TextView compositeDescription;

    private SwitchCompat enabledSwitch;
    private ListView componentList;

    private LinearLayout contextBar;
    private View runButton;
    private View editButton;
    private View scheduleButton;
    private View shortcutButton;
    private View deleteButton;

    private CompositeService composite;

    private Registry registry;
    private LocalStorage localStorage;


    public FragmentComposite() {

    }

    public static Fragment create() {
        return new FragmentComposite();
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

        if (getArguments() != null) {
            composite = registry.getComposite(getArguments().getLong(COMPOSITE_ID));
        }

        if (icicle != null) {
            long compositeId = icicle.getLong(COMPOSITE_ID);
            this.composite = registry.getComposite(compositeId);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        View root = inflater.inflate(R.layout.fragment_composite, container, false);

        compositeIcon = (ImageView) root.findViewById(R.id.composite_icon);

        compositeName = (TextView) root.findViewById(R.id.composite_name);
        compositeDescription = (TextView) root.findViewById(R.id.composite_description);

        enabledSwitch = (SwitchCompat) root.findViewById(R.id.enabled_switch);
        CheckBox runningCheck = (CheckBox) root.findViewById(R.id.composite_running);

        contextBar = (LinearLayout) root.findViewById(R.id.context_toolbar);
        runButton = root.findViewById(R.id.composite_run);
        editButton = root.findViewById(R.id.composite_edit);
        scheduleButton = root.findViewById(R.id.composite_schedule);
        shortcutButton = root.findViewById(R.id.composite_shortcut);
        deleteButton = root.findViewById(R.id.composite_delete);

        componentList = (ListView) root.findViewById(R.id.composite_component_list);

        if (composite != null) {
            updatePage();
        }

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

    @Override
    public void onViewStateRestored(Bundle in) {
        super.onViewStateRestored(in);
    }

    @Override
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

    public void setData(long id) {
        if (registry == null) {
            registry = Registry.getInstance(getActivity());
        }

        composite = registry.getComposite(id);
    }

    private void updatePage() {

        if (composite == null) {
            return;
        }

        if (composite.getComponents().size() > 0) {
            AppDescription app = composite.getComponents().get(0).getDescription().getApp();
            if (app == null || app.iconLocation() == null) {
                compositeIcon.setBackgroundResource(R.drawable.icon);
            } else {
                String iconLocation = app.iconLocation();
                if (localStorage == null)
                    localStorage = LocalStorage.getInstance();
                Bitmap b = localStorage.readIcon(iconLocation);
                compositeIcon.setImageBitmap(b);
            }
        } else {
            compositeIcon.setBackgroundResource(R.drawable.icon);
        }

        compositeName.setText(composite.getName());
        compositeDescription.setText(composite.getDescription());

        contextBar.setBackgroundResource(composite.getColour(false));

        if (composite.isEnabled()) {
            runButton.setVisibility(View.VISIBLE);
            scheduleButton.setVisibility(View.VISIBLE);
            shortcutButton.setVisibility(View.VISIBLE);
        } else {
            runButton.setVisibility(View.GONE);
            scheduleButton.setVisibility(View.GONE);
            shortcutButton.setVisibility(View.GONE);
        }

        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ActivityAppGlue) getActivity()).run(composite);
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ActivityAppGlue) getActivity()).edit(composite);
            }
        });

        scheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ActivityAppGlue) getActivity()).schedule(composite);
            }
        });

        shortcutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ActivityAppGlue) getActivity()).createShortcut(composite);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Delete")
                        .setMessage(String.format("Are you sure you want to delete %s?", composite.getName()))
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (registry.delete(composite)) {
                                    Toast.makeText(getActivity(), String.format("\"%s\" deleted successfully", composite.getName()), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), String.format("Failed to delete \"%s\"", composite.getName()), Toast.LENGTH_SHORT).show();
                                }
                                getActivity().onBackPressed();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        enabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!composite.isEnabled() && composite.canEnable()) {
                    enabledSwitch.setChecked(false);
//                    enableDialog(item, enabledSwitch);
                } else if (!composite.canEnable()) {
                    Toast.makeText(getActivity(), "Can't enable until you fix " + composite.getName() + ". Edit it and fix these errors first!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        enabledSwitch.setChecked(composite.isEnabled());

        ArrayList<ComponentService> components = composite.getComponentsAL();
        componentList.setAdapter(new CompositeComponentAdapter(getActivity(), components));

        ((ActivityAppGlue) getActivity()).onSectionAttached(ActivityAppGlue.Page.HOME);
    }

    public String getName() {
        return composite.getName();
    }

    private class CompositeComponentAdapter extends ArrayAdapter<ComponentService> {
        private ArrayList<ComponentService> items;

        public CompositeComponentAdapter(Context context, ArrayList<ComponentService> items) {
            super(context, R.layout.li_component_in_composite, items);
            this.items = items;
        }

        @SuppressLint("InflateParams")
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (v == null) {
                v = vi.inflate(R.layout.li_component_in_composite, null);
            }

            if (v == null)
                return null;

            final ServiceDescription item = items.get(position).getDescription();

            ImageView icon = (ImageView) v.findViewById(R.id.component_icon);
            TextView name = (TextView) v.findViewById(R.id.component_name);

            try {
                String iconLocation = composite.getComponents().get(0).getDescription().getApp().iconLocation();
                Bitmap b = localStorage.readIcon(iconLocation);

                if (b != null)
                    icon.setImageBitmap(b);
            } catch (Exception e) {
                icon.setImageResource(R.drawable.icon);
            }

            name.setText(item.getName());

            if (item.hasInputs())
                v.findViewById(R.id.comp_item_inputs).setBackgroundResource(R.drawable.has_io);
            else
                v.findViewById(R.id.comp_item_inputs).setBackgroundResource(R.drawable.composite_input);

            if (item.hasOutputs())
                v.findViewById(R.id.comp_item_outputs).setBackgroundResource(R.drawable.has_io);
            else
                v.findViewById(R.id.comp_item_outputs).setBackgroundResource(R.drawable.composite_output);

//            v.setOnClickListener(new View.OnClickListener()
//            {
//                @Override
//                public void onClick(View v)
//                {
//                    Intent intent = new Intent(ActivityComposite.this, ActividtyComponent.class);
//                    intent.putExtra(CLASSNAME, item.getClassName());
//                    startActivity(intent);
//                }
//
//            });

            return v;
        }

    }

    public CompositeService getComposite() {
        return composite;
    }
}
