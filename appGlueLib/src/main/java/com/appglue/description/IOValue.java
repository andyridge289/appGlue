package com.appglue.description;

import android.util.Log;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;

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

    public IOValue(long id, String name)
    {
        this.id = id;
        this.name = name;
        this.value = null;
    }

    public  IOValue(String name, Object value)
    {
        this.id = -1;
        this.name = name;
        this.value = value;
    }

    public IOValue(long id, String name, Object value)
    {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    public long getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }
	
	public String toString()
	{
		return this.name;
	}

    public void setID(long id) {
        this.id = id;
    }
	public void setValue(Object value)
	{
		this.value = value;
	}
	
	public boolean equals(Object o)
	{
        if(o == null)  {
            if(LOG) Log.d(TAG, "IOValue->Equals: null");
            return false;
        }

        if(!(o instanceof IOValue)) {
            if(LOG) Log.d(TAG, "IOValue->Equals: not IOValue");
            return false;
        }

        IOValue other = (IOValue) o;

        if(id != other.getID()) {
            if(LOG) Log.d(TAG, "IOValue->Equals: id - [" + id + " :: " + other.getID() + "]");
            return false;
        }

        if(!name.equals(other.getName())) {
            if(LOG) Log.d(TAG, "IOValue->Equals: name - [" + name + " :: " + other.getName() + "]");
            return false;
        }

        return true;
    }
}
