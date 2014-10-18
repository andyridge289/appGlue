package com.appglue.description.datatypes;

public class PhoneNumber extends Text 
{
	public PhoneNumber()
	{
		super();
		this.name = "Phone Number";
		this.className = PhoneNumber.class.getCanonicalName();
        this.sensitivity = Sensitivity.SENSITIVE;
        this.acceptsManual = true;
        this.manualLookup = true;
	}
}
