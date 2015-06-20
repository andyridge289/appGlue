package com.appglue.serviceregistry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.orhanobut.logger.Logger;

public class AppUninstalledReceiver extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent) 
	{
		String action = intent.getAction();
		
		if(!action.equals("android.intent.action.PACKAGE_REMOVED")) {
            return;
        }

        Logger.d("Package removed");

		/*Bundle b = intent.getExtras();
		int uid = b.getInt(Intent.EXTRA_UID);
        String[] packages = context.getPackageManager().getPackagesForUid(uid);
        
        Registry registry = Registry.getInstance(context);
        
        for(String packageName : packages)
        {
        	AppDescription app = registry.app(packageName);
        	
        	if(app == null)
        		continue;
        	
        	registry.setAppUninstalled(packageName);
        }*/
	}

}
