package com.appglue.datatypes;

public class PhoneNumber extends Text 
{
	public PhoneNumber()
	{
		super();
		this.name = "Phone Number";
		this.className = PhoneNumber.class.getCanonicalName();
		this.value = "+440000000000";
	}
	
	public PhoneNumber(String value)
	{
		this();
		this.value = value;
	}
}
