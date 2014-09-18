package com.appglue;

import android.content.Context;
import android.util.Log;
import android.widget.Filter;

import com.appglue.description.ServiceDescription;
import com.appglue.description.Tag;

import java.util.ArrayList;
import java.util.Locale;

import static com.appglue.Constants.TAG;

class AdapterComponentListSearch extends AdapterComponentList {

    private ComponentFilter filter;
    private final Object lock = new Object();

    public AdapterComponentListSearch(Context context, ArrayList<ServiceDescription> items,
                                      FragmentComponentListPager parentFragment) {
        super(context, items, parentFragment);
//        FragmentComponentListSearch searchFragment1 = searchFragment;
    }

    public Filter getFilter() {
        if (filter == null) {
            filter = new ComponentFilter();
        }

        return filter;
    }

    private class ComponentFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            final FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                synchronized (lock) {
                    // This might be a bit redundant...?
                    final ArrayList<ServiceDescription> filteredItems = new ArrayList<ServiceDescription>();
                    final ArrayList<ServiceDescription> localItems = new ArrayList<ServiceDescription>();
                    localItems.addAll(originalItems);
                    filteredItems.addAll(localItems);

                    results.values = filteredItems;
                    results.count = filteredItems.size();
                }
            } else {
                synchronized (lock) {
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

        private boolean matches(ServiceDescription item, String term) {
            String name = item.getName().toLowerCase(Locale.getDefault());
            String description = item.getDescription().toLowerCase(Locale.getDefault());

            if (name.contains(term))
                return true;

            if (description.contains(term))
                return true;

            ArrayList<Tag> tags = item.getTags();
            Log.w(TAG, item.getName() + " has " + tags.size() + " tags");
            for (Tag tag : tags) {
                String tagName = tag.name().toLowerCase(Locale.US);
                Log.d(TAG, "Comparing " + tagName + " to " + term);
                if (tagName.contains(term)) {
                    return true;
                }
            }


            return false;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            synchronized (lock) {
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
