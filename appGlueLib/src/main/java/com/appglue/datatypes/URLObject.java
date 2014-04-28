package com.appglue.datatypes;

public class URLObject extends Text
{
	public URLObject()
	{
		super();
		this.name = "URL";
		this.className = URLObject.class.getCanonicalName();
	}
	
	public URLObject(String value)
	{
		this();
		this.value = value;
	}
}
