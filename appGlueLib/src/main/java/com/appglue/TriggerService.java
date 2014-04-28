package com.appglue;

import static com.appglue.Constants.DATA;
import static com.appglue.Constants.ACTION_TRIGGER;
import static com.appglue.Constants.TAG;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.CLASSNAME;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public abstract class TriggerService extends Service
{    
    public void trigger(Bundle data)
    {
    	Intent broadcastIntent = new Intent();
    	
    	broadcastIntent.setAction(ACTION_TRIGGER);
    	broadcastIntent.putExtra(DATA, data);
    	broadcastIntent.putExtra(CLASSNAME, this.getClass().getCanonicalName());
    	
        sendBroadcast(broadcastIntent);
        if(LOG) Log.d(TAG, "Apparently sent broadcast");
    }
}
