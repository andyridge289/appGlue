package com.appglue;


import static com.appglue.library.AppGlueConstants.*;
import static com.appglue.Constants.CLASSNAME;
import static com.appglue.Constants.SERVICE_TYPE;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;

import com.appglue.Constants.ServiceType;
import com.appglue.serviceregistry.Registry;


public class DEADFragmentComponentListLocal extends FragmentComponentList
{	
	private int flag;
	
	public void setFlag(int flag)
	{
		this.flag = flag;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle)
	{
		View v = super.onCreateView(inflater, container, icicle);
		((TextView) v.findViewById(R.id.simple_list_none)).setText("No components on this device! (You shouldn't be seeing this.... What have you done!?)");
		return v;
	}
	
	public void onActivityCreated(Bundle icicle)
	{
		registry = Registry.getInstance(parent);
		services = registry.getAllDeviceServices();

		super.onActivityCreated(icicle);
		
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
				parent.chosenItem(services.get(position).getClassName(), ServiceType.DEVICE);
			}
		});
	}
}
