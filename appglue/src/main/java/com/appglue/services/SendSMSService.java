package com.appglue.services;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.appglue.ComposableService;

import java.util.ArrayList;

public class SendSMSService extends ComposableService {
    public static final String SMS_NUMBER = "sms_number";
    public static final String SMS_MESSAGE = "sms_message";
    public static final String SMS_LAST_EXECUTE = "sms_last_execute";

    private void sendMessage(String number, String message) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        long last = prefs.getLong(SMS_LAST_EXECUTE, -1);

        // We can only send one text every five minutes.
        long timeSinceLast = System.currentTimeMillis() - last;
        long fiveMinutes = 60 * 5 * 1000;

        if (timeSinceLast > fiveMinutes || last == -1) {
            PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, SendSMSService.class), 0);
            SmsManager sms = SmsManager.getDefault();

            sms.sendTextMessage(number, null, message, pi, null);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(SMS_LAST_EXECUTE, System.currentTimeMillis());
            editor.commit();
        } else {
            Toast.makeText(this, "You sent a text less than 5 minutes ago. Check the preferences", Toast.LENGTH_LONG).show();
        }
    }

    private boolean validatePhoneNumber(String number) {
        for (int i = 0; i < number.length(); i++) {
            char c = number.charAt(i);
            if (!(Character.isDigit(c) || c == '+')) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ArrayList<Bundle> performService(Bundle o) {
//		TextMessage message = getTextMessageFromParam(parameters);
        String number = o.getString(SMS_NUMBER);
        String message = o.getString(SMS_MESSAGE);

        if (number == null || number.equals("")) {
            boolean valid = validatePhoneNumber(number);

            if (toastMessage.equals("") || !valid) {
                toastMessage = "There was a problem with the phone number you entered";
            }

            return null;
        }

        if (message.length() > 160) {
            message = message.substring(0, 159);
        }

        sendMessage(number, message);

        return null;
    }

    @Override
    public ArrayList<Bundle> performList(ArrayList<Bundle> os) {
        if (os.size() < 1) {
            return null;
        }

//		TextMessage message = getTextMessageFromParam(parameters);
        String number = os.get(0).getString(SMS_NUMBER); //message.getNumber();
        String msg = os.get(0).getString(SMS_MESSAGE);

        if (number == null || number.equals("")) {
            boolean valid = validatePhoneNumber(number);

            if (toastMessage.equals("") || !valid) {
                toastMessage = "There was a problem with the phone number you entered";
            }

            return null;
        }

        if (msg.length() > 160) {
            msg = msg.substring(0, 159);
        }

        sendMessage(number, msg);

        return null;
    }

}
