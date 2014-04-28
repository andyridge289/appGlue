package com.appglue;

import static com.appglue.Constants.TAG;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.appglue.Constants.ProcessType;
import com.appglue.Constants.ServiceType;
import com.appglue.description.ServiceDescription;
import com.appglue.library.LocalStorage;

public class AdapterComponentList extends ArrayAdapter<ServiceDescription>
{
	private ArrayList<ServiceDescription> originalItems;
	private ArrayList<ServiceDescription> items;
	private ActivityComponentList parent;
	
	private LocalStorage localStorage;
	
	private ComponentFilter filter;
	private final Object lock = new Object();
	
	public AdapterComponentList(Context context, int textViewResourceId, ArrayList<ServiceDescription> items)
	{
		super(context, textViewResourceId, items);
		
		localStorage = LocalStorage.getInstance();
		
		this.parent = (ActivityComponentList) context;
		this.items = items;
		cloneItems();
	}
	
	private void cloneItems()
	{
		originalItems = new ArrayList<ServiceDescription>();
		int size = items.size();
		
		for(int i = 0; i < size; i++)
		{
			originalItems.add(items.get(i));
		}
	}
	
	@Override
	public int getCount()
	{
		synchronized(lock)
		{
			return items != null ? items.size() : 0;
		}
	}
	
	@Override
    public ServiceDescription getItem(int item) 
	{
		ServiceDescription gi = null;
        synchronized(lock) 
        {
                gi = items != null ? items.get(item) : null;
        }
        return gi;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup)
	{
		View v = convertView;
		LayoutInflater vi = (LayoutInflater) parent.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if(v == null)
		{
			v = vi.inflate(R.layout.component_list_item, null);
		}
		
		ServiceDescription sd = null;
		synchronized(lock)
		{	
			sd = items.get(position);
		}
		
		if(sd == null)
			return v;
		
		ImageView icon = (ImageView) v.findViewById(R.id.simple_list_icon);
		
		if(sd.getServiceType() == ServiceType.IN_APP)
			icon.setImageResource(R.drawable.icon);
		else if(sd.getServiceType() == ServiceType.LOCAL)
		{
			if(sd.getApp() == null)
				icon.setImageResource(R.drawable.ic_lock_silent_mode_vibrate);
			else
			{
				try 
				{
					Drawable d = new BitmapDrawable(parent.getResources(), localStorage.readIcon(sd.getApp().getIconLocation()));
					icon.setImageDrawable(d);
				}
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
		}	
		else
			icon.setImageResource(R.drawable.ic_menu_upload);
			
		TextView serviceName = (TextView) v.findViewById(R.id.service_name);
		serviceName.setText(sd.getName());
		
		if(sd.getProcessType() == ProcessType.TRIGGER)
		{
			v.findViewById(R.id.comp_item_trigger).setVisibility(View.VISIBLE);
			v.findViewById(R.id.comp_item_trigger_text).setVisibility(View.VISIBLE);
		}
		else
		{
			v.findViewById(R.id.comp_item_trigger).setVisibility(View.GONE);
			v.findViewById(R.id.comp_item_trigger_text).setVisibility(View.GONE);
		}
		
		if(sd.getProcessType() == ProcessType.FILTER)
		{
			v.findViewById(R.id.comp_item_filter).setVisibility(View.VISIBLE);
			v.findViewById(R.id.comp_item_filter_text).setVisibility(View.VISIBLE);
		}
		else
		{
			v.findViewById(R.id.comp_item_filter).setVisibility(View.GONE);
			v.findViewById(R.id.comp_item_filter_text).setVisibility(View.GONE);
		}
		
		if(sd.hasInputs())
		{
			v.findViewById(R.id.comp_item_inputs).setBackgroundResource(R.drawable.has_io);
		}
		else
		{
			v.findViewById(R.id.comp_item_inputs).setBackgroundResource(R.drawable.inputs);
		}
		
		if(sd.hasOutputs())
		{
			v.findViewById(R.id.comp_item_outputs).setBackgroundResource(R.drawable.has_io);
		}
		else
		{
			v.findViewById(R.id.comp_item_outputs).setBackgroundResource(R.drawable.outputs);
		}
		
		return v;
	}
	
	public Filter getFilter()
	{
		if(filter == null)
		{
			filter = new ComponentFilter();
		}
		
		return filter;
	}
	
	private class ComponentFilter extends Filter
	{
		@Override
		protected FilterResults performFiltering(CharSequence constraint) 
		{
			final FilterResults results = new FilterResults();
			
			if(constraint == null || constraint.length() == 0)
			{
//				Log.e(TAG, "Constraint dead/empty!");
				synchronized(lock)
				{
					// This might be a bit redundant...?
					final ArrayList<ServiceDescription> filteredItems = new ArrayList<ServiceDescription>();
					final ArrayList<ServiceDescription> localItems = new ArrayList<ServiceDescription>();
					localItems.addAll(originalItems);
					
					if(!parent.areAnySet())
					{
						// None of the flags are set, just add them all!
						filteredItems.addAll(localItems);
						
					}
					else
					{
						for(int i = 0; i < localItems.size(); i++)
						{
							ServiceDescription sd = localItems.get(i);

							// Work out whether we should add it or not
							if(parent.isTriggerSet() && sd.getProcessType() == ProcessType.TRIGGER)
							{
								// It doesn't matter what the other ones are set, it needs to be in there anyway
								filteredItems.add(sd);
								continue;
							}
							
							if(parent.isFilterSet() && sd.getProcessType() == ProcessType.FILTER)
							{
								filteredItems.add(sd);
								continue;
							}
							
							if(parent.hasInputSet() && sd.hasInputs())
							{
								filteredItems.add(sd);
								continue;
							}
							
							if(parent.hasOutputSet() && sd.hasOutputs())
							{
								filteredItems.add(sd);
								continue;
							}

						}

					}
					
					results.values = filteredItems;
					results.count = filteredItems.size();
				}
			}
			else
			{
				synchronized(lock)
				{
					final String data = constraint.toString().toLowerCase(Locale.getDefault());
					
					// Set up the ArrayLists we need to do the filtering
					final ArrayList<ServiceDescription> filteredItems = new ArrayList<ServiceDescription>();
					final ArrayList<ServiceDescription> localItems = new ArrayList<ServiceDescription>();
					localItems.addAll(originalItems);
					int count = localItems.size();
					
					for(int i = 0; i < count; i++)
					{
						ServiceDescription item = localItems.get(i);
						if(matches(item, data))
						{
							filteredItems.add(item);
						}
					}
				
					results.values = filteredItems;
					results.count = filteredItems.size();
				}
			}
			
			return results;
		}
		
		private boolean matches(ServiceDescription item, String term)
		{
			String name = item.getName().toLowerCase(Locale.getDefault());
			String description = item.getDescription().toLowerCase(Locale.getDefault());
			
			if(name.contains(term))
				return true;
				
			if(description.contains(term))
				return true;	
			
			ArrayList<Tag> tags = item.getTags();
			Log.w(TAG, item.getName() + " has " + tags.size() + " tags");
			for(int j = 0; j < tags.size(); j++)
			{
				String tagName = tags.get(j).getName().toLowerCase(Locale.US);
				Log.d(TAG, "Comparing " + tagName + " to " + term);
				if(tagName.contains(term))
				{
					return true;
				}
			}
			
			
			return false;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) 
		{
			synchronized(lock)
			{
				final ArrayList<ServiceDescription> localItems = (ArrayList<ServiceDescription>) results.values;
				clear();
				
				for(int i = 0; i < localItems.size(); i++)
				{
					AdapterComponentList.this.add(localItems.get(i));
				}
			}
			
			notifyDataSetChanged();
		}
		
	}
}
