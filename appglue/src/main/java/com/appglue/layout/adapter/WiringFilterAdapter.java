package com.appglue.layout.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.appglue.library.IOFilter.FilterValue;

public class WiringFilterAdapter extends ArrayAdapter<FilterValue>
{
	private FilterValue[] values;
	private Activity activity;
	
	public WiringFilterAdapter(Activity context, int resource, FilterValue[] values) 
	{
		super(context, resource, values);
		
		this.activity = context;
		this.values = values;
	}
	
	public View getView(int position, View v, ViewGroup parent)
	{
		if(v == null)
		{
			LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(android.R.layout.simple_dropdown_item_1line, null);
		}
		
		FilterValue item = values[position];
		
		TextView tv = (TextView) v.findViewById(android.R.id.text1);
		tv.setText(item.text);
		
		return v;
	}
}
