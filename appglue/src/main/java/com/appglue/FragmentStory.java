package com.appglue;

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

import com.appglue.engine.description.CompositeService;
import com.appglue.serviceregistry.Registry;

import static com.appglue.Constants.CLASSNAME;
import static com.appglue.Constants.INDEX;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.POSITION;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.NO_TRIGGERS;
import static com.appglue.library.AppGlueConstants.STORY_MODE;
import static com.appglue.library.AppGlueConstants.TRIGGERS_ONLY;

public class FragmentStory extends Fragment implements OnClickListener
{
	private LinearLayout triggerLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
        View v = inflater.inflate(R.layout.fragment_story, container, false);
        
        triggerLayout = (LinearLayout) v.findViewById(R.id.choice_trigger);
		triggerLayout.setOnClickListener(this);

        LinearLayout userLayout = (LinearLayout) v.findViewById(R.id.choice_user);
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
			CompositeService cs = registry.getCurrent();
			
			String className = intent.getStringExtra(CLASSNAME);
			final int position = intent.getIntExtra(INDEX, -1);

//			if(position == -1)
//			{
//				cs.addServiceDescription(0, registry.getServiceDescription(className));
//			}
//			else
//			{
//				cs.addServiceDescription(position, registry.getServiceDescription(className));
//			}
            // Change the above to work with ComponentService
			
			Intent i = new Intent(getActivity(), ActivityStoryParameters.class);
			i.putExtra(CLASSNAME, className);
			i.putExtra(POSITION, position);
			startActivity(i);
			
			getActivity().finish();
		}
		else if(requestCode == STORY_MODE && resultCode == Activity.RESULT_CANCELED)
		{
            if(LOG) Log.d(TAG, "Story mode cancelled.");
		}
	}
}