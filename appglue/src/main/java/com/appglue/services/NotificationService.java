package com.appglue.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.appglue.ComposableService;
import com.appglue.R;

import java.util.ArrayList;

public class NotificationService extends ComposableService {
	
	public final static String NOTIFICATION_TITLE = "title";
	public final static String NOTIFICATION_TEXT = "text";
	public final static String NOTIFICATION_URL = "url";

	@Override
	public ArrayList<Bundle> performService(Bundle input, ArrayList<Bundle> parameters) 
	{
		
		String title = input.getString(NOTIFICATION_TITLE, "");
		String text = input.getString(NOTIFICATION_TEXT, "");

        NotificationManager n = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                this)
                .setContentText(text)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.icon) // TODO Include an image for the icon, maybe large icon too
                .setPriority(NotificationCompat.PRIORITY_MIN) // TODO Priority needs to be added to the notification service
                .setVibrate(null)
                .setTicker(title + ": " + text);

        Notification notification = notificationBuilder.build();
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

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                    this)
                    .setContentText(text)
                    .setContentTitle(title)
                    .setSmallIcon(R.drawable.icon) // TODO Include an image for the icon, maybe large icon too
                    .setPriority(NotificationCompat.PRIORITY_MIN) // TODO Priority needs to be added to the notification service
                    .setVibrate(null)
                    .setTicker(title + ": " + text);

            Notification notification = notificationBuilder.build();
            n.notify(this.hashCode(), notification);

			n.notify(this.hashCode() + i, notification);
		}

		return null;
	}

}
