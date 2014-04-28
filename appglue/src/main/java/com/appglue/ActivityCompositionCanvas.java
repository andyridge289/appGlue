package com.appglue;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.appglue.description.ServiceDescription;
import com.appglue.engine.CompositeService;
import com.appglue.engine.OrchestrationService;
import com.appglue.layout.CompositionView;
import com.appglue.serviceregistry.Registry;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.appglue.Constants.CLASSNAME;
import static com.appglue.Constants.COMPOSITE_ID;
import static com.appglue.Constants.DATA;
import static com.appglue.Constants.DURATION;
import static com.appglue.Constants.INDEX;
import static com.appglue.Constants.IS_LIST;
import static com.appglue.Constants.KEY_SERVICE_LIST;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;
import static com.appglue.Constants.TEST;
import static com.appglue.library.AppGlueConstants.SERVICE_REQUEST;
import static com.appglue.library.AppGlueConstants.WIRE_COMPONENTS;

public class ActivityCompositionCanvas extends Activity 
{
	private CompositionView cv;
	
	// Swap the Wiring and the Overview
		// Zoom in gesture on the overview should go to the dialog
		// Adding a component on the overview should go to the right page in wiring
	// Wiring also needs a test button
		// Put it in the actionbar menu
	// Wiring also needs the done button to work
		// If they came compositioncanvas is needs to go back there
		// Otherwise it needs to go back to the composite list
	// Remove the name from the wiring page
		// If they haven't entered a name when they click done, they should be prompted to do so
	
	// Edit doesn't work... probably because the wiring page is just opening another one
	// There should only be one temp
	// Wiring title needs to be something meaningful
	// Component name overrunning on wiring
	// The components in the wiring page need to have a number saying what position in the composition they are in
	// Wiring mode button needs to not look like a button
	// Wiring button needs to change text when it's clicked
	
	private Registry registry;
	
	private ServiceDescription selected = null;
	private int selectedIndex = -1;

    // Ignore lint, if these are local then we can't access it from anywhere else later
	private ActionMode actionMode;
	private ActionMode.Callback actionCallback = new ActionMode.Callback()
	{
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) 
		{
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.composition_context_menu, menu);
			
			mode.setTitle("Choose action:");
			mode.setSubtitle(selected.getName());
			
			menu.setGroupVisible(R.id.composition_context_right_group, true);
			menu.setGroupVisible(R.id.composition_context_left_group, true);
			
			if(selectedIndex == 0)
				menu.setGroupVisible(R.id.composition_context_left_group, false);
			
			if(selectedIndex >= registry.getService().getComponents().size() - 1)
				menu.setGroupVisible(R.id.composition_context_right_group, false);
			
			refresh();
			
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) 
		{
			selectedIndex = -1;
			selected = null;
			actionMode = null;
			refresh();
		}
		
		// Need to make the things respond to being tapped
		
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) 
		{
			
			switch(item.getItemId())
			{
				case R.id.composition_context_left:
				{
					
					// If we're going left we need to take the one at index i and insert it at index i - 1
					ArrayList<ServiceDescription> components = registry.getService().getComponents();
					
					// Need to check what will happen to any links between components
					if(selected.hasIncomingLinks() || selected.hasOutgoingLinks())
					{
                        Log.d(TAG, "Has incoming or outgoing, probably need to clear all of them");
						// Then we need to do something to take care of this 
					}
					
					components.remove(selectedIndex);
					components.add(selectedIndex - 1, selected);
					
					// We don't want to finish, we want to move the selected index
					selectedIndex -= 1;
					selected = components.get(selectedIndex);
					
					// We also want to move the canvas so we're looking at the one that is selected
					
					// Reset the canvas and the menu - should stop us from moving it off the end
					refresh();
					
					return true;
				}
					
				case R.id.composition_context_right:
				{
					
					// If we're going right we need to take the one at index i and insert it at index i + 1
					ArrayList<ServiceDescription> components = Registry.getInstance(ActivityCompositionCanvas.this).getService().getComponents();
					
					// Need to check what will happen to any links between components
					if(selected.hasIncomingLinks() || selected.hasOutgoingLinks())
					{
                        Log.d(TAG, "Has incoming or outgoing, probably need to clear all of them");
						// Then we need to do something to take care of this 
					}
					
					components.remove(selectedIndex);
					components.add(selectedIndex + 1, selected);
					
					// We don't want to finish, we want to move the selected index
					selectedIndex += 1;
					selected = components.get(selectedIndex);
					
					// We also want to move the canvas so we're looking at the one that is selected
					
					// Reset the canvas and the menu - should stop us from moving it off the end
					refresh();

					return true;
				}
					
				case R.id.composition_context_view:
				{
					viewComponent(selected.getClassName());
					mode.finish();
					return true;
				}
					
				case R.id.composition_context_remove:
					
					// If we're going right we need to take the one at index i and insert it at index i + 1
					ArrayList<ServiceDescription> components = Registry.getInstance(ActivityCompositionCanvas.this).getService().getComponents();
					
					// Need to check what will happen to any links between components
					if(selected.hasIncomingLinks() || selected.hasOutgoingLinks())
					{
                        Log.d(TAG, "Has incoming or outgoing, probably need to clear all of them");
						// TODO Then we need to do something to take care of this
					}
					
					components.remove(selectedIndex);
					
					mode.finish();
					
					refresh();
					
					return true;
			}

			return false;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) 
		{
			return false;
		}
		
	};
	
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		
		setContentView(R.layout.composition_canvas);
		
		cv = (CompositionView) findViewById(R.id.composition_canvas);
		registry = Registry.getInstance(this);
		
		ActionBar actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setTitle(R.string.comp_title);
		
		Bundle extras = getIntent().getExtras();
		
		// This means we're editing a composite rather than creating a new one
		if(extras != null && extras.containsKey(COMPOSITE_ID))
		{
			long compositeId = extras.getLong(COMPOSITE_ID);
			registry.setService(compositeId);
			
			if(LOG) Log.d(TAG, "Registry service set to " + registry.getService().getName());
		}
	}
	
	public void refresh()
	{
		registry.updateCurrent();
		cv.invalidate(); // We're on the UI thread so we should just be able to call this [or postInvalidate()]
		this.invalidateOptionsMenu();
	}
	
//	public void setSelectedIndex(int index)
//	{
//		selectedIndex = index;
//	}
	
	public void add()
	{
		Intent i = new Intent(ActivityCompositionCanvas.this, ActivityComponentList.class);
		startActivityForResult(i, SERVICE_REQUEST);
	}
	
//	public void wire(int index)
//	{
//
//		Intent intent = new Intent(this, ActivityWiring.class);
//		intent.putExtra(INDEX, index);
//
//		startActivityForResult(intent, WIRE_COMPONENTS);
//	}
	
	private void viewComponent(String className)
	{
		// WHY DOESN'T THIS WORK
		Intent intent = new Intent(ActivityCompositionCanvas.this, ActivityComponent.class);
		intent.putExtra(CLASSNAME, className);
		startActivity(intent);
	}
	
	/**
	 * Whatever happens, update the current one
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		CompositeService service = registry.getService();
		if(requestCode == SERVICE_REQUEST && resultCode == Activity.RESULT_OK)
		{
			String className = intent.getStringExtra(CLASSNAME);
			
			if(className != null)
			{
				ServiceDescription sd = registry.getAtomic(className);
				service.addComponent(sd);
				refresh();
			}
		}
		else if(requestCode == WIRE_COMPONENTS && resultCode == Activity.RESULT_OK )
		{
			// Make it re-draw the canvas with the wires
			refresh();
		}
		else
		{
			// We're coming back from somewhere that isn't wiring or adding, so we dooo... nothing?
			// Fuck it. Just refresh. What's the worst that can happen....
			refresh();
		}
	}
	
	@Override
	public void onRestart()
	{
		super.onRestart();
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
	}
	
	@Override
	public void onDestroy()
	{	
    	registry.stopTemp();
    	super.onDestroy();
    }
	
	@Override
	public void onSaveInstanceState(@NotNull Bundle icicle)
	{
		CompositeService service = registry.getService();
		if(service.getId() != -1)
		{
			// If it's a composite service then save the id of the composite service and just pull it back out again
			icicle.putLong(COMPOSITE_ID, service.getId());
		}
		else
		{
			// If it's not a composite service then save the IDs of the services in the composition so far
			ArrayList<ServiceDescription> services = service.getComponents();
			String[] serviceIds = new String[services.size()];
			
			for(int i = 0; i < services.size(); i++)
			{
				serviceIds[i] = services.get(i).getClassName();
			}
			
			icicle.putStringArray(KEY_SERVICE_LIST, serviceIds);
		}
	}
	
	public void clear()
	{
		boolean ret = registry.reset();
		if(ret)
			refresh();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.composition, menu);
		
		// Save should also be at index 2
		if(registry.getService().getComponents().size() == 0)
			menu.getItem(2).setEnabled(false);
		else
			menu.getItem(2).setEnabled(true);
		
		return true;
	}
	
	private ServiceIO mandatoryCheck()
	{
		ArrayList<ServiceDescription> components = registry.getService().getComponents();
		if(LOG) Log.d(TAG, "Mandatory check " + components.size());

        for (ServiceDescription component : components) {
            if (!component.hasInputs())
                continue;

            ArrayList<ServiceIO> inputs = component.getInputs();

            for (ServiceIO input : inputs) {
                if (input.isMandatory() && input.getManualValue() == null && input.getConnection() == null) {
                    // If it's mandatory and both of the other 2 are null then give up!
                    return input;
                }
            }

        }
		
		return null;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == R.id.comp_add)
		{
			add();
		}
		else if(item.getItemId() == R.id.comp_clear)
		{
			clear();
		}
		else if(item.getItemId() == R.id.comp_test)
		{
			ServiceIO brokenInput = mandatoryCheck();
			if(brokenInput == null)
			{
				Intent serviceIntent = new Intent(this, OrchestrationService.class);
				ArrayList<Bundle> intentData = new ArrayList<Bundle>();
				Bundle b = new Bundle();
			
				b.putLong(COMPOSITE_ID, -1);
				b.putInt(INDEX, 0);
				b.putBoolean(IS_LIST, false);
				b.putInt(DURATION, 0);
				b.putBoolean(TEST, true);
				
				intentData.add(b);
				serviceIntent.putParcelableArrayListExtra(DATA, intentData);
				startService(serviceIntent);
				
				if(LOG) Log.d(TAG, "Starting service for test" + System.currentTimeMillis());
			}
			else  
			{
				// Make it enforce them setting mandatory parameters when they go to test it
				Toast.makeText(this, "One of your mandatory inputs (" + brokenInput.getFriendlyName() + ") isn't being set.", Toast.LENGTH_LONG);
			}
		}
		else if(item.getItemId() == R.id.comp_save)
		{
		    AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    LayoutInflater inflater = getLayoutInflater();
		    final View v = inflater.inflate(R.layout.dialog_save, null);
		    builder.setView(v);
		    
		    builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() 
		    {
		    	@Override
		    	public void onClick(DialogInterface dialog, int id) 
		    	{	
		    		CompositeService currentComposite = registry.getService();
   					currentComposite.setName(((EditText) v.findViewById(R.id.save_name)).getText().toString());
   					currentComposite.setDescription(((EditText) v.findViewById(R.id.save_description)).getText().toString());
	   					
   					// Then we need to save it
   					Log.e(TAG, "Name set to " + currentComposite.getName());
	    			registry.updateCurrent();
		    	}
		    });
		    
		    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
		    {
		    	public void onClick(DialogInterface dialog, int id) 
		    	{
		    		dialog.cancel();
	            }
		    });
		   
		    builder.create();
		    builder.show();
		}
		else if(item.getItemId() == android.R.id.home)
		{
			onBackPressed();
		}
		
		return true;
	}
	
//	public void checkSave(String name, String description)
//	{
//		CompositeService service = registry.getService();
//		
//		if(name.equals("") || name.equals(getResources().getString(R.string.save_name_default)))
//		{
//			Toast.makeText(this, "You haven't entered a name for this service!", Toast.LENGTH_LONG).show();
//			return;
//		}
//		
//		if(registry.getService().getComponents().size() == 0)
//		{
//			Toast.makeText(this, "There aren't any components in this service!", Toast.LENGTH_LONG).show();
//			return;
//		}
//		
//		service.setName(name);
//		service.setDescription(description);
//		
//		// Check if a service already exists with that name
//		if(registry.compositeExistsWithName(service.getName()))
//		{
//			// Create a dialog
//			AlertDialog.Builder builder = new AlertDialog.Builder(this);
//			builder.setMessage(String.format("A service already exists with the name %s. Are you sure you want to save another service with that name?", service.getName()))
//			       .setCancelable(false)
//			       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//			           public void onClick(DialogInterface dialog, int id) 
//			           {
//			               save();
//			           }
//			       })
//			       .setNegativeButton("No", new DialogInterface.OnClickListener() {
//			           public void onClick(DialogInterface dialog, int id) {
//			                dialog.cancel();
//			           }
//			       });
//			AlertDialog alert = builder.create();
//			alert.show();
//		}
//		
//		save();
//	}
	
//	public void save()
//	{
//		// Do a check if something already has that name
//		registry.updateCurrent();
//	}
//
//	public void startActionMode(int index, ServiceDescription selected)
//	{
//		this.selectedIndex = index;
//		this.selected = selected;
//		actionMode = startActionMode(actionCallback);
//	}
	

//	public void update()
//	{
//		long id = registry.updateCurrent();
//	}

	public int getSelectedIndex()
	{
		return selectedIndex;
	}
	
	
}