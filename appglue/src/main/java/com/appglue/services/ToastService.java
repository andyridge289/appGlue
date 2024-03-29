package com.appglue.services;

import java.util.ArrayList;

import android.os.Bundle;

import com.appglue.ComposableService;

public class ToastService extends ComposableService 
{
	public static final String TOAST_MESSAGE = "toast_message";

	@Override
	public ArrayList<Bundle> performService(Bundle o)
	{
        toastMessage = o.getString(TOAST_MESSAGE);
		return null;
	}

	@Override
	public ArrayList<Bundle> performList(ArrayList<Bundle> os)
	{
        for (Bundle o : os) {
            performService(o);
        }
		
		return null;
	}
	
}
