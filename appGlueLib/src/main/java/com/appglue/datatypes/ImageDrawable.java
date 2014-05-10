package com.appglue.datatypes;

import android.os.Bundle;

import com.appgluelib.appgluelib.R;

public class ImageDrawable extends IOType
{
	public ImageDrawable()
	{
		super();
		this.name = "Image";
		this.className = ImageDrawable.class.getCanonicalName();
		this.value = null;
	}

    @Override
    public Object getFromBundle(Bundle bundle, String key, Object defautValue)
    {
        return bundle.getInt(key, R.drawable.ic_launcher);
    }

    @Override
    public void addToBundle(Bundle b, Object o, String key) {
        b.putInt(key, (Integer) o);
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
