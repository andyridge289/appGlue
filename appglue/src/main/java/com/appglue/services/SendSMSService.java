package com.appglue.services;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;

import com.appglue.ComposableService;

public class SendSMSService extends ComposableService
{
//	private TextMessage getTextMessageFromParam(ArrayList<Bundle> parameters)
//	{
//		if(parameters == null)
//		{
//			// This shouldn't happen because the thing should be mandatory
//			toastMessage = "You haven't sent a phone number!";
//			return null;
//		}
//		
//		String numberParam = null;
//		String messageParam = null;
//		
//		for(int i = 0; i < parameters.size(); i++)
//		{
//			Bundle param = parameters.get(i);
//			String name = param.getString(NAME);
//			
//			if(name.equals(TextMessage.NUMBER))
//			{
//				numberParam = param.getString(VALUE);
//			}
//			else if(name.equals(TextMessage.MESSAGE))
//			{
//				messageParam = param.getString(VALUE);
//			}
//		}
//		
//		return new TextMessage(numberParam, messageParam);
//	}
	
	private void sendMessage(String number, String message)
	{
		PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, SendSMSService.class), 0);
		SmsManager sms = SmsManager.getDefault();
		
		sms.sendTextMessage(number, null, message, pi, null);
	}
	
	private boolean validatePhoneNumber(String number)
	{
		if (number.charAt(0) == '0')
		{
			number = number.substring(1);
			number = "+44" + number;
		}
		
		for(int i = 0; i < number.length(); i++)
		{
			char c = number.charAt(i);
			if(!(Character.isDigit(c) || c == '+'))
			{
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public ArrayList<Bundle> performService(Bundle o, ArrayList<Bundle> parameters) 
	{
//		TextMessage message = getTextMessageFromParam(parameters);
		String number = ""; //message.getNumber();
		String message = "";
		
		if(number == null || number.equals(""))
		{
			boolean valid = validatePhoneNumber(number);
			
			if(toastMessage.equals("") || !valid)
			{
				toastMessage = "There was a problem with the phone number you entered";
			}
			
			return null; 
		}
		
		String text = o.getString(ComposableService.TEXT);
		
		if(message != null)
		{
			text = message;
		}
		
		sendMessage(number, text);
		
		return null;
	}

	@Override
	public ArrayList<Bundle> performList(ArrayList<Bundle> os, ArrayList<Bundle> parameters) 
	{
//		TextMessage message = getTextMessageFromParam(parameters);
		String number = ""; //message.getNumber();
		String message = "";
		
		if(number == null || number.equals(""))
		{
			boolean valid = validatePhoneNumber(number);
			
			if(toastMessage.equals("") || !valid)
			{
				toastMessage = "There was a problem with the phone number you entered";
			}
			
			return null; 
		}
		
		StringBuilder text = new StringBuilder();

        for (Bundle o : os) {
            text.append(o.getString(ComposableService.TEXT)).append("; ");
        }
		
		String msg = text.toString();
		if(msg.length() > 160)
		{
			msg = msg.substring(0, 160);
		}
		
		if(message != null)
		{
			msg = message;
		}
		
		sendMessage(number, msg);
		
		return null;
	}

}
