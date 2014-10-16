package com.appglue.layout;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.appglue.ActivityWiring;
import com.appglue.FragmentFilter;
import com.appglue.R;
import com.appglue.description.SampleValue;
import com.appglue.description.datatypes.IOType;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.IOFilter;
import com.appglue.engine.description.IOValue;
import com.appglue.engine.description.ServiceIO;
import com.appglue.layout.adapter.FilterSampleAdapter;
import com.appglue.layout.adapter.WiringFilterAdapter;
import com.appglue.library.FilterFactory;

import java.util.ArrayList;

import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.AND;
import static com.appglue.library.AppGlueConstants.FILTER_BOOL_VALUES;
import static com.appglue.library.AppGlueConstants.FILTER_NUMBER_VALUES;
import static com.appglue.library.AppGlueConstants.FILTER_SET_VALUES;
import static com.appglue.library.AppGlueConstants.FILTER_STRING_VALUES;
import static com.appglue.library.AppGlueConstants.OR;

public class FilterValueView extends LinearLayout {

    private ActivityWiring activity;
    private FragmentFilter ff;

    private RadioGroup radioGroup;
    private RadioButton sampleRadio;
    private RadioButton manualRadio;

    private EditText manualText;
    private Spinner sampleSpinner;
    private Spinner filterConditionSpinner;

    private TextView andor;
    private Switch enableSwitch;

    private ComponentService component;

    private IOFilter filter;
    private IOValue value;
    private ServiceIO item;

    public FilterValueView(Context context, FragmentFilter ff, ComponentService component,
                           IOFilter filter, IOValue value, ServiceIO item, int childCount) {
        super(context);

        this.ff = ff;
        this.component = component;

        if(value == null) {
            value = new IOValue(item);
            filter.addValue(item, value);
        }

        this.value = value;
        this.filter = filter;
        this.item = item;

        create(context, childCount);
    }

    public FilterValueView(Context context) {
        super(context);
        create(context, 0);
    }

    public FilterValueView(Context context, AttributeSet attrs) {
        super(context, attrs);
        create(context, 0);
    }

    public FilterValueView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        create(context, 0);
    }

    private void create(Context context, int childCount) {
        this.setWillNotDraw(false);

        this.activity = (ActivityWiring) context;

        View v = View.inflate(context, R.layout.fragment_filter_value, null);
        v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        this.addView(v);

        radioGroup = (RadioGroup) v.findViewById(R.id.filter_radio);
        sampleRadio = (RadioButton) v.findViewById(R.id.filter_radio_sample);
        manualRadio = (RadioButton) v.findViewById(R.id.filter_radio_manual);

        manualText = (EditText) v.findViewById(R.id.filter_value_text);
        sampleSpinner = (Spinner) v.findViewById(R.id.filter_value_spinner);
        filterConditionSpinner = (Spinner) v.findViewById(R.id.filter_condition_spinner);

        // Need to set up the and or button
        andor = (TextView) v.findViewById(R.id.filter_value_andor);
        enableSwitch = (Switch) v.findViewById(R.id.filter_value_switch);
        View topBorder = v.findViewById(R.id.top_border);

        if (childCount == 0) {
            andor.setVisibility(View.GONE);
            topBorder.setVisibility(View.GONE);
        } else {
            andor.setVisibility(View.VISIBLE);
            topBorder.setVisibility(View.VISIBLE);
        }

        enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setChildrenEnabled(isChecked);
                value.setEnabled(isChecked);
            }
        });

        andor.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // This needs to change the andor-ness of the filter
                if(filter.getCondition(item)) {
                    filter.setCondition(item, OR);
                } else {
                    filter.setCondition(item, AND);
                }

                // We need to tell all of the other andors to change
                ff.setAndors(item);
            }
        });

        final View removeFab = v.findViewById(R.id.filter_remove_button);
        removeFab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // We'll probably need to kill the value somehow
                filter.getValues(item).remove(value);
                value = null;
                ff.remove(item, FilterValueView.this);
            }
        });

        // At this point we probably need to check if the things have been set and give up if they haven't
        if (item == null || component == null) {
            return;
        }

        IOType type = item.getType();
        ArrayList<SampleValue> values = item.getDescription().getSampleValues();
        if (values == null)
            values = new ArrayList<SampleValue>();
        boolean hasValues = values.size() != 0;

        if (type.typeEquals(IOType.Factory.getType(IOType.Factory.TEXT))) {
            setup(FILTER_STRING_VALUES, InputType.TYPE_CLASS_TEXT,
                    hasValues, values);
        } else if (type.typeEquals(IOType.Factory.getType(IOType.Factory.NUMBER))) {
            setup(FILTER_NUMBER_VALUES, InputType.TYPE_CLASS_NUMBER,
                    hasValues, values);
        } else if (type.typeEquals(IOType.Factory.getType(IOType.Factory.SET))) {
            setup(FILTER_SET_VALUES, -1,
                    hasValues, values);
        } else if (type.typeEquals(IOType.Factory.getType(IOType.Factory.BOOLEAN))) {
            if (values.size() != 0) {
                // These might need to be hard-coded as acceptable values
                values = new ArrayList<SampleValue>();
                values.add(new SampleValue("True", true));
                values.add(new SampleValue("False", false));
            }
            setup(FILTER_BOOL_VALUES, InputType.TYPE_CLASS_TEXT,
                    hasValues, values);
        } else if (type.typeEquals(IOType.Factory.getType(IOType.Factory.PHONE_NUMBER))) {
            setup(FILTER_STRING_VALUES, InputType.TYPE_CLASS_PHONE,
                    hasValues, values);
        } else if (type.typeEquals(IOType.Factory.getType(IOType.Factory.URL))) {
            setup(FILTER_STRING_VALUES, InputType.TYPE_CLASS_TEXT,
                    hasValues, values);
        } else {
            Log.e(TAG, "Type not implemented");
            // Don't know what happens here
        }

        redraw();

        // At this point we need to set the values to be whatever the default things are
        if (manualRadio.isChecked()) {
            String value = manualText.getText().toString();
            this.value.setManualValue(item.getType().fromString(value));
        } else if (sampleRadio.isChecked()) {
            SampleValue sample = (SampleValue) sampleSpinner.getSelectedItem();
            this.value.setSampleValue(sample);
        }

        this.value.setCondition((FilterFactory.FilterValue) filterConditionSpinner.getSelectedItem());
    }

    private void setChildrenEnabled(boolean isChecked) {
        radioGroup.setEnabled(isChecked);
        filterConditionSpinner.setEnabled(isChecked);

        if (!isChecked) {
            manualText.setEnabled(false);
            sampleSpinner.setEnabled(false);
            manualRadio.setEnabled(false);
            sampleRadio.setEnabled(false);
        } else {

            // We need to enable everything based on the values.
            // Then re-disable the things that don't have values

            if (item.getType().acceptsManualValues()) {
                manualRadio.setEnabled(true);
                if (manualRadio.isChecked()) {
                    manualText.setEnabled(true);
                }
            }

            if (item.getDescription().hasSampleValues()) {
                sampleRadio.setEnabled(true);
                if (sampleRadio.isChecked()) {
                    sampleSpinner.setEnabled(true);
                }
            }
        }
    }

    public void redraw() {
        if (andor == null) {
            Log.e(TAG, "For some reason redraw is being called on a dead component");
            return;
        }

        if (filter.hasValues(item)) {
            andor.setText(filter.getCondition(item) ? getResources().getString(R.string.and) :
                          getResources().getString(R.string.or));
        }

        enableSwitch.setChecked(value.isEnabled());
        setChildrenEnabled(value.isEnabled());

        if (value.getManualValue() != null) {
            manualText.setText(item.getType().toString(value.getManualValue()));
            manualRadio.setChecked(true);
        }

        if (value.getSampleValue() != null) {
            SampleValue selected = value.getSampleValue();
            for (int i = 0; i < sampleSpinner.getAdapter().getCount(); i++) {
                SampleValue sampleValue = (SampleValue) sampleSpinner.getItemAtPosition(i);
                if (sampleValue.equals(selected)) {
                    sampleSpinner.setSelection(i, true);
                    break;
                }
            }
        }

        if (item.getDescription().hasSampleValues() && item.getType().acceptsManualValues()) {
            if (value.getFilterState() == IOValue.MANUAL) {
                manualRadio.setChecked(true);
            } else {
                // If there are sample values then default to that one on UNFILTERED
                sampleRadio.setChecked(true);
            }
        }
    }

    private void setup(FilterFactory.FilterValue[] conditions, int type,
                       boolean hasSamples, ArrayList<SampleValue> values) {

        if (filterConditionSpinner != null)
            filterConditionSpinner.setAdapter(new WiringFilterAdapter(activity, conditions));

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                // enable or disable the relevant item
                // Set the condition of the ValueNode
                if (checkedId == R.id.filter_radio_manual) {
                    sampleSpinner.setEnabled(false);
                    manualText.setEnabled(true);

                    if(value != null) {
                        value.setFilterState(IOValue.MANUAL);
                    }
                } else { // It must be filter_radio_sample
                    sampleSpinner.setEnabled(true);
                    manualText.setEnabled(false);
                    if (value != null) {
                        value.setFilterState(IOValue.SAMPLE);
                    }
                }
            }
        });

        if (type != -1)
            manualText.setInputType(type);
        else {
            // Hide the manual aspect
            manualText.setEnabled(false);

            sampleRadio.setChecked(true);
            manualRadio.setChecked(false);

            sampleRadio.setEnabled(false);
            manualRadio.setEnabled(false);
        }

        if (hasSamples) {
            sampleSpinner.setAdapter(new FilterSampleAdapter(activity, values));
            sampleRadio.setChecked(true);
            sampleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    SampleValue sample = (SampleValue) sampleSpinner.getSelectedItem();
                    if (value != null) {
                        value.setSampleValue(sample);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Don't think we need to do anything here, they should just remove the thing if they don't want it anymore
                }
            });
        } else {
            sampleSpinner.setEnabled(false);

            manualRadio.setChecked(true);
            sampleRadio.setChecked(false);

            sampleRadio.setEnabled(false);
            manualRadio.setEnabled(false);

            radioGroup.setEnabled(false);
        }

        filterConditionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FilterFactory.FilterValue fv = (FilterFactory.FilterValue) filterConditionSpinner.getSelectedItem();
                if (value != null) {
                    value.setCondition(fv);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Again, don't think we need to do anything here
            }
        });

        // Just update the manual value every time they enter a key.
        manualText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (value != null) {
                    value.setManualValue(manualText.getText().toString());
                }
                return false; // I'm not sure if we need to consume the event or not
            }
        });
    }
}