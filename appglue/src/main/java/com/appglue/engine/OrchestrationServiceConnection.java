package com.appglue.engine;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.appglue.ActivityLog;
import com.appglue.ComposableService;
import com.appglue.Library;
import com.appglue.R;
import com.appglue.Test;
import com.appglue.description.datatypes.IOType;
import com.appglue.description.ServiceDescription;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.engine.description.ServiceIO;
import com.appglue.library.IOFilter;
import com.appglue.library.IOFilter.FilterValue;
import com.appglue.serviceregistry.Registry;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Set;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.PREFS;

public class OrchestrationServiceConnection implements ServiceConnection
{
	boolean isBound;
	
	Messenger messageReceiver;
    
    private int index;
    
    private Context context;
    
    private Message message;
    
    private CompositeService cs;
    
    private boolean test;
    
    private Registry registry;
    
    private static final String FILTER_REMOVED = "filter_removed";
    private static final String FILTER_RETAINED = "filter_retained";
    
    private static final String FILTER_OUTPUTID = "filter_name";
    private static final String FILTER_CONDITION = "filter_condition";
    private static final String FILTER_VALUE = "filter_value";

    private Bundle[] sent;
    private Bundle[] received;

    private long executionInstance = -1L; // Initialise this to -1
    
    public OrchestrationServiceConnection(Context context, CompositeService cs, boolean test)
    {   
    	if(LOG) Log.d(TAG, Thread.currentThread().getName() + ": OrchestrationServiceConnection() " + System.currentTimeMillis());
    	this.registry = Registry.getInstance(context);
    	this.context = context;
    	
    	this.cs = cs;
    	this.index = 0;
    	this.test = test;
    	
    	this.messageReceiver = new Messenger(new IncomingHandler(this));
    	this.message = null;

        this.sent = new Bundle[cs.size()];
        this.received = new Bundle[cs.size()];
    }
    
	@Override
	public void onServiceConnected(ComponentName className, final IBinder service) 
	{
		if(LOG) Log.d(TAG, Thread.currentThread().getName() + ": OrchestrationServiceConnection.onServiceConnected() " + System.currentTimeMillis());
        Messenger messageSender = new Messenger(service);

        try 
        {	
        	Test.isValidBundle(index, cs.getComponents().size(), message.getData(), true);
        	
        	if(registry.hasFinished(cs.getID())) {
                // Indicate that we've stopped prematurely
                if(LOG) Log.w(TAG, cs.getName() + " stopped prematurely: onServiceConnected");
//              TODO  registry.compositeStopped(cs.getID(), "Shouldn't be running but tried to execute.");
            } else {
                sent[index] = message.getData();
                messageSender.send(message);
        	}
        }
        catch (RemoteException e) 
        {
            registry.messageFail(cs.getID(), executionInstance, cs.getComponents().get(index).getDescription(), sent[index]);
        }
	}

	@Override
	public void onServiceDisconnected(ComponentName className) 
	{
        messageReceiver = null;
	}
	
	public void start()
	{
		if(LOG) Log.d(TAG, Thread.currentThread().getName() + ": OrchestrationServiceConnection.start() " + System.currentTimeMillis());
		
		// We can't send a list the first time, but I'm not sure it matters to say that we're not sending anything
		message = Message.obtain(null, ComposableService.MSG_OBJECT);
    	message.replyTo = messageReceiver;
    	
    	executionInstance = registry.startComposite(cs.getID());
		this.doBindService(cs.getComponents().get(0));
	}
	
	public void startAtPosition(int position, boolean isList, Bundle data)
	{
		if(LOG) Log.d(TAG, Thread.currentThread().getName() + ": OrchestrationServiceConnection.startAtPosition(" + position + ") " + System.currentTimeMillis());
		this.index = position;
		
		if(isList)
			message = Message.obtain(null, ComposableService.MSG_LIST);
		else
			message = Message.obtain(null, ComposableService.MSG_OBJECT);
		
		message.replyTo = messageReceiver;
		
		ArrayList<Bundle> input = new ArrayList<Bundle>();
		input.add(data);
		
		Bundle stuff = new Bundle();
		stuff.putParcelableArrayList(ComposableService.INPUT, input);
		
		message.setData(stuff);

        if (position >= cs.size()) {
            Log.e(TAG, "Trying to execute a component with too high an index");
            return;
        }

		executionInstance = registry.startComposite(cs.getID());
		this.doBindService(cs.getComponents().get(position));
	}
	
	/**
	 * Bind the service.
	 * 
	 * Also get the parameters if we have any
	 * 
	 * @param service The service to be bound
	 */
	private void doBindService(ComponentService service)
    {
		if(LOG) Log.d(TAG, Thread.currentThread().getName() + ": OrchestrationServiceConnection.doBindService(" + service.getDescription().getName() + ") " + System.currentTimeMillis());
		Bundle messageData = message.getData();
        Library.printBundle(messageData);
		
		if(registry.hasFinished(cs.getID()))
    	{
            // Indicate that we've stopped prematurely
            if(LOG) Log.w(TAG, cs.getName() + " stopped prematurely: doBindService");
//          TODO  registry.compositeStopped(cs.getID(), "Shouldn't be running but tried to execute.");
			return;
    	}
		
		if(this.index != 0)
		{
			// Make the composition execution thing take into account the filter values
			ArrayList<Bundle> outputs = messageData.getParcelableArrayList(ComposableService.INPUT);

            if(outputs == null)
                Log.e(TAG, "Message data is null...?");
            else if(outputs.size() > 0) {
				// Then we need to check for filtering
				Bundle filterValues = this.filter(outputs, cs.getComponents().get(index - 1));
				
				ArrayList<Bundle> retained = filterValues.getParcelableArrayList(FILTER_RETAINED);
				
				if(retained.size() > 0)
				{
					messageData.remove(ComposableService.INPUT);
					messageData.putParcelableArrayList(ComposableService.INPUT, retained);
				}
				else
				{
					ArrayList<Bundle> removed = filterValues.getParcelableArrayList(FILTER_REMOVED);

                    for (Bundle b : removed) {
                        ComponentService currentComponent = cs.getComponents().get(index - 1); // Need to get the last index because we've moved on
                        long outputId = b.getLong(FILTER_OUTPUTID);
                        ServiceIO io = currentComponent.getOutput(outputId);

                        registry.filter(cs, executionInstance, currentComponent, io, b.getString(FILTER_CONDITION), messageData, b.getString(FILTER_VALUE));
                    }
					return;
				}
			}
			
			if(LOG) Log.w(TAG, "Pre-mapping of getOutputs " + System.currentTimeMillis());
            if(LOG) Log.d(TAG, Library.printBundle(messageData));
			messageData = this.mapOutputs(messageData, service);
			if(LOG) Log.w(TAG, "Post-mapping of getOutputs " + System.currentTimeMillis());
            if(LOG) Log.d(TAG, Library.printBundle(messageData));
		}
		else
		{
			if(LOG) Log.w(TAG, "Pre-mapping of getOutputs " + System.currentTimeMillis());
            if(LOG) Log.d(TAG, Library.printBundle(messageData));
			messageData = this.mapOutputs(null, service);
			if(LOG) Log.w(TAG, "Post-mapping of getOutputs " + System.currentTimeMillis());
            if(LOG) Log.d(TAG, Library.printBundle(messageData));
		}

		Bundle bDescription = service.getDescription().toBundle();
		messageData.putBundle(ComposableService.DESCRIPTION, bDescription);
        ServiceDescription description = service.getDescription();
		
		// Do one last check before we send
		if(registry.hasFinished(cs.getID()))
    	{
            // Indicate that we've stopped prematurely
            if(LOG) Log.w(TAG, cs.getName() + " stopped prematurely - has Finished doBindService");
//          TODO  registry.compositeStopped(cs.getID(), "Shouldn't be running but tried to execute.");
			return;
    	}
		
		if(LOG) Log.d(TAG, "About to start " + description.getPackageName() + "  :::  " + description.getClassName() + "  " + System.currentTimeMillis());
		
    	Intent intent = new Intent();
    	intent.setClassName(description.getPackageName(), description.getClassName());
    	
    	if(LOG) Log.d(TAG, "Binding service: " + description.getPackageName() + " ; " + description.getClassName() + "  " + System.currentTimeMillis());
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
		
        if(LOG) Log.d(TAG, "Sending data " + Library.printBundle(messageData) + "  " + System.currentTimeMillis());
        message.setData(messageData);
        
        if(LOG) Log.d(TAG, "Sent to " + description.getClassName() + " " + System.currentTimeMillis());
        isBound = true;
    }
	
	private Bundle filter(ArrayList<Bundle> messageData, ComponentService service)
	{
        ServiceDescription sd = service.getDescription();

		Log.w(TAG, Thread.currentThread().getName() + ": OrchestrationServiceConnection.filter(" + sd.getName() + ") " + System.currentTimeMillis());
		ArrayList<Bundle> filtered = new ArrayList<Bundle>();
		ArrayList<Bundle> retained = new ArrayList<Bundle>();
		Bundle b = new Bundle();
		
		ArrayList<ServiceIO> outputs = service.getOutputs();

        for (Bundle data : messageData) {
            boolean fail = false;

            for (ServiceIO output : outputs) {
                if (output.getFilterState() == ServiceIO.UNFILTERED) // We don't need to worry if the output doesn't need to be filtered
                    continue;

                // Now we know we need to filter it
                FilterValue fv = IOFilter.filters.get(output.getCondition());
                Object first = data.get(output.getDescription().getName());
                Object value = null;

                // At this point we have an object that is the thing we need. Do we need to make it into something else we can deal with?

                if (output.getFilterState() == ServiceIO.MANUAL_FILTER)
                    value = output.getManualValue();
                else if (output.getFilterState() == ServiceIO.SAMPLE_FILTER)
                    value = output.getChosenSampleValue().value;

                if (value == null) // Something has gone very very wrong
                {
                    Log.e(TAG, "Filter value is dead, you've done something rather stupid");
                    continue;
                }

                if (first == null) {
                    Log.e(TAG, "No value from the component... What have you done...");
                    continue;
                }

                try {
                    // Need to test LOTS of these - unit test time me thinks...
                    // This returns whether it PASSES the test, so we need to filter it if it doesn't
                    Boolean result = (Boolean) fv.method.invoke(null, first, value);


                    if (result) {
                        retained.add(data);
                        Log.e(TAG, "Filter " + output.getDescription().getName() + ": The answer is Yes");
                    } else {
                        if (output.getFilterState() == ServiceIO.SAMPLE_FILTER)
                            Log.e(TAG, "Filter " + output.getDescription().getName() + ": The answer is No -- " + first + " ?? " + value + "(" + output.getChosenSampleValue().name + ")");
                        else
                            Log.e(TAG, "Filter " + output.getDescription().getName() + ": The answer is No -- " + first + " ?? " + value);

                        // Put the name of the condition that failed, and the value that should have been set
                        data.putLong(FILTER_OUTPUTID, output.getID());
                        data.putString(FILTER_VALUE, first.toString());
                        data.putString(FILTER_CONDITION, fv.text);

                        filtered.add(data);
                        fail = true;
                        break;
                    }
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Wrong arguments.");
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "It's private, you did it wrong");
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    Log.e(TAG, "Invocation Target Exception!?!?!?!?!?!");
                    e.printStackTrace();
                }
            }

            // If we get to here we've checked all of the values for the different IOs for that message, so it can be retained
            if (!fail)
                retained.add(data);
        }
		
		b.putParcelableArrayList(FILTER_REMOVED, filtered);
		b.putParcelableArrayList(FILTER_RETAINED, retained);
		return b;
	}

    // FIXME This stuff might be replaceable with my new way of doing it so we don't have to rely on class checking
    // FIXME Need to store the information about the type of the thing in the bundle too. Then we can invoke the right method.


    @SuppressWarnings("unchecked")
	private Bundle mapOutputs(Bundle bundle, ComponentService service)
	{
        // Get the description of the component to be mapped to
        ServiceDescription sd = service.getDescription();
		if(LOG) Log.d(TAG, Thread.currentThread().getName() + ": OrchestrationServiceConnection.mapOutputs(to " + sd.getName() + ") " + System.currentTimeMillis());

        // Get the inputs of the component to be mapped to and set up the other arraylists
		ArrayList<ServiceIO> inputs = service.getInputs();
		ArrayList<Bundle> inputList = new ArrayList<Bundle>();

		if(bundle != null && bundle.get(ComposableService.INPUT) != null) {

            ArrayList<Bundle> outputList = new ArrayList<Bundle>();
            Object o = bundle.get(ComposableService.INPUT);

            // This gives us a list of bundles to iterate through to process
            if(o instanceof Bundle) {
                outputList.add((Bundle) o);
            } else if(o.getClass().equals(ArrayList.class)) {
                outputList = (ArrayList<Bundle>) o;
            } else {
                Log.e(TAG, "It's neither a bundle nor a list of bundles, something has gone very very wrong");
                // FIXME Write an error to the log
            }

            for (Bundle outputBundle : outputList) {
                Bundle inputBundle = new Bundle();

                for (ServiceIO input : inputs) {
                    ServiceIO output = input.getConnection();

                    if (output == null) {
                        continue; // This means that they haven't provided an input for this
                    }

                    // Find the thing
                    Object thing = outputBundle.get(output.getDescription().getName());
                    if (thing == null) {
                        Log.e(TAG, "Thing dead " + output.getDescription().getName());
                        continue;
                    }

                    IOType type = output.getDescription().getType();
                    type.addToBundle(inputBundle, thing, input.getDescription().getName());
                }

                inputList.add(inputBundle);
            }
		}

        if(inputList.size() == 0) {

        }

        // Go through the getInputs and input any manual values that are there
        for (ServiceIO input : inputs) {
            if (input.getFilterState() != ServiceIO.UNFILTERED) {
                // Then add it to the input list
                String name = input.getDescription().getName();
                IOType type = input.getDescription().getType();
                Object value = null;

                if (input.getFilterState() == ServiceIO.MANUAL_FILTER)
                    value = input.getManualValue();
                else if (input.getFilterState() == ServiceIO.SAMPLE_FILTER)
                    value = input.getChosenSampleValue().value;

                if(inputList.size() == 0)
                    inputList.add(new Bundle());

                for(Bundle b : inputList)
                    type.addToBundle(b, value, name);
            }
        }

		bundle.putParcelableArrayList(ComposableService.INPUT, inputList);
		return bundle;
	}
    
    public void doUnbindService() 
    {
    	Log.w(TAG, Thread.currentThread().getName() + ": OrchestrationServiceConnection.doUnbindService() " + System.currentTimeMillis());
        if (isBound) 
        {
            // Detach our existing connection.
            context.unbindService(OrchestrationServiceConnection.this);
            isBound = false;
        }
    }

    private void incrementIndex()
    {
    	this.index++;
    }

    /**
     * Handler of incoming messages from service.
     */
    private static class IncomingHandler extends Handler
    {
    	private OrchestrationServiceConnection osc;
    	
    	private IncomingHandler(OrchestrationServiceConnection osc)
    	{
    		super(Looper.getMainLooper());
    		
    		this.osc = osc;
    	}
    	
        @Override
        public void handleMessage(Message msg) 
        {   
        	Log.w(TAG, Thread.currentThread().getName() + ": IncomingHandler.handleMessage() " + System.currentTimeMillis());
        	if(LOG) Log.d(TAG, String.format("Orchestration received [%d] - %d: %s", osc.index, msg.what, Library.printBundle(msg.getData())));
        	
        	ReceiverTask rt = osc.new ReceiverTask(osc);
        	Message m = Message.obtain();
        	m.copyFrom(msg);
        	rt.execute(m);
        }
    }
    
    private class ReceiverTask extends AsyncTask<Message, Object, Object>
    {
    	private OrchestrationServiceConnection osc;
    	
    	private ReceiverTask(OrchestrationServiceConnection osc)
    	{
    		this.osc = osc;	
    	}
    	
		@Override
		protected Object doInBackground(Message... params) 
		{
        	// Handle the case of it being the last position here because we don't need to do anything else
			ArrayList<ComponentService> components = osc.cs.getComponentsAL();
			
			// There should only be one message
			Message m = params[0];
            osc.received[osc.index] = m.getData();
			
        	if(osc.index >= components.size() - 1)
        	{
        		osc.doUnbindService();
        		
        		if(!osc.test && m.what != ComposableService.MSG_FAIL)
        			osc.registry.compositeSuccess(osc.cs.getID(), executionInstance);
        		else if(!osc.test)
        			osc.registry.componentFail(osc.cs.getID(), executionInstance, components.get(osc.index).getDescription(), m.getData(), "Failed on the last one, that's not so good");

        		return null;
        	}

            Log.d(TAG, String.format("2 [%d:%d] - %d: %s", osc.index, components.size(), m.what, Library.printBundle(m.getData())));
        	
            switch (m.what)
            {
                case ComposableService.MSG_OBJECT:
                {
                	// Got a single object back from a service, send it on to the next one
                	osc.doUnbindService();
                    Log.d(TAG, "Unbinding for object " + components.get(osc.index).getDescription().getName());

            		osc.incrementIndex();
            		ComponentService next = components.get(osc.index);

                	// Create the new register message and bind the service
            		osc.message = Message.obtain(null, ComposableService.MSG_OBJECT);
            		osc.message.replyTo = osc.messageReceiver;
                	
                	// In this case it's just a bundle, pass it along
                	osc.message.setData(m.getData());
                	osc.doBindService(next);
                }
                break;
                    
                case ComposableService.MSG_LIST:
                {
                    Log.d(TAG, "Unbinding for list " + components.get(osc.index).getDescription().getName());
                	// Then we need to make it so that it sends one message to register, and then sends another to start processing the list (one object at a time)
                	osc.doUnbindService();
                	
                	osc.incrementIndex();
                	ComponentService next = components.get(osc.index);
                	
                	if(next != null)
                	{
                		osc.message = Message.obtain(null, ComposableService.MSG_LIST);
                		osc.message.replyTo = osc.messageReceiver;
                		osc.message.setData(m.getData());
                		osc.doBindService(next);
                	}
                }
                break;
                
                case ComposableService.MSG_WAIT:
                	if(LOG) Log.d(TAG, "WAAAAAAAAIIIIIIIIT");
                	// I'm not sure if I actually need to do anything here
                	break;
                	 
                case ComposableService.MSG_FAIL:

                    // Unbind the service that failed
                    osc.doUnbindService();

                    // Add the failure to the failure log
                    Bundle b = m.getData();
                    ArrayList<Bundle> dummyList = b.getParcelableArrayList(ComposableService.INPUT);
                    Bundle errorBundle = dummyList.get(0);
                    Registry.getInstance(context).componentFail(osc.cs.getID(), executionInstance, osc.cs.getComponents().get(osc.index).getDescription(), sent[osc.index], errorBundle.getString(ComposableService.ERROR));

                    SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

                    // Sorts out the showing notifications preference.
                    if(prefs.getBoolean(context.getResources().getString(R.string.prefs_notifications), false)) {

                        // Notify the user that there has been a failure
                        NotificationManager n = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);

                    	Intent intent = new Intent(context, ActivityLog.class);
                	    PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

                        NotificationCompat.Builder notificationBuilder;
                        notificationBuilder = new NotificationCompat.Builder(
                                context)
                                .setContentText(String.format("%s failed", osc.cs.getName()))
                                .setContentTitle("AppGlue Error")
                                .setSmallIcon(R.drawable.icon)
                                .setAutoCancel(true)
                                .setLargeIcon(largeIcon)
                                .setPriority(NotificationCompat.PRIORITY_MIN)
                                .setVibrate(null)
                                .setTicker(String.format("AppGlue: %s failed", osc.cs.getName()))
                				.setContentIntent(pendingIntent);

                        Notification notification = notificationBuilder.build();
                        n.notify(this.hashCode(), notification);
                    }


                	break;
                    
                default:
                	if(LOG) Log.d(TAG, "DEFAULT");
            }
			return null;
		}
		
		protected void onProgressUpdate(Object... progress) 
		{
			// Work out what to put here
		}

		protected void onPostExecute(Object result) 
		{
			// Work out what to put here
		}
    	
    }
}