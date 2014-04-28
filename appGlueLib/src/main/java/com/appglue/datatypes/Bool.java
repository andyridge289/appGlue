package com.appglue.datatypes;

public class Bool extends IOType
{
	public Bool()
	{
		super();
		this.name = "Bool";
		this.className = Bool.class.getCanonicalName();
		this.value = false;
	}
	
	public Bool(boolean value)
	{
		this();
		this.value = value;
	}

	@Override
	public Object toStorable(Object value) 
	{
		return (Boolean) value;
	}
	
	public Object fromStorable(Object value)
	{
		return (Boolean) value;
	}
	
	public String toString(Object value)
	{
		return "" + (Boolean) value;
	}
	
	public Object fromString(String value)
	{
		return Boolean.parseBoolean(value);
	}
}
