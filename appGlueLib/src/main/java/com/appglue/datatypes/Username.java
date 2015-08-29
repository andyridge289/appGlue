package com.appglue.datatypes;

public class Username extends Text
{
	public Username()
	{
		super();
		this.name = "Username";
		this.className = Username.class.getCanonicalName();
        this.sensitivity = Sensitivity.SENSITIVE;
	}
}
