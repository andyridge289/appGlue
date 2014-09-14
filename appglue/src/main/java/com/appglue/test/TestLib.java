package com.appglue.test;

import android.content.Context;
import android.test.AndroidTestCase;

import com.appglue.IODescription;
import com.appglue.description.AppDescription;
import com.appglue.description.IOValue;
import com.appglue.description.ServiceDescription;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.engine.description.ServiceIO;
import com.appglue.library.IOFilter;
import com.appglue.serviceregistry.Registry;
import com.appglue.services.NotificationService;
import com.appglue.services.ServiceFactory;
import com.appglue.services.TubeService;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static com.appglue.Constants.JSON_SERVICE;
import static com.appglue.Constants.JSON_SERVICE_DATA;

public class TestLib {

    public static ServiceDescription getService(Registry registry, Context context, String name) throws Exception {

        ServiceFactory sf = ServiceFactory.getInstance(registry, context);

        Method helloMethod = sf.getClass().getDeclaredMethod(name);
        helloMethod.setAccessible(true);
        String helloServiceString = (String) helloMethod.invoke(sf);

        JSONObject service = new JSONObject(helloServiceString).getJSONObject(JSON_SERVICE).getJSONObject(JSON_SERVICE_DATA);
        AppDescription app = sf.getAppDescription();

        return ServiceDescription.parseFromNewJSON(service, app);
    }

    public static CompositeService createAComposite(Registry registry, Context context) throws Exception {

        ServiceDescription tubeService = getService(registry, context, "setupTubeService");
        ServiceDescription notificationService = getService(registry, context, "setupNotificationService");

        tubeService = registry.addServiceDescription(tubeService);
        notificationService = registry.addServiceDescription(notificationService);

        ComponentService tubeComponent = new ComponentService(tubeService, 0);
        ComponentService notificationComponent = new ComponentService(notificationService, 1);

        ArrayList<ComponentService> components = new ArrayList<ComponentService>();
        components.add(tubeComponent);
        components.add(notificationComponent);

        IODescription lineName = tubeComponent.getDescription().getOutput(TubeService.LINE_NAME);
        IODescription lineIcon = tubeComponent.getDescription().getOutput(TubeService.LINE_ICON);

        IODescription notificationTitle = notificationComponent.getDescription().getInput(NotificationService.NOTIFICATION_TITLE);
        IODescription notificationText = notificationComponent.getDescription().getInput(NotificationService.NOTIFICATION_TEXT);
        IODescription notificationIcon = notificationComponent.getDescription().getInput(NotificationService.NOTIFICATION_IMAGE);

        ServiceIO lineIO = new ServiceIO(tubeComponent, lineName);
        ServiceIO lineIconIO = new ServiceIO(tubeComponent, lineIcon);

        ServiceIO titleIO = new ServiceIO(notificationComponent, notificationTitle);
        ServiceIO textIO = new ServiceIO(notificationComponent, notificationText);
        ServiceIO imageIO = new ServiceIO(notificationComponent, notificationIcon);

        IOValue bakerlooSample = null;
        ArrayList<IOValue> samples = lineName.getSampleValues();
        for(IOValue sample : samples) {
            if (sample.getID() == 3) {
                bakerlooSample = sample;
            }
        }

        lineIO.setChosenSampleValue(bakerlooSample);
        lineIO.setFilterState(ServiceIO.SAMPLE_FILTER);
        lineIO.setCondition(IOFilter.STR_EQUALS.index);

        lineIO.setConnection(titleIO);
        titleIO.setConnection(lineIO);

        lineIconIO.setConnection(imageIO);
        imageIO.setConnection(lineIconIO);

        textIO.setManualValue(lineIO.getDescription().getType().fromString("Test message"));

        CompositeService fred = new CompositeService("Fred", "This is called fred", components);

        return fred;
    }
}