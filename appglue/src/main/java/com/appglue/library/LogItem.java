package com.appglue.library;

import com.appglue.engine.CompositeService;

import java.util.ArrayList;

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

    private long id;
	private long executionInstanceId;
	private CompositeService cs;
	private long startTime;
    private long endTime;
	private String message;
	private int status;
    private ArrayList<ComponentLogItem> componentLogs;
	
	public LogItem(long id, long instanceId, CompositeService cs, long startTime, long endTime, String message, int status) {

        this.id = id;
        this.executionInstanceId = instanceId;
        this.cs = cs;
        this.startTime = startTime;
        this.endTime = endTime;
        this.message = message;
        this.status = status;

        componentLogs = new ArrayList<ComponentLogItem>();
	}

    public long getLogId() {
        return id;
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

    public void addComponentLog(ComponentLogItem cli) {
        componentLogs.add(cli);
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }
}
