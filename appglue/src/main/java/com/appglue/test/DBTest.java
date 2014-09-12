package com.appglue.test;

import android.test.AndroidTestCase;
import android.util.Log;

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

import static com.appglue.Constants.JSON_SERVICE;
import static com.appglue.Constants.JSON_SERVICE_DATA;
import static com.appglue.Constants.TAG;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class DBTest extends AndroidTestCase {

    public void testServiceDescriptions() throws Exception {

        Registry registry = Registry.getInstance(getContext());
        ServiceDescription helloService = getService(registry, "setupTubeService");

        helloService = registry.addServiceDescription(helloService);
        ServiceDescription helloService2 = registry.getServiceDescription(helloService.getClassName());

        assertEquals(helloService, helloService2);
    }

    private ServiceDescription getService(Registry registry, String name)throws Exception {

        ServiceFactory sf = ServiceFactory.getInstance(registry, getContext());

        Method helloMethod = sf.getClass().getDeclaredMethod(name);
        helloMethod.setAccessible(true);
        String helloServiceString = (String) helloMethod.invoke(sf);

        JSONObject service = new JSONObject(helloServiceString).getJSONObject(JSON_SERVICE).getJSONObject(JSON_SERVICE_DATA);
        AppDescription app = sf.getAppDescription();

        return ServiceDescription.parseFromNewJSON(service, app);
    }

    public void testAComposite() throws Exception {

        Registry registry = Registry.getInstance(getContext());
        ServiceDescription tubeService = getService(registry, "setupTubeService");
        ServiceDescription notificationService = getService(registry, "setupNotificationService");

        tubeService = registry.addServiceDescription(tubeService);
        notificationService = registry.addServiceDescription(notificationService);

        ComponentService tubeComponent = new ComponentService(tubeService);
        ComponentService notificationComponent = new ComponentService(notificationService);

        ArrayList<ComponentService> components = new ArrayList<ComponentService>();
        components.add(tubeComponent);
        components.add(notificationComponent);

        IODescription lineName = tubeComponent.getDescription().getOutput(TubeService.LINE_NAME);
        IODescription notificationTitle = notificationComponent.getDescription().getInput(NotificationService.NOTIFICATION_TITLE);
        IODescription notificationText = notificationComponent.getDescription().getInput(NotificationService.NOTIFICATION_TEXT);

        ServiceIO lineIO = new ServiceIO(tubeComponent, lineName);
        ServiceIO titleIO = new ServiceIO(notificationComponent, notificationTitle);
        ServiceIO textIO = new ServiceIO(notificationComponent, notificationText);

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

        textIO.setManualValue(lineIO.description().type().fromString("Test message"));

        CompositeService fred = new CompositeService("Fred", "This is called fred", components);

        registry.saveComposite(fred);

        CompositeService fred2 = registry.getComposite(fred.getId());
        assertEquals(fred, fred2);
    }
}
