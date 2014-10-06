package com.appglue.test;

import android.content.Context;
import android.util.Log;

import com.appglue.IODescription;
import com.appglue.description.AppDescription;
import com.appglue.description.SampleValue;
import com.appglue.description.ServiceDescription;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.engine.description.IOValue;
import com.appglue.engine.description.ServiceIO;
import com.appglue.library.FilterFactory;
import com.appglue.serviceregistry.Registry;
import com.appglue.services.NotificationService;
import com.appglue.services.ServiceFactory;
import com.appglue.services.TubeService;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static com.appglue.Constants.JSON_SERVICE;
import static com.appglue.Constants.JSON_SERVICE_DATA;
import static com.appglue.Constants.TAG;

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

    public static CompositeService createAComposite(Registry registry, Context context, String name) throws Exception {

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

        SampleValue bakerlooSample = null;
        ArrayList<SampleValue> samples = lineName.getSampleValues();
        for (SampleValue sample : samples) {
            if (sample.getName().equals(TubeService.BAKERLOO)) {
                bakerlooSample = sample;
            }
        }

        ServiceIO lineIO = tubeComponent.getOutput(TubeService.LINE_NAME);
        IOValue value = new IOValue(FilterFactory.STR_EQUALS, bakerlooSample, lineIO);
        lineIO.addFilter(value);

        ServiceIO titleIO = notificationComponent.getInput(NotificationService.NOTIFICATION_TITLE);
        lineIO.setConnection(titleIO);
        titleIO.setConnection(lineIO);

        ServiceIO lineIconIO = tubeComponent.getOutput(TubeService.LINE_ICON);
        ServiceIO imageIO = notificationComponent.getInput(NotificationService.NOTIFICATION_IMAGE);

        lineIconIO.setConnection(imageIO);
        imageIO.setConnection(lineIconIO);

        ServiceIO textIO = notificationComponent.getInput(NotificationService.NOTIFICATION_TEXT);
        IOValue ioValue = new IOValue(FilterFactory.NONE, lineIO.getDescription().getType().fromString("Test message"), textIO);
        textIO.setValue(ioValue);

        return new CompositeService(name, "This is called " + name, components);
    }

    public static ComponentService createComponentForFilterSample(ServiceDescription sd, IODescription[] filterOns,
                                                                  String[] sampleValues, FilterFactory.FilterValue[] filterConditions) {

        ComponentService component = new ComponentService(sd, -1);

        for (int i = 0; i < filterOns.length; i++) {

            IODescription filterOn = filterOns[i];
            String sampleValue = sampleValues[i];
            FilterFactory.FilterValue filterCondition = filterConditions[i];

            SampleValue chosenSample = null;
            ArrayList<SampleValue> samples = filterOn.getSampleValues();
            for (SampleValue sample : samples) {
                if (sample.getName().equals(sampleValue)) {
                    chosenSample = sample;
                }
            }
            if (chosenSample == null) {
                Log.d(TAG, "chosen sample not found");
            }

            ServiceIO filterOnIO = component.getIO(filterOn.getName());

            IOValue value = new IOValue(filterCondition, chosenSample, filterOnIO);
            filterOnIO.setValue(value);
        }

        return component;
    }
}
