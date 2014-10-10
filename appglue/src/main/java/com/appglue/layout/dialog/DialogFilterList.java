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

public class DialogFilterList extends DialogFragment {

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
//        if (item.getFilterState() == ServiceIO.MANUAL) {
//
//            String result = item.getDescription().getType().toString(item.getManualValue());
//            filterValueText.setText(result);
//            rt.setChecked(true);
//        } else if (item.getFilterState() == ServiceIO.SAMPLE) {
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

}
