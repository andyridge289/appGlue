package com.appglue;

import static com.appglue.Constants.CLASSNAME;
import static com.appglue.Constants.INDEX;
import static com.appglue.Constants.POSITION;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.*;

import com.appglue.engine.CompositeService;
import com.appglue.serviceregistry.Registry;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class FragmentStory extends Fragment implements OnClickListener
{
	private LinearLayout triggerLayout;
	private LinearLayout userLayout;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
        View v = inflater.inflate(R.layout.fragment_story, container, false);
        
        triggerLayout = (LinearLayout) v.findViewById(R.id.choice_trigger);
		triggerLayout.setOnClickListener(this);
		
		userLayout = (LinearLayout) v.findViewById(R.id.choice_user);
		userLayout.setOnClickListener(this);
        
        return v;
    }
	
	public void onClick(View v)
	{
		Intent intent = new Intent(getActivity(), ActivityComponentList.class);
		
		if(v.equals(triggerLayout))
		{
			intent.putExtra(TRIGGERS_ONLY, true);
		}
		else
		{
			intent.putExtra(NO_TRIGGERS, true);
		}
		
		startActivityForResult(intent, STORY_MODE);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		Registry registry = Registry.getInstance(getActivity());
		Log.w(TAG, "Story 2: RESULT!!!");
		if(requestCode == STORY_MODE && resultCode == Activity.RESULT_OK)
		{
			// Then we need to go to add services
			CompositeService cs = registry.getService();
			
			String className = intent.getStringExtra(CLASSNAME);
			final int position = intent.getIntExtra(INDEX, -1);

			if(position == -1)
			{
				cs.addComponent(registry.getAtomic(className));
			}
			else
			{
				cs.addComponent(position, registry.getAtomic(className));
			}
			
			Intent i = new Intent(getActivity(), ActivityStoryParameters.class);
			i.putExtra(CLASSNAME, className);
			i.putExtra(POSITION, position);
			startActivity(i);
			
			getActivity().finish();
		}
		else if(requestCode == STORY_MODE && resultCode == Activity.RESULT_CANCELED)
		{
			
		}
	}
}