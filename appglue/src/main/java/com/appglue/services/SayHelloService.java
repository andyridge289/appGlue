package com.appglue.services;

import java.util.ArrayList;

import android.os.Bundle;

import com.appglue.ComposableService;

public class SayHelloService  extends ComposableService
{
	public static final String TAG_HELLO = "hello_text";
	
	@Override
	public ArrayList<Bundle> performService(Bundle o)
	{
		Bundle data = new Bundle();
		data.putString(TAG_HELLO, "Why hello there...");
		
		ArrayList<Bundle> list = new ArrayList<Bundle>();
		list.add(data);
		
		return list;
	}

	@Override
	public ArrayList<Bundle> performList(ArrayList<Bundle> os)
	{
		ArrayList<Bundle> stuff = new ArrayList<Bundle>();

        for (Bundle o : os) {
            stuff.addAll(performService(o));
        }
		
		return stuff;
	}

}
