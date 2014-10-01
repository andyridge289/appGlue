package com.appglue;

import android.app.Activity;
import android.app.Fragment;
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

import com.appglue.description.ServiceDescription;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

import static com.appglue.Constants.CLASSNAME;
import static com.appglue.Constants.INDEX;
import static com.appglue.Constants.POSITION;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.NO_TRIGGERS;
import static com.appglue.library.AppGlueConstants.STORY_MODE;

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
        componentList.setAdapter(new ComponentAdapter(getActivity(), registry.getCurrent().getComponentsAL()));
        
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
			intent.putExtra(POSITION, registry.getCurrent().getComponents().size());
			
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
			CompositeService cs = registry.getCurrent();
			
			String className = intent.getStringExtra(CLASSNAME);
			final int position = intent.getIntExtra(INDEX, -1);

//			if(position == -1)
//			{
//				cs.addServiceDescription(0, registry.getServiceDescription(className));
//			}
//			else
//			{
//				cs.addServiceDescription(position, registry.getServiceDescription(className));
//			}
            // Fix the above
			
			Intent i = new Intent(getActivity(), ActivityStoryParameters.class);
			i.putExtra(CLASSNAME, className);
			i.putExtra(POSITION, position);
			startActivity(i);
			
			getActivity().finish();
		}
		else if(requestCode == STORY_MODE && resultCode == Activity.RESULT_CANCELED)
		{
			Log.d(TAG, "Story mode cancelled!");
		}
	}
	
	private class ComponentAdapter extends ArrayAdapter<ComponentService>
	{

		public ComponentAdapter(Context context, ArrayList<ComponentService> objects) {
			super(context, R.layout.list_item_storycomponent, objects);
			
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
			
			ServiceDescription component = getItem(position).getDescription();
			
			TextView nameText = (TextView) v.findViewById(R.id.story_component_name);
			nameText.setText(component.getName());
			
			ArrayList<IODescription> inputs = component.getInputs();
			ArrayList<IODescription> outputs = component.getOutputs();
			
			LinearLayout in = (LinearLayout) v.findViewById(R.id.storycomponent_input_list);
			in.removeAllViews();
			
			LinearLayout out = (LinearLayout) v.findViewById(R.id.storycomponent_output_list);
			out.removeAllViews();
			
			// Show placeholders if they aren't set
			if(inputs == null || inputs.size() == 0)
			{
				// Just add a placeholder with no name and make the value disappear
                Log.d(TAG, "Should apparently be adding a placeholder");
				View vv = vi.inflate(R.layout.list_item_storycomponent_io, null);
				TextView ioName = (TextView) vv.findViewById(R.id.story_io_name);
				ioName.setText("Placeholder");

				TextView ioValue = (TextView) vv.findViewById(R.id.story_io_value);
				ioValue.setVisibility(View.GONE);

				in.addView(vv);
			}
			else
			{
				for(int i = 0; i < component.getInputs().size(); i++)
				{
					IODescription sio = component.getInputs().get(i);
					
					ImageView iv = new ImageView(this.getContext());
    				iv.setImageResource(R.drawable.empty_input);
					in.addView(iv);
				}
			}
			
			// Setting getInputs needs to set text to EditFilter (I don't remember what this means)
			
			// Show placeholders if they aren't set
			if(outputs == null || outputs.size() == 0)
			{ 
//				// Just add a placeholder
                Log.d(TAG, "Should apparently be adding a placeholder?");
				View vv = vi.inflate(R.layout.list_item_storycomponent_io, null);
				TextView ioName = (TextView) vv.findViewById(R.id.story_io_name);
				TextView ioValue = (TextView) vv.findViewById(R.id.story_io_value);
				ioName.setText("Placeholder");
				ioValue.setVisibility(View.GONE);
				out.addView(vv);
			}
			else
			{   
				for(int i = 0; i < component.getOutputs().size(); i++)
				{
					IODescription sio = component.getOutputs().get(i);
					
					ImageView iv = new ImageView(this.getContext());
                    iv.setImageResource(R.drawable.empty_input);
					out.addView(iv);
				}
			}
			
			return v;
		}
		
	}
}
