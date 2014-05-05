package com.appglue;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import static com.appglue.Constants.TAG;

public class AdapterComponentListSearch extends AdapterComponentList
{
    private FragmentComponentListSearch searchFragment;

	private ComponentFilter filter;
	private final Object lock = new Object();

	public AdapterComponentListSearch(Context context, int textViewResourceId, ArrayList<ServiceDescription> items,
                                      FragmentComponentListSearch searchFragment)
	{
		super(context, textViewResourceId, items);
        this.searchFragment = searchFragment;
	}

	public Filter getFilter()
	{
		if(filter == null)
		{
			filter = new ComponentFilter();
		}

		return filter;
	}

    // FIXME Enable the dedicated search fragment -- search bar
	private class ComponentFilter extends Filter
	{
		@Override
		protected FilterResults performFiltering(CharSequence constraint) 
		{
			final FilterResults results = new FilterResults();
			
			if(constraint == null || constraint.length() == 0)
			{
				synchronized(lock)
				{
					// This might be a bit redundant...?
					final ArrayList<ServiceDescription> filteredItems = new ArrayList<ServiceDescription>();
					final ArrayList<ServiceDescription> localItems = new ArrayList<ServiceDescription>();
					localItems.addAll(originalItems);
                    filteredItems.addAll(localItems);
					
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

                    for (ServiceDescription item : localItems) {
                        if (matches(item, data)) {
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
            for (Tag tag : tags) {
                String tagName = tag.getName().toLowerCase(Locale.US);
                Log.d(TAG, "Comparing " + tagName + " to " + term);
                if (tagName.contains(term)) {
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

                for (ServiceDescription localItem : localItems) {
                    AdapterComponentListSearch.this.add(localItem);
                }
			}
			
			notifyDataSetChanged();
		}
		
	}
}
