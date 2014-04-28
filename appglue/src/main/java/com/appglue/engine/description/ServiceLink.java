package com.appglue.engine.description;

import com.appglue.Constants.Composition;
import com.appglue.Constants.Level;
import com.appglue.description.ServiceDescription;

public class ServiceLink extends ServiceDescription
{
	public Composition problem;
	public Level level;
	public int position;
	public String message;
	
	public ServiceLink()
	{
		this.level = Level.OKAY;
	}
	
	public ServiceLink(Composition problem)
	{
		
	}
	
	public ServiceLink(Composition problem, int position, String message)
	{	
		this.problem = problem;
		
		switch(problem)
		{
			case EMPTY:
			case NO_MATCH:
				this.level = Level.ERROR;
				break;
				
			case FIRST_INPUT:
			case LAST_OUTPUT: 
			case NO_INPUT_NOT_FIRST:
			case NO_OUTPUT_NOT_LAST:
				this.level = Level.WARNING;
				break;
		}
		
		this.position = position;
		this.message = message;
		
		this.setIsNormal();
	}
	
}
