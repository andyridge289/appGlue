package com.appglue.description.datatypes;

import android.os.Bundle;

public class Text extends IOType
{
	public Text()
	{
		super();
		this.name = "Text";
		this.className = Text.class.getCanonicalName();
        this.sensitivity = Sensitivity.NORMAL;
	}

    @Override
	public Object getFromBundle(Bundle bundle, String key, Object defaultValue)
    {
        String ret = bundle.getString(key);
        if(ret == null)
            return defaultValue;

        return ret;
    }

    @Override
    public void addToBundle(Bundle b, Object object, String key) {
        b.putString(key, (String) object);
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

    public boolean compare(Object a, Object b) {
        return a.equals(b);
    }
}
