package com.appglue;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.appglue.description.SampleValue;
import com.appglue.description.ServiceDescription;
import com.appglue.description.datatypes.IOType;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.layout.adapter.FilterSampleAdapter;
import com.appglue.layout.adapter.WiringFilterAdapter;
import com.appglue.library.FilterFactory.FilterValue;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.FILTER_BOOL_VALUES;
import static com.appglue.library.AppGlueConstants.FILTER_NUMBER_VALUES;
import static com.appglue.library.AppGlueConstants.FILTER_SET_VALUES;
import static com.appglue.library.AppGlueConstants.FILTER_STRING_VALUES;

public class FragmentStoryParameters extends Fragment {
    private Registry registry;

    private ServiceDescription previous;

    private TextView nameText;

    private TextView noInputText;
    private TextView noOutputText;

    private LinearLayout inputContainer;
    private LinearLayout outputContainer;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_story_parameters, container, false);

        nameText = (TextView) v.findViewById(R.id.story_param_name);

        noInputText = (TextView) v.findViewById(R.id.story_param_no_inputs);
        noOutputText = (TextView) v.findViewById(R.id.story_param_no_outputs);

        inputContainer = (LinearLayout) v.findViewById(R.id.story_param_inputs);
        outputContainer = (LinearLayout) v.findViewById(R.id.story_param_outputs);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle icicle) {
        super.onActivityCreated(icicle);

        registry = Registry.getInstance(getActivity());
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

    public void setData(int position) {
        if (registry == null)
            registry = Registry.getInstance(getActivity());

        CompositeService cs = registry.getCurrent();
        ArrayList<ComponentService> components = cs.getComponentsAL();
        position = position == -1 ? components.size() - 1 : position;
        ServiceDescription component = components.get(position).getDescription(); // SparseArray
        previous = position > 0 ? components.get(position - 1).getDescription() : null;
        Log.w(TAG, "And now position " + position);

        nameText.setText(component.getName());

        if (component.hasInputs()) {
            // Show the list, hide the text, set the adapter
            inputContainer.setVisibility(View.VISIBLE);
            setupInputs(inputContainer, component.getInputs());
            noInputText.setVisibility(View.GONE);
        } else {
            // Show the text, hide the list
            inputContainer.setVisibility(View.GONE);
            noInputText.setVisibility(View.VISIBLE);
        }

        if (component.hasOutputs()) {
            // Show the list, hide the text, set the adapter
            outputContainer.setVisibility(View.VISIBLE);
            setupOutputs(outputContainer, component.getOutputs());
            noOutputText.setVisibility(View.GONE);
        } else {
            // Show the text, hide the list
            outputContainer.setVisibility(View.GONE);
            noOutputText.setVisibility(View.VISIBLE);
        }
    }


    /**
     * *******************************
     * <p/>
     * LIST SHIT BELOW HERE
     * <p/>
     * *******************************
     */

    private void setupInputs(LinearLayout inputContainer, ArrayList<IODescription> inputs) {
        for (int i = 0; i < inputs.size(); i++) {
            View v = setInput(inputs, i);
            inputContainer.addView(v);
        }
    }

    @SuppressLint("InflateParams")
    private View setInput(ArrayList<IODescription> inputs, final int index) {
        LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.list_item_storyparam_input, null);

        final IODescription item = inputs.get(index);
        IOType type = item.getType();

        TextView nameText = (TextView) v.findViewById(R.id.param_input_name);
        nameText.setText(item.getFriendlyName());

        TextView typeText = (TextView) v.findViewById(R.id.param_input_type);
        typeText.setText(type.getName());

        final View container = v.findViewById(R.id.filter_container);
        final Button doneButton = (Button) v.findViewById(R.id.storyparam_done_button);
        final LinearLayout doneContainer = (LinearLayout) v.findViewById(R.id.button_container);
        final Button filterButton = (Button) v.findViewById(R.id.filter_button_story);
        final Button dontFilterButton = (Button) v.findViewById(R.id.dont_filter_button);

        filterButton.setOnClickListener(new OnClickListener() {
            public void onClick(View vv) {
                doneContainer.setVisibility(View.VISIBLE);
                container.setVisibility(View.VISIBLE);
                vv.setVisibility(View.GONE);
            }
        });

        dontFilterButton.setOnClickListener(new OnClickListener() {
            public void onClick(View vv) {
                // Clear all the layout items
                container.findViewById(R.id.param_value_spinner).setSelected(false);
                ((EditText) container.findViewById(R.id.param_value_text)).setText("");
                container.findViewById(R.id.param_previous_spinner).setSelected(false);

                doneContainer.setVisibility(View.GONE);
                container.setVisibility(View.GONE);
                filterButton.setVisibility(View.VISIBLE);

                // Don't save anything to the object
            }
        });

        doneButton.setOnClickListener(new OnClickListener() {
            public void onClick(View vv) {
                // If we get here they chose a value

                // 2 See which tab is selected - hopefully this is the same as the last one they selected?
                TabHost tabs = (TabHost) container.findViewById(R.id.param_tabhost);

                // 3 See what the value of the corresponding text box/spinner is
                if (tabs.getCurrentTab() == 0) {
                    // It's the sample one
                    SampleValue value = (SampleValue) ((Spinner) container.findViewById(R.id.param_value_spinner)).getSelectedItem();
//					item.setChosenSampleValue(value);
//					item.setFilterState(IODescription.SAMPLE_FILTER); ServiceIO not IODescription
                } else if (tabs.getCurrentTab() == 1) {
                    // It's the custom one
                    String sValue = ((EditText) container.findViewById(R.id.param_value_text)).getText().toString();
                    Object value = item.getType().fromString(sValue);
//					item.setManualValue(value);
//					item.setFilterState(IODescription.MANUAL_FILTER); ServiceIO not IODescription
                } else if (tabs.getCurrentTab() == 2) {
                    // It needs to be linked to a previous one.. This could be more complicated!
                    IODescription value = (IODescription) ((Spinner) container.findViewById(R.id.param_previous_spinner)).getSelectedItem();
                    Log.e(TAG, value.getFriendlyName());


                    // get previous output from the last component and then match them up
                    IODescription previousOut = previous.getOutput(value.getID());
//					item.setConnection(previousOut);
//					previousOut.setConnection(item); ServiceIO not IODescription
                }

                doneContainer.setVisibility(View.GONE);
                container.setVisibility(View.GONE);
                filterButton.setVisibility(View.VISIBLE);
            }
        });

        ArrayList<SampleValue> values = item.getSampleValues();
        if (values == null)
            values = new ArrayList<SampleValue>();

        final boolean hasSamples = values.size() != 0;

        if (!hasSamples) {
            values = new ArrayList<SampleValue>();
            values.add(new SampleValue("No samples", ""));
        }

        if (type.equals(IOType.Factory.getType(IOType.Factory.TEXT))) {
            setupInput(item, InputType.TYPE_CLASS_TEXT,
                    hasSamples, values, item,
                    v);
        } else if (type.equals(IOType.Factory.getType(IOType.Factory.NUMBER))) {
            setupInput(item, InputType.TYPE_CLASS_NUMBER,
                    hasSamples, values, item,
                    v);
        } else if (type.equals(IOType.Factory.getType(IOType.Factory.SET))) {
            setupInput(item, -1,
                    hasSamples, values, item,
                    v);
        } else if (type.equals(IOType.Factory.getType(IOType.Factory.BOOLEAN))) {
            if (!hasSamples) {
                // These might need to be hard-coded as acceptable values
                values = new ArrayList<SampleValue>();
                values.add(new SampleValue("True", true));
                values.add(new SampleValue("False", false));
            }

            setupInput(item, InputType.TYPE_CLASS_TEXT,
                    hasSamples, values, item,
                    v);
        } else if (type.equals(IOType.Factory.getType(IOType.Factory.PHONE_NUMBER))) {
            setupInput(item, InputType.TYPE_CLASS_PHONE,
                    hasSamples, values, item,
                    v);
        } else if (type.equals(IOType.Factory.getType(IOType.Factory.URL))) {
            setupInput(item, InputType.TYPE_CLASS_TEXT,
                    hasSamples, values, item,
                    v);
        } else {
            Log.d(TAG, "We've hit the else FragmentStoryParameters->setInput");
            // Don't know what happens here
        }

        return v;
    }


    private void setupInput(IODescription current, int type,
                            boolean hasSamples, ArrayList<SampleValue> values, IODescription item,
                            View v) {

        final EditText valueText = (EditText) v.findViewById(R.id.param_value_text);
        final Spinner valueSpinner = (Spinner) v.findViewById(R.id.param_value_spinner);
        final Spinner previousSpinner = (Spinner) v.findViewById(R.id.param_previous_spinner);
        final Spinner conditionSpinner = (Spinner) v.findViewById(R.id.param_condition_spinner);

        ArrayList<IODescription> matching = new ArrayList<IODescription>();

        if (previous != null) {
            ArrayList<IODescription> outputs = previous.getOutputs();

            for (IODescription output : outputs) {
                if (output.getType().equals(current.getType())) {
                    matching.add(output);
                }
            }
        }

        TabHost tabs = (TabHost) v.findViewById(R.id.param_tabhost);
        tabs.setup();

        TabHost.TabSpec spec1 = tabs.newTabSpec("tag1");
        spec1.setContent(R.id.param_value_spinner);
        spec1.setIndicator("Sample");
        tabs.addTab(spec1);

        TabHost.TabSpec spec2 = tabs.newTabSpec("tag2");
        spec2.setContent(R.id.param_value_text);
        spec2.setIndicator("Custom");
        tabs.addTab(spec2);

        TabHost.TabSpec spec3 = tabs.newTabSpec("tag3");
        spec3.setContent(R.id.param_previous_spinner);
        spec3.setIndicator("Previous");
        tabs.addTab(spec3);

        TabWidget widget = tabs.getTabWidget();
        View sampleTab = widget.getChildTabViewAt(0);
        View customTab = widget.getChildTabViewAt(1);
        View previousTab = widget.getChildTabViewAt(2);

        if (matching.size() > 0) {
            previousSpinner.setAdapter(new MatchingAdapter(getActivity(), matching));
            tabs.setCurrentTab(2);
        } else {
            matching.add(new IODescription("No matching"));
            // It shouldn't be a sample value, because it ain't a sample. It's a ServiceIO. You tit.
            previousSpinner.setAdapter(new MatchingAdapter(getActivity(), matching));
            previousSpinner.setEnabled(false);

            previousTab.setEnabled(false);
            previousTab.setAlpha(0.2f);
        }

        if (type != -1) {
            valueText.setInputType(type);
            tabs.setCurrentTab(1);
        } else {
            valueText.setEnabled(false);
            customTab.setEnabled(false);
            customTab.setAlpha(0.2f);
        }

        if (hasSamples) {
            valueSpinner.setAdapter(new FilterSampleAdapter(getActivity(), values));
            tabs.setCurrentTab(0);
        } else {
            // In this case we might have already added the no samples one...?
            valueSpinner.setAdapter(new FilterSampleAdapter(getActivity(), values));
            valueSpinner.setEnabled(false);
            sampleTab.setEnabled(false);
            sampleTab.setAlpha(0.2f);
        }


        // Make it load the saved filter value
//		if (item.getFilterState() == IODescription.MANUAL_FILTER)
//		{
//			String result = item.type().toString(item.getManualValue());
//			valueText.setText(result);
//		}
//		else if (item.getFilterState() == IODescription.SAMPLE_FILTER)
//		{
//			IOValue selected = item.getChosenSampleValue();
//			for (int i = 0; i < valueSpinner.getAdapter().getCount(); i++) {
//				IOValue ioValue = (IOValue) valueSpinner.getItemAtPosition(i);
//				if (ioValue.equals(selected)) {
//					valueSpinner.setSelection(i, true);
//					break;
//				}
//			}
//		}
//
//		if (item.getFilterState() != IODescription.UNFILTERED && conditionSpinner != null) {
//			FilterValue fv = IOFilter.filters.get(item.getCondition());
//
//			for (int i = 0; i < conditionSpinner.getAdapter().getCount(); i++) {
//				FilterValue fv2 = (FilterValue) conditionSpinner.getItemAtPosition(i);
//				if (fv.index == fv2.index) {
//					conditionSpinner.setSelection(i, true);
//					break;
//				}
//			}
//		}

        // ServiceIO not IODescription
        // And now look up if anything had a type in the other service ?????
    }

    private void setupOutputs(LinearLayout outputContainer, ArrayList<IODescription> outputs) {
        for (int i = 0; i < outputs.size(); i++) {
            View v = setupOutput(outputs, i);
            outputContainer.addView(v);
        }
    }

    @SuppressLint("InflateParams")
    private View setupOutput(final ArrayList<IODescription> outputs, final int index) {
        LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = vi.inflate(R.layout.list_item_storyparam_output, null);

        final IODescription item = outputs.get(index);
        IOType type = item.getType();

        final TextView nameText = (TextView) v.findViewById(R.id.param_output_name);
        nameText.setText(item.getFriendlyName());

        TextView typeText = (TextView) v.findViewById(R.id.param_output_type);
        typeText.setText(type.getName());

        final View container = v.findViewById(R.id.filter_container);
        final Button doneButton = (Button) v.findViewById(R.id.story_done_button);
        final LinearLayout doneContainer = (LinearLayout) v.findViewById(R.id.button_container);
        final Button filterButton = (Button) v.findViewById(R.id.filter_button_story);
        final Button dontFilterButton = (Button) v.findViewById(R.id.dont_filter_button);


        filterButton.setOnClickListener(new OnClickListener() {
            public void onClick(View vv) {
                doneContainer.setVisibility(View.VISIBLE);
                container.setVisibility(View.VISIBLE);
                vv.setVisibility(View.GONE);
            }
        });

        dontFilterButton.setOnClickListener(new OnClickListener() {
            public void onClick(View vv) {
                // Clear all the layout items
                container.findViewById(R.id.param_value_spinner).setSelected(false);
                ((EditText) container.findViewById(R.id.param_value_text)).setText("");

                doneContainer.setVisibility(View.GONE);
                container.setVisibility(View.GONE);
                filterButton.setVisibility(View.VISIBLE);

                // Don't save anything to the object

            }
        });

        doneButton.setOnClickListener(new OnClickListener() {
            public void onClick(View vv) {
                // Save the stuff to the item

                // 2 See which tab is selected - hopefully this is the same as the last one they selected?
                TabHost tabs = (TabHost) container.findViewById(R.id.param_tabhost);

                // 3 See what the value of the corresponding text box/spinner is
                if (tabs.getCurrentTab() == 0) {
                    // It's the sample one
                    SampleValue value = (SampleValue) ((Spinner) container.findViewById(R.id.param_value_spinner)).getSelectedItem();
//					item.setChosenSampleValue(value);
//					item.setFilterState(IODescription.SAMPLE_FILTER); ServiceIO not IODescription
                } else if (tabs.getCurrentTab() == 1) {
                    // It's the custom one
                    String sValue = ((EditText) container.findViewById(R.id.param_value_text)).getText().toString();
                    Object value = item.getType().fromString(sValue);
//					item.setManualValue(value);
//					item.setFilterState(IODescription.MANUAL_FILTER); ServiceIO not IODescription
                }

                doneContainer.setVisibility(View.GONE);
                container.setVisibility(View.GONE);
                filterButton.setVisibility(View.VISIBLE);
                filterButton.setText("Edit filter");
            }
        });

        ArrayList<SampleValue> values = item.getSampleValues();
        if (values == null)
            values = new ArrayList<SampleValue>();

        final boolean hasSamples = values.size() != 0;

        if (!hasSamples) {
            values = new ArrayList<SampleValue>();
            values.add(new SampleValue("No samples", ""));
        }

        if (type.equals(IOType.Factory.getType(IOType.Factory.TEXT))) {
            setupOutput(FILTER_STRING_VALUES, InputType.TYPE_CLASS_TEXT,
                    hasSamples, values, item,
                    v);
        } else if (type.equals(IOType.Factory.getType(IOType.Factory.NUMBER))) {
            setupOutput(FILTER_NUMBER_VALUES, InputType.TYPE_CLASS_NUMBER,
                    hasSamples, values, item,
                    v);
        } else if (type.equals(IOType.Factory.getType(IOType.Factory.SET))) {
            setupOutput(FILTER_SET_VALUES, -1,
                    hasSamples, values, item,
                    v);
        } else if (type.equals(IOType.Factory.getType(IOType.Factory.BOOLEAN))) {
            if (!hasSamples) {
                // These might need to be hard-coded as acceptable values
                values = new ArrayList<SampleValue>();
                values.add(new SampleValue("True", true));
                values.add(new SampleValue("False", false));
            }

            setupOutput(FILTER_BOOL_VALUES, -1,
                    hasSamples, values, item,
                    v);
        } else if (type.equals(IOType.Factory.getType(IOType.Factory.PHONE_NUMBER))) {
            setupOutput(FILTER_STRING_VALUES, InputType.TYPE_CLASS_PHONE,
                    hasSamples, values, item,
                    v);
        } else if (type.equals(IOType.Factory.getType(IOType.Factory.URL))) {
            setupOutput(FILTER_STRING_VALUES, InputType.TYPE_CLASS_TEXT,
                    hasSamples, values, item,
                    v);
        } else {
            // Don't know what happens here
            Log.d(TAG, "We've hit the else, we probably shouldn't've (FragmentStoryParameters->setOutput");
        }

        return v;
    }

    private void setupOutput(FilterValue[] conditions, int type,
                             boolean hasSamples, ArrayList<SampleValue> values, IODescription item,
                             View v) {

        final Spinner valueSpinner = (Spinner) v.findViewById(R.id.param_value_spinner);
        final Spinner conditionSpinner = (Spinner) v.findViewById(R.id.param_condition_spinner);
        final EditText valueText = (EditText) v.findViewById(R.id.param_value_text);

        final TabHost tabs = (TabHost) v.findViewById(R.id.param_tabhost);
        tabs.setup();

        final int SAMPLE = 0;
        final int CUSTOM = 1;

        TabHost.TabSpec spec1 = tabs.newTabSpec("tag1");
        spec1.setContent(R.id.param_value_spinner);
        spec1.setIndicator("Sample");
        tabs.addTab(spec1);

        TabHost.TabSpec spec2 = tabs.newTabSpec("tag2");
        spec2.setContent(R.id.param_value_text);
        spec2.setIndicator("Custom");
        tabs.addTab(spec2);

        TabWidget widget = tabs.getTabWidget();
        View sampleTab = widget.getChildTabViewAt(SAMPLE);
        View customTab = widget.getChildTabViewAt(CUSTOM);


        if (conditionSpinner != null)
            conditionSpinner.setAdapter(new WiringFilterAdapter(getActivity(),
                    conditions));

        // This also needs to take into account what type the thing is

        if (type != -1) {
            valueText.setInputType(type);
            tabs.setCurrentTab(1);
        } else {
            // Hide the manual aspect
            valueText.setEnabled(false);
            customTab.setEnabled(false);
            customTab.setAlpha(0.2f);
        }

        if (hasSamples) {
            Log.e(TAG, "Pre-setting value with prior samples 1");
            valueSpinner.setAdapter(new FilterSampleAdapter(getActivity(),
                    values));
            tabs.setCurrentTab(0);
        } else {
            Log.e(TAG, "Pre-setting value with prior samples 2");
            valueSpinner.setAdapter(new FilterSampleAdapter(getActivity(),
                    values));
            valueSpinner.setEnabled(false);
            sampleTab.setEnabled(false);
            sampleTab.setAlpha(0.2f);
        }

        // Make it load the saved filter value
//		if (item.getFilterState() == IODescription.MANUAL_FILTER)
//		{
//
//			String result = item.type().toString(item.getManualValue());
//			valueText.setText(result);
//		}
//		else if (item.getFilterState() == IODescription.SAMPLE_FILTER) {
//			IOValue selected = item.getChosenSampleValue();
//			for (int i = 0; i < valueSpinner.getAdapter().getCount(); i++) {
//				IOValue ioValue = (IOValue) valueSpinner.getItemAtPosition(i);
//				if (ioValue.equals(selected)) {
//					valueSpinner.setSelection(i, true);
//					break;
//				}
//			}
//		}
//
//		if (item.getFilterState() != IODescription.UNFILTERED && conditionSpinner != null) {
//			FilterValue fv = IOFilter.filters.get(item.getCondition());
//
//			for (int i = 0; i < conditionSpinner.getAdapter().getCount(); i++) {
//				FilterValue fv2 = (FilterValue) conditionSpinner.getItemAtPosition(i);
//				if (fv.index == fv2.index) {
//					conditionSpinner.setSelection(i, true);
//					break;
//				}
//			}
//		} ServiceIO not IODescription
    }

    private class MatchingAdapter extends ArrayAdapter<IODescription> {
        public MatchingAdapter(Context context, ArrayList<IODescription> objects) {
            super(context, android.R.layout.simple_dropdown_item_1line, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            View v = convertView;
            LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (v == null)
                v = vi.inflate(android.R.layout.simple_dropdown_item_1line, null);

            IODescription other = getItem(position);

            TextView tv = (TextView) v.findViewById(android.R.id.text1);
            tv.setText(other.getFriendlyName());

            return v;
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (v == null) {
                v = vi.inflate(android.R.layout.simple_dropdown_item_1line, null);
            }

            IODescription other = getItem(position);

            TextView tv = (TextView) v.findViewById(android.R.id.text1);
            tv.setText(other.getFriendlyName());

            return v;
        }
    }
}
