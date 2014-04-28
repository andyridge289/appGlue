package com.appglue;

import static com.appglue.Constants.*;
import static com.appglue.library.AppGlueConstants.*;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.appglue.description.ServiceDescription;
import com.appglue.engine.CompositeService;
import com.appglue.layout.DepthPageTransformer;
import com.appglue.serviceregistry.Registry;

public class ActivityWiring extends FragmentActivity
{
	private CompositeService cs;
	private int currentIndex;

    // FIXME Only have one temp service
	
	private ViewPager pager;
	private WiringPagerAdapter pagerAdapter;
	private TextView status;
	
	private Registry registry;
	
	public static final int MODE_WIRING = 0;
	public static final int MODE_SETTING = 1;
	
	private int mode = MODE_SETTING;
	private Button modeButton;
	
	private TextView csNameText;
	private EditText csNameEdit;
	private Button csNameSet;
	
	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.activity_wiring);
		
		ActionBar actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setTitle(R.string.comp_title);
		
		Intent extras = this.getIntent();
		long compositeId = extras.getLongExtra(COMPOSITE_ID, -1);
		
		registry = Registry.getInstance(this);
		cs = registry.getService();
		
		if(cs == null)
		{
			registry.createService();
			cs = registry.getService();
		}
		
		status = (TextView) findViewById(R.id.status);
		
		csNameText = (TextView) findViewById(R.id.cs_name);
		csNameEdit = (EditText) findViewById(R.id.cs_name_edit);
		csNameSet = (Button) findViewById(R.id.cs_name_edit_button);
		
		if(cs.getName().equals(""))
		{
			csNameText.setText("Temp name");
			csNameEdit.setText("Temp name");
		}
		else
		{
			csNameText.setText(cs.getName());
			csNameEdit.setText(cs.getName());
		}
		
		csNameText.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				csNameText.setVisibility(View.GONE);
				csNameEdit.setVisibility(View.VISIBLE);
				csNameSet.setVisibility(View.VISIBLE);
			}
		});
		
		csNameSet.setOnClickListener(new OnClickListener() 
		{	
			@Override
			public void onClick(View v) 
			{
				String name = csNameEdit.getText().toString();
				cs.setName(name);
				registry.updateCurrent();
				csNameText.setText(name);
				
				csNameText.setVisibility(View.VISIBLE);
				csNameEdit.setVisibility(View.GONE);
				csNameSet.setVisibility(View.INVISIBLE);
			}
		});

		pager = (ViewPager) findViewById(R.id.pager);	
		pager.setOnPageChangeListener(new OnPageChangeListener() 
		{
			@Override
			public void onPageSelected(int arg0) 
			{
				invalidateOptionsMenu();
			}

			@Override
			public void onPageScrollStateChanged(int arg0) 
			{
				
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) 
			{
				
			}
		});
		
		modeButton = (Button) findViewById(R.id.change_mode);
		modeButton.setText(mode == MODE_WIRING ? "Setting Mode" : "Wiring Mode");
		modeButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				if(mode == MODE_WIRING)
					mode = MODE_SETTING;
				else
					mode = MODE_WIRING;  
				
				redraw();
			}
		});
		
		Intent intent = this.getIntent();
		int index = intent.getIntExtra(INDEX, -1);
		boolean createNew = intent.getBooleanExtra(CREATE_NEW, false);
		
		Log.d(TAG, "Create new: " + createNew);
		
		if(createNew)
		{
			Log.w(TAG, "In resume, going to get another component");
			// Then we need to add a component.
			Intent anotherIntent = new Intent(ActivityWiring.this, ActivityComponentList.class);
			anotherIntent.putExtra(POSITION, 0);
			anotherIntent.putExtra(FIRST, true);
			anotherIntent.putExtra(CREATE_NEW, true);
			startActivityForResult(anotherIntent, SERVICE_REQUEST);			
		}
		else
		{
			pager.setCurrentItem(index);
		}
	}
	
	
	
	public void onPause()
	{
		
		
		super.onPause();
	}
	
	public void onResume()
	{
		super.onResume();
		
		pagerAdapter = new WiringPagerAdapter(getFragmentManager());
		pager.setAdapter(pagerAdapter);
		
		Intent intent = this.getIntent();
		int index = intent.getIntExtra(INDEX, -1);
		
		if(index != -1)
		{
			pager.setCurrentItem(index);
		}
	}
	
	private void redraw()
	{
		if(mode == MODE_WIRING)
		{
			getActionBar().setSubtitle("Wire up connections");
		}
		else
		{
			getActionBar().setSubtitle("Manualy set data");
		}
		
		// Tell all the fragments to redraw...
		for(int i = 0; i < pagerAdapter.getCount(); i++)
		{
			FragmentWiring f = (FragmentWiring) pagerAdapter.getItem(i);
			f.redraw();
		}
	}
	
	public int getCurrentIndex()
	{
		return currentIndex;
	}
	
	public ArrayList<ServiceDescription> getComponents()
	{
		return cs.getComponents();
	}
	
	public int getNumComponents()
	{
		return this.cs.getComponents().size();
	}
	
	public int getMode()
	{
		return mode;
	}
	
	public void setStatus(String statusString)
	{
		status.setText(statusString);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.wiring, menu);
	    
	    menu.findItem(R.id.wiring_previous).setEnabled(pager.getCurrentItem() > 0);
	    
	    ArrayList<ServiceDescription> components = cs.getComponents();
	    
	    if(components != null & components.size() > 0)
	    	menu.findItem(R.id.wiring_next).setEnabled(pager.getCurrentItem() < components.size());
		
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == android.R.id.home)
		{
			finish();
		}
		else if(item.getItemId() == R.id.wiring_overview)
		{
			Intent intent = new Intent(ActivityWiring.this, ActivityCompositionCanvas.class);
			intent.putExtra(COMPOSITE_ID, cs.getId());
			startActivity(intent);
		}
		
		else if(item.getItemId() == R.id.wiring_done)
		{
			if(cs.getId() != -1)
			{
				// This means it's been saved
				boolean success = registry.updateWiring(cs);
				
				if(success && LOG)
					Log.d(TAG, "Updated " + cs.getName());
			}

            // TODO The adding things placeholder needs to have some kind of highlight when you click on it
			// TODO Component list doesn't need to have a status message
            // TODO If it's saying choose first components, it needs to have triggers too.
            // FIXME We're running out of memory and I'm not sure why

			Intent intent = new Intent();
			if (getParent() == null) 
			{
			    setResult(Activity.RESULT_OK, intent);
			}
			else 
			{
			    getParent().setResult(Activity.RESULT_OK, intent);
			}
			finish();
		}
		else if(item.getItemId() == R.id.wiring_previous)
		{
			pager.setCurrentItem(pager.getCurrentItem() - 1);
		}
		else if(item.getItemId() == R.id.wiring_next)
		{
			pager.setCurrentItem(pager.getCurrentItem() + 1);
		}
		
		return true;
	}
	
	
	/**
	 * Whatever happens, update the current one
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		Log.w(TAG, "Wiring: RESULT!!!");
		CompositeService cs = registry.getService();
		if(requestCode == SERVICE_REQUEST && resultCode == Activity.RESULT_OK)
		{
			String className = intent.getStringExtra(CLASSNAME);
			final int position = intent.getIntExtra(INDEX, -1);
			Log.w(TAG, "Got " + className + " at " + position);
			
			ServiceDescription component = registry.getAtomic(className);
			final boolean first = intent.getBooleanExtra(FIRST, false);
			
			if(first)
			{
				cs.addComponent(position, component);
				pagerAdapter.notifyDataSetChanged();
				redraw();
				pager.postDelayed(new Runnable(){
					public void run(){
						pager.setCurrentItem(position + 1);
					}
				}, 100);
			}
			else
			{
				// Put it on the end and don't move!
				cs.addComponent(component);
				pagerAdapter.notifyDataSetChanged();
				redraw();
				pager.postDelayed(new Runnable(){
					public void run(){
						pager.setCurrentItem(position - 1);
					}
				}, 100);
			}
			
			registry.updateCurrent();
		}
	}
	
	private class WiringPagerAdapter extends FragmentStatePagerAdapter
	{
		private Fragment[] fragments;
		
		public WiringPagerAdapter(FragmentManager fragmentManager)
		{
			super(fragmentManager);
			fragments = new Fragment[cs.getComponents().size() + 1];
		}

		@Override
        public Fragment getItem(int position) 
		{
			if(fragments.length <= position)
			{
				fragments = new Fragment[cs.getComponents().size() + 1];
			}
			
			if(fragments[position] == null)
				fragments[position] = FragmentWiring.create(position);
			
            return fragments[position];
        }

        @Override
        public int getCount() 
        {
        	ArrayList<ServiceDescription> components = cs.getComponents();
            return components == null ? 0 : components.size() + 1;
        }
    }
}
