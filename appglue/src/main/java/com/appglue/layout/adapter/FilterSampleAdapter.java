package com.appglue.layout.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.appglue.description.IOValue;

import java.util.ArrayList;

public class FilterSampleAdapter extends ArrayAdapter<IOValue>
{
	private ArrayList<IOValue> values;
	private Activity activity;
	
	public FilterSampleAdapter(Activity context, ArrayList<IOValue> values)
	{
		super(context, android.R.layout.simple_dropdown_item_1line, values);

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
		
		IOValue item = values.get(position);
		
		TextView tv = (TextView) v.findViewById(android.R.id.text1);
		tv.setText(item.name);
		
		return v;
	}
}