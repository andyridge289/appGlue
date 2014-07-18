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
    public static final String NOTIFICATION_PRIORITY = "priority";

    public IOType textType = IOType.Factory.getType(IOType.Factory.TEXT);
    public IOType url = IOType.Factory.getType(IOType.Factory.URL);
    public IOType imageDrawable = IOType.Factory.getType(IOType.Factory.IMAGE_DRAWABLE);
    public IOType set = IOType.Factory.getType(IOType.Factory.SET);

    private void notify(String title, String text, int iconResource, int hashCode, int priority) {

        NotificationManager n = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                this)
                .setContentText(text)
                .setContentTitle(title)
                .setPriority(priority)
                .setVibrate(null)
                .setTicker(title + ": " + text);

        if(iconResource != -1) {
            notificationBuilder.setSmallIcon(iconResource);
        } else {
            notificationBuilder.setSmallIcon(R.drawable.icon);
        }

        Notification notification = notificationBuilder.build();
        n.notify(hashCode, notification);
    }

    @Override
    public ArrayList<Bundle> performService(Bundle input, ArrayList<Bundle> parameters) {
        String title = (String) textType.getFromBundle(input, NOTIFICATION_TITLE, "");
        String text = (String) textType.getFromBundle(input, NOTIFICATION_TEXT, "");
        int iconResource = (Integer) imageDrawable.getFromBundle(input, NOTIFICATION_IMAGE, -1);
        int priority = (Integer) set.getFromBundle(input, NOTIFICATION_PRIORITY, NotificationCompat.PRIORITY_DEFAULT);

        notify(title, text, iconResource, this.hashCode(), priority);

		return null;
	}

	@Override
	public ArrayList<Bundle> performList(ArrayList<Bundle> os, ArrayList<Bundle> parameters) {

		for (int i = 0; i < os.size(); i++) {
			Bundle b = os.get(i);
			
			String title = b.getString(NOTIFICATION_TITLE);
			String text = b.getString(NOTIFICATION_TEXT);
            int iconResource = (Integer) imageDrawable.getFromBundle(b, NOTIFICATION_IMAGE, -1);
            int priority = (Integer) set.getFromBundle(b, NOTIFICATION_PRIORITY, NotificationCompat.PRIORITY_DEFAULT);

            notify(title, text, iconResource, this.hashCode() + i, priority);
        }

		return null;
	}

}
