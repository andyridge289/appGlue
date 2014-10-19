package com.appglue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.appglue.engine.description.CompositeService;
import com.appglue.engine.OrchestrationService;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

import static com.appglue.Constants.DATA;
import static com.appglue.Constants.INDEX;
import static com.appglue.Constants.IS_LIST;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.COMPOSITE_ID;
import static com.appglue.Constants.DURATION;
import static com.appglue.Constants.RUN_NOW;
import static com.appglue.library.AppGlueConstants.TEST;

public class ShortcutActivity extends Activity
{
	    public void onCreate(Bundle icicle) 
	    {
	        super.onCreate(icicle);
	        
	        Intent stuff = this.getIntent();
	        Bundle b = stuff.getExtras();
	        
	        // Maybe if it isn't set we could ask them which one they want to execute?
	        long id = b.getLong(COMPOSITE_ID);
	        
	        Registry registry = Registry.getInstance(this);
			CompositeService cs = registry.getComposite(id);

            Intent serviceIntent = new Intent(this, OrchestrationService.class);
            ArrayList<Bundle> intentData = new ArrayList<Bundle>();
            Bundle data = new Bundle();
            data.putLong(COMPOSITE_ID, cs.getID());
            data.putInt(INDEX, 0);
            data.putBoolean(IS_LIST, false);
            data.putInt(DURATION, 0);
            data.putBoolean(TEST, false);

            if (LOG) Log.w(TAG, "Trying to run " + cs.getID() + " : " + cs.getName());

            intentData.add(data);
            serviceIntent.putParcelableArrayListExtra(DATA, intentData);
            startService(serviceIntent);
	        finish();
	    }
}
