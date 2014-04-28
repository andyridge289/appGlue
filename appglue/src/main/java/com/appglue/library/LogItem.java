package com.appglue.library;

import static com.appglue.Constants.CLASSNAME;
import static com.appglue.Constants.COMPOSITE_ID;
import static com.appglue.Constants.ID;
import static com.appglue.library.AppGlueConstants.TIME;
import static com.appglue.library.AppGlueConstants.MESSAGE;
import static com.appglue.library.AppGlueConstants.LOG_TYPE;

import com.appglue.engine.CompositeService;
import com.appglue.serviceregistry.Registry;

import android.database.Cursor;

public class LogItem 
{
	private long id;
	private CompositeService cs;
	private String className;
	private String time;
	private String message;
	private boolean success;
	
	public LogItem(long id, long compositeId, String className, String time, String message, boolean success) 
	{
		this.id = id;
		this.cs = null; 
		this.className = className;
		this.time = time;
		this.message = message;
		this.success = success;
	}
	
	public LogItem(Cursor c)
	{
		this.id = c.getLong(c.getColumnIndex(ID));
		
		long compositeId = c.getLong(c.getColumnIndex(COMPOSITE_ID)); 
		
		if(compositeId != -1)
			this.cs = Registry.getInstance(null).getComposite(compositeId);
		else
			this.cs = new CompositeService();
			
		this.className = c.getString(c.getColumnIndex(CLASSNAME));
		this.time = c.getString(c.getColumnIndex(TIME));
		this.message = c.getString(c.getColumnIndex(MESSAGE));
		
		int iSuccess = c.getInt(c.getColumnIndex(LOG_TYPE));
		this.success = iSuccess == 1 ? true : false;
	}

	public long getId() 
	{
		return id;
	}

	public void setId(long id) 
	{
		this.id = id;
	}

	public String getClassName() 
	{
		return className;
	}

	public void setClassName(String className) 
	{
		this.className = className;
	}

	public String getTime() 
	{
		return time;
	}

	public void setTime(String time) 
	{
		this.time = time;
	}

	public String getMessage() 
	{
		return message;
	}

	public void setMessage(String message) 
	{
		this.message = message;
	}

	public boolean isSuccess() 
	{
		return success;
	}

	public void setSuccess(boolean success) 
	{
		this.success = success;
	}

	public CompositeService getComposite() 
	{
		return cs;
	}

	public void setComposite(CompositeService cs) 
	{
		this.cs = cs;
	}
	
	
	
}
