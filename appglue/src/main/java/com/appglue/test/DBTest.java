package com.appglue.test;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.MediumTest;

import com.appglue.description.ServiceDescription;
import com.appglue.engine.description.CompositeService;
import com.appglue.serviceregistry.Registry;


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

        // See if it's there

        // Add some stuff to it, see what happens

        // Reset it, see what happens

        // Save it to another composite, see what happens

        assertEquals(1, 2);
    }


}
