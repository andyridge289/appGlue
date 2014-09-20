package com.appglue.test;

import android.content.Intent;
import android.os.Bundle;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import com.appglue.ComposableService;
import com.appglue.IODescription;
import com.appglue.description.ServiceDescription;
import com.appglue.engine.OrchestrationService;
import com.appglue.engine.OrchestrationServiceConnection;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.library.AppGlueLibrary;
import com.appglue.library.IOFilter;
import com.appglue.serviceregistry.Registry;
import com.appglue.services.NotificationService;
import com.appglue.services.TubeService;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static com.appglue.Constants.TAG;

public class EngineTest extends ServiceTestCase<OrchestrationService> {

    public static boolean executeFinished = false;
    public static boolean timerFinished = false;

    public EngineTest() {
        super(OrchestrationService.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), OrchestrationService.class);

    }

    String out = "{\"update\":\"Kept old\",\"lines\":[{\"name\":\"Bakerloo\",\"id\":\"bakerloo\",\"status\":\"minor delays\",\"messages\":[]},{\"name\":\"Central\",\"id\":\"central\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"Circle\",\"id\":\"circle\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"District\",\"id\":\"district\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"DLR\",\"id\":\"docklands\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"H'smith & City\",\"id\":\"hammersmithcity\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"Jubilee\",\"id\":\"jubilee\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"Metropolitan\",\"id\":\"metropolitan\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"Northern\",\"id\":\"northern\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"Overground\",\"id\":\"overground\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"Piccadilly\",\"id\":\"piccadilly\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"Victoria\",\"id\":\"victoria\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"Waterloo & City\",\"id\":\"waterloocity\",\"status\":\"good service\",\"messages\":[]}]}";
    String out2 = "{\"update\":\"Kept old\",\"lines\":[{\"name\":\"Bakerloo\",\"id\":\"bakerloo\",\"status\":\"minor delays\",\"messages\":[]},{\"name\":\"Central\",\"id\":\"central\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"Circle\",\"id\":\"circle\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"District\",\"id\":\"district\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"DLR\",\"id\":\"docklands\",\"status\":\"severe delays\",\"messages\":[]},{\"name\":\"H'smith & City\",\"id\":\"hammersmithcity\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"Jubilee\",\"id\":\"jubilee\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"Metropolitan\",\"id\":\"metropolitan\",\"status\":\"minor delays\",\"messages\":[]},{\"name\":\"Northern\",\"id\":\"northern\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"Overground\",\"id\":\"overground\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"Piccadilly\",\"id\":\"piccadilly\",\"status\":\"part closure\",\"messages\":[]},{\"name\":\"Victoria\",\"id\":\"victoria\",\"status\":\"good service\",\"messages\":[]},{\"name\":\"Waterloo & City\",\"id\":\"waterloocity\",\"status\":\"good service\",\"messages\":[]}]}";

    @LargeTest

    public void testMapOutputs() throws Exception {

        Registry registry = Registry.getInstance(getContext());
        CompositeService fred = TestLib.createAComposite(registry, getContext(), "Fred");
        registry.addComposite(fred);

        OrchestrationServiceConnection osc = new OrchestrationServiceConnection(getContext(), fred, false);
        Method mapMethod = OrchestrationServiceConnection.class.getDeclaredMethod("mapOutputs", Bundle.class, ComponentService.class);
        mapMethod.setAccessible(true);

        TubeService ts = new TubeService();
        Method invokeMethod = TubeService.class.getDeclaredMethod("processOutput", String.class);
        invokeMethod.setAccessible(true);
        ArrayList<Bundle> tubeData = (ArrayList<Bundle>) invokeMethod.invoke(ts, out);

//        for(Bundle b : tubeData)
//            Log.d(TAG, AppGlueLibrary.bundleToString(b));

        Bundle data = new Bundle();
        data.putParcelableArrayList(ComposableService.INPUT, tubeData);

        ArrayList<ComponentService> components = fred.getComponents("com.appglue.services.NotificationService");
        ComponentService current = components.get(0);

        Bundle answers = new Bundle();
        answers.putString(NotificationService.NOTIFICATION_TITLE, TubeService.BAKERLOO);
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
//    public void testManualFilter() throws Exception {
//        assertEquals(1, 2);
//    }

    @LargeTest
    public void testSampleFilter() throws Exception {

        // Run it and assert that a new entry has been added to the Log to account for it running properly
        Registry registry = Registry.getInstance(getContext());
        CompositeService fred = TestLib.createAComposite(registry, getContext(), "Fred");
        registry.addComposite(fred);

        OrchestrationServiceConnection osc = new OrchestrationServiceConnection(getContext(), fred, false);
        Method filterMethod = OrchestrationServiceConnection.class.getDeclaredMethod("filter", ArrayList.class, ComponentService.class);
        filterMethod.setAccessible(true);

        TubeService ts = new TubeService();
        Method invokeMethod = TubeService.class.getDeclaredMethod("processOutput", String.class);
        invokeMethod.setAccessible(true);
        ArrayList<Bundle> tubeData = (ArrayList<Bundle>) invokeMethod.invoke(ts, out2);

        ArrayList<ComponentService> components = fred.getComponents("com.appglue.services.TubeService");
        ServiceDescription sd = components.get(0).getDescription();

        IODescription lineName = sd.getOutput(TubeService.LINE_NAME);
        IODescription lineStatus = sd.getOutput(TubeService.LINE_STATUS);

        int strEquals = IOFilter.STR_EQUALS.index;
        int strNEquals = IOFilter.STR_NOTEQUALS.index;

//        Bundle { line_icon => 2130837606; line_name => Bakerloo; line_status => minor delays; line_url => http://www.google.co.uk; }
//        Bundle { line_icon => 2130837606; line_name => DLR; line_status => severe delays; line_url => http://www.google.co.uk; }
//        Bundle { line_icon => 2130837606; line_name => Metropolitan; line_status => minor delays; line_url => http://www.google.co.uk; }
//        Bundle { line_icon => 2130837606; line_name => Piccadilly; line_status => part closure; line_url => http://www.google.co.uk; }

        // First filter on Bakerloo and check what's in the thing
        ComponentService isBakerlooComponent = TestLib.createComponentForFilterSample(sd, new IODescription[]{ lineName },
                                                new String[]{ TubeService.BAKERLOO }, new int[]{ strEquals });

        // Test whether it isn't the bakerloo line
        ComponentService isntBakerlooComponent = TestLib.createComponentForFilterSample(sd, new IODescription[]{ lineName },
                                                new String[]{ TubeService.BAKERLOO }, new int[]{ strNEquals });

        // Get the ones with minor delays
        ComponentService minorDelaysComponent = TestLib.createComponentForFilterSample(sd, new IODescription[]{ lineStatus },
                                                new String[]{ TubeService.MINOR_DELAYS }, new int[]{ strEquals });

        // Get one where the it's the Bakerloo line with not minor delays
        ComponentService bakerlooMinorDelays = TestLib.createComponentForFilterSample(sd, new IODescription[]{ lineName, lineStatus },
                                                new String[]{ TubeService.BAKERLOO, TubeService.MINOR_DELAYS },
                                                new int[]{ strEquals, strNEquals});

        // Get one where isn't not part closed
        ComponentService notPartClosed = TestLib.createComponentForFilterSample(sd, new IODescription[]{ lineStatus },
                                                new String[]{ TubeService.PART_CLOSURE }, new int[]{ strNEquals });

        ComponentService none = TestLib.createComponentForFilterSample(sd, new IODescription[]{}, new String[]{}, new int[]{});

        ArrayList<ComponentService> testComponents = new ArrayList<ComponentService>();
        testComponents.add(isBakerlooComponent);
        testComponents.add(isntBakerlooComponent);
        testComponents.add(minorDelaysComponent);
        testComponents.add(bakerlooMinorDelays);
        testComponents.add(notPartClosed);
        testComponents.add(none);

        String[] keptNames = new String[]{ TubeService.BAKERLOO };
        String[] removedNames = new String[]{ TubeService.DLR, TubeService.METROPOLITAN, TubeService.PICCADILLY };

        String[] mdKept = new String[]{ TubeService.BAKERLOO, TubeService.METROPOLITAN };
        String[] mdRemoved = new String[]{ TubeService.DLR, TubeService.PICCADILLY };

        String[] bmdKept = new String[]{ };
        String[] bmdRemoved = new String[] { TubeService.BAKERLOO, TubeService.DLR, TubeService.METROPOLITAN, TubeService.PICCADILLY };

        String[] npcKept = new String[] { TubeService.BAKERLOO, TubeService.DLR, TubeService.METROPOLITAN };
        String[] npcRemoved = new String[] { TubeService.PICCADILLY };

        String[] noneKept = new String[] { TubeService.BAKERLOO, TubeService.DLR, TubeService.METROPOLITAN, TubeService.PICCADILLY };
        String[] noneRemoved = new String[]{ };

        ArrayList<String[]> testKept = new ArrayList<String[]>();
        testKept.add(keptNames);
        testKept.add(removedNames);
        testKept.add(mdKept);
        testKept.add(bmdKept);
        testKept.add(npcKept);
        testKept.add(noneKept);

        ArrayList<String[]> testRemoved = new ArrayList<String[]>();
        testRemoved.add(removedNames);
        testRemoved.add(keptNames);
        testRemoved.add(mdRemoved);
        testRemoved.add(bmdRemoved);
        testRemoved.add(npcRemoved);
        testRemoved.add(noneRemoved);

        ArrayList<String> filterParameters = new ArrayList<String>();
        filterParameters.add(TubeService.LINE_NAME);
        filterParameters.add(TubeService.LINE_NAME);
        filterParameters.add(TubeService.LINE_NAME);
        filterParameters.add(TubeService.LINE_NAME);
        filterParameters.add(TubeService.LINE_NAME);
        filterParameters.add(TubeService.LINE_NAME);

        for(int i = 0; i < testComponents.size(); i++) {

            Bundle filterResults = (Bundle) filterMethod.invoke(osc, tubeData, testComponents.get(i));
            ArrayList<Bundle> kept = filterResults.getParcelableArrayList(OrchestrationServiceConnection.FILTER_RETAINED);
            ArrayList<Bundle> removed = filterResults.getParcelableArrayList(OrchestrationServiceConnection.FILTER_REMOVED);

            String originalText = "";
            for (Bundle b : tubeData) {
                originalText += b.getString(filterParameters.get(i)) + " ";
            }
            Log.d(TAG, originalText);

            if (!keptCheck(testKept.get(i), kept, filterParameters.get(i))) {
                Log.d(TAG, "Kept fail " + i);
                String keptText = "Kept: ";
                for (Bundle b : kept) {
                    keptText += b.getString(filterParameters.get(i)) + " ";
                }
                Log.d(TAG, keptText);
                assertEquals(1, 2);
            }

            if (!keptCheck(testRemoved.get(i), removed, filterParameters.get(i))) {
                Log.d(TAG, "Removed fail" + i);
                String removedText = "Removed: ";
                for (Bundle b : removed) {
                    removedText += b.getString(filterParameters.get(i)) + " ";
                }
                Log.d(TAG, removedText);
                assertEquals(1, 2);
            }
        }

        assertEquals(1, 1);
    }

    private boolean keptCheck(String[] names, ArrayList<Bundle> things, String key) {
        if (names.length != things.size()) {
            Log.d(TAG, "Size mis-match: " + names.length + " -- " + things.size());
            return false;
        }

        boolean foundAll = true;
        for (String s : names) {
            boolean found = false;
            for (int i = 0; i < things.size(); i++) {
                if (s.equals(things.get(i).getString(key))) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                Log.d(TAG, "Didn't find " + s);
                foundAll = false;
            }
        }

        return foundAll;
    }

//    @LargeTest
//    public void testExecution() throws Exception {
//
//        // Run it and assert that a new entry has been added to the Log to account for it running properly
//        Registry registry = Registry.getInstance(getContext());
//        CompositeService notFred = TestLib.createAComposite(registry, getContext());
//        notFred.setName("Not fred");
//        registry.addComposite(notFred);
//
//        Intent serviceIntent = new Intent(getContext(), OrchestrationService.class);
//        ArrayList<Bundle> intentData = new ArrayList<Bundle>();
//        Bundle b = new Bundle();
//
//        b.putLong(COMPOSITE_ID, notFred.getID());
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
//        boolean allWin = true;
//
//        // When it gets here, do the database lookup to see if the composite was successfully executed in the last 5ish seconds
//        ArrayList<LogItem> logs = registry.getExecutionLog(notFred);
//        for(LogItem log : logs) {
//            if(log.getComposite().getID() == notFred.getID()) {
//                long endTimeMillis = log.getEndTime();
//                long current = System.currentTimeMillis();
//
//                long diff = current - endTimeMillis;
//                if(diff > 5000) {
//                    allWin = false;
//                }
//            }
//        }
//
//        if(!allWin)
//            assertEquals(1, 2);
//    }
}
