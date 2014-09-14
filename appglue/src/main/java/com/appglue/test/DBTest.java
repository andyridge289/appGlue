package com.appglue.test;

import android.content.Intent;
import android.os.Bundle;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.LargeTest;

import com.appglue.description.ServiceDescription;
import com.appglue.engine.OrchestrationService;
import com.appglue.engine.description.CompositeService;
import com.appglue.serviceregistry.Registry;

import java.util.ArrayList;

import static com.appglue.Constants.COMPOSITE_ID;
import static com.appglue.Constants.DATA;
import static com.appglue.Constants.DURATION;
import static com.appglue.Constants.INDEX;
import static com.appglue.Constants.IS_LIST;
import static com.appglue.Constants.TEST;

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


}
