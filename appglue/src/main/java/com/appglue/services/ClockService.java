package com.appglue.services;

import android.os.Bundle;

import com.appglue.ComposableService;

import java.util.ArrayList;

public class ClockService extends ComposableService
{
    @Override
    public ArrayList<Bundle> performService(Bundle o, ArrayList<Bundle> parameters) {
        return null;
    }

    @Override
    public ArrayList<Bundle> performList(ArrayList<Bundle> os, ArrayList<Bundle> parameters) {
        return null;
    }
//	@Override
//	public Bundle performService(Bundle input, ArrayList<Bundle> parameters) 
//	{
//		ArrayList<String> times = new ArrayList<String>();
//		times.add("1");
//		times.add("2");
//		times.add("3");
//		isList = true;
//		
//		return null;
//	}
//
//	@Override
//	public Bundle performList(ArrayList<Bundle> inputs, ArrayList<Bundle> parameters) 
//	{
//		return null;
//	}
}