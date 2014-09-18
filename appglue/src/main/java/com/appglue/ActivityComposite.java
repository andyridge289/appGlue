package com.appglue;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.appglue.Constants.ProcessType;
import com.appglue.engine.description.CompositeService;
import com.appglue.library.LocalStorage;
import com.appglue.library.LogItem;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.COMPOSITE_ID;

public class ActivityComposite extends Activity
{
	private CompositeService cs;
	
	private boolean edit;

    private LocalStorage localStorage;
	private Registry registry;
	
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		
		Intent intent = getIntent();
		
		localStorage = LocalStorage.getInstance();
		registry = Registry.getInstance(this);
		
		long compositeId = intent.getLongExtra(COMPOSITE_ID, -1);
		if(compositeId == -1){ finish(); return; }
		
		cs = registry.getComposite(compositeId);
        ArrayList<LogItem> logs = registry.getExecutionLog(cs);
        Log.d(TAG, "Lots of logs: " + logs.size());

		edit = false;
		
		setup();
		
		ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.view_composite));
            actionBar.setSubtitle(cs.getName());
        }

	}
	
	private void setup()
	{
		if(edit)
		{
			setContentView(R.layout.activity_composite_edit);

            EditText nameEdit = (EditText) findViewById(R.id.composite_name_edit);
			nameEdit.setText(cs.getName());

            EditText descriptionEdit = (EditText) findViewById(R.id.composite_description_edit);
			descriptionEdit.setText(cs.getDescription());
			
//			ImageButton editComposition = (ImageButton) findViewById(R.getID.composite_edit_composition);
//			editComposition.setOnClickListener(new OnClickListener()
//			{
//				@Override
//				public void onClick(View v)
//				{
//					Intent intent = new Intent(ActivityComposite.this, ActivityCompositionCanvas.class);
//					intent.putExtra(KEY_COMPOSITE, cs.getID());
//					startActivity(intent);
//				}
//			});
		}
		else
		{
			setContentView(R.layout.activity_composite);
			
			TextView compositeName = (TextView) findViewById(R.id.composite_name);
			compositeName.setText(cs.getName());
			
			TextView compositeDescription = (TextView) findViewById(R.id.description_text);
			compositeDescription.setText(cs.getDescription());
		}
		
		LocalStorage ls = LocalStorage.getInstance();
		ImageView icon = (ImageView) findViewById(R.id.composite_icon);
		
		try 
		{
			String iconLocation = cs.getComponents().get(0).getDescription().getApp().iconLocation();
			Bitmap b = ls.readIcon(iconLocation);
			
			if(b != null)
				icon.setImageBitmap(b);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		// Stuff for both
		
		// Do a look up in the DB to see if the composite is currently running (right now) or not
        CheckBox runningCheck = (CheckBox) findViewById(R.id.composite_running);

        long instanceId = registry.isCompositeRunning(cs.getID());
		runningCheck.setChecked(instanceId != -1);

        CheckBox activeCheck = (CheckBox) findViewById(R.id.composite_active);
		if(cs.getComponents().get(0).getDescription().getProcessType() == ProcessType.TRIGGER)
		{
			activeCheck.setText("Active");
		}
		else
		{
			activeCheck.setText("On timer");
		}
		
		// It doesn't matter what it is, just set the check or not
		activeCheck.setChecked(registry.enabled(cs.getID()));
		
		ListView componentList = (ListView) findViewById(R.id.composite_component_list);
//		componentList.setAdapter(new CompositeComponentAdapter(this, cs.getComponentsAL()));
			
		if(edit)
		{
			activeCheck.setEnabled(true);
		}
		else
		{
			activeCheck.setEnabled(false);
		}
		
		// Probably need some way of killing ones that are running
	}
	
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		
		if(!edit)
			inflater.inflate(R.menu.composite_menu, menu);
		else
			inflater.inflate(R.menu.composite_menu_edit, menu);
	    
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == R.id.composite_edit)
		{
			edit = true;
			invalidateOptionsMenu();
			setup();
		}
		else if(item.getItemId() == R.id.composite_edit_done)
		{
			edit = false;
			invalidateOptionsMenu();
			setup();
		}
		else if(item.getItemId() == R.id.composite_share)
		{
			// Implement sharing - Google+?
            Log.d(TAG, "Sharing not implemented yet");
		}
		
		return false;
	}
}
