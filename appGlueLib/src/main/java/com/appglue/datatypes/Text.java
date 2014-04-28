package com.appglue.datatypes;

public class Text extends IOType
{
	public Text()
	{
		super();
		this.name = "Text";
		this.className = Text.class.getCanonicalName();
		this.value = "";
	}
	
	public Text(String value)
	{
		this();
		this.value = value;
	}
	
	@Override
	public Object toStorable(Object value) 
	{
		return (String) value;
	}

	@Override
	public Object fromStorable(Object value) 
	{
		// It's just a string so we're good
		return (String) value;
	}
	
	@Override
	public String toString(Object value)
	{
		return (String) value;
	}
	
	public Object fromString(String value)
	{
		return value;
	}
}
