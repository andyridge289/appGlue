package com.appglue.layout.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.appglue.R;
import com.appglue.description.SampleValue;
import com.appglue.layout.adapter.FilterSampleAdapter;
import com.appglue.layout.adapter.WiringFilterAdapter;
import com.appglue.library.FilterFactory;

import java.util.ArrayList;

public class DialogFilterValue extends DialogFragment {


    private Spinner filterConditionSpinner;
    private Spinner filterValueSpinner;

    private EditText filterValueText;

    private RadioButton sampleRadio;
    private RadioButton manualRadio;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_valuelist, container, false);

        filterConditionSpinner = (Spinner) rootView.findViewById(R.id.filter_condition_spinner);
//        filterValueSpinner = (Spinner) rootView.findViewById(R.id.filter_value_spinner);
//
//        filterValueText = (EditText) rootView.findViewById(R.id.filter_value_text);
//
//        sampleRadio = (RadioButton) rootView.findViewById(R.id.filter_radio_spinner);
//        manualRadio = (RadioButton) rootView.findViewById(R.id.filter_radio_text);

        return rootView;
    }

    void setupDialog(FilterFactory.FilterValue[] conditions, int type,
                     boolean hasSamples, ArrayList<SampleValue> values) {

        if (filterConditionSpinner != null)
            filterConditionSpinner.setAdapter(new WiringFilterAdapter(getActivity(),
                    conditions));

        if (type != -1)
            filterValueText.setInputType(type);
        else {
            // Hide the manual aspect
            filterValueText.setEnabled(false);
            sampleRadio.setSelected(true);
            sampleRadio.setEnabled(false);
            manualRadio.setEnabled(false);
        }

        if (hasSamples) {
            filterValueSpinner.setAdapter(new FilterSampleAdapter(getActivity(),
                    values));
            sampleRadio.setChecked(true);
        } else {
            filterValueSpinner.setAdapter(new FilterSampleAdapter(getActivity() ,
                    values));
            filterValueSpinner.setEnabled(false);
            manualRadio.setChecked(true);
            sampleRadio.setEnabled(false);
            manualRadio.setEnabled(false);
        }

        // TODO This needs to go back in
        // Make it load the saved filter value
//        if (item.getFilterState() == ServiceIO.MANUAL_FILTER) {
//
//            String result = item.getDescription().getType().toString(item.getManualValue());
//            filterValueText.setText(result);
//            rt.setChecked(true);
//        } else if (item.getFilterState() == ServiceIO.SAMPLE_FILTER) {
//            SampleValue selected = item.getChosenSampleValue();
//            for (int i = 0; i < filterValueSpinner.getAdapter().getCount(); i++) {
//                SampleValue sampleValue = (SampleValue) filterValueSpinner.getItemAtPosition(i);
//                if (sampleValue.equals(selected)) {
//                    filterValueSpinner.setSelection(i, true);
//                    break;
//                }
//            }
//        }

//        if (item.getFilterState() != ServiceIO.UNFILTERED && filterConditionSpinner != null) {
//            FilterValue fv = IOFilter.filters.get(item.getCondition());
//
//            for (int i = 0; i < filterConditionSpinner.getAdapter().getCount(); i++) {
//                FilterValue fv2 = (FilterValue) filterConditionSpinner.getItemAtPosition(i);
//                if (fv.index == fv2.index) {
//                    filterConditionSpinner.setSelection(i, true);
//                    break;
//                }
//            }
//        }
    }

//    public DialogFilter(final ActivityWiring activity, final ServiceIO io) {
//        super(activity);
//
//        LayoutInflater inflater = activity.getLayoutInflater();
//        final View v = inflater.inflate(R.layout.dialog_filter, null);
//        setView(v);
//
//
//
//        FragmentManager fm = getSupportFragmentManager();
//        fm.beginTransaction().replace(R.id.container, attach).commit();

//        // 1 - Work out what we're filtering and set up the shit
//        setTitle("Filter: " + description.getFriendlyName());
//
//        final LinearLayout listContainer = (LinearLayout) v.findViewById(R.id.list_container);
//        final LinearLayout valueContainer = (LinearLayout) v.findViewById(R.id.value_container);
//
//        if (io.getValues().size() == 0) {
//            // Then we need to go straight to the second view
//            listContainer.setVisibility(View.GONE);
//            valueContainer.setVisibility(View.VISIBLE);
//        } else {
//            for (int i = 0; i < io.getValues().size(); i++) {
//                if (!setValues.contains(io.getValues().get(i))) {
//                    setValues.add(io.getValues().get(i));
//                }
//            }
//
//            // Show the list version
//            listContainer.setVisibility(View.VISIBLE);
//            valueContainer.setVisibility(View.GONE);
//        }
//
//        LinearLayout valueList = (LinearLayout) v.findViewById(R.id.value_list);
//        valueList.removeAllViews();
//        for (int i = 0; i < setValues.size(); i++) {
//
//            LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.dialog_filter_value, null);
//            TextView valueName = (TextView) ll.findViewById(R.id.value_name);
//
//            IOValue value = setValues.get(i);
//            if (value.getFilterState() == IOValue.SAMPLE_FILTER) {
//                valueName.setText(value.getSampleValue().getName());
//            } else if (value.getFilterState() == IOValue.MANUAL_FILTER) {
//                valueName.setText(io.getType().toString(value.getManualValue()));
//            }
//
//            if (setValues.size() == 1)
//                ll.findViewById(R.id.and_or).setVisibility(View.GONE);
//
//            valueList.addView(ll);
//        }
//
//        Button positiveButton = (Button) v.findViewById(R.id.dialog_filter_positive);
//        Button neutralButton = (Button) v.findViewById(R.id.dialog_filter_neutral);
//        Button negativeButton = (Button) v.findViewById(R.id.dialog_filter_negative);
//        Button addButton = (Button) v.findViewById(R.id.dialog_filter_add);
//
//        final RadioGroup radio = (RadioGroup) v.findViewById(R.id.filter_radio);
//        final RadioButton spinnerRadio = (RadioButton) v.findViewById(R.id.filter_radio_spinner);
//        final RadioButton textRadio = (RadioButton) v.findViewById(R.id.filter_radio_text);
//
//        final EditText filterValueText = (EditText) v.findViewById(R.id.filter_value_text);
//        final Spinner filterValueSpinner = (Spinner) v.findViewById(R.id.filter_value_spinner);
//        final Spinner filterConditionSpinner = (Spinner) v.findViewById(R.id.filter_condition_spinner);
//
//        ImageView addImage = (ImageView) v.findViewById(R.id.another_value);
//
//        addButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                listContainer.setVisibility(View.VISIBLE);
//                valueContainer.setVisibility(View.GONE);
//
//                FilterValue condition = (FilterValue) filterConditionSpinner.getSelectedItem();
//
//                // Get the value that has been selected, and add it to the array list of values
//                if (textRadio.isChecked()) {
//                    Object value = description.getType().fromString(filterValueText.getText().toString());
//                    item.addFilter(new IOValue(condition, value, io));
//                    DialogFilter.this.activity.setStatus("Set manual filter for " + description.getName());
//                } else if (spinnerRadio.isChecked()) {
//                    // Then look up the index of the spinner that's selected - shouldn't need to worry about data types
//                    SampleValue value = (SampleValue) filterValueSpinner.getSelectedItem();
//                    item.addFilter(new IOValue(condition, value, io));
//                    DialogFilter.this.activity.setStatus("Set sample filter for " + description.getName());
//                }
//
////                // The setting of the list values needs to move to the creating of the list. Do an invalidate
////                registry.updateComposite(DialogFilter.this.activity.getComposite());
////                DialogFilter.this.activity.redraw();
////                dismiss();
//
//                // TODO Make list add elements to the dialog
//            }
//        });
//
//        addImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                listContainer.setVisibility(View.GONE);
//                valueContainer.setVisibility(View.VISIBLE);
//
//                // TODO Remove the things from the spinner that are in the (sample) values list
//            }
//        });
//
//        IOType type = description.getType();
//        ArrayList<SampleValue> values = description.getSampleValues();
//        if (values == null)
//            values = new ArrayList<SampleValue>();
//
//        final boolean hasSamples = values.size() != 0;
//
//        if (!hasSamples) {
//            values = new ArrayList<SampleValue>();
//            values.add(new SampleValue("No samples", ""));
//        }
//
//
//
//        radio.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                switch (checkedId) {
//                    case R.id.filter_radio_spinner:
//                        filterValueSpinner.setEnabled(true);
//                        filterValueText.setEnabled(false);
//                        break;
//
//                    case R.id.filter_radio_text:
//                        filterValueSpinner.setEnabled(false);
//                        filterValueText.setEnabled(true);
//                        break;
//                }
//            }
//        });
//
//        // 2 - Set up all of the components
//        // Condition spinner needs to have the right things
//        // Set value spinner needs to have the right things or be disabled saying there are none
//        // Text field needs to be the right type or be hidden if boolean
//
//        if (type.typeEquals(IOType.Factory.getType(IOType.Factory.TEXT))) {
//            setupDialog(filterConditionSpinner, FILTER_STRING_VALUES, InputType.TYPE_CLASS_TEXT,
//                    hasSamples, filterValueSpinner, values, spinnerRadio, textRadio, item,
//                    String.class, filterValueText);
//        } else if (type.typeEquals(IOType.Factory.getType(IOType.Factory.NUMBER))) {
//            setupDialog(filterConditionSpinner, FILTER_NUMBER_VALUES, InputType.TYPE_CLASS_NUMBER,
//                    hasSamples, filterValueSpinner, values, spinnerRadio, textRadio, item,
//                    Integer.class, filterValueText);
//        } else if (type.typeEquals(IOType.Factory.getType(IOType.Factory.SET))) {
//            setupDialog(filterConditionSpinner, FILTER_SET_VALUES, -1,
//                    hasSamples, filterValueSpinner, values, spinnerRadio, textRadio, item,
//                    Integer.class, filterValueText);
//        } else if (type.typeEquals(IOType.Factory.getType(IOType.Factory.BOOLEAN))) {
//            if (!hasSamples) {
//                // These might need to be hard-coded as acceptable values
//                values = new ArrayList<SampleValue>();
//                values.add(new SampleValue("True", true));
//                values.add(new SampleValue("False", false));
//            }
//
//            setupDialog(filterConditionSpinner, FILTER_BOOL_VALUES, InputType.TYPE_CLASS_TEXT,
//                    hasSamples, filterValueSpinner, values, spinnerRadio, textRadio, item,
//                    Integer.class, filterValueText);
//        } else if (type.typeEquals(IOType.Factory.getType(IOType.Factory.PHONE_NUMBER))) {
//            setupDialog(filterConditionSpinner, FILTER_STRING_VALUES, InputType.TYPE_CLASS_PHONE,
//                    hasSamples, filterValueSpinner, values, spinnerRadio, textRadio, item,
//                    String.class, filterValueText);
//        } else if (type.typeEquals(IOType.Factory.getType(IOType.Factory.URL))) {
//            setupDialog(filterConditionSpinner, FILTER_STRING_VALUES, InputType.TYPE_CLASS_TEXT,
//                    hasSamples, filterValueSpinner, values, spinnerRadio, textRadio, item,
//                    String.class, filterValueText);
//        } else {
//            Log.e(TAG, "Type not implemented");
//            // Don't know what happens here
//        }
//
//
//        positiveButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO We need to add everything that's in the values array list
////                // Get the value they entered - not sure what happens
//
//            }
//        });
//
//        negativeButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                cancel();
//            }
//        });
//
//        neutralButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO Remove everything from the value list
//////                item.setFilterState(ServiceIO.UNFILTERED);
////                DialogFilter.this.activity.setStatus("Cleared filter for " + description.getName());
////                registry.updateComposite(DialogFilter.this.activity.getComposite());
////                DialogFilter.this.activity.redraw();
//            }
//        });
//    }
}
