package com.appglue;

import static com.appglue.Constants.*;
import static com.appglue.library.AppGlueConstants.*;

import com.appglue.serviceregistry.Registry;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public class ActivityStoryComponents extends Activity
{
	private FragmentStoryComponents componentFragment;	
	private Registry registry;
	
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.activity_story_components);
		
		registry = Registry.getInstance(this);
		
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
		FragmentStoryComponents newFragment = new FragmentStoryComponents();
		ft.add(R.id.fragment_story_component_container, newFragment);
		ft.commit();
	}
	
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.story_components, menu);   
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == R.id.story_params_ok)
		{
			// Then go to the page
			Intent intent = new Intent(ActivityStoryComponents.this, ActivityWiring.class);
			startActivity(intent);
		}
		
		return false;
	}
	
	// https://developer.android.com/training/basics/fragments/fragment-ui.html
	public void doneBuilding()
	{
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
		FragmentStoryAnnotate newFragment = new FragmentStoryAnnotate();
		ft.replace(R.id.fragment_story_component_container, newFragment);
		ft.addToBackStack(null);
		ft.commit();
	}
}
