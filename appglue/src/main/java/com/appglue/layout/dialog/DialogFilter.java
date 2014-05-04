package com.appglue.layout.dialog;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;

import com.appglue.ActivityWiring;
import com.appglue.IOValue;
import com.appglue.R;
import com.appglue.ServiceIO;
import com.appglue.datatypes.IOType;
import com.appglue.layout.WiringMap;
import com.appglue.library.IOFilter.FilterValue;

import java.util.ArrayList;

import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.FILTER_BOOL_VALUES;
import static com.appglue.library.AppGlueConstants.FILTER_NUMBER_VALUES;
import static com.appglue.library.AppGlueConstants.FILTER_SET_VALUES;
import static com.appglue.library.AppGlueConstants.FILTER_STRING_VALUES;

public class DialogFilter extends DialogCustom
{
	public DialogFilter(ActivityWiring activity, WiringMap wm, final ServiceIO io) 
	{
		super(activity, wm, io);
		
		LayoutInflater inflater = activity.getLayoutInflater();
		final View v = inflater.inflate(R.layout.dialog_filter, null);
		
		// 1 - Work out what we're filtering and set up the shit
		setTitle("Filter: " + item.getFriendlyName());
		
		final EditText filterValueText = (EditText) v.findViewById(R.id.filter_value_text);
		final Spinner filterValueSpinner = (Spinner) v.findViewById(R.id.filter_value_spinner);
		final Spinner filterConditionSpinner = (Spinner) v.findViewById(R.id.filter_condition_spinner);
		
		IOType type = item.getType();
		ArrayList<IOValue> values = item.getSampleValues();
		if(values == null)
			values = new ArrayList<IOValue>();
		
		final boolean hasSamples = values.size() != 0;
		
		if(!hasSamples)
		{
			values = new ArrayList<IOValue>();
			values.add(new IOValue("No samples", ""));
		}
		
		// XXX Compound filters
		
		final RadioGroup radio = (RadioGroup) v.findViewById(R.id.filter_radio);
		final RadioButton spinnerRadio = (RadioButton) v.findViewById(R.id.filter_radio_spinner);
		final RadioButton textRadio = (RadioButton) v.findViewById(R.id.filter_radio_text);
		
		radio.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch(checkedId)
                {
	                case R.id.filter_radio_spinner:
                		filterValueSpinner.setEnabled(true);
	                	filterValueText.setEnabled(false);
	                    break;
	                    
	                case R.id.filter_radio_text:
	                	filterValueSpinner.setEnabled(false);
	                	filterValueText.setEnabled(true);
	                    break;
                }
            }
        });
		
		// 2 - Set up all of the components
			// Condition spinner needs to have the right things
			// Set value spinner needs to have the right things or be disabled saying there are none
			// Text field needs to be the right type or be hidden if boolean
		
		if(type.equals(IOType.Factory.getType(IOType.Factory.TEXT)))
		{
			setupDialog(filterConditionSpinner, FILTER_STRING_VALUES, InputType.TYPE_CLASS_TEXT,
							  hasSamples, filterValueSpinner,  values, spinnerRadio, textRadio, item,
							  String.class, filterValueText);
		}
		else if(type.equals(IOType.Factory.getType(IOType.Factory.NUMBER)))
	    {
			setupDialog(filterConditionSpinner, FILTER_NUMBER_VALUES, InputType.TYPE_CLASS_NUMBER,
					  		  hasSamples, filterValueSpinner,  values, spinnerRadio, textRadio, item, 
					  		  Integer.class, filterValueText);
	    }
		else if(type.equals(IOType.Factory.getType(IOType.Factory.SET)))
	    {
	    	setupDialog(filterConditionSpinner, FILTER_SET_VALUES, -1,
    				hasSamples, filterValueSpinner, values, spinnerRadio, textRadio, item,
    				Integer.class, filterValueText);
	    }
		else if(type.equals(IOType.Factory.getType(IOType.Factory.BOOLEAN)))
		{
			if(!hasSamples)
			{
				// These might need to be hard-coded as acceptable values
				values = new ArrayList<IOValue>();
				values.add(new IOValue("True", true));
				values.add(new IOValue("False", false));
			}
			
			setupDialog(filterConditionSpinner, FILTER_BOOL_VALUES, InputType.TYPE_CLASS_TEXT,
			  		  hasSamples, filterValueSpinner,  values, spinnerRadio, textRadio, item, 
			  		  Integer.class, filterValueText);
		}
		else if(type.equals(IOType.Factory.getType(IOType.Factory.PHONE_NUMBER)))
		{
			setupDialog(filterConditionSpinner, FILTER_STRING_VALUES, InputType.TYPE_CLASS_PHONE,
					  hasSamples, filterValueSpinner,  values, spinnerRadio, textRadio, item,
					  String.class, filterValueText);
		}
		else if(type.equals(IOType.Factory.getType(IOType.Factory.URL)))
		{
			setupDialog(filterConditionSpinner, FILTER_STRING_VALUES, InputType.TYPE_CLASS_TEXT,
					  hasSamples, filterValueSpinner,  values, spinnerRadio, textRadio, item,
					  String.class, filterValueText);
		}
	    else
	    {
            Log.e(TAG, "Type not implemented");
	    	// Don't know what happens here
	    }
		
		
		setView(v);
		
		Button positiveButton = (Button) v.findViewById(R.id.dialog_filter_positive);
		Button neutralButton = (Button) v.findViewById(R.id.dialog_filter_neutral);
		Button negativeButton = (Button) v.findViewById(R.id.dialog_filter_negative);
		
		positiveButton.setOnClickListener(new View.OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				// Get the value they entered - not sure what happens 
				if(textRadio.isChecked())
				{
					Object value = item.getType().fromString(filterValueText.getText().toString());
					// This should work, it's the same as the other stuff. But it might not...
					item.setManualValue(value);
					item.setFilterState(ServiceIO.MANUAL_FILTER);
					DialogFilter.this.activity.setStatus("Set manual filter for " + item.getName());
				}
				else if(spinnerRadio.isChecked())
				{
					// Then look up the index of the spinner that's selected - shouldn't need to worry about data types
					IOValue value = (IOValue) filterValueSpinner.getSelectedItem();
					item.setChosenSampleValue(value);
					item.setFilterState(ServiceIO.SAMPLE_FILTER);
					DialogFilter.this.activity.setStatus("Set sample filter for " + item.getName());
				}
				
	    		// Now we just need to make sure that the condition is set
				FilterValue condition = (FilterValue) filterConditionSpinner.getSelectedItem();
	    		item.setCondition(condition.index);
	    		
	    		// The setting of the list values needs to move to the creating of the list. Do an invalidate
	    		registry.updateCurrent();
	    		parent.redraw();
	    		dismiss();
			}
		});
		
		negativeButton.setOnClickListener(new View.OnClickListener() 
	    {
	    	public void onClick(View v) 
	    	{
	    		cancel();
            }
	    });
		
		neutralButton.setOnClickListener(new View.OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				item.setFilterState(ServiceIO.UNFILTERED);
				DialogFilter.this.activity.setStatus("Cleared filter for " + item.getName());
				registry.updateCurrent();
				parent.redraw();
			}
		});
	}
}
