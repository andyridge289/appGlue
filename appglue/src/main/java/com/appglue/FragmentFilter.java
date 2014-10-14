package com.appglue;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.support.v4.util.LongSparseArray;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.appglue.description.SampleValue;
import com.appglue.description.ServiceDescription;
import com.appglue.description.datatypes.IOType;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.engine.description.IOFilter;
import com.appglue.engine.description.IOValue;
import com.appglue.engine.description.ServiceIO;
import com.appglue.layout.FilterValueView;
import com.appglue.layout.adapter.FilterSampleAdapter;
import com.appglue.layout.adapter.WiringFilterAdapter;
import com.appglue.library.FilterFactory;
import com.appglue.library.FilterFactory.FilterValue;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

import static com.appglue.Constants.INDEX;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.AND;
import static com.appglue.library.AppGlueConstants.COMPONENT_ID;
import static com.appglue.library.AppGlueConstants.FILTER_ID;
import static com.appglue.library.AppGlueConstants.FILTER_BOOL_VALUES;
import static com.appglue.library.AppGlueConstants.FILTER_NUMBER_VALUES;
import static com.appglue.library.AppGlueConstants.FILTER_SET_VALUES;
import static com.appglue.library.AppGlueConstants.FILTER_STRING_VALUES;
import static com.appglue.library.AppGlueConstants.OR;

public class FragmentFilter extends Fragment {

    private Registry registry;

    private ComponentService component;
    private LinearLayout outputList;

    private IOFilter filter;

    private LongSparseArray<ArrayList<FilterValueView>> filterViews;

    public static Fragment create(long componentId, long filterId) {
        FragmentFilter fragment = new FragmentFilter();
        Bundle args = new Bundle();
        args.putLong(COMPONENT_ID, componentId);
        args.putLong(FILTER_ID, filterId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        registry = Registry.getInstance(getActivity());
        filterViews = new LongSparseArray<ArrayList<FilterValueView>>();

        if(getArguments() != null) {
            CompositeService cs = ((ActivityWiring) getActivity()).getComposite();
            component = cs.getComponent(getArguments().getLong(COMPONENT_ID));
            long filterId = getArguments().getLong(FILTER_ID);
            if (filterId == -1) {
                // Then we need to create a new one
                filter = new IOFilter(component);
            } else {
                // We need to load the relevant filter from the composite
                filter = component.getFilter(filterId);
            }
            component.addFilter(filter);
            Log.d(TAG, "Added filter to " + component.getID() + "( " + component.getDescription().getName() + ")");
        } else {
            Log.e(TAG, "No arguments!");
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_filter, container, false);
        outputList = (LinearLayout) v.findViewById(R.id.filter_output_list);

        if (component == null) {
            // Back out and hope that someone will redraw it
            return v;
        }

        // Loop through the components and create a container for each of the things
        for (final ServiceIO output : component.getOutputs()) {

            final View vv = inflater.inflate(R.layout.fragment_filter_item, null, false);

            TextView ioName = (TextView) vv.findViewById(R.id.filter_io_name);
            ioName.setText(output.getDescription().getFriendlyName());

            TextView ioType = (TextView) vv.findViewById(R.id.filter_io_type);
            ioType.setText(output.getDescription().getType().getName());

            final LinearLayout valueLayout = (LinearLayout) vv.findViewById(R.id.filter_value_container);

            // Set up the add button
            View addButton = vv.findViewById(R.id.filter_add_button);
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    vv.findViewById(R.id.no_filters).setVisibility(View.GONE);

                    FilterValueView vvv = new FilterValueView(getActivity(), FragmentFilter.this, component, filter, null, output, valueLayout.getChildCount());

                    // Keep track of all of the filter views we've created
                    if (filterViews.get(output.getID()) == null) {
                        filterViews.put(output.getID(), new ArrayList<FilterValueView>());
                    }

                    filterViews.get(output.getID()).add(vvv);
                    valueLayout.addView(vvv);
                }
            });

            // If there are any values set already we need to add those
            if (filter.hasValues(output)) {
                ArrayList<IOValue> values = filter.getValues(output);
                if (values != null) {
                    for (IOValue value : values) {
                        FilterValueView vvv = new FilterValueView(getActivity(), this, component, filter, value, output, valueLayout.getChildCount());

                        if (filterViews.get(output.getID()) == null) {
                            filterViews.put(output.getID(), new ArrayList<FilterValueView>());
                        }

                        filterViews.get(output.getID()).add(vvv);
                        valueLayout.addView(vvv);
                    }
                }
            }


            outputList.addView(vv);
        }

        return v;
    }

    @Override
    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setAndors(ServiceIO item) {
        ArrayList<FilterValueView> views = filterViews.get(item.getID());

        // Tell all of the children with that item to redraw - probably to reflect AND/OR change
        if (views != null) {
           for (FilterValueView v : views) {
               v.redraw();
           }
        }

    }

    // TODO Put in a "No filters" placeholder for each thing

    public void remove(ServiceIO io, FilterValueView v) {
        // Need to remove it from the list of values so that it doesn't get told to redraw
        filterViews.get(io.getID()).remove(v);
        ((LinearLayout) v.getParent()).removeView(v);
    }
}
