package com.appglue.engine.io;

import com.appglue.datatypes.IOType;


public class IORepository 
{
	private IOType root;
	
	public static IORepository ioRepository = null;
	
	private IORepository()
	{
		// Set it up containing object
//		this.root = new IOType(Object.class.getCanonicalName(), "");
	}
	
	public IOType getRoot()
	{
		return this.root;
	}
	
	public void write()
	{
//		String out = this.root.toString();
	}
	
	public void read()
	{
		
	}
	
	public static IORepository getInstance()
	{
		if(ioRepository == null)
			ioRepository = new IORepository();
		
		return ioRepository;
	}
}
