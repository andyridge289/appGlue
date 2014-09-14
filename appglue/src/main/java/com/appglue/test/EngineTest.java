package com.appglue.test;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.test.AndroidTestCase;
import android.test.ServiceTestCase;
import android.test.mock.MockApplication;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import android.util.SparseArray;

import com.appglue.AppGlue;
import com.appglue.ComposableService;
import com.appglue.engine.Orchestration;
import com.appglue.engine.OrchestrationService;
import com.appglue.engine.OrchestrationServiceConnection;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.library.AppGlueLibrary;
import com.appglue.serviceregistry.Registry;
import com.appglue.services.NotificationService;
import com.appglue.services.TubeService;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static com.appglue.Constants.COMPOSITE_ID;
import static com.appglue.Constants.TAG;
import static com.appglue.Constants.DURATION;
import static com.appglue.Constants.INDEX;
import static com.appglue.Constants.IS_LIST;
import static com.appglue.Constants.TEST;

public class EngineTest extends ServiceTestCase<OrchestrationService> {

    private Intent startIntent;

    public static boolean executeFinished = false;
    public static boolean timerFinished = false;

    public EngineTest() {
        super(OrchestrationService.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        startIntent = new Intent();
        startIntent.setClass(getContext(), OrchestrationService.class);

    }

    String out = "{\"update\":\"Kept old\",\"lines\":[{\"name\":\"Bakerloo\",\"id\":\"bakerloo\",\"status\":\"minor delays\",\"messages\":[]},{\"name\":\"Central\",\"id\":\"central\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"Circle\",\"id\":\"circle\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"District\",\"id\":\"district\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"DLR\",\"id\":\"docklands\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"H'smith & City\",\"id\":\"hammersmithcity\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"Jubilee\",\"id\":\"jubilee\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"Metropolitan\",\"id\":\"metropolitan\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"Northern\",\"id\":\"northern\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"Overground\",\"id\":\"overground\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"Piccadilly\",\"id\":\"piccadilly\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"Victoria\",\"id\":\"victoria\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"Waterloo & City\",\"id\":\"waterloocity\",\"status\":\"good service\",\"messages\":[]}]}";

    @LargeTest
    public void testMapOutputs() throws Exception {

        Registry registry = Registry.getInstance(getContext());
        CompositeService fred = TestLib.createAComposite(registry, getContext());
        registry.addComposite(fred);

        OrchestrationServiceConnection osc = new OrchestrationServiceConnection(getContext(), fred, false);
        Method mapMethod = OrchestrationServiceConnection.class.getDeclaredMethod("mapOutputs", Bundle.class, ComponentService.class);
        mapMethod.setAccessible(true);

        TubeService ts = new TubeService();
        Method invokeMethod = TubeService.class.getDeclaredMethod("processOutput", String.class);
        invokeMethod.setAccessible(true);
        ArrayList<Bundle> tubeData = (ArrayList<Bundle>) invokeMethod.invoke(ts, out);

        for(Bundle b : tubeData)
            Log.d(TAG, AppGlueLibrary.bundleToString(b));

        Bundle data = new Bundle();
        data.putParcelableArrayList(ComposableService.INPUT, tubeData);

        SparseArray<ComponentService> components = fred.getComponents();
        ComponentService current = null;
        for(int i = 0; i < components.size(); i++) {
            ComponentService component = components.valueAt(i);
            if(component.getDescription().getClassName().equals("com.appglue.services.NotificationService")) {
                current = component;
            }
        }

        Bundle answers = new Bundle();
        answers.putString(NotificationService.NOTIFICATION_TITLE, "Bakerloo");
        answers.putString(NotificationService.NOTIFICATION_TEXT, "Test message");
        answers.putInt(NotificationService.NOTIFICATION_IMAGE, tubeData.get(0).getInt(TubeService.LINE_ICON));

        ArrayList<Bundle> mappedList = ((Bundle) mapMethod.invoke(osc, data, current)).getParcelableArrayList(ComposableService.INPUT);
        assertEquals(mappedList.size(), tubeData.size());

        Log.d(TAG, AppGlueLibrary.bundleToString(answers));
        for(Bundle mapped : mappedList)
            Log.d(TAG, AppGlueLibrary.bundleToString(mapped));

        int firstFail = -1;

        for(int i = 0 ; i < mappedList.size(); i++) {
            if(!AppGlueLibrary.bundlesEqual(mappedList.get(i), answers)) {
                firstFail = i;
                break;
            }
        }

        assertEquals(-1, firstFail);
    }

//    @LargeTest
//    public void testExecution() throws Exception {
//
//        // Run it and assert that a new entry has been added to the Log to account for it running properly
//
//        Intent serviceIntent = new Intent(getContext(), OrchestrationService.class);
//        ArrayList<Bundle> intentData = new ArrayList<Bundle>();
//        Bundle b = new Bundle();
//
//        b.putLong(COMPOSITE_ID, fred.getID());
//        b.putInt(INDEX, 0);
//        b.putBoolean(IS_LIST, false);
//        b.putInt(DURATION, 0);
//        b.putBoolean(TEST, false);
//
//        intentData.add(b);
//        serviceIntent.putParcelableArrayListExtra(DATA, intentData);
//        startService(serviceIntent);
//
//        int count = 0;
//        while(!executeFinished) {
//            if(count > 5) {
//                assertEquals(1, 2);
//                return;
//            }
//
//            Thread.sleep(1000);
//            count++;
//        }
//
//        When it gets here, do the database lookup to see if the composite was successfully executed in the last 5ish seconds
//    }
//
//    public void testTimerExecution() throws Exception {
//        assertEquals(1,2);
//    }
}
