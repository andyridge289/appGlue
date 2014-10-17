package com.appglue.services;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;

import com.appglue.ComposableService;

public class SendSMSService extends ComposableService
{
    public static final String SMS_NUMBER = "sms_number";
    public static final String SMS_MESSAGE = "sms_message";
	
	private void sendMessage(String number, String message)
	{
		PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, SendSMSService.class), 0);
		SmsManager sms = SmsManager.getDefault();
		
		sms.sendTextMessage(number, null, message, pi, null);
	}
	
	private boolean validatePhoneNumber(String number)
	{
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

    // TODO This needs to have some serious checks in it
	
	@Override
	public ArrayList<Bundle> performService(Bundle o)
	{
//		TextMessage message = getTextMessageFromParam(parameters);
		String number = o.getString(SMS_NUMBER);
		String message = o.getString(SMS_MESSAGE);
		
		if(number == null || number.equals(""))
		{
			boolean valid = validatePhoneNumber(number);
			
			if(toastMessage.equals("") || !valid)
			{
				toastMessage = "There was a problem with the phone number you entered";
			}
			
			return null; 
		}

        if(message.length() > 160)
        {
            message = message.substring(0, 159);
        }
		
		sendMessage(number, message);
		
		return null;
	}

	@Override
	public ArrayList<Bundle> performList(ArrayList<Bundle> os)
	{
        if (os.size() < 1) {
            return null;
        }

//		TextMessage message = getTextMessageFromParam(parameters);
		String number = os.get(0).getString(SMS_NUMBER); //message.getNumber();
		String msg = os.get(0).getString(SMS_MESSAGE);
		
		if(number == null || number.equals(""))
		{
			boolean valid = validatePhoneNumber(number);
			
			if(toastMessage.equals("") || !valid)
			{
				toastMessage = "There was a problem with the phone number you entered";
			}
			
			return null; 
		}

		if(msg.length() > 160)
		{
			msg = msg.substring(0, 159);
		}

		sendMessage(number, msg);
		
		return null;
	}

}
