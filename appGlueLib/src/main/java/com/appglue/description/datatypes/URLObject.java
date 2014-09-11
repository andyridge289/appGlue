package com.appglue.description.datatypes;

public class URLObject extends Text
{
	public URLObject()
	{
		super();
		this.name = "URL";
		this.className = URLObject.class.getCanonicalName();
        this.sensitivity = Sensitivity.NORMAL;
	}
	
	public URLObject(String value)
	{
		this();
		this.value = value;
	}
}
