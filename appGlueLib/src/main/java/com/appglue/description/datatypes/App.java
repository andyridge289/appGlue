package com.appglue.description.datatypes;

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
