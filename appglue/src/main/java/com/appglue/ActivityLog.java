package com.appglue;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.appglue.engine.description.CompositeService;
import com.appglue.library.LogItem;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

public class ActivityLog extends Activity
{

    // The log needs sorting out so that the thing happens
	
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		
		setContentView(R.layout.activity_log);

        ListView logList = (ListView) findViewById(R.id.log_list);
		TextView noLog = (TextView) findViewById(R.id.no_log);
		
		Registry registry = Registry.getInstance(this);
        ArrayList<CompositeService> composites = registry.getComposites(false);

        if(composites.size() == 0)
            return;

		ArrayList<LogItem> log = registry.getLog(composites.get(0).getID()); // TODO This needs to be clever
		
		if(log == null || log.size() == 0)
		{
			logList.setVisibility(View.GONE);
			noLog.setVisibility(View.VISIBLE);
		}
		else
		{
			logList.setAdapter(new LogAdapter(this, log));
			noLog.setVisibility(View.GONE);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu_log, menu);
		
		return true;
	}
	
	private class LogAdapter extends ArrayAdapter<LogItem>
	{
		private ArrayList<LogItem> items;
		
		public LogAdapter(Context context, ArrayList<LogItem> items)
		{
			super(context, R.layout.list_item_log, items);
			
			this.items = items;
		}
		
		// Add options so that they can actually do things with the log messages
		// Make the log look a bit nicer
		// Implement clearing of the log
		// Add more options to the log viewer - filtering, sorting, etc.
		
		@Override
		public View getView(int position, View convertView, ViewGroup viewGroup)
		{
			View v = convertView;
			LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			if(v == null)
			{
				v = vi.inflate(R.layout.list_item_log, viewGroup);
			}
			
			LogItem log = items.get(position);
			
			TextView logTitle = (TextView) v.findViewById(R.id.log_title);
			logTitle.setText(log.getComposite().getName());
			
			TextView logTime = (TextView) v.findViewById(R.id.log_time);
			logTime.setText("");
			
			TextView logMessage = (TextView) v.findViewById(R.id.log_message);
			logMessage.setText(log.getMessage());
			
			return v;
		}
		
	}
}
