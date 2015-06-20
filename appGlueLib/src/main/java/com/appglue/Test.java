package com.appglue;


import java.util.ArrayList;

import android.os.Bundle;

import com.orhanobut.logger.Logger;


public class Test 
{	
	public static boolean isValidBundle(int index, int length, Bundle bundle, boolean sending)
	{
		boolean result = true;
		
		if(!bundle.containsKey(ComposableService.DESCRIPTION) && sending)
		{
			Logger.e("Bundle error: Doesn't have DESCRIPTION (" + index + ", " + sending + ")");
			return false;
		}
		
		// The bundle should have three elements: INPUT, DESCRIPTION, PARAMS
		if((index == 0 && sending) || (index == length - 1 && !sending))
		{
			// If it's going to the first one
			return true;
		}
		else if(!bundle.containsKey(ComposableService.INPUT))
		{
			Logger.e("Bundle error: Doesn't have INPUT (" + index + ", " + sending + ")");
			return false;
		}
		
		Object obj = bundle.get(ComposableService.INPUT);
		
		Class<?> className = obj.getClass();
		
		if(index == 0)
		{
            Logger.d("The index is zero");
			// Do nothing?
		}
		if (className.equals(Bundle.class))
		{
			// If input is a bundle, then it should have a TEXT
			
			Bundle input = (Bundle) obj;
			
		}
		else if (className.equals(ArrayList.class))
		{
			// If input is an ArrayList, each element should have a TEXT
			
			ArrayList<Bundle> getInputs = (ArrayList<Bundle>) obj;
			 
//			for(int i = 0; i < getInputs.size(); i++)
//			{
//				if(!getInputs.get(i).containsKey(ComposableService.TEXT))
//				{
//					Logger.e(String.format("Bundle error: Input [%s] doesn't have TEXT (" + index + ", " + sending + ")", "" + i));
//					return false;
//				}
//			}
		}
		else
		{
			Logger.e(String.format("Class name is %s, this shouldn't have happened (" + index + ", " + sending + ")", className.getCanonicalName()));
			return false;
		}
		
		if(!result)
		{
			Logger.d(String.format("Orch Sending [%s]: %s", "" + index, Library.printBundle(bundle)));
		}
		
		return result;
	}
}
