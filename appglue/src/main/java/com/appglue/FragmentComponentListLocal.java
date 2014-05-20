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

import com.appglue.datatypes.IOType;
import com.appglue.description.ServiceDescription;
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


public class FragmentComponentListLocal extends FragmentComponentList
{	
	public FragmentComponentListLocal()
	{
		super();
	}
	
	private boolean triggers;
	private boolean hasInputs;
	private boolean hasOutputs;
	private boolean matching;
	private int position;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle)
	{
		View v = super.onCreateView(inflater, container, icicle);
		
		((TextView) v.findViewById(R.id.simple_list_none)).setText("No components on this device! (You shouldn't be seeing this.... What have you done!?)");
		
		registry = Registry.getInstance(parent);
		
		Bundle args = getArguments();
		triggers = args.getBoolean(TRIGGERS_ONLY, false);
		hasInputs = args.getBoolean(HAS_INPUTS, false);
		hasOutputs = args.getBoolean(HAS_OUTPUTS, false);
		matching = args.getBoolean(MATCHING, false);
		position = args.getInt(POSITION, -1);
		
		ComponentLoaderTask bl = new ComponentLoaderTask();
		bl.execute();
		
		serviceListView.setOnItemLongClickListener(new OnItemLongClickListener() 
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View v, int position, long id) 
			{
				Intent intent = new Intent(parent, ActivityComponent.class);
				intent.putExtra(SERVICE_TYPE, ServiceType.DEVICE.index);
				intent.putExtra(CLASSNAME, services.get(position).getClassName());
				intent.putExtra(JUST_A_LIST, parent.justAList());
				parent.startActivityForResult(intent, SERVICE_REQUEST);
				return true;
			}
		});
		
		serviceListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View v, int position, long id)
			{
				parent.chosenItem(services.get(position).getClassName());
			}
		});
		
		return v;
	}
	
	public void onActivityCreated(Bundle icicle)
	{
		super.onActivityCreated(icicle);
	}
	
	private class ComponentLoaderTask extends AsyncTask<Void, Void, ArrayList<ServiceDescription>>
	{

		@Override
		protected ArrayList<ServiceDescription> doInBackground(Void... params) 
		{
			ArrayList<ServiceDescription> services = new ArrayList<ServiceDescription>();
			
			if(triggers)
			{
				services = registry.getTriggers();
			}
			else if(matching)
			{
				// Need to get the matching components
				if(registry.getService() != null)
				{
					// Get the current list, find the components before and/or after where we're trying to add
					ArrayList<ServiceDescription> currentComponents = registry.getService().getComponents();
					Log.w(TAG, "position is " + position + " and size is" + currentComponents.size());
					
					if(position == -1)
					{
						services = registry.getOutputOnlyComponents();
					}
					else
					{
						ServiceDescription prior = position == 0 ? null : currentComponents.get(position - 1);
						ServiceDescription next = position < currentComponents.size() - 1? currentComponents.get(position + 1) : null;
						
						if(prior == null && next == null)
						{
							// Both null, get everything
							services = registry.getComponents();
						}
						else if(prior != null && next == null)
						{
							// Prior is alive, next isn't, just use priors outputs
							
							// If there's nothing to match, just get everything
							if(prior.hasOutputs())
								services = registry.getMatchingForOutputs(prior);
							else
								services = registry.getComponents();
						}
						else {
                            if (prior == null && next != null) {
                                // Prior is dead, next is alive, just use next's inputs

                                // IF there's nothing to match, just get everything
                                if (next.hasInputs())
                                    services = registry.getMatchingForInputs(next);
                                else
                                    services = registry.getComponents();
                            } else {
                                // Both are alive, so get them based on the outputs and then filter on the inputs
                                if (prior.hasOutputs()) {
                                    services = registry.getMatchingForOutputs(prior);

                                    // So here we have a list of services whose inputs match the outputs of the prior one

                                    if (next.hasInputs()) {
                                        // We need to find the types of the inputs of the next one and compare these with the outputs we've got in our service list

                                        // Then filter it based on the inputs of the other one
                                        ArrayList<ServiceIO> nextInputs = next.getInputs();
                                        HashMap<String, Integer> types = new HashMap<String, Integer>();
                                        for (ServiceIO nextInput : nextInputs) {
                                            IOType type = nextInput.getType();
                                            if (!types.containsKey(type.getClassName()))
                                                types.put(type.getClassName(), type.getID());
                                        }

                                        for (int i = 0; i < services.size(); ) {
                                            ArrayList<ServiceIO> outputs = services.get(i).getOutputs();
                                            boolean match = false;

                                            for (ServiceIO output : outputs) {
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
                                    services = registry.getComponents();


                            }
                        }
					}
				}
			}
			else if(!hasInputs)
			{
				// Output only
				services = registry.getOutputOnlyComponents();
			}
			else if(!hasOutputs)
			{
				// Input only
				services = registry.getInputOnlyComponents();
			}
			else
			{
				// Everything
				services = registry.getComponents();
			}
			
			return services;
		}
		
		@Override
		protected void onPostExecute(ArrayList<ServiceDescription> components)
		{
			// Need to set the components to be on this and get rid of the loading spinner
			services = components;
			
			loader.setVisibility(View.GONE);
			
			if(services.size() > 0)
			{
				serviceListView.setVisibility(View.VISIBLE);
				AdapterComponentList adapter = new AdapterComponentList(parent, R.layout.component_list_item, services);
				serviceListView.setAdapter(adapter);
			}
			else
				noneFound.setVisibility(View.VISIBLE);
			
		}
		
	}
}
