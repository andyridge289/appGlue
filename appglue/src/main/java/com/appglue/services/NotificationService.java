package com.appglue.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.appglue.ComposableService;
import com.appglue.R;
import com.appglue.datatypes.IOType;

import java.util.ArrayList;

public class NotificationService extends ComposableService {
	
	public final static String NOTIFICATION_TITLE = "title";
	public final static String NOTIFICATION_TEXT = "text";
	public final static String NOTIFICATION_URL = "url";
    public static final String NOTIFICATION_IMAGE = "image";

    public IOType textType = IOType.Factory.getType(IOType.Factory.TEXT);
    public IOType url = IOType.Factory.getType(IOType.Factory.URL);
    public IOType imageDrawable = IOType.Factory.getType(IOType.Factory.IMAGE_DRAWABLE);

	@Override
	public ArrayList<Bundle> performService(Bundle input, ArrayList<Bundle> parameters) 
	{
		String title = (String) textType.getFromBundle(input, NOTIFICATION_TITLE, "");
        String text = (String) textType.getFromBundle(input, NOTIFICATION_TEXT, "");
        Integer iconResource = (Integer) imageDrawable.getFromBundle(input, NOTIFICATION_IMAGE, -1);

        NotificationManager n = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentText(text)
                .setContentTitle(title)
                .setVibrate(null)
                .setTicker(title + ": " + text);

        if(iconResource != -1) {
            notificationBuilder.setSmallIcon(iconResource);
        } else {
            notificationBuilder.setSmallIcon(R.drawable.icon);
        }

//        .setLargeIcon(icon)
//            .setSmallIcon(R.drawable.icon) // XXX Include an image for the icon, maybe large icon too
//            .setPriority(NotificationCompat.PRIORITY_MIN) // XXX Priority needs to be added to the notification service

        Notification notification = notificationBuilder.build();
        n.notify(this.hashCode(), notification);
		
		return null;
	}

	@Override
	public ArrayList<Bundle> performList(ArrayList<Bundle> os, ArrayList<Bundle> parameters) {
		NotificationManager n = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

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
