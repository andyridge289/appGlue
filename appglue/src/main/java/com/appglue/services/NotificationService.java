package com.appglue.services;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

import com.appglue.ComposableService;
import com.appglue.R;

public class NotificationService extends ComposableService {
	
	public final static String NOTIFICATION_TITLE = "title";
	public final static String NOTIFICATION_TEXT = "text";
	public final static String NOTIFICATION_URL = "url";

	@Override
	public ArrayList<Bundle> performService(Bundle input, ArrayList<Bundle> parameters) 
	{
		
		String title = input.getString(NOTIFICATION_TITLE, "");
		String text = input.getString(NOTIFICATION_TEXT, "");
//		String url = input.getString(NOTIFICATION_URL, "");

		NotificationManager n = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Context context = getApplicationContext();

		Notification notification = new Notification.Builder(context)
				.setContentTitle(title).setContentText(text)
				.setSmallIcon(R.drawable.ic_launcher).getNotification();

		n.notify(this.hashCode(), notification);
		
		return null;
	}

	@Override
	public ArrayList<Bundle> performList(ArrayList<Bundle> os, ArrayList<Bundle> parameters) {
		NotificationManager n = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Context context = getApplicationContext();

		for (int i = 0; i < os.size(); i++) {
			Bundle b = os.get(i);
			
			String title = b.getString(NOTIFICATION_TITLE);
			String text = b.getString(NOTIFICATION_TEXT);
//			String url = b.getString(NOTIFICATION_URL, "");

			Notification notification = new Notification.Builder(context)
					.setContentTitle(title)
					.setContentText(text)
					.setSmallIcon(R.drawable.ic_launcher).getNotification();

			n.notify(this.hashCode() + i, notification);
		}

		return null;
	}

}
