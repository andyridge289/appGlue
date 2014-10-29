package com.appglue.serviceregistry;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.util.LongSparseArray;
import android.util.Log;
import android.widget.Toast;

import com.appglue.ComposableService;
import com.appglue.description.AppDescription;
import com.appglue.description.ServiceDescription;
import com.appglue.engine.Schedule;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.library.AppGlueLibrary;
import com.appglue.library.LogItem;

import java.util.ArrayList;
import java.util.HashMap;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;

//import com.appglue.test.EngineTest;

public class Registry {
    public static Registry registry = null;
    private LocalDBHandler dbHandler = null;

    private LongSparseArray<CompositeService> composites;

    private HashMap<String, ServiceDescription> remoteCache;

    // This is whatever the current service being edited (or the last one to be edited).
    private CompositeService composite;
    private CompositeService temp;

    private Context context;

    private Registry(Context context) {
        dbHandler = new LocalDBHandler(context);
        remoteCache = new HashMap<String, ServiceDescription>();
        composites = new LongSparseArray<CompositeService>();
        this.context = context;
    }

    public static Registry getInstance(Context context) {
        if (registry == null)
            registry = new Registry(context);

        return registry;
    }

    public void setCurrent(CompositeService service) {
        this.composite = service;
    }

    public void setCurrent(long id) {
        this.composite = this.getComposite(id);
    }

    public CompositeService resetTemp() {
        temp = dbHandler.resetTemp();
        return temp;
    }

    public CompositeService getCurrent() {
        return composite;
    }

    public CompositeService getTemp() {
        temp = getComposite(CompositeService.TEMP_ID);
        composite = temp;
        return composite;
    }

    public boolean tempExists() {
        CompositeService cs = dbHandler.getComposite(CompositeService.TEMP_ID);
        return cs.getComponents().size() > 0;
    }

    public CompositeService saveTempAsComposite(String name, boolean enabled) {

        if (name.equals("temp")) {
            do {
                name = AppGlueLibrary.generateRandomName();
            } while (dbHandler.compositeExistsWithName(name));
        }

        CompositeService composite = dbHandler.saveTempAsComposite(name, getTemp(), enabled);
        Toast.makeText(context, "Saved composite with name \"" + name + "\"", Toast.LENGTH_LONG).show();
        dbHandler.resetTemp();
        return composite;
    }

    public void addRemote(ServiceDescription service) {
        remoteCache.put(service.getClassName(), service);
    }

    public void addRemotes(ArrayList<ServiceDescription> services) {
        for (ServiceDescription service : services)
            this.addRemote(service);
    }

    public ServiceDescription getRemote(String className) {
        return this.remoteCache.get(className);
    }

    public void saveComposite(CompositeService cs) {
        dbHandler.updateComposite(cs);
    }

    public ArrayList<CompositeService> componentAtPosition(String className, int position) {
        return dbHandler.componentAtPosition(className, position);
    }

    public ServiceDescription addServiceFromBroadcast(ServiceDescription sd) {
        // Don't care what it says it is, it's lying.
//        sd.setServiceType(ServiceType.LOCAL);
        return dbHandler.addServiceDescription(sd);
    }

    public ServiceDescription addServiceDescription(ServiceDescription sd) {

        ServiceDescription alreadyThere = dbHandler.getServiceDescription(sd.getClassName());
        if (alreadyThere != null) {
            return alreadyThere;
        }

        // Don't care what it says it is, it's lying.
//        sd.setServiceType(ServiceType.IN_APP);
        return dbHandler.addServiceDescription(sd);
    }

    public ServiceDescription getServiceDescription(String className) {
        return dbHandler.getServiceDescription(className);
    }

    public ArrayList<ServiceDescription> getInputOnlyComponents() {
        ArrayList<ServiceDescription> components = dbHandler.getServiceDescriptions(0);

        for (int i = 0; i < components.size(); ) {
            if (components.get(i).getOutputs().size() > 0)
                components.remove(i);
            else
                i++;
        }

        return components;
    }

    public ArrayList<ServiceDescription> getOutputOnlyComponents() {
        ArrayList<ServiceDescription> components = dbHandler.getServiceDescriptions(0);

        for (int i = 0; i < components.size(); ) {
            if (components.get(i).getInputs().size() > 0)
                components.remove(i);
            else
                i++;
        }

        return components;
    }

    public CompositeService addComposite(CompositeService cs) {
        return dbHandler.addComposite(cs);
    }

    public CompositeService getComposite(long compositeId) {
        if (compositeId == -1) {
            return null;
        } else {
            long[] id = new long[]{compositeId};
            ArrayList<CompositeService> composites = dbHandler.getComposites(id, true);
            if (composites.size() == 0)
                return null;

            return composites.get(0);
        }
    }

    public ArrayList<CompositeService> getComposites() {
        ArrayList<CompositeService> composites = dbHandler.getComposites(null, false);

        for (CompositeService composite : composites) {
            this.composites.put(composite.getID(), composite);
        }

        return composites;
    }

    public boolean delete(CompositeService cs) {
        if (cs.getID() == 1) {
            Log.e(TAG, "You can't delete the temp");
            return false;
        }

        return dbHandler.deleteComposite(cs) > 0;
    }

    public int updateComposite(CompositeService composite) {
        return dbHandler.updateComposite(composite);
    }

    public boolean isEnabled(CompositeService composite) {
        return dbHandler.isEnabled(composite);
    }

    public ArrayList<ComponentService> getComponents(String className, int position) {
        return dbHandler.getComponents(className, position);
    }

    public long addComponent(ComponentService component) {
        return dbHandler.addComponent(component);
    }

    public ArrayList<CompositeService> getExamples(String componentName) {
        return dbHandler.getExamples(componentName);
    }

    public AppDescription getApp(String packageName) {
        return dbHandler.getApp(packageName);
    }

    public ArrayList<ServiceDescription> getComponentsForApp(String packageName) {
        return dbHandler.getComponentsForApp(packageName);
    }

    public ArrayList<ServiceDescription> getTriggers() {
        return dbHandler.getServiceDescriptions(ComposableService.FLAG_TRIGGER);
    }

    public boolean isTerminated(CompositeService composite, long executionInstance) {
        return dbHandler.isTerminated(composite, executionInstance);
    }

    public boolean terminate(CompositeService composite, long executionInstance, int status,
                             String message) {
        return dbHandler.terminate(composite, executionInstance, status, message);
    }

    public ArrayList<ServiceDescription> getMatchingForOutputs(ServiceDescription prior) {
        return dbHandler.getMatchingForIOs(prior, false);
    }

    public ArrayList<ServiceDescription> getMatchingForInputs(ServiceDescription next) {
        return dbHandler.getMatchingForIOs(next, true);
    }

    public ArrayList<LogItem> getExecutionLog() {
        return dbHandler.getLog();
    }

    public ArrayList<LogItem> getExecutionLog(CompositeService composite) {
        return dbHandler.getLog(composite);
    }

    /**
     * Start the composite and give back the execution getID of the instance of the running composite.
     *
     * @param composite The composite to start
     * @return The execution ID of the running instance of the composite
     */
    public long startComposite(CompositeService composite) {
        long execID = dbHandler.startComposite(composite);
        if (LOG)
            Log.d(TAG, "Started composite ID " + composite.getID() + "(" + composite.getName() + ") with execID " + execID);
        return execID;
    }

    public boolean compositeSuccess(CompositeService composite, long executionInstance) {
        //EngineTest.executeFinished = true;
        return dbHandler.terminate(composite, executionInstance, LogItem.SUCCESS, "Successfully executed");
    }

    public boolean componentSuccess(CompositeService composite, long executionInstance, ComponentService component, String message, Bundle outputData) {
        // If a component works, then say what output it gave back to the orchestrator
        return dbHandler.addToLog(composite, executionInstance, component, message, null, outputData, LogItem.SUCCESS, 0);
    }

    public void genericTriggerFail(ComponentService component, Bundle inputData, String error) {
        dbHandler.addToLog(null, -1L, component, error, inputData, null, LogItem.GENERIC_TRIGGER_FAIL, 0);
        //EngineTest.executeFinished = true;
    }

    /**
     * Record that a component has failed to execute properly, and stop the associated composite
     *
     * @param composite         The composite containing the component that failed
     * @param executionInstance The instance of the running composite that caused the problem
     * @param component         The class of the component that failed
     * @param inputData         The input that was passed to the component when it failed
     * @param message           The message that the component gave when it failed
     * @return An indicator of the success or failure of the logging
     */
    public boolean componentCompositeFail(CompositeService composite, long executionInstance, ComponentService component, Bundle inputData, String message) {
        // If a component fails, we should tell the user what the input to the component was when it failed
        boolean logComponent = dbHandler.addToLog(composite, executionInstance, component, message, inputData, null, LogItem.COMPONENT_FAIL, 0);
        boolean logComposite = dbHandler.terminate(composite, executionInstance, LogItem.COMPONENT_FAIL, message);

        //EngineTest.executeFinished = true;

        if (logComponent && logComposite) {
            return true;
        } else {
            Log.e(TAG, String.format("Failed to register component failure: %d, %d, %s, getInputs set: %b", composite.getID(),
                    executionInstance, component.getDescription().getClassName(), inputData != null));
            return false;
        }
    }

    public boolean messageFail(CompositeService composite, long executionInstance, ComponentService component, Bundle inputData) {
        String message = "Failed to send message.";
        boolean logComponent = dbHandler.addToLog(composite, executionInstance, component, message, inputData, null, LogItem.MESSAGE_FAIL, 0);
        boolean logComposite = dbHandler.terminate(composite, executionInstance, LogItem.COMPONENT_FAIL, message);

        //EngineTest.executeFinished = true;

        if (logComponent && logComposite) {
            return true;
        } else {
            Log.e(TAG, String.format("Failed to register message failure: %d, %d, %s, getInputs set: %b", composite.getID(),
                    executionInstance, component.getDescription().getClassName(), inputData != null));
            return false;
        }
    }

    public boolean filter(CompositeService cs, long executionInstance, ComponentService component,
                          Bundle inputData) {

        // When we stop at a filter, say what the data was at that point
//        ServiceDescription sd = component.getDescription();
        String message = "Stopped execution at filter: expected and got \"" + AppGlueLibrary.bundleToString(inputData) + "\"";

        boolean logComponent = dbHandler.addToLog(cs, executionInstance, component, message, inputData, null, LogItem.FILTER, 0);
        boolean logComposite = dbHandler.terminate(cs, executionInstance, LogItem.FILTER, message);

        //EngineTest.executeFinished = true;
        return logComposite && logComponent;
    }

    public boolean orchestratorFail(CompositeService composite, long executionInstance, ServiceDescription sd, String message) {
        return dbHandler.terminate(composite, executionInstance, LogItem.ORCH_FAIL, message);
    }

    public boolean cantExecute(CompositeService composite, long executionInstance, ComponentService component, int execStatus) {
        String message = "Stopped due to user preferences";

        boolean logComponent = dbHandler.addToLog(composite, executionInstance, component, message, null, null, LogItem.PARAM_STOP, execStatus);
        boolean logComposite = dbHandler.terminate(composite, executionInstance, LogItem.PARAM_STOP, message);

        return logComponent && logComposite;
    }

    public ArrayList<ServiceDescription> getAllServiceDescriptions() {
        return dbHandler.getServiceDescriptions(0);
    }

    public ArrayList<Schedule> getScheduledComposites() {
        return dbHandler.getScheduledComposites();
    }

    public void setupIDs(ServiceDescription sd) {
        dbHandler.setupIDs(sd);
    }

    public void add(Schedule s) {
        dbHandler.addSchedule(s);
    }

    public Schedule getSchedule(long id) {
        return dbHandler.getSchedule(id);
    }

    public void update(Schedule s) {
        dbHandler.updateSchedule(s);
    }

    public void executeSchedule(Schedule s) {
        dbHandler.executedSchedule(s);
    }

    public boolean delete(Schedule s) {
        return dbHandler.deleteSchedule(s);
    }

    public Schedule getNextScheduledItem() {
        return dbHandler.getNextScheduledItem();
    }
}