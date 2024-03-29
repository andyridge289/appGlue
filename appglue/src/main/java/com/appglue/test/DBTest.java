package com.appglue.test;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

import com.appglue.IODescription;
import com.appglue.description.SampleValue;
import com.appglue.description.ServiceDescription;
import com.appglue.engine.Schedule;
import com.appglue.engine.model.ComponentService;
import com.appglue.engine.model.CompositeService;
import com.appglue.engine.model.IOFilter;
import com.appglue.engine.model.IOValue;
import com.appglue.engine.model.ServiceIO;
import com.appglue.library.FilterFactory;
import com.appglue.serviceregistry.Registry;
import com.appglue.services.NotificationService;
import com.appglue.services.TubeService;

import java.util.ArrayList;

public class DBTest extends AndroidTestCase {

    @MediumTest
    public void testCompositeComponentRemove() throws Exception {

        Registry registry = Registry.getInstance(getContext());
        CompositeService fred = TestLib.createAComposite(registry, getContext(), "Fred");
        registry.addComposite(fred);

        CompositeService fred2 = registry.getComposite(fred.getID());

        ServiceDescription notificationService =  TestLib.getService(registry, getContext(), "setupNotificationService");
        ComponentService notificationComponent = new ComponentService(notificationService, 2);

        fred.addComponent(notificationComponent, 2);



//        if (fred.equals(fred2)) {
//            assertEquals(1, 1);
//        } else {
//            assertEquals(1, 2);
//        }
    }

    @MediumTest
    public void testCompositeComponentSwap() throws Exception {

    }

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
        CompositeService fred = TestLib.createAComposite(registry, getContext(), "Fred");
        registry.addComposite(fred);

        CompositeService fred2 = registry.getComposite(fred.getID());
        assertEquals (fred, fred2);
    }

    @SmallTest
    public void testSchedule() throws Exception {

        Registry registry = Registry.getInstance(getContext());
        CompositeService fred = TestLib.createAComposite(registry, getContext(), "Fred");
        registry.addComposite(fred);

        Schedule s = new Schedule(-1, fred, true,
                Schedule.ScheduleType.INTERVAL.index, 1, Schedule.Interval.HOUR.index, -1,
                Schedule.TimePeriod.DAY.index, 0, 0, 7, 0, 0L, false, 0);

        registry.add(s);
        Schedule t = registry.getSchedule(s.getID());

        assertEquals(s, t);

        // Change the details of S
        s.setScheduleType(Schedule.ScheduleType.TIME);
        s.setNumeral(2);
        s.setInterval(Schedule.Interval.DAY);
        s.setEnabled(false);

        // Update s
        registry.update(s);

        // Get S back out again
        t = registry.getSchedule(s.getID());
        assertEquals(s, t);

        // "Execute" S
        s.setLastExecuteTime(System.currentTimeMillis());
        registry.executeSchedule(s);
        t = registry.getSchedule(s.getID());
        assertEquals(s, t);

        // Delete S from the database
        registry.delete(s);
        t = registry.getSchedule(s.getID());
        assertEquals(null, t);
    }

    public void testUpdate() throws Exception {
        Registry registry = Registry.getInstance(getContext());
        CompositeService fred = TestLib.createAComposite(registry, getContext(), "Fred");
        registry.addComposite(fred);

        CompositeService fred2 = registry.getComposite(fred.getID());
        assertEquals(fred, fred2);

        // Change fred
        fred.setName("Fred updated");
        fred.setDescription("A description");

        // Add another component to fred
        ServiceDescription notificationService2 = TestLib.getService(registry, getContext(), "setupNotificationService");
        registry.setupIDs(notificationService2);

        ComponentService notificationComponent2 = new ComponentService(notificationService2, 2);
        fred.addComponent(notificationComponent2, 2);

        // Put a filter in the first component
        ComponentService tubeComponent = fred.getComponent(0);
        ComponentService notificationComponent = fred.getComponent(1);

        ArrayList<IOFilter> filters = tubeComponent.getFilters();
        IOFilter filter = filters.get(0);

        IODescription lineName = tubeComponent.getDescription().getOutput(TubeService.LINE_NAME);
        SampleValue victoriaSample = null;
        ArrayList<SampleValue> samples = lineName.getSampleValues();
        for (SampleValue sample : samples) {
            if (sample.getName().equals(TubeService.VICTORIA)) {
                victoriaSample = sample;
            }
        }

        ServiceIO lineIO = tubeComponent.getOutput(TubeService.LINE_NAME);
        ArrayList<IOValue> values = filter.getValues(lineIO);
        IOValue value = values.get(0);
        value.setSampleValue(victoriaSample); // Victoria minor delays

        // Change the link between the tube line and the text, then the status and the title
        ServiceIO statusIO = tubeComponent.getOutput(TubeService.LINE_STATUS);
        ServiceIO titleIO = notificationComponent.getInput(NotificationService.NOTIFICATION_TITLE);
        ServiceIO textIO = notificationComponent.getInput(NotificationService.NOTIFICATION_TEXT);

        lineIO.setConnection(textIO);
        textIO.setConnection(lineIO);
        statusIO.setConnection(titleIO);
        titleIO.setConnection(statusIO);

        // Set some manual values for the third component
        ServiceIO titleIO2 = notificationComponent2.getInput(NotificationService.NOTIFICATION_TITLE);
        ServiceIO textIO2 = notificationComponent2.getInput(NotificationService.NOTIFICATION_TEXT);

        IOValue titleValue = new IOValue(FilterFactory.STR_EQUALS, "A title", titleIO2);
        titleIO2.setValue(titleValue);
        IOValue textValue = new IOValue(FilterFactory.STR_EQUALS, "A message", textIO2);
        textIO2.setValue(textValue);

        registry.updateComposite(fred);

        fred2 = registry.getComposite(fred.getID());
        assertEquals(fred, fred2);

        // Remove the filter
        tubeComponent.removeFilter(filter);

        // Remove the last component we added
        fred.remove(tubeComponent);

        // Update
        registry.updateComposite(fred);

        // Check
        fred2 = registry.getComposite(fred.getID());
        assertEquals(fred, fred2);

        // Check what happens when you delete it
        registry.delete(fred);
        fred2 = registry.getComposite(fred.getID());
        assertEquals(null, fred2);
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
        if (tubeService == null) {
            tubeService = TestLib.getService(registry, getContext(), "setupTubeService");
            tubeService = registry.addServiceDescription(tubeService);
        }

        ServiceDescription notificationService = registry.getServiceDescription(NotificationService.class.getCanonicalName());
        if (notificationService == null) {
            notificationService = TestLib.getService(registry, getContext(), "setupNotificationService");
            notificationService = registry.addServiceDescription(notificationService);
        }

        // Add some stuff to it, see what happens
        ComponentService helloComponent = new ComponentService(origTemp, tubeService, 0);
        ComponentService notificationComponent = new ComponentService(origTemp, notificationService, 1);

        registry.addComponent(helloComponent);
        registry.addComponent(notificationComponent);

        origTemp.addComponent(helloComponent, helloComponent.getPosition());
        origTemp.addComponent(notificationComponent, notificationComponent.getPosition());

        assertEquals(2, origTemp.size());

        registry.updateComposite(origTemp);
        CompositeService newTemp = registry.getComposite(CompositeService.TEMP_ID);

        assertEquals(2, origTemp.size());
        assertEquals(2, newTemp.size());

        assertEquals(origTemp, newTemp);

        // Save it to another composite, see what happens
        CompositeService fred = registry.saveTempAsComposite("fred", true);
        origTemp.setID(fred.getID());
        origTemp.setName(fred.getName());
        origTemp.setDescription(fred.getDescription());

        assertEquals(origTemp, fred);

        origTemp = registry.getComposite(CompositeService.TEMP_ID);
        assertEquals(origTemp, testTemp);

        // Can't delete the temp, that would be bad.
    }
}
