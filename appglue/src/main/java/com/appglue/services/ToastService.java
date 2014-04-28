package com.appglue.services;

import java.util.ArrayList;

import android.os.Bundle;

import com.appglue.ComposableService;

public class ToastService extends ComposableService 
{
	public static final String TOAST_MESSAGE = "toast_message";

	@Override
	public ArrayList<Bundle> performService(Bundle o, ArrayList<Bundle> parameters) 
	{
		final String text = o.getString(TOAST_MESSAGE);
		toastMessage = text;
		return null;
	}

	@Override
	public ArrayList<Bundle> performList(ArrayList<Bundle> os, ArrayList<Bundle> parameters) 
	{
		for(int i = 0 ; i < os.size(); i++)
		{
			performService(os.get(i), parameters);
		}
		
		return null;
	}
	
}
