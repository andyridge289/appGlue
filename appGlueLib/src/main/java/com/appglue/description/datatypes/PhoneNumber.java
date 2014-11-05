package com.appglue.description.datatypes;

import android.text.InputType;

public class PhoneNumber extends Text
{
	public PhoneNumber() {
		super();
		this.name = "Phone Number";
		this.className = PhoneNumber.class.getCanonicalName();
        this.sensitivity = Sensitivity.SENSITIVE;
        this.acceptsManual = true;
        this.manualLookup = true;
        this.manualEditTextType = InputType.TYPE_CLASS_PHONE;
	}
}
