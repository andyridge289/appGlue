package com.appglue.layout.dialog;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.appglue.ActivityWiring;
import com.appglue.R;
import com.appglue.description.SampleValue;
import com.appglue.description.datatypes.IOType;
import com.appglue.engine.description.IOValue;
import com.appglue.engine.description.ServiceIO;
import com.appglue.library.FilterFactory;

import java.util.ArrayList;

import static com.appglue.library.AppGlueConstants.FILTER_BOOL_VALUES;
import static com.appglue.library.AppGlueConstants.FILTER_NUMBER_VALUES;
import static com.appglue.library.AppGlueConstants.FILTER_STRING_VALUES;

public class DialogIO extends DialogCustom {
    public DialogIO(ActivityWiring context, final ServiceIO io) {
        super(context, io);

        LayoutInflater inflater = activity.getLayoutInflater();
        final View v = inflater.inflate(R.layout.dialog_io, null);

        final RadioButton textRadio = (RadioButton) v.findViewById(R.id.io_radio_text);
        final RadioButton spinnerRadio = (RadioButton) v.findViewById(R.id.io_radio_spinner);
        final EditText ioText = (EditText) v.findViewById(R.id.io_value_text);
        final Spinner ioSpinner = (Spinner) v.findViewById(R.id.io_value_spinner);

        final IOType type = description.getType();
        ArrayList<SampleValue> values = description.getSampleValues();
        if (values == null)
            values = new ArrayList<SampleValue>();

        final boolean hasSamples = values.size() != 0;

        if (!hasSamples) {
            values = new ArrayList<SampleValue>();
            values.add(new SampleValue("No samples", ""));
        }

        if (type.equals(IOType.Factory.getType(IOType.Factory.TEXT))) {
            // This is for text
            setupDialog(null, FILTER_STRING_VALUES, InputType.TYPE_CLASS_TEXT,
                    hasSamples, ioSpinner, values, spinnerRadio, textRadio, item,
                    String.class, ioText);
        } else if (type.equals(IOType.Factory.getType(IOType.Factory.NUMBER))) {
            setupDialog(null, FILTER_NUMBER_VALUES, InputType.TYPE_CLASS_NUMBER,
                    hasSamples, ioSpinner, values, spinnerRadio, textRadio, item,
                    Integer.class, ioText);
        } else if (type.equals(IOType.Factory.getType(IOType.Factory.BOOLEAN))) {
            if (!hasSamples) {
                // These might need to be hard-coded as acceptable values
                values = new ArrayList<SampleValue>();
                values.add(new SampleValue("True", true));
                values.add(new SampleValue("False", false));
            }

            setupDialog(null, FILTER_BOOL_VALUES, -1,
                    hasSamples, ioSpinner, values, spinnerRadio, textRadio, item,
                    Integer.class, ioText);
        }

        setView(v);

        Button positiveButton = (Button) v.findViewById(R.id.dialog_io_positive);
        Button neutralButton = (Button) v.findViewById(R.id.dialog_io_neutral);
        Button negativeButton = (Button) v.findViewById(R.id.dialog_io_negative);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the value they entered - not sure what happens
                if (textRadio.isChecked()) {
                    // Then look up the text value
                    Object objectValue = description.getType().fromString(ioText.getText().toString());
                    IOValue value = new IOValue(FilterFactory.NONE, objectValue, io);
                    item.setValue(value);
                    DialogIO.this.activity.setStatus("Set manual value for " + description.getName());
                } else if (spinnerRadio.isChecked()) {
                    // Then look up the index of the spinner that's selected - shouldn't need to worry about data types
                    SampleValue sampleValue = (SampleValue) ioSpinner.getSelectedItem();
                    IOValue value = new IOValue(FilterFactory.NONE, sampleValue, io);
                    item.setValue(value);
                    DialogIO.this.activity.setStatus("Set sample value for " + description.getName());
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
    }
}