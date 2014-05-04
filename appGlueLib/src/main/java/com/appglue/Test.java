package com.appglue;


import java.util.ArrayList;

import android.os.Bundle;
import android.util.Log;

import static com.appglue.Constants.TAG;
import static com.appglue.Constants.LOG;

public class Test 
{	
	public static boolean isValidBundle(int index, int length, Bundle bundle, boolean sending)
	{
		boolean result = true;
		
		if(!bundle.containsKey(ComposableService.DESCRIPTION) && sending)
		{
			Log.e(TAG, "Bundle error: Doesn't have DESCRIPTION (" + index + ", " + sending + ")");
			return false;
		}
		
//		if(!bundle.containsKey(ComposableService.PARAMS) && sending)
//		{
//			Log.e(TAG, "Bundle error: Doesn't have PARAMS (" + index + ", " + sending + ")");
//			return false;
//		}
		
		// The bundle should have three elements: INPUT, DESCRIPTION, PARAMS
		if((index == 0 && sending) || (index == length - 1 && !sending))
		{
			// If it's going to the first one
			return true;
		}
		else if(!bundle.containsKey(ComposableService.INPUT))
		{
			Log.e(TAG, "Bundle error: Doesn't have INPUT (" + index + ", " + sending + ")");
			return false;
		}
		
		Object obj = bundle.get(ComposableService.INPUT);
		
		Class<?> className = obj.getClass();
		
		if(index == 0)
		{
            if(LOG) Log.d(TAG, "The index is zero");
			// Do nothing?
		}
		if(className.equals(Bundle.class))
		{
			// If input is a bundle, then it should have a TEXT
			
//			Bundle input = (Bundle) obj;
			
//			if(!input.containsKey(ComposableService.TEXT))
//			{
//				Log.e(TAG, "Bundle error: Single input doesn't have TEXT (" + index + ", " + sending + ")");
//				return false;
//			}
		}
		else if(className.equals(ArrayList.class))
		{
			// If input is an ArrayList, each element should have a TEXT
			
//			ArrayList<Bundle> inputs = (ArrayList<Bundle>) obj;
			 
//			for(int i = 0; i < inputs.size(); i++)
//			{
//				if(!inputs.get(i).containsKey(ComposableService.TEXT))
//				{
//					Log.e(TAG, String.format("Bundle error: Input [%s] doesn't have TEXT (" + index + ", " + sending + ")", "" + i));
//					return false;
//				}
//			}
		}
		else
		{
			Log.e(TAG, String.format("Class name is %s, this shouldn't have happened (" + index + ", " + sending + ")", className.getCanonicalName()));
			return false;
		}
		
		if(!result)
		{
			if(LOG) Log.d(TAG, String.format("Orch Sending [%s]: %s", "" + index, Library.printBundle(bundle)));
		}
		
		return result;
	}
}
