package com.appglue;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.appglue.Constants.Interval;
import com.appglue.engine.description.CompositeService;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

public class ActivityRunning extends Activity
{
	private Registry registry;

    @Override
	public void onCreate(Bundle icicle)
	{
		setContentView(R.layout.running);
		
		registry = Registry.getInstance(this);

        ArrayList<CompositeService> runningServices = registry.getIntendedRunningServices();
		
		ListView runningList = (ListView) findViewById(R.id.running_list);
        RunningAdapter adapter = new RunningAdapter(this, runningServices);
		runningList.setAdapter(adapter);
		
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(getResources().getString(R.string.title_running));
		
		super.onCreate(icicle);
	}
	
	private class RunningAdapter extends ArrayAdapter<CompositeService>
	{
		private ArrayList<CompositeService> items;
		
		public RunningAdapter(Context context, ArrayList<CompositeService> items)
		{
			super(context, R.layout.running_list_item, items);
			
			this.items = items;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View v = convertView;
			
			if(v == null)
			{
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.running_list_item, null);
			}
			
			final CompositeService service = items.get(position);
			boolean trigger = service.containsTrigger();
			
			TextView name = (TextView) v.findViewById(R.id.running_name);
			name.setText(service.getName());

            boolean enabled = registry.enabled(service.getId());

//			Pair<Boolean, Boolean> running = registry.running(service.id());

				
			final Button startButton = (Button) v.findViewById(R.id.running_go_button);
			final Button pauseButton = (Button) v.findViewById(R.id.running_pause_button);
			final Button stopButton = (Button) v.findViewById(R.id.running_stop_button);
			
			Pair<Long, Interval> timings = registry.getTimerDuration(service.getId());
			
			TextView numeralText = (TextView) v.findViewById(R.id.numeral_interval);
			numeralText.setText(String.format("Runs once every %d %s%s", timings.first, timings.second.name, (timings.first > 1) ? "s" : ""));
			
			if(trigger)
			{
				startButton.setVisibility(View.GONE);
				pauseButton.setVisibility(View.GONE);
				
				if(enabled)
					stopButton.setText(getResources().getString(R.string.disable));
				else
					stopButton.setText(getResources().getString(R.string.enable));
				
				stopButton.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
                        boolean enabled = registry.enabled(service.getId());
						
						if(enabled)
						{
							registry.setEnabled(service.getId(), false);
							stopButton.setText(getResources().getString(R.string.enable));
						}
						else
						{
							registry.setEnabled(service.getId(), true);
							stopButton.setText(getResources().getString(R.string.disable));
						}
					}
				});
			}
			else	
			{

                // FIXME NEed to do something with starting and stopping the bastard based on the executionInstanceID
//				if(running.second) // It's running
//				{
//					startButton.setEnabled(false);
//					pauseButton.setEnabled(true);
//				}
//				else
//				{
//					startButton.setEnabled(true);
//					pauseButton.setEnabled(false);
//				}
//
//				startButton.setVisibility(View.VISIBLE);
//				startButton.setOnClickListener(new OnClickListener()
//				{
//					@Override
//					public void onClick(View v)
//					{
//						Pair<Long, Interval> timings = registry.getTimerDuration(service.id());
//						registry.setIsRunning(service.id());
//
//						Intent intent = new Intent(ActivityRunning.this, OrchestrationService.class);
//						intent.putExtra(COMPOSITE_ID, service.id());
//						intent.putExtra(DURATION, timings.first * timings.second.value);
//						intent.putExtra(RUN_NOW, true);
//						startService(intent);
//
//						startButton.setEnabled(false);
//						pauseButton.setEnabled(true);
//					}
//				});
//
//				pauseButton.setVisibility(View.VISIBLE);
//				pauseButton.setOnClickListener(new OnClickListener()
//				{
//					@Override
//					public void onClick(View v)
//					{
//						// This should stop it from running again
//						registry.setIsntRunning(service.id());
//
//						startButton.setEnabled(true);
//						pauseButton.setEnabled(false);
//					}
//				});
//
//				stopButton.setOnClickListener(new OnClickListener()
//				{
//					@Override
//					public void onClick(View v)
//					{
//						registry.setDisabled(service.id());
//						adapter.remove(service);
//						adapter.notifyDataSetChanged();
//					}
//				});
			}
			
			return v;
		}
	}
}
