package com.appglue.serviceregistry;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import com.appglue.Constants.Interval;
import com.appglue.Constants.ServiceType;
import com.appglue.description.AppDescription;
import com.appglue.description.ServiceDescription;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.library.AppGlueLibrary;
import com.appglue.library.LogItem;
import com.appglue.test.EngineTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.ProcessType;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.TEMP_ID;

public class Registry {
    public static Registry registry = null;
    private LocalDBHandler dbHandler = null;

    private HashMap<String, ServiceDescription> remoteCache;

    // XXX Make the registry cache some things so we don't have to keep retrieving them
    // Save things in a variable
    // Null the variable when something relevant changes
    // Then do a lookup and if it ain't null just use it
    // FIXME The caches should be in this rather than the local DB handler

    // This is whatever the current service being edited (or the last one to be edited).
    private CompositeService service;

    private Registry(Context context) {
        dbHandler = new LocalDBHandler(context, this);

        remoteCache = new HashMap<String, ServiceDescription>();
    }

    public static Registry getInstance(Context context) {
        if (registry == null)
            registry = new Registry(context);

        return registry;
    }

    public void setService(CompositeService service) {
        this.service = service;
    }

    public void setService(long id) {
        this.service = this.getComposite(id);
    }

    public CompositeService createTemp() {
        dbHandler.resetTemp();

        service = new CompositeService(true);
        return service;
    }

    public CompositeService getTemp() {
        service = getComposite(TEMP_ID);
        return service;
    }

    public boolean tempExists() {
        CompositeService cs = dbHandler.getComposite(TEMP_ID);
        return cs.getComponents().size() > 0;
    }

    public void saveTempAsComposite(String name) {
        dbHandler.saveTempAsComposite(name);
    }

    public CompositeService getService() {
        return service;
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
        sd.setServiceType(ServiceType.LOCAL);
        return dbHandler.addServiceDescription(sd);
    }

    public ServiceDescription addServiceDescription(ServiceDescription sd) {

        ServiceDescription alreadyThere = dbHandler.getServiceDescription(sd.getClassName());
        if(alreadyThere != null) {
            return alreadyThere;
        }

        // Don't care what it says it is, it's lying.
        sd.setServiceType(ServiceType.IN_APP);
        return dbHandler.addServiceDescription(sd);
    }

    public ServiceDescription getServiceDescription(String className) {
        return dbHandler.getServiceDescription(className);
    }

    public ArrayList<ServiceDescription> getInputOnlyComponents() {
        ArrayList<ServiceDescription> components = dbHandler.getServiceDescriptions(null);

        for (int i = 0; i < components.size(); ) {
            if (components.get(i).getOutputs().size() > 0)
                components.remove(i);
            else
                i++;
        }

        return components;
    }

    public ArrayList<ServiceDescription> getOutputOnlyComponents() {
        ArrayList<ServiceDescription> components = dbHandler.getServiceDescriptions(null);

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
            return dbHandler.getComposite(compositeId);
        }
    }

    public ArrayList<CompositeService> getComposites(boolean includeTemp) {
        return dbHandler.getComposites(includeTemp);
    }

    public boolean deleteComposite(CompositeService cs) {
        return dbHandler.deleteComposite(cs);
    }

    public long updateCurrent() {

        if (service == null) {
            Log.e(TAG, "Update current: service is null. This is dire");
            return -1;
        }

        Log.d(TAG, "Updating current: " + service.getName());
        return dbHandler.updateComposite(service);
    }

    public boolean reset() {
        // Reset the current composite and then reset it in the database.
        boolean success = dbHandler.deleteComposite(service);

        service = new CompositeService(true);
        dbHandler.resetTemp();

        return success;
    }

    public ArrayList<ComponentService> getComponents(String className, int position) {
        return dbHandler.getComponents(className, position);
    }

    public long addComponent(ComponentService component) {
        return dbHandler.addComponent(component);
    }

//	public boolean compositeExistsWithName(String name)
//	{
//		return dbHandler.compositeExistsWithName(name);
//	}

    /**
     * Returns an indicator of:
     * whether it should be running
     * whether it is running
     *
     * @param id The getID of the service to check
     * @return And indication of whether the thing is running or not
     */
    public boolean enabled(long id) {
        return dbHandler.compositeEnabled(id);
    }

    public ArrayList<CompositeService> getIntendedRunningServices() {
        return dbHandler.getIntendedRunningComposites();
    }

    public Pair<Long, Interval> getTimerDuration(long compositeId) {
        return dbHandler.getTimerDuration(compositeId);
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
        return dbHandler.getServiceDescriptions(ProcessType.TRIGGER);
    }

    public long isCompositeRunning(long compositeId) {
        return dbHandler.isCompositeRunning(compositeId);
    }

    public boolean isInstanceRunning(long compositeId, long executionInstance) {
        return dbHandler.isInstanceRunning(compositeId, executionInstance);
    }

    public boolean updateWiring(CompositeService cs) {
        dbHandler.updateWiring(cs);
        return dbHandler.updateFiltersAndValues(cs);

    }

    public boolean setEnabled(long id, boolean enabled) {
        return dbHandler.setCompositeActive(id, enabled ? 1 : 0);
    }

    public boolean isTerminated(CompositeService composite, long executionInstance) {
        return dbHandler.isTerminated(composite, executionInstance);
    }

    public boolean terminate(CompositeService composite, long executionInstance, int status,
                             String message) {
        boolean success = dbHandler.terminate(composite, executionInstance, status, message);
        return success;
    }

    public ArrayList<ServiceDescription> getMatchingForOutputs(ServiceDescription prior) {
        return dbHandler.getMatchingForIOs(prior, false);
    }

    public ArrayList<ServiceDescription> getMatchingForInputs(ServiceDescription next) {
        return dbHandler.getMatchingForIOs(next, true);
    }

    public void dumpSQLLog() {
        try {
            dbHandler.dumpSQLLog();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        if(LOG) Log.d(TAG, "Started composite ID " + composite.getID() + "(" + composite.getName() + ") with execID " + execID);
        return execID;
    }

    public boolean compositeSuccess(CompositeService composite, long executionInstance) {
        boolean ret = dbHandler.terminate(composite, executionInstance, LogItem.SUCCESS, "Successfully executed");
        EngineTest.executeFinished = true;
        return ret;
    }

    public boolean componentSuccess(CompositeService composite, long executionInstance, ComponentService component, String message, Bundle outputData) {
        // If a component works, then say what output it gave back to the orchestrator
        return dbHandler.addToLog(composite, executionInstance, component, message, null, outputData, LogItem.SUCCESS);
    }

    public void genericTriggerFail(ComponentService component, Bundle inputData, String error) {
        dbHandler.addToLog(null, -1L, component, error, inputData, null, LogItem.GENERIC_TRIGGER_FAIL);
    }

    /**
     * Record that a component has failed to execute properly, and stop the associated composite
     *
     * @param composite The composite containing the component that failed
     * @param executionInstance The instance of the running composite that caused the problem
     * @param component The class of the component that failed
     * @param inputData The input that was passed to the component when it failed
     * @param message The message that the component gave when it failed
     * @return An indicator of the success or failure of the logging
     */
    public boolean componentCompositeFail(CompositeService composite, long executionInstance, ComponentService component, Bundle inputData, String message) {
        // If a component fails, we should tell the user what the input to the component was when it failed
        boolean logComponent = dbHandler.addToLog(composite, executionInstance, component, message, inputData, null, LogItem.COMPONENT_FAIL);
        boolean logComposite = dbHandler.terminate(composite, executionInstance, LogItem.COMPONENT_FAIL, message);

        EngineTest.executeFinished = true;

        if(logComponent && logComposite) {
            return true;
        } else {
            Log.e(TAG, String.format("Failed to register component failure: %d, %d, %s, getInputs set: %b", composite.getID(),
                    executionInstance, component.getDescription().getClassName(), inputData != null));
            return false;
        }
    }

    public boolean messageFail(CompositeService composite, long executionInstance, ComponentService component, Bundle inputData) {
        String message = "Failed to send message.";
        boolean logComponent = dbHandler.addToLog(composite, executionInstance, component, message, inputData, null, LogItem.MESSAGE_FAIL);
        boolean logComposite = dbHandler.terminate(composite, executionInstance, LogItem.COMPONENT_FAIL, message);

        if(logComponent && logComposite) {
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
        ServiceDescription sd = component.getDescription();
        String message = "Stopped execution at filter: expected and got \"" + AppGlueLibrary.bundleToString(inputData) + "\"";

        boolean logComponent = dbHandler.addToLog(cs, executionInstance, component, message, inputData, null, LogItem.FILTER);
        boolean logComposite = dbHandler.terminate(cs, executionInstance, LogItem.FILTER, message);

        EngineTest.executeFinished = true;
        return logComposite && logComponent;
    }

    public boolean orchestratorFail(CompositeService composite, long executionInstance, ServiceDescription sd, String message) {
        boolean logComposite = dbHandler.terminate(composite, executionInstance, LogItem.ORCH_FAIL, message);
        return logComposite;
    }

    public ArrayList<ServiceDescription> getAllServiceDescriptions() {
        return dbHandler.getServiceDescriptions(null);
    }
}