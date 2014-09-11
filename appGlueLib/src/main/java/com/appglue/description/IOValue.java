package com.appglue.description;

import android.content.ContentValues;

import static com.appglue.Constants.ID;
import static com.appglue.Constants.NAME;
import static com.appglue.Constants.VALUE;

public class IOValue
{
	public long id;
	public String name;
	public Object value;
	
	public IOValue()
	{
		this.id = -1;
		this.name = "";
	}
	
	public IOValue(String name, Object value)
	{
		this.id = -1;
		this.name = name;
		this.value = value;
	}

    public IOValue(long id, String name)
    {
        this.id = id;
        this.name = name;
    }

    public IOValue(long id, String name, Object value)
    {
        this.id = id;
        this.name = name;
        this.value = value;
    }
	
	public String toString()
	{
		return this.name;
	}
	
	public void setValue(Object value)
	{
		this.value = value;
	}
	
	public boolean equals(IOValue other)
	{
        return this.name.equals(other.name) && this.value.equals(other.value);
    }
}
