package com.appglue;


import static com.appglue.library.AppGlueConstants.JUST_A_LIST;
import static com.appglue.Constants.CLASSNAME;
import static com.appglue.Constants.SERVICE_TYPE;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.appglue.Constants.ServiceType;

public class DEADFragmentComponentListRemote extends FragmentComponentList
{	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle)
	{
		View v = super.onCreateView(inflater, container, icicle);
		((TextView) v.findViewById(R.id.simple_list_none)).setText("No remote components");
		return v;
	}
	
	public void onActivityCreated(Bundle icicle)
	{
		super.onActivityCreated(icicle);
		
		serviceListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View v, int position, long id)
			{
				Intent intent = new Intent(parent, ActivityComponent.class);
				intent.putExtra(SERVICE_TYPE, ServiceType.REMOTE.index);
				intent.putExtra(CLASSNAME, services.get(position).getClassName());
				intent.putExtra(JUST_A_LIST, parent.justAList());
				parent.startActivityForResult(intent, 0);
			}
		});
	}
}
