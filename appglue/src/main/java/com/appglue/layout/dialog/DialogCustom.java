package com.appglue.layout.dialog;

import java.util.ArrayList;

import com.appglue.ActivityWiring;
import com.appglue.IOValue;
import com.appglue.ServiceIO;
import com.appglue.layout.WiringMap;
import com.appglue.layout.adapter.FilterSampleAdapter;
import com.appglue.layout.adapter.WiringFilterAdapter;
import com.appglue.library.IOFilter;
import com.appglue.library.IOFilter.FilterValue;
import com.appglue.serviceregistry.Registry;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

public abstract class DialogCustom extends AlertDialog 
{
	protected ActivityWiring activity;
	protected WiringMap parent;
	protected ServiceIO item;
	protected Registry registry;
	
	protected DialogCustom(ActivityWiring context, WiringMap parent, ServiceIO item) 
	{
		super(context);
		this.activity = context;
		this.parent = parent;
		this.item = item;
		this.registry = Registry.getInstance(context);
	}

	protected void setupDialog(Spinner fcs, FilterValue[] conditions, int type,
			boolean hasSamples, Spinner fvs, ArrayList<IOValue> values,
			RadioButton rs, RadioButton rt, ServiceIO item,
			Class<? extends Object> cast, EditText fvt) {

		if (fcs != null)
			fcs.setAdapter(new WiringFilterAdapter(activity,
					android.R.layout.simple_dropdown_item_1line, conditions));

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
					android.R.layout.simple_dropdown_item_1line, values));
			rs.setChecked(true);
		} else {
			fvs.setAdapter(new FilterSampleAdapter(activity,
					android.R.layout.simple_dropdown_item_1line, values));
			fvs.setEnabled(false);
			rt.setChecked(true);
			rs.setEnabled(false);
			rt.setEnabled(false);
		}

		// Make it load the saved filter value
		if (item.isFiltered() == ServiceIO.MANUAL_FILTER) {

			String result = item.getType().toString(item.getManualValue());
			fvt.setText(result);
			rt.setChecked(true);
		} else if (item.isFiltered() == ServiceIO.SAMPLE_FILTER) {
			IOValue selected = item.getChosenSampleValue();
			for (int i = 0; i < fvs.getAdapter().getCount(); i++) {
				IOValue ioValue = (IOValue) fvs.getItemAtPosition(i);
				if (ioValue.equals(selected)) {
					fvs.setSelection(i, true);
					break;
				}
			}
		}

		if (item.isFiltered() != ServiceIO.UNFILTERED && fcs != null) {
			FilterValue fv = IOFilter.filters.get(item.getCondition());

			for (int i = 0; i < fcs.getAdapter().getCount(); i++) {
				FilterValue fv2 = (FilterValue) fcs.getItemAtPosition(i);
				if (fv.index == fv2.index) {
					fcs.setSelection(i, true);
					break;
				}
			}
		}
	}
}
