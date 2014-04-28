package com.appglue;

import static com.appglue.Constants.*;
import static com.appglue.library.AppGlueConstants.*;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appglue.description.ServiceDescription;
import com.appglue.layout.WiringMap;
import com.appglue.library.LocalStorage;

public class FragmentWiring extends Fragment
{
	private int position;
	
	private WiringMap map;
	
	private ServiceDescription first;
	private ServiceDescription second;
	
	HashMap<String, Integer> hueMap;
	
	private TextView firstName;
	private TextView secondName;
	
	private ImageView firstIcon;
	private ImageView secondIcon;
	
	private RelativeLayout firstContainer;
	private RelativeLayout secondContainer;
	
	public static Fragment create(int position)
	{
		FragmentWiring fragment = new FragmentWiring();
		Bundle args = new Bundle();
		args.putInt(INDEX, position);
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		position = getArguments().getInt(INDEX);
	}
	
	public int getPosition()
	{
		return position;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle)
	{
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_wiring, container, false);
		
		hueMap = new HashMap<String, Integer>();
		
		map = (WiringMap) rootView.findViewById(R.id.firstWiringMap);
		
		firstName = (TextView) rootView.findViewById(R.id.first_name);
		secondName = (TextView) rootView.findViewById(R.id.second_name);
		
		firstIcon = (ImageView) rootView.findViewById(R.id.first_icon);
		secondIcon = (ImageView) rootView.findViewById(R.id.second_icon);
		
		firstContainer = (RelativeLayout) rootView.findViewById(R.id.wiring_first);
		secondContainer = (RelativeLayout) rootView.findViewById(R.id.wiring_second);
		
		ArrayList<ServiceDescription> components = ((ActivityWiring) getActivity()).getComponents();
		
		first = position > 0 ? components.get(position - 1) : null;
		second = position < components.size() ? components.get(position) : null;
		
		map.set(first, second);
		
		LocalStorage localStorage = LocalStorage.getInstance();
		
		// Set the icon of either to be the big purple plus if there's not a component in that position
		if(first != null)
		{
			firstName.setText(first.getName());
			firstName.setTextColor(Color.BLACK);
			
			try
			{
				String iconLocation = first.getApp().getIconLocation();
				Bitmap b = localStorage.readIcon(iconLocation);
				firstIcon.setImageBitmap(b);
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			
			firstContainer.setBackgroundResource(R.drawable.wiring_component);
			firstContainer.setOnLongClickListener(new OnLongClickListener() 
			{
				@Override
				public boolean onLongClick(View v) 
				{
					// Clicking on the first container should let you move/rename it
					return false;
				}
			});
		}
		else
		{
			firstName.setText("Add");
			firstName.setTextColor(getResources().getColor(R.color.android_purple));
			firstIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_new));
			firstContainer.setBackgroundResource(R.drawable.wiring_add);
			
			// Make it add at this position when we click it
			firstContainer.setOnClickListener(new OnClickListener() 
			{
				@Override
				public void onClick(View v) 
				{
					Intent i = new Intent(getActivity(), ActivityComponentList.class);
					i.putExtra(POSITION, position);
					i.putExtra(FIRST, true);
					getActivity().startActivityForResult(i, SERVICE_REQUEST);
				}
			});
			
		}
		
		// Make the right icon be the left half, Make the left icon be the right half
		
		if(second != null)
		{
			secondName.setText(second.getName());
			secondName.setTextColor(Color.BLACK);
			secondContainer.setBackgroundResource(R.drawable.wiring_component);
			
			secondContainer.setOnLongClickListener(new OnLongClickListener() 
			{
				@Override
				public boolean onLongClick(View v) 
				{
					// Clicking on the second container should let you move/rename it
					return false;
				}
			});
		}
		else
		{
			secondName.setText("Add");
			secondName.setTextColor(getResources().getColor(R.color.android_purple));
			secondContainer.setBackgroundResource(R.drawable.wiring_add);
			secondIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_new));
			
			// Make it add at this position when we click it
			secondContainer.setOnClickListener(new OnClickListener() 
			{
				@Override
				public void onClick(View v) 
				{
					Intent i = new Intent(getActivity(), ActivityComponentList.class);
					i.putExtra(POSITION, position);
					i.putExtra(FIRST, false);
					getActivity().startActivityForResult(i, SERVICE_REQUEST);
				}
			});
		}
		
		return rootView;
	}
	
	public void onPause()
	{
		super.onPause();
	}
	
	public void onResume()
	{
		super.onResume();
	}
	
	public ServiceDescription getFirst()
	{
		return first;
	}
	
	public ServiceDescription getSecond()
	{
		return second;
	}
	
	public WiringMap getMap()
	{
		return map;
	}
	
	public void redraw()
	{
		if(map == null)
		{
			Log.w(TAG, "Map is null for " + position);
			return;
		}
		
		map.redraw();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{

	}
	
	
}
