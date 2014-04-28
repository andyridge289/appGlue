package com.appglue;

import static com.appglue.library.AppGlueConstants.*;
import static com.appglue.Constants.*;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

import com.appglue.Constants.ProcessType;
import com.appglue.Constants.ServiceType;
import com.appglue.description.ServiceDescription;
import com.appglue.serviceregistry.Registry;

import android.support.v7.app.ActionBar;
//import android.support.
import android.support.v7.app.ActionBarActivity;

public class ActivityComponentList extends ActionBarActivity
{
	private PagerAdapter adapter;
	private ViewPager viewPager;

    private Registry registry;
	
	private boolean justAList;
	private ServiceDescription lastService;
	private int position;
	private boolean isFirst;
	
	private ArrayList<FragmentComponentListLocal> fragments;

	private EditText search;
	
	private final String SHOW_ADV = "Show advanced filter";
	private final String HIDE_ADV = "Hide advanced filter";
	
	public static final int FLAG_TRIGGER = 0;
	public static final int FLAG_NOINPUT = 1;
	public static final int FLAG_NOOUTPUT = 2;
	public static final int FLAG_MATCHING = 3;
	public static final int FLAG_ALL = 4;
	
	private boolean triggersOnly = false;
	private boolean noTriggers = false;
	
	// Flags for filtering
	private boolean fTRIGGER = false;
	private boolean fFILTER = false;
	private boolean fHAS_INPUT = false;
	private boolean fMATCHING = false;
	private boolean fHAS_OUTPUT = false;
	
	private boolean fDEFAULT_SEARCH = true;
	private boolean fSHOW = true;
	
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		
		setContentView(R.layout.activity_component_list);

        this.registry = Registry.getInstance(this);
		
		Intent intent = this.getIntent();
		
		// This is the stuff for story mode
		triggersOnly = intent.getBooleanExtra(TRIGGERS_ONLY, false);
		noTriggers = intent.getBooleanExtra(NO_TRIGGERS, false);
		
		justAList = intent.getBooleanExtra(JUST_A_LIST, false);
		position = intent.getIntExtra(POSITION, -1);
		isFirst = intent.getBooleanExtra(FIRST, false);
		
		fragments = new ArrayList<FragmentComponentListLocal>();
		
		if(triggersOnly)
		{
			Bundle args = new Bundle();
			args.putBoolean(TRIGGERS_ONLY, true);
			FragmentComponentListLocal triggers = new FragmentComponentListLocal();
			triggers.setArguments(args);
			triggers.setName("TRIGGERS");
			fragments.add(triggers);
		}
		else
		{
            if(!noTriggers)
            {
                Bundle args = new Bundle();
                args.putBoolean(TRIGGERS_ONLY, true);
                FragmentComponentListLocal triggers = new FragmentComponentListLocal();
                triggers.setArguments(args);
                triggers.setName("TRIGGERS");
                fragments.add(triggers);
            }

			Bundle noInputArgs = new Bundle();
			noInputArgs.putBoolean(HAS_INPUTS, false);
			noInputArgs.putBoolean(HAS_OUTPUTS, true);			
			FragmentComponentListLocal noInput = new FragmentComponentListLocal();
			noInput.setArguments(noInputArgs);
			noInput.setName("OUTPUT ONLY");
			fragments.add(noInput);
			
			Bundle noOutputArgs = new Bundle();
			noOutputArgs.putBoolean(HAS_INPUTS, true);
			noOutputArgs.putBoolean(HAS_OUTPUTS, false);			
			FragmentComponentListLocal noOutput = new FragmentComponentListLocal();
			noOutput.setArguments(noOutputArgs);
			noOutput.setName("INPUT ONLY");
			fragments.add(noOutput);
			
			Bundle matchingArgs = new Bundle();
			matchingArgs.putBoolean(MATCHING, true);
			matchingArgs.putInt(POSITION, position);
			FragmentComponentListLocal matching = new FragmentComponentListLocal();
			matching.setArguments(matchingArgs);
			matching.setName("MATCHING COMPONENTS");
			fragments.add(matching);
			
			Bundle args = new Bundle();
			args.putBoolean(HAS_INPUTS, true);
			args.putBoolean(HAS_OUTPUTS, true);
			FragmentComponentListLocal all = new FragmentComponentListLocal();
			all.setArguments(args);
			all.setName("ALL COMPONENTS");
			fragments.add(all);
		}
		
		adapter = new PagerAdapter(getSupportFragmentManager());
		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(adapter);
		
		if(!triggersOnly)
		{
			// need to move to one that they might actually want
			if(isFirst)
			{
				// Then we want to be on the trigger page
				viewPager.setCurrentItem(FLAG_NOINPUT);
			}
			else if(position > 0)
			{
				viewPager.setCurrentItem(FLAG_ALL);
			}
		}
		
		String lastServiceName = intent.getStringExtra(LAST_CLASSNAME);
		
		if(lastServiceName != null)
			lastService = registry.getAtomic(lastServiceName);
		
		// Setup the searchbar
//		search = (EditText) findViewById(R.id.component_search);
//		search.setOnFocusChangeListener(new View.OnFocusChangeListener() 
//		{
//			@Override
//			public void onFocusChange(View v, boolean hasFocus) 
//			{
//				EditText et = (EditText) v;
//				if(hasFocus)
//				{
//					if(et.getText().toString().equals("Search"))
//					{
//						fDEFAULT_SEARCH = false;
//						et.setText("");
//					}
//				}
//				else
//				{
//					if(et.getText().toString().equals(""))
//					{
//						et.setText("Search");
//						fDEFAULT_SEARCH = true;
//					}
//				}
//			}
//		});
		
//		search.addTextChangedListener(new TextWatcher() 
//		{	
//			@Override
//			public void onTextChanged(CharSequence s, int start, int before, int count) {}
//			
//			@Override
//			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//			
//			@Override
//			public void afterTextChanged(Editable s) 
//			{
//				FragmentComponentList currentFragment = ((FragmentComponentList) getCurrentFragment());
//				if(currentFragment == null)
//					return;
//				
//				AdapterComponentList localAdapter = currentFragment.getAdapter();
//				if(localAdapter != null)
//				{
//					localAdapter.getFilter().filter(s);
//					localAdapter.notifyDataSetChanged();
//				}
//			}
//		});
		
		// Setup the checkbox filters
		((CheckBox) findViewById(R.id.component_adv_trigger)).setOnCheckedChangeListener(new OnCheckedChangeListener() 
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
			{
				fTRIGGER = isChecked;
				FragmentComponentList currentFragment = ((FragmentComponentList) getCurrentFragment());
				if(currentFragment == null)
					return;
				
				AdapterComponentList adapter = currentFragment.getAdapter();
				adapter.getFilter().filter(fDEFAULT_SEARCH ? "" : search.getText());
				adapter.notifyDataSetChanged();
			}
		});
		((CheckBox) findViewById(R.id.component_adv_filter)).setOnCheckedChangeListener(new OnCheckedChangeListener() 
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
			{
				fFILTER = isChecked;
				FragmentComponentList currentFragment = ((FragmentComponentList) getCurrentFragment());
				if(currentFragment == null)
					return;
				
				AdapterComponentList adapter = currentFragment.getAdapter();
				adapter.getFilter().filter(fDEFAULT_SEARCH ? "" : search.getText());
				adapter.notifyDataSetChanged();
			}
		});
		((CheckBox) findViewById(R.id.component_adv_inputs)).setOnCheckedChangeListener(new OnCheckedChangeListener() 
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
			{
				fHAS_INPUT = isChecked;
				FragmentComponentList currentFragment = ((FragmentComponentList) getCurrentFragment());
				if(currentFragment == null)
					return;
				
				AdapterComponentList adapter = currentFragment.getAdapter();
				adapter.getFilter().filter(fDEFAULT_SEARCH ? "" : search.getText());
				adapter.notifyDataSetChanged();
			}
		});
		((CheckBox) findViewById(R.id.component_adv_outputs)).setOnCheckedChangeListener(new OnCheckedChangeListener() 
		{
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
			{
				fHAS_OUTPUT = isChecked;
				FragmentComponentList currentFragment = ((FragmentComponentList) getCurrentFragment());
				if(currentFragment == null)
					return;
				
				AdapterComponentList adapter = currentFragment.getAdapter();
				adapter.getFilter().filter(fDEFAULT_SEARCH ? "" : search.getText());
				adapter.notifyDataSetChanged();
			}
		});
		
		findViewById(R.id.component_adv_hide).setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				// then we need to hide the advanced filter
				findViewById(R.id.component_adv_container).setVisibility(View.GONE);
				
				// And change the title of the menu item to show
				ActivityComponentList.this.invalidateOptionsMenu();
			}
		});
		
		ActionBar actionBar = getSupportActionBar();
		boolean createNew = intent.getBooleanExtra(CREATE_NEW, false);
		
		if(actionBar != null)
		{
			if(!createNew)
				actionBar.setTitle("Available components");
			else
				actionBar.setTitle("Choose first component");
				
			actionBar.setHomeButtonEnabled(true);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		if(fSHOW)
			menu.add(SHOW_ADV).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		else
			menu.add(HIDE_ADV).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		
		return true;
	}
	
	public Fragment getCurrentFragment()
	{
		return adapter.getItem(viewPager.getCurrentItem());
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == android.R.id.home)
		{
			finish();
			return true;
		}
		else if(item.getTitle().toString().equals(SHOW_ADV))
		{
			// Then we need to show the advanced filter
			findViewById(R.id.component_adv_container).setVisibility(View.VISIBLE);
			
			// And change the title of this one to hide
			item.setTitle(HIDE_ADV);
		}
		else if(item.getTitle().toString().equals(HIDE_ADV))
		{
			// then we need to hide the advanced filter
			findViewById(R.id.component_adv_container).setVisibility(View.GONE);
			
			// And change the title of this one to show
			item.setTitle(SHOW_ADV);
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	public boolean justAList()
	{
		return justAList;
	}
	
	public ServiceDescription getLastService()
	{
		return lastService;
	}
	
	public boolean isComponentList()
	{
		return justAList;
	}
	
	public boolean isTriggerSet()
	{
		return fTRIGGER;
	}

	public boolean isFilterSet()
	{
		return fFILTER;
	}
	
	public boolean hasInputSet()
	{
		return fHAS_INPUT;
	}
	
	public boolean hasOutputSet()
	{
		return fHAS_OUTPUT;
	}
	
	public boolean areAnySet()
	{
		return fTRIGGER || fFILTER || fHAS_INPUT || fHAS_OUTPUT;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		if(intent == null)
			return;
		
		int result = intent.getIntExtra(RESULT, NOT_SET);
		
		if(justAList)
		{
//			localFragment.update(registry.getAllDeviceServices());
//			remoteFragment.update(new ArrayList<ServiceDescription>());
			
			return;
		}
		
				
		switch(result)
		{
			case MARKET_LOOKUP:
			{
				// Make the lists do a refresh
//				localFragment.update(registry.getAllDeviceServices());
//				remoteFragment.update(new ArrayList<ServiceDescription>());
				break;
			}
				
			case SUCCESS:
				// Don't think we need to do anything here
				break;
		
			case NOT_SET:
				// They haven't selected anything, so don't do anything
				return;
		}
		
		String className = intent.getStringExtra(CLASSNAME);
		int serviceType = intent.getIntExtra(SERVICE_TYPE, -1);
		
		Intent i = new Intent();
		i.putExtra(CLASSNAME, className);
		i.putExtra(SERVICE_TYPE, serviceType);
		i.putExtra(FIRST, isFirst);
		i.putExtra(POSITION, position);
		
		if (getParent() == null) 
		{
		    setResult(Activity.RESULT_OK, i);
		}
		else 
		{
		    getParent().setResult(Activity.RESULT_OK, i);
		}
		
		finish();
	}
	
	public void chosenItem(String className, ServiceType type)
	{
		Intent i = new Intent();
		i.putExtra(CLASSNAME, className);
		i.putExtra(SERVICE_TYPE, type.index);
		i.putExtra(FIRST, isFirst);
		i.putExtra(INDEX, position);
		
		Log.w(TAG, "Putting before I finish: " +  className + " " + position);
		
		if (getParent() == null) 
		{
		    setResult(Activity.RESULT_OK, i);
		}
		else 
		{
		    getParent().setResult(Activity.RESULT_OK, i);
		}
		
		finish();
	}
	
	private class PagerAdapter extends FragmentStatePagerAdapter
	{
		public PagerAdapter(FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public Fragment getItem(int i) 
		{
            if(fragments.get(i) == null)
            {
                // TODO Do something better than this
                return fragments.get(0);
            }
            else return fragments.get(i);

		}

		@Override
		public int getCount() 
		{
			return fragments.size();
		}
		
		public CharSequence getPageTitle(int position)
		{
			return fragments.get(position).getName();
		}

	}
}
