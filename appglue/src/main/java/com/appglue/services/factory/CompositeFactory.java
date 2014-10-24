package com.appglue.services.factory;

import android.content.Context;

import com.appglue.description.ServiceDescription;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.engine.description.ServiceIO;
import com.appglue.serviceregistry.Registry;
import com.appglue.services.NotificationService;
import com.appglue.services.TubeService;

import java.util.ArrayList;

public class CompositeFactory {

    // FIXME Create some sample composites and put the in the database on start up
    private static Registry registry;

    public static ArrayList<CompositeService> createSampleComposites(Context context) {

        registry = Registry.getInstance(context);
        ArrayList<CompositeService> composites = new ArrayList<CompositeService>();
        composites.add(TubeStatusNotification());

        return composites;
    }

    // TODO Need to make sure this only happens once

    private static CompositeService TubeStatusNotification() {

        ServiceDescription tubeService = registry.getServiceDescription(TubeService.class.getCanonicalName());
        ServiceDescription notificationService = registry.getServiceDescription(NotificationService.class.getCanonicalName());

        tubeService = registry.addServiceDescription(tubeService);
        notificationService = registry.addServiceDescription(notificationService);

        ComponentService tubeComponent = new ComponentService(tubeService, 0);
        ComponentService notificationComponent = new ComponentService(notificationService, 1);

        ArrayList<ComponentService> components = new ArrayList<ComponentService>();
        components.add(tubeComponent);
        components.add(notificationComponent);

        ServiceIO lineIO = tubeComponent.getOutput(TubeService.LINE_NAME);
        ServiceIO titleIO = notificationComponent.getInput(NotificationService.NOTIFICATION_TITLE);
        lineIO.setConnection(titleIO);
        titleIO.setConnection(lineIO);

        ServiceIO lineIconIO = tubeComponent.getOutput(TubeService.LINE_ICON);
        ServiceIO imageIO = notificationComponent.getInput(NotificationService.NOTIFICATION_IMAGE);

        lineIconIO.setConnection(imageIO);
        imageIO.setConnection(lineIconIO);

        ServiceIO statusIO = tubeComponent.getOutput(TubeService.LINE_STATUS);
        ServiceIO textIO = notificationComponent.getInput(NotificationService.NOTIFICATION_TEXT);

        statusIO.setConnection(textIO);
        textIO.setConnection(statusIO);

        return new CompositeService("Tube status Notification", "Looks up the current state of the tube and displays it as a notification", components);
    }
}
