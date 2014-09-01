package com.appglue.datatypes;

import android.os.Bundle;

public class NumberInt extends IOType
{
	public NumberInt()
	{
		super();
		this.name = "NumberInt";
		this.className = NumberInt.class.getCanonicalName();
		this.value = 0;
        this.sensitivity = Sensitivity.NORMAL;
	}

    @Override
    public Object getFromBundle(Bundle bundle, String key, Object defautValue) {
        return bundle.getInt(key, (Integer) defautValue);
    }

    @Override
    public void addToBundle(Bundle b, Object o, String key) {
        b.putInt(key, (Integer) o);
    }


    public String toString(Object value)
	{
		return "" + value;
	}
	
	public Object fromString(String value)
	{
		return Integer.parseInt(value);
	}
	
}
