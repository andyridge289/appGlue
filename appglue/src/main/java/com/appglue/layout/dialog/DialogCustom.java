package com.appglue.layout.dialog;

import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.appglue.ActivityWiring;
import com.appglue.IODescription;
import com.appglue.description.SampleValue;
import com.appglue.engine.description.ServiceIO;
import com.appglue.layout.adapter.FilterSampleAdapter;
import com.appglue.layout.adapter.WiringFilterAdapter;
import com.appglue.library.IOFilter.FilterValue;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

abstract class DialogCustom extends AlertDialog {
    ActivityWiring activity;
    IODescription description;
    ServiceIO item;
    Registry registry;

    DialogCustom(ActivityWiring context, ServiceIO item) {
        super(context);
        this.activity = context;
        this.item = item;
        this.description = item.getDescription();
        this.registry = Registry.getInstance(context);
    }

    void setupDialog(Spinner fcs, FilterValue[] conditions, int type,
                     boolean hasSamples, Spinner fvs, ArrayList<SampleValue> values,
                     RadioButton rs, RadioButton rt, ServiceIO item,
                     Class<?> cast, EditText fvt) {

        if (fcs != null)
            fcs.setAdapter(new WiringFilterAdapter(activity,
                    conditions));

        if (type != -1)
            fvt.setInputType(type);
        else {
            // Hide the manual aspect
            fvt.setEnabled(false);
            rs.setSelected(true);
            rs.setEnabled(false);
            rt.setEnabled(false);
        }

        if (hasSamples) {
            fvs.setAdapter(new FilterSampleAdapter(activity,
                    values));
            rs.setChecked(true);
        } else {
            fvs.setAdapter(new FilterSampleAdapter(activity,
                    values));
            fvs.setEnabled(false);
            rt.setChecked(true);
            rs.setEnabled(false);
            rt.setEnabled(false);
        }

        // TODO This needs to go back in
        // Make it load the saved filter value
//        if (item.getFilterState() == ServiceIO.MANUAL_FILTER) {
//
//            String result = item.getDescription().getType().toString(item.getManualValue());
//            fvt.setText(result);
//            rt.setChecked(true);
//        } else if (item.getFilterState() == ServiceIO.SAMPLE_FILTER) {
//            SampleValue selected = item.getChosenSampleValue();
//            for (int i = 0; i < fvs.getAdapter().getCount(); i++) {
//                SampleValue sampleValue = (SampleValue) fvs.getItemAtPosition(i);
//                if (sampleValue.equals(selected)) {
//                    fvs.setSelection(i, true);
//                    break;
//                }
//            }
//        }

//        if (item.getFilterState() != ServiceIO.UNFILTERED && fcs != null) {
//            FilterValue fv = IOFilter.filters.get(item.getCondition());
//
//            for (int i = 0; i < fcs.getAdapter().getCount(); i++) {
//                FilterValue fv2 = (FilterValue) fcs.getItemAtPosition(i);
//                if (fv.index == fv2.index) {
//                    fcs.setSelection(i, true);
//                    break;
//                }
//            }
//        }
    }
}
