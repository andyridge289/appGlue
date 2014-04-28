package com.appglue;

import com.appglue.Constants.ProcessType;


public abstract class FilterService extends ComposableService 
{
	public static ProcessType processType = ProcessType.FILTER;
	
	public abstract boolean perform(Object o);
	
	public void onCreate()
	{
		super.onCreate();
		processType = ProcessType.FILTER;
	}
	
	public Object performService(Object o)
	{
		return perform(o);
	}
}
