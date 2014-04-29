package com.appglue.engine;

import static com.appglue.Constants.*;

import java.util.ArrayList;

import com.appglue.description.ServiceDescription;

import static com.appglue.library.AppGlueConstants.TEMP_ID;

public class CompositeService 
{
	private long id;
	private String name;
	private String description;
	
	private long numeral;
	private Interval interval;
	
	private boolean shouldBeRunning;
	
	public static final int NEW_COMPOSITE_PLACEHOLER = Integer.MIN_VALUE;
	
	private ArrayList<ServiceDescription> components;
	
	public CompositeService(boolean temp)
	{
        if(temp)
            this.id = TEMP_ID;
        else
		    this.id = -1;
		this.name = ""; // We know that the name can never be blank so we're good
		this.description = "";
		this.components = new ArrayList<ServiceDescription>();
		this.shouldBeRunning = false;
	}
	
	public CompositeService(String name, String description, ArrayList<ServiceDescription> components)
	{
		this(false);
		this.name = name;
		this.description = description;
		this.components = components;
	}
	
	public static CompositeService makePlaceholder()
	{
		return new CompositeService(NEW_COMPOSITE_PLACEHOLER, "Nothing", "Nothing", null, false);
	}
	
	public CompositeService(long id)
	{
		
	}
	
	public CompositeService(long id, String name, String description, boolean shouldBeRunning)
	{
		this.id = id;
		this.name = name;
		this.components = null;
		this.description = description;
		
		this.shouldBeRunning = shouldBeRunning;
	}
	
	public CompositeService(long id, String name, String description, ArrayList<ServiceDescription> services, boolean shouldBeRunning)
	{
		this.id = id;
		this.name = name;
		this.components = services;
		this.description = description;
		
		this.shouldBeRunning = shouldBeRunning;
	}
	
	public CompositeService(long id, String name, ArrayList<ServiceDescription> services, long numeral, Interval interval)
	{
		this.id = id;
		this.name = name;
		this.components = services;
		
		this.numeral = numeral;
		this.interval = interval;
		
		this.shouldBeRunning = false;
	}
	
	
	public CompositeService(ArrayList<ServiceDescription> orchestration)
	{
		// Generate a random name
		this.id = -1;
		this.name = "Random Service";
		
		this.components = orchestration;
		
		this.shouldBeRunning = false;
	}
	
	/**
	 * @param className
	 * @return
	 */
	public ServiceDescription getComponent(String className)
	{
		for(int i = 0 ; i < components.size(); i++)
		{
			if(components.get(i).getClassName().equals(className))
				return components.get(i);
		}
		
		return null;
	}
	
	public String getName() 
	{
		return name;
	}

	public void setName(String name) 
	{
		this.name = name;
	}
	
	public void addComponent(ServiceDescription service)
	{
		this.components.add(service);
	}
	
	public void addComponent(int position, ServiceDescription component)
	{
		this.components.add(position, component);
	}
	
	public void resetOrchestration()
	{
		this.components.clear();
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description) 
	{
		this.description = description;
	}

	public long getId() 
	{
		return id;
	}

	public void setId(long id) 
	{
		this.id = id;
	}

	public long getNumeral() 
	{
		return numeral;
	}

	public void setNumeral(long numeral) 
	{
		this.numeral = numeral;
	}

	public Interval getInterval() 
	{
		return interval;
	}

	public void setInterval(Interval interval) 
	{
		this.interval = interval;
	}

	public ArrayList<ServiceDescription> getComponents() 
	{
		return components;
	}

	public void setComponents(ArrayList<ServiceDescription> components)
	{
		this.components = components;
	}
	
	public boolean isShouldBeRunning() 
	{
		return shouldBeRunning;
	}

	public void setShouldBeRunning(boolean shouldBeRunning) 
	{
		this.shouldBeRunning = shouldBeRunning;
	}

	public boolean containsTrigger()
	{
		if(this.components.get(0).getProcessType() == ProcessType.TRIGGER)
			return true;
		else
			return false;
	}
}
