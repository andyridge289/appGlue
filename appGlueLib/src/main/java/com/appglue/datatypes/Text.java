package com.appglue.datatypes;

import android.os.Bundle;

public class Text extends IOType
{
	public Text()
	{
		super();
		this.name = "Text";
		this.className = Text.class.getCanonicalName();
		this.value = "";
	}

    @Override
	public Object getFromBundle(Bundle bundle, String key, Object defaultValue)
    {
        return bundle.getString(key, (String) defaultValue);
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
}
