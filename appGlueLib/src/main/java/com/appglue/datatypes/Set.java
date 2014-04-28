package com.appglue.datatypes;

public class Set extends NumberInt
{
	public Set()
	{
		super();
		this.name = "Set";
		this.className = Set.class.getCanonicalName();
	}
	
	public Set(int value)
	{
		this();
		this.value = value;
	}
}
