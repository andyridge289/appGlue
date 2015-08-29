package com.appglue.datatypes;

public class App extends Text
{	
	public App()
	{
		super();
		this.name = "App";
		this.className = App.class.getCanonicalName();
        this.sensitivity = Sensitivity.NORMAL;
        this.acceptsManual = false;
        this.manualLookup = true;
	}
	
	public App(String value)
	{
		this();
	}
}
