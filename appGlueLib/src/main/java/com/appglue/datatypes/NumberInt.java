package com.appglue.datatypes;

public class NumberInt extends IOType
{
	public NumberInt()
	{
		super();
		this.name = "NumberInt";
		this.className = NumberInt.class.getCanonicalName();
		this.value = 0;
	}
	
	public NumberInt(int value)
	{
		this();
		this.value = value;
	}

	@Override
	public Object toStorable(Object value) 
	{
		return (Integer) value;
	}
	
	public Object fromStorable(Object value)
	{
		return (Integer) value;
	}
	
	public String toString(Object value)
	{
		return "" + (Integer) value;
	}
	
	public Object fromString(String value)
	{
		return Integer.parseInt(value);
	}
	
}
