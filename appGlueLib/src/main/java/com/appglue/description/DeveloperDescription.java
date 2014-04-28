package com.appglue.description;

public class DeveloperDescription 
{

	private String name;
	
	public DeveloperDescription(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String toString()
	{
		return "Dev: " + this.name;
	}
}
