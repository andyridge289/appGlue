package com.appglue.datatypes;

import android.os.Bundle;
import android.util.Log;

import static com.appglue.Constants.TAG;

public abstract class IOType
{
	protected long id;

	protected String name;
	protected String className;
	
	protected Object value;
	
	public IOType()
	{
		this.id = -1;
		this.name = "";
	}
	
	public IOType(String name, String className)
	{
		this.id = -1;
		this.name = name;
	}
	
	public IOType(long id, String name, String className)
	{
		this.id = id;
		this.name = name;
	}

    public String toString()
    {
        return this.id + ": " + this.name;
    }
	
	public long getID()
	{
		return this.id;
	}

	public void setID(long id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}
	
	public String getClassName()
	{
		return className;
	}
	
	public void setClassName(String className)
	{
		this.className = className;
	}

	public void setName(String name) 
	{
		this.name = name;
	}

	public boolean equals(IOType other)
	{
        return this.className.equals(other.getClassName());
    }

    public abstract Object getFromBundle(Bundle b, String key, Object defautValue);
    public abstract void addToBundle(Bundle b, Object o, String key);
	
	public abstract String toString(Object value); // One for any OTHER iotype
	public abstract Object fromString(String value);
	
	public static class Factory
	{
		public static final String TEXT = Text.class.getCanonicalName();
		public static final String URL = URLObject.class.getCanonicalName();
		public static final String NUMBER = NumberInt.class.getCanonicalName();
		public static final String PHONE_NUMBER = PhoneNumber.class.getCanonicalName();
		public static final String BOOLEAN = Bool.class.getCanonicalName();
		public static final String SET = Set.class.getCanonicalName();
		public static final String APP = App.class.getCanonicalName();
        public static final String IMAGE = Image.class.getCanonicalName();
        public static final String IMAGE_DRAWABLE = ImageDrawableResource.class.getCanonicalName();

        public static IOType getType(String name)
		{

            if(name.equals(TEXT))
                return new Text();
            else if(name.equals(URL))
                return new URLObject();
            else if(name.equals(PHONE_NUMBER))
                return new PhoneNumber();
            else if(name.equals(NUMBER))
                return new NumberInt();
            else if(name.equals(BOOLEAN))
                return new Bool();
            else if(name.equals(SET))
                return new Set();
            else if(name.equals(APP))
                return new App();
            else if(name.equals(IMAGE))
                return new Image();
            else if(name.equals(IMAGE_DRAWABLE))
                return new ImageDrawableResource();
            else
                Log.e(TAG, "Fail to find type " + name);

			return null;
		}
		
		
	}
}
