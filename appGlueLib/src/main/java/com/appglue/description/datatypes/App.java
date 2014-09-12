package com.appglue.description.datatypes;

import android.util.Log;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;

public class App extends Text
{	
	public App()
	{
		super();
		this.name = "App";
		this.className = App.class.getCanonicalName();
        this.sensitivity = Sensitivity.NORMAL;
	}
	
	public App(String value)
	{
		this();
	}
}
