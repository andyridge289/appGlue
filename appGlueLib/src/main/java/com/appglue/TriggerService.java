package com.appglue;

import static com.appglue.Constants.DATA;
import static com.appglue.Constants.ACTION_TRIGGER;
import static com.appglue.Constants.CLASSNAME;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;

import com.orhanobut.logger.Logger;

public abstract class TriggerService extends Service
{    
    public void trigger(Bundle data)
    {
    	Intent broadcastIntent = new Intent();
    	
    	broadcastIntent.setAction(ACTION_TRIGGER);
    	broadcastIntent.putExtra(DATA, data);
    	broadcastIntent.putExtra(CLASSNAME, this.getClass().getCanonicalName());
    	
        sendBroadcast(broadcastIntent);
        Logger.d("Apparently sent broadcast");
    }
}
