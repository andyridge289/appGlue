package com.appglue.datatypes;

public class Set extends NumberInt
{
	public Set()
	{
		super();
		this.name = "Set";
		this.className = Set.class.getCanonicalName();
        this.sensitivity = Sensitivity.NORMAL;
        this.acceptsManual = false;
	}
}
