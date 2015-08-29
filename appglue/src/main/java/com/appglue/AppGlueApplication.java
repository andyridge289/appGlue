package com.appglue;

import com.appglue.library.FilterFactory;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;
import com.raizlabs.android.dbflow.config.FlowManager;

import android.app.Application;
import android.os.Build;

public class AppGlueApplication extends Application
{

    private static final String TAG = "appGlue";

    @Override
	public void onCreate() {
		super.onCreate();
		FilterFactory.filterFactory();
        LogLevel level = BuildConfig.DEBUG ? LogLevel.FULL : LogLevel.NONE;
        Logger.init(TAG).setLogLevel(level);
        FlowManager.init(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        FlowManager.destroy();
    }
}