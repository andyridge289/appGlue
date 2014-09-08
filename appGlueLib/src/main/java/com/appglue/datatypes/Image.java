package com.appglue.datatypes;

import android.graphics.BitmapFactory;
import android.os.Bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Image extends IOType
{
    private String location;

	public Image()
	{
		super();
		this.name = "Image";
		this.className = Image.class.getCanonicalName();
		this.value = null;
        this.sensitivity = Sensitivity.NORMAL;
	}

    @Override
    public Object getFromBundle(Bundle bundle, String key, Object defaultValue)
    {
        String filename = bundle.getString(key);
        if(filename == null)
            filename = (String) defaultValue;

        File f = new File(filename);

        try {
            value = BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return value;
    }

    @Override
    public void addToBundle(Bundle b, Object o, String key) {

    }

    public void loadFile()
    {
        File f = new File(this.location);

        try {
            value = BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

//	@Override
//	public String fromStorable(Object value) {
//
//        this.location = (String) value;
//        loadFile();
//        return location;
//    }

	@Override
	public String toString(Object value)
	{
		return (String) value;
	}
	
	public Object fromString(String value)
	{
		return value;
	}

    public String getLocation() { return location; }
}
