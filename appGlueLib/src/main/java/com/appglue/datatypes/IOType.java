package com.appglue.datatypes;

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
		if(!this.className.equals(other.getClassName()))
			return false;
		
		return true;
	}
	
	public abstract Object toStorable(Object value);
	public abstract Object fromStorable(Object value);
	
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
		
		public static IOType getType(String name)
		{
//			try
			{		
				// The first 3 are string types so it's pretty easy
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
//				{
//					return new IOType("App", App.class.getCanonicalName(), IOType.class.getDeclaredMethod("toStorable", String.class));
//				}
//				else
//				{
//					Log.e(TAG, "Class name isn't supported by the factory yet " + name);
//				}
			}
//			catch(NoSuchMethodException e)
//			{
//				Log.e(TAG, "Error: Method not found!");
//			}
			
//			return new IOType();
			return null;
		}
		
		
	}
}
