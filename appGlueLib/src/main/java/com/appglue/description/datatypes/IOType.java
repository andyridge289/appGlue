package com.appglue.description.datatypes;

import android.os.Bundle;
import android.util.Log;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;

public abstract class IOType
{
	protected long id;

	protected String name;
	protected String className;

    protected Sensitivity sensitivity;

    enum Sensitivity {
        NORMAL,
        SENSITIVE,
        PRIVATE
    }

	public IOType()
	{
		this.id = -1;
		this.name = "";
        this.sensitivity = Sensitivity.NORMAL;
	}
	
	public IOType(String name, String className)
	{
        this();
		this.name = name;
        this.className = className;
	}
	
	public IOType(long id, String name, String className)
	{
		this(name, className);
        this.id = id;
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

    public boolean typeEquals(IOType t) {
        return className.equals(t.getClassName());
    }

	public boolean equals(Object o) {

        if(o == null)  {
            if(LOG) Log.d(TAG, "IOType->Equals: null");
            return false;
        }

        if(!(o instanceof IOType)) {
            if(LOG) Log.d(TAG, "IOType->Equals: not ServiceIO");
            return false;
        }

        IOType other = (IOType) o;

        if(id != other.getID()) {
            if(LOG) Log.d(TAG, "IOType->Equals: id - [" + id + " :: " + other.getID() + "]");
            return false;
        }

        if(!name.equals(other.getName())) {
            if(LOG) Log.d(TAG, "IOType->Equals: name - [" + name + " :: " + other.getName() + "]");
            return false;
        }

        if(!className.equals(other.getClassName())) {
            if(LOG) Log.d(TAG, "IOType->Equals: class name - [" + className + " :: " + other.getClassName() + "]");
            return false;
        }

        if(sensitivity != other.getSensitivity()) {
            if(LOG) Log.d(TAG, "IOType->Equals: sensitivity - [" + sensitivity + " :: " + other.getSensitivity() + "]");
            return false;
        }

        return true;
    }

    public Sensitivity getSensitivity() {
        return this.sensitivity;
    }

    public void setSensitivity(Sensitivity sensitivity) {
        this.sensitivity = sensitivity;
    }

    public abstract Object getFromBundle(Bundle b, String key, Object defaultValue);
    public abstract void addToBundle(Bundle b, Object o, String key);
	
	public abstract String toString(Object value); // One for any OTHER iotype
	public abstract Object fromString(String value);

    public abstract boolean compare(Object a, Object b);
	
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
        public static final String USERNAME = Username.class.getCanonicalName();
        public static final String PASSWORD = Password.class.getCanonicalName();

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
            else if(name.equals(USERNAME))
                return new Username();
            else if(name.equals(PASSWORD))
                return new Password();
            else
                Log.e(TAG, "Fail to find type " + name);

			return null;
		}
	}
}
