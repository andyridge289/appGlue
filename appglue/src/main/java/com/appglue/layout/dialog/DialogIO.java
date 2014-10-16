package com.appglue.layout.dialog;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.appglue.ActivityWiring;
import com.appglue.IODescription;
import com.appglue.R;
import com.appglue.description.SampleValue;
import com.appglue.description.datatypes.IOType;
import com.appglue.engine.description.IOValue;
import com.appglue.engine.description.ServiceIO;
import com.appglue.layout.adapter.FilterSampleAdapter;
import com.appglue.library.FilterFactory;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

public class DialogIO extends AlertDialog {

    private ActivityWiring activity;
    private IODescription description;
    private ServiceIO item;
    private Registry registry;

    private RadioGroup radioGroup;
    private RadioButton sampleRadio;
    private RadioButton manualRadio;

    private Spinner sampleSpinner;
    private EditText manualText;

    public DialogIO(ActivityWiring context, final ServiceIO io) {
        super(context);

        this.activity = context;
        this.item = io;
        this.description = item.getDescription();
        this.registry = Registry.getInstance(context);

        LayoutInflater inflater = activity.getLayoutInflater();
        final View v = inflater.inflate(R.layout.dialog_io, null);

        radioGroup = (RadioGroup) v.findViewById(R.id.io_radio);
        manualRadio = (RadioButton) v.findViewById(R.id.io_radio_text);
        sampleRadio = (RadioButton) v.findViewById(R.id.io_radio_spinner);
        manualText = (EditText) v.findViewById(R.id.io_value_text);
        sampleSpinner = (Spinner) v.findViewById(R.id.io_value_spinner);

        final IOType type = description.getType();
        ArrayList<SampleValue> values = description.getSampleValues();
        if (values == null)
            values = new ArrayList<SampleValue>();

        final boolean hasSamples = values.size() != 0;

        if (!hasSamples) {
            values = new ArrayList<SampleValue>();
            values.add(new SampleValue("No samples", ""));
        }

        if (!type.acceptsManualValues()) {
            manualText.setEnabled(false);
            manualRadio.setEnabled(false);
            sampleRadio.setChecked(true);
            sampleRadio.setEnabled(false);
            radioGroup.setEnabled(false);
        } else {
            // Set the type of the edit text depending on the type
            if (type.typeExtends(IOType.Factory.getType(IOType.Factory.NUMBER))) {
                // TODO Set it to be a number type
            } else if (type.typeEquals(IOType.Factory.getType(IOType.Factory.PASSWORD))) {
                // TODO Set it to be a hidden password thing
            }
        }

        sampleSpinner.setAdapter(new FilterSampleAdapter(activity, values));

        if (!hasSamples) {
            sampleSpinner.setEnabled(false);
            manualRadio.setEnabled(false);
            manualRadio.setChecked(true);
            sampleRadio.setEnabled(false);
            radioGroup.setEnabled(false);
        }

        setView(v);

        Button positiveButton = (Button) v.findViewById(R.id.dialog_io_positive);
        Button neutralButton = (Button) v.findViewById(R.id.dialog_io_neutral);
        Button negativeButton = (Button) v.findViewById(R.id.dialog_io_negative);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the value they entered - not sure what happens
                if (manualRadio.isChecked()) {
                    // Then look up the text value
                    Object objectValue = description.getType().fromString(manualText.getText().toString());
                    IOValue value = new IOValue(FilterFactory.NONE, objectValue, io);
                    item.setValue(value);
//                    DialogIO.this.activity.setStatus("Set manual value for " + description.getName());
                } else if (sampleRadio.isChecked()) {
                    // Then look up the index of the spinner that's selected - shouldn't need to worry about data types
                    SampleValue sampleValue = (SampleValue) sampleSpinner.getSelectedItem();
                    IOValue value = new IOValue(FilterFactory.NONE, sampleValue, io);
                    item.setValue(value);
//                    DialogIO.this.activity.setStatus("Set sample value for " + description.getName());
                }

                // The setting of the list values needs to move to the creating of the list. Do an invalidate
                registry.updateComposite(activity.getComposite());
                DialogIO.this.activity.redraw();
                dismiss();
            }
        });

        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Put this back
//                item.setFilterState(ServiceIO.UNFILTERED);
                DialogIO.this.activity.setStatus("Removed for " + description.getName());
                registry.updateComposite(activity.getComposite());
                DialogIO.this.activity.redraw();
                cancel();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cancel();
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                // enable or disable the relevant item
                // Set the condition of the ValueNode
                if (checkedId == R.id.filter_radio_manual) {
                    sampleSpinner.setEnabled(false);
                    manualText.setEnabled(true);
                } else { // It must be filter_radio_sample
                    sampleSpinner.setEnabled(true);
                    manualText.setEnabled(false);
                }
            }
        });

        // TODO Loading saved values
    }

//    private void create(Context context, int childCount) {
//
//        if (type.typeEquals(IOType.Factory.getType(IOType.Factory.TEXT))) {
//            setup(FILTER_STRING_VALUES, InputType.TYPE_CLASS_TEXT,
//                    hasValues, values);
//        } else if (type.typeEquals(IOType.Factory.getType(IOType.Factory.NUMBER))) {
//            setup(FILTER_NUMBER_VALUES, InputType.TYPE_CLASS_NUMBER,
//                    hasValues, values);
//        } else if (type.typeEquals(IOType.Factory.getType(IOType.Factory.SET))) {
//            setup(FILTER_SET_VALUES, -1,
//                    hasValues, values);
//        } else if (type.typeEquals(IOType.Factory.getType(IOType.Factory.BOOLEAN))) {
//            if (values.size() != 0) {
//                // These might need to be hard-coded as acceptable values
//                values = new ArrayList<SampleValue>();
//                values.add(new SampleValue("True", true));
//                values.add(new SampleValue("False", false));
//            }
//            setup(FILTER_BOOL_VALUES, InputType.TYPE_CLASS_TEXT,
//                    hasValues, values);
//        } else if (type.typeEquals(IOType.Factory.getType(IOType.Factory.PHONE_NUMBER))) {
//            setup(FILTER_STRING_VALUES, InputType.TYPE_CLASS_PHONE,
//                    hasValues, values);
//        } else if (type.typeEquals(IOType.Factory.getType(IOType.Factory.URL))) {
//            setup(FILTER_STRING_VALUES, InputType.TYPE_CLASS_TEXT,
//                    hasValues, values);
//        } else {
//            Log.e(TAG, "Type not implemented");
//            // Don't know what happens here
//        }
//
//        redraw();
//
//        // At this point we need to set the values to be whatever the default things are
//        if (manualRadio.isChecked()) {
//            String value = manualText.getText().toString();
//            this.value.setManualValue(item.getType().fromString(value));
//        } else if (sampleRadio.isChecked()) {
//            SampleValue sample = (SampleValue) sampleSpinner.getSelectedItem();
//            this.value.setSampleValue(sample);
//        }
//
//        this.value.setCondition((FilterFactory.FilterValue) filterConditionSpinner.getSelectedItem());
//    }
//
//        if (type != -1)
//            manualText.setInputType(type);
//        else {
//            // Hide the manual aspect
//            manualText.setEnabled(false);
//
//            sampleRadio.setChecked(true);
//            manualRadio.setChecked(false);
//
//            sampleRadio.setEnabled(false);
//            manualRadio.setEnabled(false);
//        }
//
//        if (hasSamples) {
//            sampleSpinner.setAdapter(new FilterSampleAdapter(activity, values));
//            sampleRadio.setChecked(true);
//            sampleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                    SampleValue sample = (SampleValue) sampleSpinner.getSelectedItem();
//                    if (value != null) {
//                        value.setSampleValue(sample);
//                    }
//                }
//
//                @Override
//                public void onNothingSelected(AdapterView<?> parent) {
//                    // Don't think we need to do anything here, they should just remove the thing if they don't want it anymore
//                }
//            });
//        } else {
//            sampleSpinner.setEnabled(false);
//
//            manualRadio.setChecked(true);
//            sampleRadio.setChecked(false);
//
//            sampleRadio.setEnabled(false);
//            manualRadio.setEnabled(false);
//
//            radioGroup.setEnabled(false);
//        }
//

//    }
}