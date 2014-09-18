package com.appglue.test;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.MediumTest;

import com.appglue.description.ServiceDescription;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.serviceregistry.Registry;
import com.appglue.services.NotificationService;
import com.appglue.services.TubeService;

public class DBTest extends AndroidTestCase {

    @LargeTest
    public void testServiceDescriptions() throws Exception {

        Registry registry = Registry.getInstance(getContext());
        ServiceDescription helloService = TestLib.getService(registry, getContext(), "setupTubeService");

        helloService = registry.addServiceDescription(helloService);
        ServiceDescription helloService2 = registry.getServiceDescription(helloService.getClassName());

        assertEquals(helloService, helloService2);
    }

    @LargeTest
    public void testAComposite() throws Exception {

        Registry registry = Registry.getInstance(getContext());
        CompositeService fred = TestLib.createAComposite(registry, getContext());
        registry.addComposite(fred);

        CompositeService fred2 = registry.getComposite(fred.getID());
        assertEquals(fred, fred2);
    }

    @MediumTest
    public void testTemp() throws Exception {

        // Create the temp
        Registry registry = Registry.getInstance(getContext());
        CompositeService origTemp = registry.resetTemp();

        // See if it's there
        CompositeService testTemp = new CompositeService();
        testTemp.setID(CompositeService.TEMP_ID);
        testTemp.setName(CompositeService.TEMP_NAME);
        testTemp.setDescription(CompositeService.TEMP_DESCRIPTION);

        assertEquals(origTemp, testTemp);

        ServiceDescription tubeService = registry.getServiceDescription(TubeService.class.getCanonicalName());
        if(tubeService == null) {
            tubeService = TestLib.getService(registry, getContext(), "setupTubeService");
            tubeService = registry.addServiceDescription(tubeService);
        }

        ServiceDescription notificationService = registry.getServiceDescription(NotificationService.class.getCanonicalName());
        if(notificationService == null) {
            notificationService = TestLib.getService(registry, getContext(), "setupNotificationService");
            notificationService = registry.addServiceDescription(notificationService);
        }

        // Add some stuff to it, see what happens
        ComponentService helloComponent = new ComponentService(origTemp, tubeService, 0);
        ComponentService notificationComponent = new ComponentService(origTemp, notificationService, 1);

        registry.addComponent(helloComponent);
        registry.addComponent(notificationComponent);

        origTemp.addComponent(helloComponent, helloComponent.getPosition());
        origTemp.addComponent(notificationComponent, helloComponent.getPosition());

        assertEquals(2, origTemp.size());

        registry.updateComposite(origTemp);
        CompositeService newTemp = registry.getComposite(CompositeService.TEMP_ID);

        assertEquals(2, origTemp.size());
        assertEquals(2, newTemp.size());

        assertEquals(origTemp, newTemp);

        // Save it to another composite, see what happens
        CompositeService fred = registry.saveTempAsComposite("fred");
        origTemp.setID(fred.getID());
        origTemp.setName(fred.getName());
        origTemp.setDescription(fred.getDescription());

        assertEquals(origTemp, fred);

        origTemp = registry.getComposite(CompositeService.TEMP_ID);
        assertEquals(origTemp, testTemp);


        // Reset it, see what happens

//        assertEquals(1, 2);
    }


}
