package com.appglue.datatypes;

public class Password extends Text
{
	public Password()
	{
		super();
		this.name = "Password";
		this.className = Password.class.getCanonicalName();
        this.sensitivity = Sensitivity.PRIVATE;
	}

	public Password(String value)
	{
		this();
	}
}
