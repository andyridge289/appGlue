package com.appglue.library;

import android.database.Cursor;
import android.os.Bundle;

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
    public static final int SUCCESS = 0x1;

    public static final int FILTER = 0x1;
    public static final int COMPOSITE_STOP = 0x2;

    public static final int COMPONENT_FAIL = 0x3; // IF the component crashes
    public static final int MESSAGE_FAIL = 0x4; // IF the message passing fails
    public static final int NETWORK_FAIL = 0x5; // If some networking fails somewhere
    public static final int OTHER_FAIL = 0x6; // Derp

    public static final int GENERIC_TRIGGER_FAIL = 0x7; // This is very much a special case

	private long executionInstanceId;
	private CompositeService cs;
	private long startTime;
    private long endTime;
	private String message;
	private int status;
	
	public LogItem(Cursor c)
	{
		this.executionInstanceId = c.getLong(c.getColumnIndex(ID));
		long compositeId = c.getLong(c.getColumnIndex(COMPOSITE_ID));
		
//		if(compositeId != -1)
//			this.cs = Registry.getInstance(null).getComposite(compositeId);
//		else
//			this.cs = new CompositeService(false);
//
//		this.className = c.getString(c.getColumnIndex(CLASSNAME));
//		this.time = c.getString(c.getColumnIndex(TIME));
//		this.message = c.getString(c.getColumnIndex(MESSAGE));
//
//        this.status = c.getInt(c.getColumnIndex(LOG_TYPE));
	}

	public long getId() 
	{
		return executionInstanceId;
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

    private class ComponentLog {
        private long id;
        private String className;
        private String message;
        private Bundle inputBundle;
        private Bundle outputBundle;
        private int logType;
        private long time;

        private ComponentLog(long id, String className, String message, Bundle inputBundle, Bundle outputBundle, int logType, long time) {
            this.id = id;
            this.className = className;
            this.message = message;
            this.inputBundle = inputBundle;
            this.outputBundle = outputBundle;
            this.logType = logType;
            this.time = time;
        }
    }

}
