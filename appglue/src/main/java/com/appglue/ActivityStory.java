package com.appglue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.appglue.serviceregistry.Registry;

import static com.appglue.Constants.COMPOSITE_ID;
import static com.appglue.Constants.TAG;

public class ActivityStory extends Activity implements OnClickListener
{
	private FragmentStory storyFragment;

    public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.activity_story);

        Registry registry = Registry.getInstance(this);
		
		Intent intent = this.getIntent();
		long compositeId = intent.getLongExtra(COMPOSITE_ID, -1);

		if(compositeId == -1)
		{
			registry.createTemp();
		}
		else
		{
			// We need to skip this activity
            Log.d(TAG, "It isn't -1, does this mean we need to load the last one?");
		}

		storyFragment = (FragmentStory) getFragmentManager().findFragmentById(R.id.story_fragment);
		

		
	}
	
	public void onClick(View v)
	{
        storyFragment.getActivity();
//		Intent intent = new Intent(ActivityStory.this, ActivityComponentList.class);
//		
//		if(v.equals(triggerLayout))
//		{
//			intent.putExtra(TRIGGERS_ONLY, true);
//		}
//		else
//		{
//			intent.putExtra(NO_TRIGGERS, true);
//		}
//		
//		startActivityForResult(intent, STORY_MODE);
	}
	
//	public void onActivityResult(int requestCode, int resultCode, Intent intent)
//	{
//		Log.w(TAG, "Story: RESULT!!!");
//		if(requestCode == STORY_MODE && resultCode == Activity.RESULT_OK)
//		{
//			// Then we need to go to add services
//			CompositeService cs = registry.getService();
//			
//			String className = intent.getStringExtra(CLASSNAME);
//			final int position = intent.getIntExtra(INDEX, -1);
//
//			if(position == -1)
//			{
//				cs.addServiceDescription(registry.getAtomic(className));
//			}
//			else
//			{
//				cs.addServiceDescription(position, registry.getAtomic(className));
//			}
//			
//			Intent i = new Intent(ActivityStory.this, ActivityStoryParameters.class);
//			i.putExtra(CLASSNAME, className);
//			i.putExtra(POSITION, position);
//			startActivity(i);
//			
//			finish();
//		}
//		else if(requestCode == STORY_MODE && resultCode == Activity.RESULT_CANCELED)
//		{
//			
//		}
//	}

}
