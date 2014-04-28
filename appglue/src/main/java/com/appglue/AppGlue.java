package com.appglue;

import com.appglue.library.IOFilter;

import android.app.Application;

public class AppGlue extends Application
{
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		IOFilter.filterFactory();
	}	
}