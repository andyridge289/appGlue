package com.appglue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.appglue.engine.CompositeService;
import com.appglue.engine.OrchestrationService;
import com.appglue.serviceregistry.Registry;

import static com.appglue.Constants.COMPOSITE_ID;
import static com.appglue.Constants.DURATION;
import static com.appglue.Constants.RUN_NOW;

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
	        
	        Intent intent = new Intent(ShortcutActivity.this, OrchestrationService.class);
			intent.putExtra(COMPOSITE_ID, cs.getId());
			intent.putExtra(DURATION, 0);
			intent.putExtra(RUN_NOW, false);
			
	        startService(intent);
	        finish();
	    }
}
