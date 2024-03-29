package com.appglue.datatypes;

import android.os.Bundle;

import com.appglue.description.IOType;

public class Bool extends IOType
{
	public Bool()
	{
		super();
		this.name = "True / False";
		this.className = Bool.class.getCanonicalName();
        this.sensitivity = Sensitivity.NORMAL;
        this.acceptsManual = false;
	}

    @Override
    public Object getFromBundle(Bundle bundle, String key, Object defaultValue) {
        return bundle.getBoolean(key, false);
    }

    @Override
    public void addToBundle(Bundle b, Object o, String key) {
        b.putBoolean(key, (Boolean) o);
    }


    public String toString(Object value)
	{
		return "" + value;
	}
	
	public Object fromString(String value)
	{
		return Boolean.parseBoolean(value);
	}

    public boolean compare(Object a, Object b) {
        return a.equals(b);
    }
}
