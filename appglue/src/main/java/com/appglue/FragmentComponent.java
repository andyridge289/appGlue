package com.appglue;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.appglue.description.ServiceDescription;
import com.appglue.engine.description.ComponentService;
import com.appglue.library.AppGlueLibrary;
import com.appglue.library.LocalStorage;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

import static com.appglue.Constants.CLASSNAME;
import static com.appglue.Constants.TAG;

public class FragmentComponent extends Fragment {

    private TextView componentName;
    private TextView componentDescription;

    private ImageView appIcon;
    private TextView appName;

    private TextView viewAppButton;
    private TextView launchAppButton;

    private ListView inputList;
    private ListView outputList;

    private TextView noInputs;
    private TextView noOutputs;

    private LinearLayout flagContainer;

    private ServiceDescription sd;

    private Registry registry;

    public static Fragment create() {
        return new FragmentComponent();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        registry = Registry.getInstance(getActivity());

        if (getArguments() != null) {
            sd = registry.getServiceDescription(getArguments().getString(CLASSNAME));
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        View root = inflater.inflate(R.layout.fragment_component, container, false);

        ImageView componentIcon = (ImageView) root.findViewById(R.id.component_icon);

        componentName = (TextView) root.findViewById(R.id.component_name);
        componentDescription = (TextView) root.findViewById(R.id.component_description);

        appIcon = (ImageView) root.findViewById(R.id.component_app_icon);
        appName = (TextView) root.findViewById(R.id.component_app_name);

        viewAppButton = (TextView) root.findViewById(R.id.component_view_app);
        launchAppButton = (TextView) root.findViewById(R.id.component_launch_app);

        inputList = (ListView) root.findViewById(R.id.component_input_list);
        outputList = (ListView) root.findViewById(R.id.component_output_list);

        noInputs = (TextView) root.findViewById(R.id.component_no_inputs);
        noOutputs = (TextView) root.findViewById(R.id.component_no_outputs);

        flagContainer = (LinearLayout) root.findViewById(R.id.flag_container);

        if (sd != null) {
            setupPage();
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

    public void setData(String className) {
        if (registry == null)
            registry = Registry.getInstance(getActivity());

        sd = registry.getServiceDescription(className);
    }

    // TODO There's a bug where setup page isn't being called

    private void setupPage() {

        componentName.setText(sd.getName());
        componentDescription.setText(sd.getDescription());

        LayoutInflater vi = getActivity().getLayoutInflater();
        AppGlueLibrary.addFlagsToLayout(flagContainer, sd, vi, true, true);

        if (sd.getApp() == null) {
            appName.setText("");
        } else {
            appName.setText(sd.getApp().getName());
        }

        if (sd.getApp() != null) {
            appIcon.setImageBitmap(LocalStorage.getInstance().readIcon(sd.getApp().iconLocation()));
            appIcon.setImageBitmap(LocalStorage.getInstance().readIcon(sd.getApp().iconLocation()));
        }

        ArrayList<IODescription> inputs = sd.getInputs();
        if (inputs == null || inputs.size() == 0) {
            inputList.setVisibility(View.GONE);
            noInputs.setVisibility(View.VISIBLE);
        } else {
            inputList.setAdapter(new IOAdapter(getActivity(), R.layout.list_item_wiring_in, inputs, true));
            noInputs.setVisibility(View.GONE);
        }

        ArrayList<IODescription> outputs = sd.getOutputs();
        if (outputs == null || outputs.size() == 0) {
            outputList.setVisibility(View.GONE);
            noOutputs.setVisibility(View.VISIBLE);
        } else {
            outputList.setAdapter(new IOAdapter(getActivity(), R.layout.list_item_wiring_out, outputs, false));
            noOutputs.setVisibility(View.GONE);
        }

        launchAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PackageManager pm = getActivity().getPackageManager();

                    if (pm != null) {
                        Intent i = pm.getLaunchIntentForPackage(sd.getApp().getPackageName());
                        getActivity().startActivity(i);
                    } else {
                        // Do something?
                        Log.e(TAG, "Couldn't launch app because the package manager is null: " + sd.getApp().getPackageName());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Trying to launch app? " + e.getMessage());
                }
            }
        });

        if (sd.getApp().getPackageName().contains("com.appglue")) {
            launchAppButton.setTextColor(getResources().getColor(R.color.hexCCC));
            launchAppButton.setEnabled(false);
        }

        viewAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FragmentComponents) getParentFragment()).showApp(sd.getApp().getPackageName());
            }
        });
    }

    public String getName() {
        return sd.getName();
    }

    public ServiceDescription getComponent() {
        return sd;
    }

    private class IOAdapter extends ArrayAdapter<IODescription> {
        private ArrayList<IODescription> items;
        private boolean inputs;

        public IOAdapter(Context context, int textViewResourceId, ArrayList<IODescription> items, boolean inputs) {
            super(context, textViewResourceId, items);

            this.items = items;
            this.inputs = inputs;
        }

        @SuppressLint("InflateParams")
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                if (inputs)
                    v = vi.inflate(R.layout.list_item_wiring_in, null);
                else
                    v = vi.inflate(R.layout.list_item_wiring_out, null);
            }

            if (v == null)
                return null;

            final IODescription io = items.get(position);

            ((TextView) v.findViewById(R.id.io_name)).setText(io.getFriendlyName());
            ((TextView) v.findViewById(R.id.io_type)).setText(io.getType().getName());

            // It needs to say whether it's mandatory or not
            if (io.isMandatory() && inputs) {
                v.findViewById(R.id.mandatory_text).setVisibility(View.VISIBLE);
            } else if (inputs) {
                v.findViewById(R.id.mandatory_text).setVisibility(View.GONE);
            }

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), io.getFriendlyName() + ": " + io.description(), Toast.LENGTH_LONG).show();
                }
            });

            return v;
        }
    }
}
