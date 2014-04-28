package com.appglue.serviceregistry;

import static com.appglue.Constants.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.Pair;

import com.appglue.ServiceIO;
import com.appglue.Constants.Interval;
import com.appglue.Constants.ServiceType;
import com.appglue.description.AppDescription;
import com.appglue.description.ServiceDescription;
import com.appglue.engine.CompositeService;
import com.appglue.library.LocalDBHandler;
import com.appglue.library.LogItem;

public class Registry
{
	public static Registry registry = null;
	
	private LocalDBHandler dbHandler = null; 
	
	private HashMap<String, ServiceDescription> inAppServices;
	private HashMap<String, ServiceDescription> localServices;
	private HashMap<String, ServiceDescription> remoteCache;
	
	// XXX Make the registry cache some things so we don't have to keep retrieving them
		// Save things in a variable
		// Null the variable when something relevant changes
		// Then do a lookup and if it ain't null just use it
	
	private CompositeService service;
	
	private Registry(Context context)
	{
		dbHandler = new LocalDBHandler(context);
		
		remoteCache = new HashMap<String, ServiceDescription>();
	}
	
	public static Registry getInstance(Context context)
	{
		if(registry == null)
				registry = new Registry(context);
		
		return registry;
	}
	
	public void setService(CompositeService service)
	{
		this.service = service;
	}
	
	public void setService(long id)
	{
		this.service = this.getComposite(id);
	}
	
	public void createService()
	{
		service = new CompositeService();
	}
	
	public CompositeService getService()
	{
		return service;
	}
	
	public void addRemote(ServiceDescription service)
	{
		remoteCache.put(service.getClassName(), service);
	}
	
	public void addRemotes(ArrayList<ServiceDescription> services)
	{
		for(ServiceDescription service : services)
			this.addRemote(service);
	}
	
	public ServiceDescription getRemote(String className)
	{
		return this.remoteCache.get(className);
	}
	
	public int deleteRemoteAtomics()
	{
		return dbHandler.deleteRemoteAtomics();
	}
	
	public int deleteDeviceAtomics()
	{
		return dbHandler.deleteDeviceAtomics();
	}
	
	public void saveComposite(CompositeService cs)
	{
		dbHandler.updateComposite(cs);
	}
	
	
	public ArrayList<CompositeService> atomicAtPosition(String className, int position)
	{
		return dbHandler.atomicAtPosition(className, position);
	}
	
	public boolean initialised()
	{
		return dbHandler.initialiased();
	}
	
	public long addAtomic(ServiceDescription sd)
	{
		long atomicId = dbHandler.addAtomic(sd);
		return atomicId;
	}
	
	public long addServiceFromBroadcast(ServiceDescription sd)
	{
		// Don't care what it says it is, it's lying.
		sd.setServiceType(ServiceType.LOCAL);
		return dbHandler.addAtomic(sd);
	}
	
	public long addService(ServiceDescription sd)
	{
		// Don't care what it says it is, it's lying.
		sd.setServiceType(ServiceType.IN_APP);
		return dbHandler.addAtomic(sd);
	}
	
	/**
	 * This updates a service with the information that is stored in the online database that isn't in the local one.
	 * 
	 * @param service The name of the service to be updated
	 * @param service	A description object reprsenting the service to be updated
	 */
	public void updateServiceFromLookup(ServiceDescription service)
	{
		dbHandler.updateServiceFromLookup(service);
	}
	
	public LocalDBHandler getDBHandler()
	{
		return dbHandler;
	}
	
	public void updateServices(Activity activity)
	{
		Registry registry = Registry.getInstance(activity);
		
		if(inAppServices == null)
			inAppServices = new HashMap<String, ServiceDescription>();
		
		ArrayList<ServiceDescription> inApp = registry.getInAppServices();
		for(int i = 0; i < inApp.size(); i++)
		{
			ServiceDescription sd = inApp.get(i);
			if(!this.inAppServices.containsKey(sd.getClassName()))
				inAppServices.put(sd.getClassName(), sd);
		}
		
		if(localServices == null)
			localServices = new HashMap<String, ServiceDescription>();
		
		ArrayList<ServiceDescription> local = registry.getLocalServices();
		for(int i = 0; i < local.size(); i++)
		{
			ServiceDescription sd = local.get(i);
			if(!this.localServices.containsKey(sd.getClassName()))
				localServices.put(sd.getClassName(), sd);
		}
	}
	
	public ArrayList<ServiceDescription> getInAppServices()
	{
		ArrayList<ServiceDescription> services = dbHandler.getAtomics(ServiceType.IN_APP, false);
		 
		if(services.size() == 0)
		{
		
		}
		
		return services;
	}
	
	public ArrayList<ServiceDescription> getLocalServices()
	{
		ArrayList<ServiceDescription> services = dbHandler.getAtomics(ServiceType.LOCAL, false);
		 
		if(services.size() == 0)
		{
			Log.e(TAG, "No local services!");
		}
		
		return services;
	}
	
	public ArrayList<ServiceDescription> getInputOnlyComponents() 
	{	
		ArrayList<ServiceDescription> components = getAllDeviceServices();
		
		for(int i = 0; i < components.size(); )
		{
			if(components.get(i).getOutputs().size() > 0)
				components.remove(i);
			else
				i++;
		}
		
		return components;
	}
	
	public ArrayList<ServiceDescription> getOutputOnlyComponents()
	{
		ArrayList<ServiceDescription> components = getAllDeviceServices();
		
		for(int i = 0; i < components.size(); )
		{
			if(components.get(i).getInputs().size() > 0)
				components.remove(i);
			else
				i++;
		}
		
		return components;
	}
	
	public ArrayList<ServiceDescription> getAllDeviceServices()
	{
		ArrayList<ServiceDescription> services = dbHandler.getAtomics(ServiceType.DEVICE, false);
		
		if(services.size() == 0)
		{
			Log.e(TAG, "No local services!");
		}
		
		return services;
	}
	
	public ArrayList<ServiceDescription> getComponents(String classString)
	{
		String[] classes = classString.split(",");
		
		ArrayList<ServiceDescription> services = new ArrayList<ServiceDescription>();
		for(String s : classes)
		{
			services.add(this.getAtomic(s));
		}
		
		return services;
	}
	
	public ServiceDescription getAtomic(String className)
	{
		return dbHandler.getAtomic(className);
	}
	
	public ArrayList<ServiceDescription> getConverterServices()
	{
		ArrayList<ServiceDescription> services = dbHandler.getAtomics(ServiceType.ANY, true);
		return services;
	}
	
	public CompositeService getComposite(long compositeId)
	{
		if(compositeId == -1)
			return null;
		else
			return dbHandler.getComposite(compositeId);
	}
	
	public ArrayList<CompositeService> getComposites()
	{
		ArrayList<CompositeService> composites = dbHandler.getComposites();
		return composites;
	}
	
	public boolean deleteComposite(CompositeService cs)
	{
		return dbHandler.deleteComposite(cs);
	}
	
	public long updateCurrent()
	{
		Log.d(TAG, "Updating current: " + service.getName());
		return dbHandler.updateComposite(service);
	}
	
	public boolean reset()
	{
		// Reset the current composite and then reset it in the database.
		boolean success = dbHandler.deleteComposite(service);
		service = new CompositeService();
		
		return success;
	}
	
	public boolean compositeExistsWithName(String name) 
	{
		return dbHandler.compositeExistsWithName(name);
	}
	
	/**
	 * Returns an indicator of:
	 * 		whether it should be running
	 * 		whether it is running
	 * 
	 * @param id
	 * @return
	 */
	public Pair<Boolean, Boolean> running(long id)
	{
		return dbHandler.compositeRunning(id);
	}
	
	public boolean isCompositeActive(long id)
	{
		return dbHandler.compositeRunning(id).first;
	}
	
	public boolean isCompositeRunning(long id)
	{
		return dbHandler.compositeRunning(id).second;
	}
	
	public ArrayList<CompositeService> getIntendedRunningServices()
	{
		return dbHandler.getIntendedRunningComposites();
	}
	
	public Pair<Long, Interval> getTimerDuration(long compositeId)
	{
		return dbHandler.getTimerDuration(compositeId);
	}
	
	public ArrayList<CompositeService> getExamples(String componentName)
	{
		return dbHandler.getExamples(componentName);
	}
	
	public AppDescription getApp(String packageName)
	{
		return dbHandler.getApp(packageName);
	}
	
	public ArrayList<ServiceDescription> getComponentsForApp(String packageName)
	{
		return dbHandler.getComponentsForApp(packageName);
	}
	
	public ArrayList<ServiceDescription> getTriggers()
	{
		return dbHandler.getComponents(ProcessType.TRIGGER);
	}

	public boolean setAppUninstalled(String packageName) 
	{
		return dbHandler.setAppInstalled(packageName, false);
	}
	
	public boolean success(long compositeId)
	{
		this.setIsntRunning(compositeId);
		return dbHandler.addToLog(compositeId, "", "", true);
	}
	
	public boolean fail(long compositeId, String className, String message)
	{
		this.setIsntRunning(compositeId);
		return dbHandler.addToLog(compositeId, className, message, false);
	}
	
	public boolean filter(CompositeService cs, ServiceDescription sd, ServiceIO io, String condition, Object value)
	{
		this.setIsntRunning(cs.getId());
		return dbHandler.addToLog(cs.getId(), sd.getClassName(), "Stopped execution: expected [" + condition + " \"" + io.getManualValue() + "\"] and got \"" + value + "\"", false);
	}

	public ArrayList<LogItem> getLog() 
	{
		return dbHandler.getLog();
	}

	public boolean updateWiring(CompositeService cs) 
	{
		boolean wiring = dbHandler.updateWiring(cs);
		boolean filters = dbHandler.updateFiltersAndValues(cs);
		
		return wiring & filters;
		
	}
	
	public boolean setIsRunning(long id)
	{
		return dbHandler.setCompositeIsRunning(id, 1);
	}
	
	public boolean setIsntRunning(long id)
	{
		return dbHandler.setCompositeIsRunning(id, 0);
	}
	
	public boolean setShouldBeRunning(long id)
	{
		return dbHandler.setCompositeActive(id, 1);
	}
	
	public boolean setShouldntBeRunning(long id)
	{
		return dbHandler.setCompositeActive(id, 0);
	}
	

	public boolean shouldBeRunning(long id) 
	{
		return dbHandler.shouldBeRunning(id);
	}
	
	public void startComposite(long id)
	{
		setIsRunning(id);
		setShouldBeRunning(id);
	}
	
	public void stopComposite(long id)
	{
		setShouldntBeRunning(id);
	}
	
	public void stopTemp()
	{
		stopComposite(this.service.getId());
	}
	
	public void finishComposite(long id)
	{
		setIsntRunning(id);
		setShouldntBeRunning(id);
	}

	public ArrayList<ServiceDescription> getMatchingForOutputs(ServiceDescription prior) 
	{
		return dbHandler.getMatchingForIOs(prior, false);
	}

	public ArrayList<ServiceDescription> getMatchingForInputs(ServiceDescription next) 
	{
		return dbHandler.getMatchingForIOs(next, true);
	}

	
}