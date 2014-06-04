package com.appglue;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.appglue.Constants.ServiceType;
import com.appglue.description.ServiceDescription;
import com.appglue.engine.CompositeService;
import com.appglue.library.LocalStorage;
import com.appglue.serviceregistry.Registry;

import java.io.IOException;
import java.util.ArrayList;

import static com.appglue.Constants.CLASSNAME;
import static com.appglue.Constants.ID;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.PACKAGENAME;
import static com.appglue.Constants.RESULT;
import static com.appglue.Constants.SERVICE_TYPE;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.JUST_A_LIST;
import static com.appglue.library.AppGlueConstants.MARKET_LOOKUP;
import static com.appglue.library.AppGlueConstants.NOT_SET;
import static com.appglue.library.AppGlueConstants.SUCCESS;

public class ActivityComponent extends Activity
{
	private ServiceDescription service;
	
	private boolean atomicList = false;
	
	private int type;
	
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		
		setContentView(R.layout.activity_component);
		Intent intent =  this.getIntent();
		
		if(!intent.hasExtra(CLASSNAME))
			finish();
		
		String className = intent.getStringExtra(CLASSNAME);
		atomicList = intent.getBooleanExtra(JUST_A_LIST, false);
		type = intent.getIntExtra(SERVICE_TYPE, -1);
		
		if(className == null || className.equals(""))
		{
			className = icicle.getString(CLASSNAME);
			atomicList = icicle.getBoolean(JUST_A_LIST);
			type = icicle.getInt(SERVICE_TYPE);
		}
		
		if(className == null || className.equals(""))
			finish();
		
		Registry registry = Registry.getInstance(this);
		if(type == ServiceType.LOCAL.index || type == ServiceType.DEVICE.index)
			service = registry.getAtomic(className);
		else if(type == ServiceType.REMOTE.index)
			service = registry.getRemote(className);
		else
			service = null;

        registry.dumpSQLLog();

		if(service == null)
			finish();
		
		TextView name = (TextView) findViewById(R.id.component_name);
		
		if(name == null || service == null || service.getName() == null)
		{
			finish();
			return;
		}
			
		name.setText(service.getName());
		
		TextView description = (TextView) findViewById(R.id.description_text);
		description.setText(service.getDescription());
		
		TextView developerName = (TextView) findViewById(R.id.component_dev);
		developerName.setText(service.getApp().getDeveloper());
		
		TextView appName = (TextView) findViewById(R.id.component_app_name);
		ImageView appIcon = (ImageView) findViewById(R.id.component_app_icon);
		
		if(service.getServiceType() != ServiceType.REMOTE)
		{
			
			if(service.getApp() == null)
				appName.setText("");
			else
				appName.setText(service.getApp().getName());
			
			try 
			{
				if(service.getApp() != null)
					appIcon.setImageBitmap(LocalStorage.getInstance().readIcon(service.getApp().getIconLocation()));
			}
			catch (IOException e) 
			{
				Log.w(TAG, "Tried to set Image bitmap for " + service.getName());
			}
		}
		else
		{
			appName.setVisibility(View.GONE);
			appIcon.setVisibility(View.GONE);
			findViewById(R.id.simple_title_App).setVisibility(View.GONE);
		}
		
		
		ListView inputList = (ListView) findViewById(R.id.input_list);
		ArrayList<ServiceIO> inputs = service.getInputs();
		
		if(inputs == null || inputs.size() == 0)
		{
			inputList.setVisibility(View.GONE);
			findViewById(R.id.no_inputs).setVisibility(View.VISIBLE);
		}
		else
		{
			inputList.setAdapter(new IOAdapter(this, R.layout.list_item_wiring_in, inputs, true));
			findViewById(R.id.no_inputs).setVisibility(View.GONE);
		}
		
		
		ListView outputList = (ListView) findViewById(R.id.output_list);
		ArrayList<ServiceIO> outputs = service.getOutputs();
		
		if(outputs == null || outputs.size() == 0)
		{
			outputList.setVisibility(View.GONE);
			findViewById(R.id.no_outputs).setVisibility(View.VISIBLE);
		}
		else
		{
			outputList.setAdapter(new IOAdapter(this, R.layout.list_item_wiring_out, outputs, false));
			findViewById(R.id.no_outputs).setVisibility(View.GONE);
		}
		
		
		
		TextView launchApp = (TextView) findViewById(R.id.component_launch_app);
		launchApp.setOnClickListener(new OnClickListener() 
		{	
			@Override
			public void onClick(View v) 
			{
				try 
				{
                    PackageManager pm = ActivityComponent.this.getPackageManager();

                    if(pm != null)
                    {
				        Intent i = pm.getLaunchIntentForPackage(service.getApp().getPackageName());
				        ActivityComponent.this.startActivity(i);
                    }
                    else
                    {
                        // Do something?
                        Log.e(TAG, "Couldn't launch app because the package manager is null: " + service.getApp().getPackageName());
                    }
				}
				catch (Exception e) 
				{
					Log.e(TAG, "Trying to launch app? " + e.getMessage());
				}
			}
		});
		
		TextView viewApp = (TextView) findViewById(R.id.component_view_app);
		viewApp.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				Intent intent = new Intent(ActivityComponent.this, ActivityApp.class);
				intent.putExtra(PACKAGENAME, service.getApp().getPackageName());
				startActivity(intent);
			}
		});
		
		final ArrayList<CompositeService> examples = registry.getExamples(service.getClassName());
		LayoutInflater inflater = this.getLayoutInflater();
		
		LinearLayout exampleContainer = (LinearLayout) findViewById(R.id.component_eg_container);
		for(int i = 0; i < examples.size(); i++)
		{
			View v = inflater.inflate(R.layout.example_composite, exampleContainer);
			final int index = i;

            if(v != null)
            {
                ((TextView) v.findViewById(R.id.example_name)).setText("Example " + (i + 1));
                v.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(ActivityComponent.this, ActivityComposite.class);
                        intent.putExtra(ID, examples.get(index).getId());
                        startActivity(intent);
                    }
                });
                exampleContainer.addView(v);

                if(i < examples.size() - 1)
                {
                    inflater.inflate(R.layout.spacer_horiz, exampleContainer);
                }
            }
		}
		
		if(examples.size() == 0)
		{
			findViewById(R.id.scroll_eg_container).setVisibility(View.GONE);
			findViewById(R.id.eg_none).setVisibility(View.VISIBLE);
		}
		
		ActionBar actionBar = getActionBar();
        if (actionBar != null)
        {
            actionBar.setIcon(R.drawable.ic_menu_back);
            actionBar.setHomeButtonEnabled(true);

            if(this.atomicList)
                actionBar.setTitle(R.string.component_title_view);
            else
                actionBar.setTitle(R.string.component_title_use);
        }
	}

    /**
     *
     * @param icicle The Bundle into which to save all the stuff
     */
	@Override
	public void onSaveInstanceState(Bundle icicle)
	{
		icicle.putString(CLASSNAME, service.getClassName());
		icicle.putBoolean(JUST_A_LIST, atomicList);
		icicle.putInt(SERVICE_TYPE, this.type);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle icicle)
	{
		super.onRestoreInstanceState(icicle);
		
		if(!icicle.containsKey(CLASSNAME))
			return;
		
		restoreState(icicle);
	}
	
	private void restoreState(Bundle icicle)
	{
		String className = icicle.getString(CLASSNAME);
		atomicList = icicle.getBoolean(JUST_A_LIST, false);
		type = icicle.getInt(SERVICE_TYPE, -1);
		
		Registry registry = Registry.getInstance(this);
		if(type == ServiceType.LOCAL.index || type == ServiceType.DEVICE.index)
			service = registry.getAtomic(className);
		else if(type == ServiceType.REMOTE.index)
			service = registry.getRemote(className);
	}
	
	public void onStart()
	{
		super.onStart();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.simple_service_menu, menu);

        MenuItem useItem = menu.findItem(R.id.simple_use_button);
        MenuItem getItem = menu.findItem(R.id.simple_get_button);

        if(useItem == null || getItem == null)
            return false;

	    if(atomicList)
            useItem.setVisible(false);
	    
	    if(this.type == ServiceType.REMOTE.index)
	    {
            useItem.setVisible(false);
	    }
	    else
	    {
	    	getItem.setVisible(false);
	    }
	    
		return true;
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		if(requestCode == MARKET_LOOKUP)
		{
			Intent i = new Intent();
			i.putExtra(RESULT, MARKET_LOOKUP);
			
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
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		
		if(item.getItemId() == R.id.simple_get_button)
		{
			String marketUri = "market://details?id=" + service.getPackageName();
			if(LOG) Log.d(TAG, "Market URI " + marketUri);
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(marketUri));
			startActivityForResult(intent, MARKET_LOOKUP);
			return true;
		}
		else
		{
			if(!atomicList)
			{
				int result = NOT_SET;
				if(item.getItemId() == R.id.simple_use_button)
				{
					result = SUCCESS;
				}
				else if(item.getItemId() != android.R.id.home)
				{
					result = NOT_SET;
				}
				
				Intent i = new Intent();
				i.putExtra(RESULT, result);
				i.putExtra(CLASSNAME, service.getClassName());
				i.putExtra(SERVICE_TYPE, type);
				
				if (getParent() == null) {
				    setResult(Activity.RESULT_OK, i);
				} else {
				    getParent().setResult(Activity.RESULT_OK, i);
				}
			}
			finish();	
		}
		
    	return false;
	}
	
	@Override
	public void onBackPressed()
	{
		if(!atomicList)
		{
			Intent i = new Intent();
			i.putExtra("result", false);
			
			if (getParent() == null) {
			    setResult(Activity.RESULT_OK, i);
			} else {
			    getParent().setResult(Activity.RESULT_OK, i);
			}
		}
		finish();
	}
	
	private class IOAdapter extends ArrayAdapter<ServiceIO>
	{
		private ArrayList<ServiceIO> items;
		private boolean inputs;
	
		public IOAdapter(Context context, int textViewResourceId, ArrayList<ServiceIO> items, boolean inputs)
		{
			super(context, textViewResourceId, items);
			
			this.items = items;
			this.inputs = inputs;
		}
		
		public View getView(final int position, View convertView, ViewGroup parent)
		{			
			View v = convertView;
			
			if(v == null)
			{
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				
				if(inputs)
                    v = vi.inflate(R.layout.list_item_wiring_in, null);
                else
                    v = vi.inflate(R.layout.list_item_wiring_out, null);
            }

            if(v == null)
                return null;

			final ServiceIO io = items.get(position);


			((TextView) v.findViewById(R.id.io_name)).setText(io.getFriendlyName());
			((TextView) v.findViewById(R.id.io_type)).setText(io.getType().getName());
			
			// It needs to say whether it's mandatory or not
			if(io.isMandatory() && inputs)
			{
				v.findViewById(R.id.mandatory_text).setVisibility(View.VISIBLE);
			}
			else if(inputs)
			{
				v.findViewById(R.id.mandatory_text).setVisibility(View.GONE);
			}
			
			v.setOnClickListener(new OnClickListener() 
			{
				@Override
				public void onClick(View v) 
				{
					Toast.makeText(ActivityComponent.this, io.getFriendlyName() + ": " + io.getDescription(), Toast.LENGTH_LONG).show();
				}
			});
			
			return v;
		}
	}
	
	
}
