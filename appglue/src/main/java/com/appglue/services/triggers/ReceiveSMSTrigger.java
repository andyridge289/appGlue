package com.appglue.services.triggers;

import static com.appglue.Constants.TAG;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class ReceiveSMSTrigger extends GenericTrigger
{
	public static final String SMS_NUMBER = "sms_number";
	public static final String SMS_MESSAGE = "sms_message";
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
		if(!intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
		{
			return;
		}
		
	    Bundle bundle = intent.getExtras();
		String number = "";
		String message = "";
		
        if (bundle != null) 
        {
            Object[] pdus = (Object[]) bundle.get("pdus");

            for (Object pdu : pdus) {
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);

                if (number.equals("")) {
                    number = sms.getOriginatingAddress();
                    message = sms.getDisplayMessageBody();
                } else if (sms.getOriginatingAddress().equals(number)) {
                    message += sms.getDisplayMessageBody();
                } else {
                    Log.e(TAG, "Not really expecting this to happen to be honest (SMS list not from same person)");
                }
            }
        }
        
        Bundle b = new Bundle();
		b.putString(SMS_NUMBER, number);
		b.putString(SMS_MESSAGE, message);
		
		super.trigger(context, this.getClass().getCanonicalName(), b, false, 0);
	}
	
}

