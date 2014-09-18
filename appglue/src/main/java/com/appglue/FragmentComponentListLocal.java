package com.appglue;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;

import com.appglue.description.datatypes.IOType;
import com.appglue.description.ServiceDescription;
import com.appglue.engine.description.ComponentService;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;
import java.util.HashMap;

import static com.appglue.Constants.CLASSNAME;
import static com.appglue.Constants.POSITION;
import static com.appglue.Constants.SERVICE_TYPE;
import static com.appglue.Constants.ServiceType;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.HAS_INPUTS;
import static com.appglue.library.AppGlueConstants.HAS_OUTPUTS;
import static com.appglue.library.AppGlueConstants.JUST_A_LIST;
import static com.appglue.library.AppGlueConstants.MATCHING;
import static com.appglue.library.AppGlueConstants.SERVICE_REQUEST;
import static com.appglue.library.AppGlueConstants.TRIGGERS_ONLY;


public class FragmentComponentListLocal extends FragmentComponentList {
    public FragmentComponentListLocal() {
        super();
    }

    private boolean triggers;
    private boolean hasInputs;
    private boolean hasOutputs;
    private boolean matching;
    private int position;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        View v = super.onCreateView(inflater, container, icicle);

        ((TextView) v.findViewById(R.id.simple_list_none)).setText("No components on this device! (You shouldn't be seeing this.... What have you done!?)");

        registry = Registry.getInstance(getActivity());

        Bundle args = getArguments();
        triggers = args.getBoolean(TRIGGERS_ONLY, false);
        hasInputs = args.getBoolean(HAS_INPUTS, false);
        hasOutputs = args.getBoolean(HAS_OUTPUTS, false);
        justList = args.getBoolean(JUST_A_LIST, false);
        matching = args.getBoolean(MATCHING, false);
        position = args.getInt(POSITION, -1);

        ComponentLoaderTask bl = new ComponentLoaderTask();
        bl.execute();

//        serviceListView.setOnItemLongClickListener(new OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> adapterView, View v, int position, long id) {
//                Intent intent = new Intent(getActivity(), ActivityComponent.class);
//                intent.putExtra(SERVICE_TYPE, ServiceType.DEVICE.index);
//                intent.putExtra(CLASSNAME, services.get(position).getClassName());
//                intent.putExtra(JUST_A_LIST, justList);
//
//                if(!homeParent) {
//                    getActivity().startActivityForResult(intent, SERVICE_REQUEST);
//                }

//                return true;
//            }
//        });

//        serviceListView.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
//            if (!justList) {
//                ((ActivityWiring) getActivity()).chooseItem(services.get(position).getClassName());
//            } else {
//                getParentFragment().showComponent(services.get(position))
//            }
//            }
//        });

        return v;
    }

    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);
    }

    private class ComponentLoaderTask extends AsyncTask<Void, Void, ArrayList<ServiceDescription>> {

        @Override
        protected ArrayList<ServiceDescription> doInBackground(Void... params) {
            ArrayList<ServiceDescription> services = new ArrayList<ServiceDescription>();

            if (triggers) {
                services = registry.getTriggers();
            } else if (matching) {
                // Need to get the matching components
                if (registry.getService() != null) {
                    // Get the current list, find the components before and/or after where we're trying to add
                    // FIXME Below needs to work with SparseArray

                    ArrayList<ComponentService> currentComponents = registry.getService().getComponentsAL();
                    Log.w(TAG, "position is " + position + " and size is" + currentComponents.size());

                    if (position == -1) {
                        services = registry.getOutputOnlyComponents();
                    } else {
                        ServiceDescription prior = position == 0 ? null : currentComponents.get(position - 1).getDescription();
                        ServiceDescription next = position < currentComponents.size() - 1 ? currentComponents.get(position + 1).getDescription() : null;

                        if (prior == null && next == null) {
                            // Both null, get everything
                            services = registry.getAllServiceDescriptions();
                        } else if (prior != null && next == null) {
                            // Prior is alive, next isn't, just use priors getOutputs

                            // If there's nothing to match, just get everything
                            if (prior.hasOutputs())
                                services = registry.getMatchingForOutputs(prior);
                            else
                                services = registry.getAllServiceDescriptions();
                        } else {
                            if (prior == null && next != null) {
                                // Prior is dead, next is alive, just use next's getInputs

                                // IF there's nothing to match, just get everything
                                if (next.hasInputs())
                                    services = registry.getMatchingForInputs(next);
                                else
                                    services = registry.getAllServiceDescriptions();
                            } else {
                                // Both are alive, so get them based on the getOutputs and then filter on the getInputs
                                if (prior.hasOutputs()) {
                                    services = registry.getMatchingForOutputs(prior);

                                    // So here we have a list of services whose getInputs match the getOutputs of the prior one

                                    if (next.hasInputs()) {
                                        // We need to find the types of the getInputs of the next one and compare these with the getOutputs we've got in our service list

                                        // Then filter it based on the getInputs of the other one
                                        ArrayList<IODescription> nextInputs = next.getInputs();
                                        HashMap<String, Long> types = new HashMap<String, Long>();
                                        for (IODescription nextInput : nextInputs) {
                                            IOType type = nextInput.getType();
                                            if (!types.containsKey(type.getClassName()))
                                                types.put(type.getClassName(), type.getID());
                                        }

                                        for (int i = 0; i < services.size(); ) {
                                            ArrayList<IODescription> outputs = services.get(i).getOutputs();
                                            boolean match = false;

                                            for (IODescription output : outputs) {
                                                if (types.containsKey(output.getType().getClassName()))
                                                    match = true;
                                            }

                                            if (match)
                                                i++;
                                            else
                                                services.remove(i);
                                        }
                                    }
                                } else if (next.hasInputs()) {
                                    services = registry.getMatchingForInputs(next);
                                } else
                                    services = registry.getAllServiceDescriptions();


                            }
                        }
                    }
                }
            } else if (!hasInputs) {
                // Output only
                services = registry.getOutputOnlyComponents();
            } else if (!hasOutputs) {
                // Input only
                services = registry.getInputOnlyComponents();
            } else {
                // Everything
                services = registry.getAllServiceDescriptions();
            }

            return services;
        }

        @Override
        protected void onPostExecute(ArrayList<ServiceDescription> components) {
            // Need to set the components to be on this and get rid of the loading spinner
            services = components;

            loader.setVisibility(View.GONE);

            if (services.size() > 0) {
                serviceListView.setVisibility(View.VISIBLE);
                AdapterComponentList adapter = new AdapterComponentList(getActivity(), services, (FragmentComponentListPager) getParentFragment());
                serviceListView.setAdapter(adapter);
            } else
                noneFound.setVisibility(View.VISIBLE);

        }

    }
}
