package com.appglue;

import java.util.ArrayList;
import java.util.List;

import static com.appglue.Constants.*;
import static com.appglue.library.AppGlueConstants.*;

import com.appglue.description.ServiceDescription;
import com.appglue.engine.CompositeService;
import com.appglue.serviceregistry.Registry;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class FragmentStoryComponents extends Fragment implements OnClickListener
{
	private Registry registry;
	
	private LinearLayout addLayout;
	private LinearLayout doneLayout;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
        View v =  inflater.inflate(R.layout.fragment_story_components, container, false);
        
        registry = Registry.getInstance(getActivity());
        
        ListView componentList = (ListView) v.findViewById(R.id.story_composite_components);
        componentList.setAdapter(new ComponentAdapter(getActivity(), R.layout.list_item_storycomponent, registry.getService().getComponents()));
        
        addLayout = (LinearLayout) v.findViewById(R.id.choice_carry_on);
        addLayout.setOnClickListener(this);
        
        doneLayout = (LinearLayout) v.findViewById(R.id.choice_done);
        doneLayout.setOnClickListener(this);
        
        return v;
    }
	
	public void onClick(View v)
	{
		if(v.equals(addLayout))
		{
			Intent intent = new Intent(getActivity(), ActivityComponentList.class);
			intent.putExtra(NO_TRIGGERS, true);
			intent.putExtra(POSITION, registry.getService().getComponents().size());
			
			// Send the position to make the component list start in a different place.
			startActivityForResult(intent, STORY_MODE);
		}
		else if(v.equals(doneLayout))
		{
			((ActivityStoryComponents) getActivity()).doneBuilding();
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		Registry registry = Registry.getInstance(getActivity());
		Log.w(TAG, "Story 2: RESULT!!!");
		if(requestCode == STORY_MODE && resultCode == Activity.RESULT_OK)
		{
			// Then we need to go to add services
			CompositeService cs = registry.getService();
			
			String className = intent.getStringExtra(CLASSNAME);
			final int position = intent.getIntExtra(INDEX, -1);

			if(position == -1)
			{
				cs.addComponent(registry.getAtomic(className));
			}
			else
			{
				cs.addComponent(position, registry.getAtomic(className));
			}
			
			Intent i = new Intent(getActivity(), ActivityStoryParameters.class);
			i.putExtra(CLASSNAME, className);
			i.putExtra(POSITION, position);
			startActivity(i);
			
			getActivity().finish();
		}
		else if(requestCode == STORY_MODE && resultCode == Activity.RESULT_CANCELED)
		{
			
		}
	}
	
	private class ComponentAdapter extends ArrayAdapter<ServiceDescription>
	{

		public ComponentAdapter(Context context, int textViewResourceId, ArrayList<ServiceDescription> objects) {
			super(context, textViewResourceId, objects);
			
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup viewGroup)
		{
			View v = convertView;
			LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			if(v == null)
			{
				v = vi.inflate(R.layout.list_item_storycomponent, null);
			}
			
			ServiceDescription component = getItem(position);
			
			TextView nameText = (TextView) v.findViewById(R.id.story_component_name);
			nameText.setText(component.getName());
			
			ArrayList<ServiceIO> inputs = component.getInputs();
			ArrayList<ServiceIO> outputs = component.getOutputs();
			
			LinearLayout in = (LinearLayout) v.findViewById(R.id.input_list);
			in.removeAllViews();
			
			LinearLayout out = (LinearLayout) v.findViewById(R.id.output_list);
			out.removeAllViews();
			
			// Show placeholders if they aren't set
			if(inputs == null || inputs.size() == 0)
			{
//				// Just add a placeholder with no name and make the value disappear
//				View vv = vi.inflate(R.layout.list_item_storycomponent_io, null);
//				TextView ioName = (TextView) vv.findViewById(R.id.story_io_name);
//				ioName.setText("None");
//				
//				TextView ioValue = (TextView) vv.findViewById(R.id.story_io_value);
//				ioValue.setVisibility(View.GONE);
//				
//				in.addView(vv);
			}
			else
			{
				for(int i = 0; i < component.getInputs().size(); i++)
				{
					ServiceIO sio = component.getInputs().get(i);
					
					ImageView iv = new ImageView(this.getContext());
					
					if(sio.hasValue())
						iv.setImageResource(R.drawable.empty_input);
					else
						iv.setImageResource(R.drawable.empty_input);
					
					in.addView(iv);
				}
			}
			
			// FIXME Setting inputs needs to set text to EditFilter
			
			// Show placeholders if they aren't set
			if(outputs == null || outputs.size() == 0)
			{ 
//				// Just add a placeholder
//				View vv = vi.inflate(R.layout.list_item_storycomponent_io, null);
//				TextView ioName = (TextView) vv.findViewById(R.id.story_io_name);
//				TextView ioValue = (TextView) vv.findViewById(R.id.story_io_value);
//				ioName.setText("None");
//				ioValue.setVisibility(View.GONE);   
//				out.addView(vv);
			}
			else
			{   
				for(int i = 0; i < component.getOutputs().size(); i++)
				{
					ServiceIO sio = component.getOutputs().get(i);
					
					ImageView iv = new ImageView(this.getContext());
					
					if(sio.hasValue())
						iv.setImageResource(R.drawable.empty_input);
					else
						iv.setImageResource(R.drawable.empty_input);
					
					out.addView(iv);
				}
			}
			
			return v;
		}
		
	}
	
//	private class IOAdapter extends ArrayAdapter<ServiceIO>
//	{
//		private boolean isInput;
//		
//		public IOAdapter(Context context, int textViewResourceId, ArrayList<ServiceIO> objects, boolean isInput) {
//			super(context, textViewResourceId, objects);
//			this.isInput = isInput;
//		}
//		
//		@Override
//		public View getView(int position, View convertView, ViewGroup viewGroup)
//		{
//			View v = convertView;
//			LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//			
//			if(v == null)
//			{
//				v = vi.inflate(R.layout.list_item_storycomponent_io, null);
//			}
//			
//			ServiceIO component = getItem(position);
//			
//			TextView nameText = (TextView) v.findViewById(R.id.story_io_name);
//			nameText.setText(component.getFriendlyName());
//			
//			
//			
//			// FIXME Put what the value is
//			
//			return v;
//		}
//	}
}
