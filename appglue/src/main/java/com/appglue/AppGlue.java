package com.appglue;

import com.appglue.library.FilterFactory;

import android.app.Application;

public class AppGlue extends Application
{

	@Override
	public void onCreate() {
		super.onCreate();
		FilterFactory.filterFactory();
    }
}