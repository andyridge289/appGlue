package com.appglue.layout.view;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.appglue.WiringActivity;
import com.appglue.layout.FragmentFilter;
import com.appglue.R;
import com.appglue.description.SampleValue;
import com.appglue.description.datatypes.IOType;
import com.appglue.engine.model.ComponentService;
import com.appglue.engine.model.IOFilter;
import com.appglue.engine.model.IOValue;
import com.appglue.engine.model.ServiceIO;
import com.appglue.layout.adapter.FilterSampleAdapter;
import com.appglue.layout.adapter.WiringFilterAdapter;
import com.appglue.layout.dialog.DialogIO;
import com.appglue.library.AppGlueLibrary;
import com.appglue.library.FilterFactory;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import static com.appglue.library.AppGlueConstants.AND;
import static com.appglue.library.AppGlueConstants.FILTER_BOOL_VALUES;
import static com.appglue.library.AppGlueConstants.FILTER_NUMBER_VALUES;
import static com.appglue.library.AppGlueConstants.FILTER_SET_VALUES;
import static com.appglue.library.AppGlueConstants.FILTER_STRING_VALUES;
import static com.appglue.library.AppGlueConstants.OR;

public class FilterValueView extends LinearLayout {

    private WiringActivity activity;
    private FragmentFilter ff;

    private RadioGroup radioGroup;
    private RadioButton sampleRadio;
    private RadioButton manualRadio;

    private EditText manualText;
    private Spinner sampleSpinner;
    private Spinner conditionSpinner;

    private Button manualButton;

    private TextView andor;
    private Switch enableSwitch;

    private ComponentService component;

    private IOFilter filter;
    private IOValue value;
    private ServiceIO item;

    public IOValue getValue() {
        return value;
    }

    public FilterValueView(Context context, FragmentFilter ff, ComponentService component,
                           IOFilter filter, IOValue value, ServiceIO item, int childCount) {
        super(context);

        this.ff = ff;
        this.component = component;

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

        this.activity = (WiringActivity) context;

        View v = View.inflate(context, R.layout.fragment_filter_value, null);
        v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        this.addView(v);

        radioGroup = (RadioGroup) v.findViewById(R.id.filter_radio);
        sampleRadio = (RadioButton) v.findViewById(R.id.filter_radio_sample);
        manualRadio = (RadioButton) v.findViewById(R.id.filter_radio_manual);

        manualText = (EditText) v.findViewById(R.id.filter_value_text);
        sampleSpinner = (Spinner) v.findViewById(R.id.filter_value_spinner);
        conditionSpinner = (Spinner) v.findViewById(R.id.filter_condition_spinner);
        manualButton = (Button) v.findViewById(R.id.choose_button);

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

        enableSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setChildrenEnabled(isChecked);
            value.setEnabled(isChecked);
        });

        andor.setOnClickListener(v1 -> {
            // This needs to change the andor-ness of the filter
            if (filter.getCondition(item)) {
                filter.setCondition(item, OR);
            } else {
                filter.setCondition(item, AND);
            }

            // We need to tell all of the other andors to change
            ff.setAndors(item);
        });

        final View remove = v.findViewById(R.id.filter_remove_button);
        remove.setOnClickListener(v1 -> {
            // We'll probably need to kill the value somehow
            filter.getValues(item).remove(value);
            value = null;
            ff.remove(item, FilterValueView.this);
        });

        // At this point we probably need to check if the things have been set and give up if they haven't
        if (item == null || component == null) {
            return;
        }

        IOType type = item.getType();
        ArrayList<SampleValue> values = item.getDescription().getSampleValues();
        if (values == null) {
            values = new ArrayList<>();
        }
        boolean hasValues = values.size() != 0;

        if (type.typeEquals(IOType.Factory.getType(IOType.Factory.BOOLEAN))) {
            if (values.size() == 0) {
                // These might need to be hard-coded as acceptable values
                values = new ArrayList<>();
                values.add(new SampleValue("True", true));
                values.add(new SampleValue("False", false));
            }
            setup(FILTER_BOOL_VALUES, type, hasValues, values);
        } else if (type.typeEquals(IOType.Factory.getType(IOType.Factory.IMAGE_DRAWABLE))) {
            manualButton.setText("Choose drawable");
            manualButton.setOnClickListener(v1 -> {
                DialogIO di = new DialogIO(activity, item, FilterValueView.this);
                di.show();
            });
            setup(FILTER_BOOL_VALUES, type, hasValues, values);
        } else if (type.typeEquals(IOType.Factory.getType(IOType.Factory.PHONE_NUMBER))) {
            manualButton.setText("Choose contact");
            manualButton.setOnClickListener(v1 -> {
                // Look up the contact
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                        ContactsContract.Contacts.CONTENT_URI);
                activity.startActivityForResult(FilterValueView.this, contactPickerIntent, WiringActivity.CONTACT_PICKER_FILTER);
            });
            setup(FILTER_STRING_VALUES, type, hasValues, values);
        } else if (type.typeEquals(IOType.Factory.getType(IOType.Factory.NUMBER))) {
            setup(FILTER_NUMBER_VALUES, type, hasValues, values);
        } else if (type.typeEquals(IOType.Factory.getType(IOType.Factory.SET))) {
            setup(FILTER_SET_VALUES, type, hasValues, values);
        } else if (type.typeEquals(IOType.Factory.getType(IOType.Factory.URL))) {
            setup(FILTER_STRING_VALUES, type, hasValues, values);
        } else if (type.typeEquals(IOType.Factory.getType(IOType.Factory.TEXT))) {
            setup(FILTER_STRING_VALUES, type, hasValues, values);
        } else {
            Logger.e("Type not implemented");
            // Don't know what happens here
        }

        redraw();

        // At this point we need to set the values to be whatever the default things are
        if (manualRadio.isChecked()) {
            // Set the name of the contact to be on the button, and the phone number of the contact to be in the manual text box
            String value = manualText.getText().toString();
            this.value.setManualValue(item.getType().fromString(value));

        } else if (sampleRadio.isChecked()) {
            if (!type.typeEquals(IOType.Factory.getType(IOType.Factory.PHONE_NUMBER))) {
                SampleValue sample = (SampleValue) sampleSpinner.getSelectedItem();
                this.value.setSampleValue(sample);
            }
        }

        this.value.setCondition((FilterFactory.FilterValue) conditionSpinner.getSelectedItem());
    }

    private void setChildrenEnabled(boolean isChecked) {
        radioGroup.setEnabled(isChecked);
        conditionSpinner.setEnabled(isChecked);

        if (!isChecked) {
            manualText.setEnabled(false);
            sampleSpinner.setEnabled(false);
            manualButton.setEnabled(false);
            manualRadio.setEnabled(false);
            sampleRadio.setEnabled(false);
        } else {

            // We need to enable everything based on the values.
            // Then re-disable the things that don't have values

            if (item.getType().supportsManualLookup()) {
                manualButton.setEnabled(true);
                manualButton.setVisibility(View.VISIBLE);
            } else {
                manualButton.setEnabled(false);
                manualButton.setVisibility(View.GONE);
                // We need to resize this so everything isn't swamped
                manualButton.setHeight((int) AppGlueLibrary.dpToPx(activity.getResources(), 40));
            }

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
            Logger.e("For some reason redraw is being called on a dead component");
            return;
        }

        if (filter.hasValues(item)) {
            andor.setText(filter.getCondition(item) ? getResources().getString(R.string.and) :
                    getResources().getString(R.string.or));
        }

        enableSwitch.setChecked(value.isEnabled());
        setChildrenEnabled(value.isEnabled());

        if (value.getManualValue() != null) {
            // Set the name of the contact to be on the button, and the phone number of the contact to be in the manual text box
            String mV = item.getType().toString(value.getManualValue());
            manualText.setText(mV);
            manualRadio.setChecked(true);

            // Set the name of the contact to be on the button, and the phone number of the contact to be in the manual text box
            if (item.getType().typeEquals(IOType.Factory.getType(IOType.Factory.PHONE_NUMBER))) {
                String contactName = AppGlueLibrary.getContactName(getContext(), mV);
                manualButton.setText(contactName);
            }
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

    private void setup(FilterFactory.FilterValue[] conditions, IOType type,
                       boolean hasSamples, ArrayList<SampleValue> values) {

        if (conditionSpinner != null) {
            conditionSpinner.setAdapter(new WiringFilterAdapter(activity, conditions));
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {

            // enable or disable the relevant item
            // Set the condition of the ValueNode
            if (checkedId == R.id.filter_radio_manual) {
                sampleSpinner.setEnabled(false);
                manualText.setEnabled(true);

                if (item.getType().supportsManualLookup()) {
                    manualButton.setEnabled(true);
                }

                if (value != null) {
                    value.setFilterState(IOValue.MANUAL);
                }
            } else { // It must be filter_radio_sample
                sampleSpinner.setEnabled(true);
                manualButton.setEnabled(true);
                manualText.setEnabled(false);

                if (value != null) {
                    value.setFilterState(IOValue.SAMPLE);
                }
            }
        });

        if (type.acceptsManualValues()) {
            manualText.setInputType(type.getManualEditTextType());
        } else {
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

            if (item.getType().supportsManualLookup()) {
                manualButton.setEnabled(true);
            }

            manualRadio.setChecked(true);
            sampleRadio.setChecked(false);

            sampleRadio.setEnabled(false);
            manualRadio.setEnabled(false);

            radioGroup.setEnabled(false);
        }

        conditionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FilterFactory.FilterValue fv = (FilterFactory.FilterValue) conditionSpinner.getSelectedItem();
                if (value != null) {
                    value.setCondition(fv);
                    Logger.d("Setting condition spinner " + position);
                } else {
                    Logger.e("Can't do that, value is null");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Again, don't think we need to do anything here
            }
        });

        if (value.getCondition() != null) {
            conditionSpinner.setSelection(0);
        } else {
            conditionSpinner.setSelection(((WiringFilterAdapter) conditionSpinner.getAdapter()).getPosition(value.getCondition()));
        }

        // Just update the manual value every time they enter a key.
        manualText.setOnKeyListener((v, keyCode, event) -> {
            if (value != null) {
                value.setManualValue(manualText.getText().toString());
            }
            return false; // I'm not sure if we need to consume the event or not
        });

        if (value.getManualValue() != null) {
            manualText.setText(item.getType().toString(item.getValue().getManualValue()));
        }
    }

    public void setContact(Pair<String, ArrayList<String>> data) {
        manualButton.setText(data.first);

        if (data.second.size() == 0) {
            Toast.makeText(getContext(), "You don't have the phone number for the contact you chose", Toast.LENGTH_SHORT).show();
        } else {
            manualText.setText(data.second.get(0));
            value.setManualValue(data.second.get(0));
        }

        manualRadio.setChecked(true);
    }

    /**
     * This is where we go to when we have come back from the IODialog for images
     *
     * @param s The name of the value
     * @param obj The value of the value
     */
    public void setManualValue(String s, Object obj) {

        manualButton.setText(s);

        value.setManualValue(obj);
        String strValue = item.getType().toString(obj);
        manualText.setText(strValue);
    }
}
