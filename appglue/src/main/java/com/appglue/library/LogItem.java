package com.appglue.library;

import android.database.Cursor;

import com.appglue.engine.CompositeService;
import com.appglue.serviceregistry.Registry;

import static com.appglue.Constants.CLASSNAME;
import static com.appglue.Constants.COMPOSITE_ID;
import static com.appglue.Constants.ID;
import static com.appglue.library.AppGlueConstants.LOG_TYPE;
import static com.appglue.library.AppGlueConstants.MESSAGE;
import static com.appglue.library.AppGlueConstants.TIME;

public class LogItem 
{
    public static final int LOG_COMPOSITE_SUCCESS = 0x5;
    public static final int LOG_SUCCESS = 0x1;
    public static final int LOG_FAIL = 0x2;
    public static final int LOG_STOP = 0x3;
    public static final int LOG_FILTER = 0x4;

	private long id;
	private CompositeService cs;
	private String className;
	private String time;
	private String message;
	private int status;
	
	public LogItem(Cursor c)
	{
		this.id = c.getLong(c.getColumnIndex(ID));
		
		long compositeId = c.getLong(c.getColumnIndex(COMPOSITE_ID));
		
		if(compositeId != -1)
			this.cs = Registry.getInstance(null).getComposite(compositeId);
		else
			this.cs = new CompositeService(false);
			
		this.className = c.getString(c.getColumnIndex(CLASSNAME));
		this.time = c.getString(c.getColumnIndex(TIME));
		this.message = c.getString(c.getColumnIndex(MESSAGE));

        this.status = c.getInt(c.getColumnIndex(LOG_TYPE));
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

	public String getMessage() 
	{
		return message;
	}

	public void setMessage(String message) 
	{
		this.message = message;
	}

	public int getStatus()
	{
		return status;
	}

	public void setStatus(int success)
	{
		this.status = success;
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
