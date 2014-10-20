package com.appglue.services.factory;

import android.content.Context;

import com.appglue.IODescription;
import com.appglue.description.SampleValue;
import com.appglue.description.ServiceDescription;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.engine.description.IOValue;
import com.appglue.engine.description.ServiceIO;
import com.appglue.library.FilterFactory;
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

        IODescription lineName = tubeComponent.getDescription().getOutput(TubeService.LINE_NAME);

        SampleValue bakerlooSample = null;
        ArrayList<SampleValue> samples = lineName.getSampleValues();
        for (SampleValue sample : samples) {
            if (sample.getName().equals(TubeService.BAKERLOO)) {
                bakerlooSample = sample;
            }
        }

        ServiceIO lineIO = tubeComponent.getOutput(TubeService.LINE_NAME);
        IOValue value = new IOValue(FilterFactory.STR_EQUALS, bakerlooSample, lineIO);

        ServiceIO titleIO = notificationComponent.getInput(NotificationService.NOTIFICATION_TITLE);
        lineIO.setConnection(titleIO);
        titleIO.setConnection(lineIO);

        ServiceIO lineIconIO = tubeComponent.getOutput(TubeService.LINE_ICON);
        ServiceIO imageIO = notificationComponent.getInput(NotificationService.NOTIFICATION_IMAGE);

        lineIconIO.setConnection(imageIO);
        imageIO.setConnection(lineIconIO);
//
//        ServiceIO textIO = notificationComponent.getInput(NotificationService.NOTIFICATION_TEXT);
//        IOValue ioValue = new IOValue(FilterFactory.NONE, lineIO.getDescription().getType().fromString("Test message"), textIO);
//        textIO.setValue(ioValue);
//
//        IOFilter filter = new IOFilter(tubeComponent);
//        filter.addValue(lineIO, value);
//        tubeComponent.addFilter(filter);

        return new CompositeService("Tube status Notification", "Looks up the current state of the tube and displays it as a notification", components);
    }
}
