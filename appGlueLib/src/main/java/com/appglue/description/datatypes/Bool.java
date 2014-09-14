package com.appglue.description.datatypes;

import android.os.Bundle;

public class Bool extends IOType
{
	public Bool()
	{
		super();
		this.name = "Bool";
		this.className = Bool.class.getCanonicalName();
//		this.value = false;
        this.sensitivity = Sensitivity.NORMAL;
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
