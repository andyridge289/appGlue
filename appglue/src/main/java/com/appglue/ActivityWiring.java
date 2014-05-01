package com.appglue;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.appglue.description.ServiceDescription;
import com.appglue.engine.CompositeService;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

import static com.appglue.Constants.CLASSNAME;
import static com.appglue.Constants.COMPOSITE_ID;
import static com.appglue.Constants.INDEX;
import static com.appglue.Constants.POSITION;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.CREATE_NEW;
import static com.appglue.library.AppGlueConstants.FIRST;
import static com.appglue.library.AppGlueConstants.SERVICE_REQUEST;

public class ActivityWiring extends FragmentActivity
{
	private CompositeService cs;

	private ViewPager pager;
	private WiringPagerAdapter pagerAdapter;
	private TextView status;
	
	private Registry registry;
	
	public static final int MODE_WIRING = 0;
	public static final int MODE_SETTING = 1;
	
	private int mode = MODE_SETTING;

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
		final long compositeId = extras.getLongExtra(COMPOSITE_ID, -1);

		registry = Registry.getInstance(this);

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

        if(compositeId == -1)
        {
            // They are creating a new one

            if(registry.tempExists())
            {
                // There is stuff in the temp -- they might want to save it
                AlertDialog.Builder keepTemp = new AlertDialog.Builder(this);
                keepTemp.setMessage("You have a saved draft, do you want to carry on with it, or start again?");
                keepTemp.setCancelable(true);
                keepTemp.setPositiveButton("Keep draft",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                cs = registry.getTemp();
                                finishWiringSetup();
                            }
                        });
                keepTemp.setNegativeButton("Start new",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                cs = registry.createTemp();
                                finishWiringSetup();
                            }
                        });

                keepTemp.create().show();
            }
            else
            {
                // There isn't stuff in the temp, just use that
                cs = registry.createTemp();
                finishWiringSetup();
            }
        }
        else
        {
            if(registry.tempExists())
            {
                AlertDialog.Builder keepTemp = new AlertDialog.Builder(this);
                keepTemp.setMessage("You have a saved draft, do you want to save it?");
                keepTemp.setCancelable(true);
                keepTemp.setPositiveButton("Save draft",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                registry.saveTemp("Saved draft");
                                finishWiringSetup();
                            }
                        });
                keepTemp.setNegativeButton("Discard draft",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                registry.createTemp();
                                cs = registry.getComposite(compositeId);
                                finishWiringSetup();
                            }
                        });

                keepTemp.create().show();
            }
            else
            {
                cs = registry.getComposite(compositeId);
                finishWiringSetup();
            }
        }
    }

    private void finishWiringSetup()
    {
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

        Button modeButton = (Button) findViewById(R.id.change_mode);
		modeButton.setText(mode == MODE_WIRING ? "Setting Mode" : "Wiring Mode");
		modeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode == MODE_WIRING)
                    mode = MODE_SETTING;
                else
                    mode = MODE_WIRING;

                redraw();
            }
        });
		
		Intent intent = this.getIntent();
		int index = intent.getIntExtra(INDEX, -1);
		boolean createNew = intent.getBooleanExtra(CREATE_NEW, false);
		
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
	
	public ArrayList<ServiceDescription> getComponents()
	{
		return cs.getComponents();
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
			saveDialog();
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


    private void saveDialog()
    {
        if(cs.getId() == 1)
        {
            // Then it's the temp, we should save it
            String name = csNameEdit.getText().toString();

            if(name.equals("Temp name"))
            {
                String tempName = "";
                for(ServiceDescription sd : cs.getComponents())
                    tempName += sd.getName() + "  ";

                name = tempName;
            }

            registry.saveTemp(name);
        }
        else if(cs.getId() == -1)
        {
            // It's not the temp, but we're still saving a new one (I'm not really sure how this has happened)
            // TODO Probably the same as the above, but I'm not really sure...
            Log.d(TAG, "the CS is -1, this might be bad.");
        }
        else
        {
            // We're just updating one that already exists
            boolean success = registry.updateWiring(cs);
            if(success)
                Log.d(TAG, "Updated " + cs.getName());
        }

        // FIXME If they do decide to save it, this should also clear out the temp so that they don't get pestered at the beginning of the next thing
        // FIXME It needs to move to the right place when you add a new component to the wiring page
        // FIXME Click on the status message to view more status messages


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

            // FIXME cs is null at this point, probably because we've not gone through the setup properly?
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
