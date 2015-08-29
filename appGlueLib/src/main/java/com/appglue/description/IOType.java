package com.appglue.description;

import android.os.Bundle;

import com.appglue.db.AppGlueDB;
import com.appglue.datatypes.App;
import com.appglue.datatypes.Bool;
import com.appglue.datatypes.Image;
import com.appglue.datatypes.ImageDrawableResource;
import com.appglue.datatypes.NumberInt;
import com.appglue.datatypes.Password;
import com.appglue.datatypes.PhoneNumber;
import com.appglue.datatypes.Set;
import com.appglue.datatypes.Text;
import com.appglue.datatypes.URLObject;
import com.appglue.datatypes.Username;
import com.orhanobut.logger.Logger;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

public abstract class IOType extends BaseModel {

	protected long id;
	protected String name;
	protected String className;
    protected Sensitivity sensitivity;
    protected boolean acceptsManual;
    protected int manualEditTextType = -1;
    protected boolean manualLookup;

    public enum Sensitivity {
        NORMAL,
        SENSITIVE,
        PRIVATE
    }

	public IOType() {
		this.id = -1;
		this.name = "";
        this.sensitivity = Sensitivity.NORMAL;
        this.acceptsManual = false;
        this.manualLookup = false;
	}
	
	public IOType(String name, String className, boolean acceptsManual, boolean manualLookup, int manualEditTextType) {
        this();
		this.name = name;
        this.className = className;
        this.acceptsManual = acceptsManual;
        this.manualLookup = manualLookup;
        this.manualEditTextType = manualEditTextType;
	}
	
	public IOType(long id, String name, String className, boolean acceptsManual, boolean manualLookup, int manualEditTextType) {
		this(name, className, acceptsManual, manualLookup, manualEditTextType);
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

    public boolean acceptsManualValues() {
        return acceptsManual;
    }

    public void setManualAcceptance(boolean acceptsManual) {
        this.acceptsManual = acceptsManual;
    }

    public boolean supportsManualLookup() {
        return manualLookup;
    }

    public void setManualLookupSupport(boolean manualLookup) {
        this.manualLookup = manualLookup;
    }

    public int getManualEditTextType() {
        return manualEditTextType;
    }

    public boolean typeEquals(IOType t) {
        return className.equals(t.getClassName());
    }

    public boolean typeExtends(IOType t) {
        return this.getClass().isAssignableFrom(t.getClass());
    }

	public boolean equals(Object o) {

        if(o == null)  {
            Logger.d("IOType->Equals: null");
            return false;
        }

        if(!(o instanceof IOType)) {
            Logger.d("IOType->Equals: not ServiceIO");
            return false;
        }

        IOType other = (IOType) o;

        if(!name.equals(other.getName())) {
            Logger.d("IOType->Equals: name - [" + name + " :: " + other.getName() + "]");
            return false;
        }

        if(!className.equals(other.getClassName())) {
            Logger.d("IOType->Equals: class name - [" + className + " :: " + other.getClassName() + "]");
            return false;
        }

        if(sensitivity != other.getSensitivity()) {
            Logger.d("IOType->Equals: sensitivity - [" + sensitivity + " :: " + other.getSensitivity() + "]");
            return false;
        }

        if (this.acceptsManual != other.acceptsManualValues()) {
            Logger.d("IOType->Equals: manual accepted - [" + acceptsManual + " :: " + other.acceptsManualValues() + "]");
            return false;
        }

        if (this.manualEditTextType != other.getManualEditTextType()) {
            Logger.d("IOType->Equals: manual text type - [" + manualEditTextType + " :: " + other.getManualEditTextType() + "]");
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

        public static IOType getType(String name) {
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
                Logger.e("Fail to find type " + name);

			return null;
		}
	}
}
