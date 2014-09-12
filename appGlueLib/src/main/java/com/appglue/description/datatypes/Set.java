package com.appglue.description.datatypes;

public class Set extends NumberInt
{
	public Set()
	{
		super();
		this.name = "Set";
		this.className = Set.class.getCanonicalName();
        this.sensitivity = Sensitivity.NORMAL;
	}
}
