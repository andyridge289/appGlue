package com.appglue.datatypes;

public class App extends Text
{	
	public App()
	{
		super();
		this.name = "App";
		this.className = App.class.getCanonicalName();
	}
	
	public App(String value)
	{
		this();
		this.value = value;
	}
}
