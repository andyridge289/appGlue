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
	public Boolean toStorable(Object value)
	{
		return (Boolean) value;
	}
	
	public Boolean fromStorable(Object value)
	{
		return (Boolean) value;
	}
	
	public String toString(Object value)
	{
		return "" + value;
	}
	
	public Object fromString(String value)
	{
		return Boolean.parseBoolean(value);
	}
}
