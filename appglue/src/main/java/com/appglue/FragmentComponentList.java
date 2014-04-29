package com.appglue;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.appglue.description.ServiceDescription;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

public class FragmentComponentList extends Fragment
{
	protected ListView serviceListView;
	protected TextView noneFound;
	protected ImageView loader;
	
	protected ActivityComponentList parent;

    // TODO Apparently adapter is never assigned, this is a problem
	private AdapterComponentList adapter;
	
	protected Registry registry;
	
	protected String name;
	
	protected ArrayList<ServiceDescription> services;
	protected ArrayList<ServiceDescription> renderServices;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle)
	{
		View v = inflater.inflate(R.layout.fragment_component_list, container, false);
		
		serviceListView = (ListView) v.findViewById(R.id.simple_list);
		serviceListView.setDivider(null); 
		serviceListView.setDividerHeight(0);
		
		loader = (ImageView) v.findViewById(R.id.loading_spinner);
		noneFound = (TextView) v.findViewById(R.id.simple_list_none);
		
		AnimationDrawable ad = (AnimationDrawable) loader.getBackground();
		ad.start();
		
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle icicle)
	{	
		super.onActivityCreated(icicle);
		
		this.parent = (ActivityComponentList) getActivity();
		
		renderServices = new ArrayList<ServiceDescription>();
		
//		// Remove triggers if it's not position 0
//		if(parent.getLastService() != null && services != null)
//		{
//			for(int i = 0; i < services.size(); i++)
//			{
//				if(services.get(i).getProcessType() != ProcessType.TRIGGER)
//				{
//					renderServices.add(services.get(i));
//				}
//			}
//		}
//		else
//		{
//			renderServices = services == null ? new ArrayList<ServiceDescription>() : services;
//		}
//			
//		if(renderServices.size() > 0)
//		{
//			
//		}
//		else
//		{
//			// Then we need to hide the list and show a text view to say there's nothing there
////			this.getView().findViewById(R.id.simple_list_none).setVisibility(View.VISIBLE);
//		}
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
//	public void update(ArrayList<ServiceDescription> components)
//	{
//		this.services = components;
//		adapter.clear();
//
//		adapter = new AdapterComponentList(parent, R.layout.component_list_item, services);
//		serviceListView.setAdapter(adapter);
//	}
	
	public AdapterComponentList getAdapter()
	{
		return adapter;
	}
}