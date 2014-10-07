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
import android.util.Pair;
import android.util.SparseArray;

import com.appglue.ActivityLog;
import com.appglue.ComposableService;
import com.appglue.Library;
import com.appglue.R;
import com.appglue.TST;
import com.appglue.Test;
import com.appglue.description.ServiceDescription;
import com.appglue.description.datatypes.IOType;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.engine.description.IOFilter;
import com.appglue.engine.description.IOValue;
import com.appglue.engine.description.ServiceIO;
import com.appglue.library.AppGlueLibrary;
import com.appglue.library.FilterFactory.FilterValue;
import com.appglue.library.LogItem;
import com.appglue.library.err.OrchestrationException;
import com.appglue.serviceregistry.Registry;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.PLAY_SERVICES;
import static com.appglue.library.AppGlueConstants.PREFS;

public class OrchestrationServiceConnection implements ServiceConnection {
    boolean isBound;

    Messenger messageReceiver;

    private int index;

    private Context context;

    private Message message;

    private CompositeService cs;

    private boolean test;

    private Registry registry;

    public static final String FILTER_REMOVED = "filter_removed";
    public static final String FILTER_RETAINED = "filter_retained";

    private static final String FILTER_OUTPUTID = "filter_name";
    private static final String FILTER_CONDITION = "filter_condition";
    private static final String FILTER_VALUE = "filter_value";

    private Bundle[] sent;
    private Bundle[] received;

    private long executionInstance = -1L; // Initialise this to -1

    public OrchestrationServiceConnection(Context context, CompositeService cs, boolean test) {
        if (LOG)
            Log.d(TAG, Thread.currentThread().getName() + ": OrchestrationServiceConnection() " + System.currentTimeMillis());
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
    public void onServiceConnected(ComponentName className, final IBinder service) {
        if (LOG)
            Log.d(TAG, Thread.currentThread().getName() + ": OrchestrationServiceConnection.onServiceConnected() " + System.currentTimeMillis());
        Messenger messageSender = new Messenger(service);

        try {
            Test.isValidBundle(index, cs.getComponents().size(), message.getData(), true);

            if (registry.isTerminated(cs, executionInstance)) {

            } else {
                sent[index] = message.getData();
                messageSender.send(message);
            }
        } catch (RemoteException e) {
            registry.messageFail(cs, executionInstance, cs.getComponents().get(index), sent[index]);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName className) {
        messageReceiver = null;
    }

    public void start() {
        if (LOG)
            Log.d(TAG, Thread.currentThread().getName() + ": OrchestrationServiceConnection.start() " + System.currentTimeMillis());

        // We can't send a list the first time, but I'm not sure it matters to say that we're not sending anything
        message = Message.obtain(null, ComposableService.MSG_OBJECT);
        message.replyTo = messageReceiver;

        executionInstance = registry.startComposite(cs);
        this.doBindService(cs.getComponents().get(0));
    }

    public void startAtPosition(int position, boolean isList, Bundle data) {
        if (LOG)
            Log.d(TAG, Thread.currentThread().getName() + ": OrchestrationServiceConnection.startAtPosition(" + position + ") " + System.currentTimeMillis());
        this.index = position;

        if (isList)
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
            Log.e(TAG, String.format("Tried Executing %d (%s) at position %d [size %d]",
                    cs.getID(), cs.getName(), position, cs.size()));
            return;
        }

        executionInstance = registry.startComposite(cs);
        this.doBindService(cs.getComponents().get(position));
    }

    /**
     * Bind the service.
     * <p/>
     * Also get the parameters if we have any
     *
     * @param service The service to be bound
     */
    private void doBindService(ComponentService service) {

        if (LOG)
            Log.d(TAG, Thread.currentThread().getName() + ": OrchestrationServiceConnection.doBindService(" + service.getDescription().getName() + ") " + System.currentTimeMillis());
        Bundle messageData = message.getData();
        Library.printBundle(messageData);

        if (registry.isTerminated(cs, executionInstance)) {
            return;
        }

        if (this.index != 0) {
            // Make the composition execution thing take into account the filter values
            ArrayList<Bundle> outputs = messageData.getParcelableArrayList(ComposableService.INPUT);

            if (outputs == null) {
                registry.terminate(cs, executionInstance, LogItem.ORCH_FAIL, "The message has been killed");
                return;
            } else if (outputs.size() > 0) {

                // Then we need to check for filtering
                Bundle filterValues = null;
                try {
                    filterValues = this.filter(outputs, cs.getComponents().get(index - 1));
                } catch (OrchestrationException e) {
                    registry.terminate(cs, executionInstance, LogItem.ORCH_FAIL, "Filter failed: " + e.getMessage());
                    return;
                }

                ArrayList<Bundle> retained = filterValues.getParcelableArrayList(FILTER_RETAINED);

                if (retained.size() > 0) {
                    messageData.remove(ComposableService.INPUT);
                    messageData.putParcelableArrayList(ComposableService.INPUT, retained);
                } else {
                    ArrayList<Bundle> removed = filterValues.getParcelableArrayList(FILTER_REMOVED);
                    for (Bundle b : removed) {

                        ComponentService currentComponent = cs.getComponents().get(index - 1); // Need to get the last index because we've moved on
                        registry.filter(cs, executionInstance, currentComponent, b);
                    }
                    return;
                }
            }

            try {
                messageData = this.mapOutputs(messageData, service);
            } catch (OrchestrationException e) {
                return;
            }
        } else {
            try {
                messageData = this.mapOutputs(null, service);
            } catch (OrchestrationException e) {
                return;
            }
        }

        Bundle bDescription = service.getDescription().toBundle();
        messageData.putBundle(ComposableService.DESCRIPTION, bDescription);
        ServiceDescription description = service.getDescription();

        // Do one last check before we send
        if (registry.isTerminated(cs, executionInstance)) {
            return;
        }

        Intent intent = new Intent();
        intent.setClassName(description.getPackageName(), description.getClassName());

        if (LOG)
            Log.d(TAG, Thread.currentThread().getName() + ": Sending data -- " + Library.printBundle(messageData) + "  " + System.currentTimeMillis());
        message.setData(messageData);

        if (LOG)
            Log.d(TAG, Thread.currentThread().getName() + ": Binding service -- " + description.getPackageName() + " ; " + description.getClassName() + "  " + System.currentTimeMillis());
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);

        if (LOG)
            Log.d(TAG, Thread.currentThread().getName() + "Sent to " + description.getClassName() + " " + System.currentTimeMillis());
        isBound = true;
    }

    private Pair<ArrayList<Bundle>, ArrayList<Bundle>> filter2(ArrayList<Bundle> messageData, ComponentService component) throws OrchestrationException {

        ArrayList<Bundle> retained = new ArrayList<Bundle>();
        ArrayList<Bundle> removed = new ArrayList<Bundle>();

        ArrayList<IOFilter> filters = component.getFilters();
        boolean and = component.getFilterCondition();

        String FILTER_ID = "filter_id";

        // Give each bundle a filter ID so that we can keep track of them
        for (int i = 0; i < messageData.size(); i++) {
            messageData.get(i).putInt(FILTER_ID, i);
        }

        // Record each of the results, splitting them into retained and removed
        ArrayList<Pair<ArrayList<Bundle>, ArrayList<Bundle>>> pairs = new ArrayList<Pair<ArrayList<Bundle>, ArrayList<Bundle>>>();
        for (int i = 0; i < filters.size(); i++) {
            pairs.add(filter(messageData, filters.get(i)));
        }

        if (pairs.size() == 1) { // What about if there aren't any?
            // If there's only one we just return it, AND and OR are both the same
            return pairs.get(0);
        }

        SparseArray<Bundle> retSparse = new SparseArray<Bundle>();

        if (and) {
            // We need to see what's in the first one, and then check if it's in all the others.
            // If it is then we put it in retained, else, removed
            ArrayList<Bundle> first = pairs.get(0).first;
            for (Bundle b : first) {
                // Check if the bundle is in ALL of the others
                // If it is, put it in the sparse array of things we need to keep
                int id = b.getInt(FILTER_ID);
                boolean foundAll = true;

                for (int i = 1; i < pairs.size(); i++) {
                    // Try to find it in this pair
                    ArrayList<Bundle> ret = pairs.get(i).first;
                    boolean found = false;

                    for (int j = 0; j < ret.size(); j++) {
                        if (ret.get(j).getInt(FILTER_ID) == id) {
                            found = true;
                            break;
                        }
                    }

                    if(!found) {
                        foundAll = false;
                        break;
                    }
                }

                if (foundAll) {
                    retSparse.put(id, b);
                }
            }

        } else {
            // Add what's in the first one, what's in the second one that hasn't already been added, Ad nauseum

            for (int i = 0; i < pairs.size(); i++) {
                ArrayList<Bundle> ret = pairs.get(i).first;
                for(int j = 0; j < ret.size(); j++) {
                    if(retSparse.get(ret.get(j).getInt(FILTER_ID)) == null) {
                        retSparse.put(ret.get(j).getInt(FILTER_ID), ret.get(j));
                    }
                }
            }
        }

        // Put the message data in the two lists depending on how our condition goes
        // If we've added it to the sparse array then it exists in all of the things
        for (Bundle b : messageData) {
            if(retSparse.get(b.getInt(FILTER_ID)) != null) {
                retained.add(b);
            } else {
                removed.add(b);
            }
        }

        // And now remove all of the filter IDs because no one else needs them
        for (Bundle b : retained) {
            b.remove(FILTER_ID);
        }
        for (Bundle b : removed) {
            b.remove(FILTER_ID);
        }

        return new Pair<ArrayList<Bundle>, ArrayList<Bundle>>(retained, removed);
    }

    private Pair<ArrayList<Bundle>, ArrayList<Bundle>> filter(ArrayList<Bundle> messageData, IOFilter filter) throws OrchestrationException {
        ArrayList<Bundle> retained = new ArrayList<Bundle>();
        ArrayList<Bundle> removed = new ArrayList<Bundle>();

        // We need to know what IOs are being looked at in the filter so that we can check each of them.
        ArrayList<ServiceIO> ios = filter.getIOs();

        for (Bundle data : messageData) {

            // For now it needs to match ALL of the IOs in this filter, but I suppose that might change later
            boolean allMatch = true;
            for (int i = 0; i < ios.size(); i++) {

                ServiceIO io = ios.get(i);
                boolean and = filter.getCondition(io);
                ArrayList<IOValue> values = filter.getValues(io);

                // Then for each IO we need to see what values we are expecting
                if(!filterTest(data.get(io.getDescription().getName()), and, values)) {
                    allMatch = false;
                }
            }

            if(allMatch) {
                retained.add(data);
            } else {
                removed.add(data);
            }
        }

        return new Pair<ArrayList<Bundle>, ArrayList<Bundle>>(retained, removed);
    }

    private boolean filterTest(Object actualValue, boolean and, ArrayList<IOValue> filterValues) throws OrchestrationException {

        // We need to compare the actual value with each of the expected values
        // Deal with the result differently depending on whether its an AND or an OR

        ArrayList<Boolean> results = new ArrayList<Boolean>();

        for(int i = 0 ; i < filterValues.size(); i++) {
            IOValue ioValue = filterValues.get(i);
            FilterValue fv = ioValue.getCondition();
            Object expectedValue = null;

            if (ioValue.getFilterState() == IOValue.MANUAL_FILTER) {
                expectedValue = ioValue.getManualValue();
            } else if (ioValue.getFilterState() == IOValue.SAMPLE_FILTER) {
                expectedValue = ioValue.getSampleValue().getValue();
            }

            if (ioValue == null) {  // Something has gone very very wrong
                Log.e(TAG, "Filter value is dead, you've done something rather stupid");
                continue;
            }

            if (actualValue == null) {
                Log.e(TAG, "No value from the component... What have you done...");
                continue;
            }

            try {
                // This returns whether it PASSES the test, so we need to filter it if it doesn't
                results.add((Boolean) fv.method.invoke(null, actualValue, expectedValue));
            } catch (IllegalArgumentException e) {
                throw new OrchestrationException("Wrong arguments passed to filter method: " +
                        fv.method.getName() + actualValue + ", " + expectedValue);
            } catch (IllegalAccessException e) {
                throw new OrchestrationException("Can't access filter condition method: " +
                        fv.method.getName());
            } catch (InvocationTargetException e) {
                throw new OrchestrationException("Invocation target exception in filter method. Not sure what this means");
            }
        }

        if (and) {
            // IF one of them is false then we need to say no
            for (boolean bool : results) {
                if (!bool)
                    return false;
            }

            return true;

        } else {
            // If one of them is true then we say yes
            for (boolean bool : results) {
                if (bool)
                    return true;
            }

            return false;
        }
    }

    private Bundle filter(ArrayList<Bundle> messageData, ComponentService service) throws OrchestrationException {
        ArrayList<Bundle> filtered = new ArrayList<Bundle>();
        ArrayList<Bundle> retained = new ArrayList<Bundle>();

        Bundle b = new Bundle();

        ArrayList<ServiceIO> outputs = service.getOutputs();

        for (Bundle data : messageData) {

            int matchCount = 0;
            int skipCount = 0;

            Log.d(TAG, "Checking: " + AppGlueLibrary.bundleToString(data));

            for (int i = 0; i < outputs.size(); i++) {

                ServiceIO output = outputs.get(i);

                if (!output.hasValues()) {
                    skipCount++;
                    continue;
                }

                // Now we know we need to filter it
                FilterValue fv = output.getCondition();
                Object first = data.get(output.getDescription().getName());
                Object value = null;

                // At this point we have an object that is the thing we need. Do we need to make it into something else we can deal with?

                ArrayList<Boolean> results = new ArrayList<Boolean>();
                for (IOValue outputValue : output.getValues()) {

                    if (outputValue.getFilterState() == IOValue.MANUAL_FILTER)
                        value = outputValue.getManualValue();
                    else if (outputValue.getFilterState() == IOValue.SAMPLE_FILTER)
                        value = outputValue.getSampleValue().getValue();

                    if (value == null) {  // Something has gone very very wrong
                        Log.e(TAG, "Filter value is dead, you've done something rather stupid");
                        continue;
                    }

                    if (first == null) {
                        Log.e(TAG, "No value from the component... What have you done...");
                        continue;
                    }

                    try {
                        // This returns whether it PASSES the test, so we need to filter it if it doesn't
                        results.add((Boolean) fv.method.invoke(null, first, value));
                    } catch (IllegalArgumentException e) {
                        throw new OrchestrationException("Wrong arguments passed to filter method: " +
                                fv.method.getName() + first + ", " + value);
                    } catch (IllegalAccessException e) {
                        throw new OrchestrationException("Can't access filter condition method: " +
                                fv.method.getName());
                    } catch (InvocationTargetException e) {
                        throw new OrchestrationException("Invocation target exception in filter method. Not sure what this means");
                    }
                }

                if (output.getValueCombinator() == ServiceIO.COMBO_AND) {
                    // If it's an AND relation then only increment if all are true
                    boolean allSet = true;
                    for (int j = 0; j < results.size(); j++) {
                        if (!results.get(j)) {
                            allSet = false;
                            break;
                        }
                    }

                    if (allSet)
                        matchCount++;

                } else {
                    // If it's an OR relation then increment if one is true
                    for (int j = 0; j < results.size(); j++) {
                        if (results.get(j)) {
                            matchCount++;
                            break;
                        }
                    }
                }
            }

            // If we get to here we've checked all of the values for the different IOs for that message, so it can be retained
            if (matchCount + skipCount == outputs.size()) {
                retained.add(data);
            } else {
                filtered.add(data);
            }
        }

        b.putParcelableArrayList(FILTER_REMOVED, filtered);
        b.putParcelableArrayList(FILTER_RETAINED, retained);
        return b;
    }

    @SuppressWarnings("unchecked")
    private Bundle mapOutputs(Bundle bundle, ComponentService service) throws OrchestrationException {
        // Get the description of the component to be mapped to
        ServiceDescription sd = service.getDescription();
        if (LOG)
            Log.d(TAG, Thread.currentThread().getName() + ": OrchestrationServiceConnection.mapOutputs(to " + sd.getName() + ") " + System.currentTimeMillis());

        // Get the inputs of the component to be mapped to and set up the other array lists
        ArrayList<ServiceIO> inputs = service.getInputs();
        ArrayList<Bundle> inputList = new ArrayList<Bundle>();

        if (bundle != null && bundle.get(ComposableService.INPUT) != null) {

            ArrayList<Bundle> outputList = new ArrayList<Bundle>();
            Object o = bundle.get(ComposableService.INPUT);

            // This gives us a list of bundles to iterate through to process
            if (o instanceof Bundle) {
                outputList.add((Bundle) o);
            } else if (o.getClass().equals(ArrayList.class)) {
                outputList = (ArrayList<Bundle>) o;
            } else {
                String message = "Map outputs: Data is not Bundle or AL<Bundle>: " + o.getClass().getCanonicalName();
                registry.orchestratorFail(cs, executionInstance, sd, message);
                throw new OrchestrationException(message);
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

        if (inputList.size() == 0) {

        }

        // Go through the getInputs and input any manual values that are there
        for (ServiceIO input : inputs) {
            if (input.hasValues()) {
                // Then add it to the input list
                String name = input.getDescription().getName();
                IOType type = input.getDescription().getType();
                IOValue inputValue = input.getValues().get(0);
                Object value = null;

                if (inputValue.getFilterState() == IOValue.MANUAL_FILTER)
                    value = inputValue.getManualValue();
                else if (inputValue.getFilterState() == IOValue.SAMPLE_FILTER)
                    value = inputValue.getSampleValue().getValue();

                if (inputList.size() == 0)
                    inputList.add(new Bundle());

                for (Bundle b : inputList)
                    type.addToBundle(b, value, name);
            }
        }

        bundle = new Bundle();
        bundle.putParcelableArrayList(ComposableService.INPUT, inputList);
        return bundle;
    }

    public void doUnbindService() {
        if (LOG)
            Log.d(TAG, Thread.currentThread().getName() + ": OrchestrationServiceConnection.doUnbindService() " + System.currentTimeMillis());
        if (isBound) {
            // Detach our existing connection.
            context.unbindService(OrchestrationServiceConnection.this);
            isBound = false;
        }
    }

    private void incrementIndex() {
        this.index++;
    }

    /**
     * Handler of incoming messages from service.
     */
    private static class IncomingHandler extends Handler {
        private OrchestrationServiceConnection osc;

        private IncomingHandler(OrchestrationServiceConnection osc) {
            super(Looper.getMainLooper());

            this.osc = osc;
        }

        @Override
        public void handleMessage(Message msg) {
            Log.w(TAG, Thread.currentThread().getName() + ": IncomingHandler.handleMessage() " + System.currentTimeMillis());
            if (LOG)
                Log.d(TAG, String.format("Orchestration received [%d] - %d: %s", osc.index, msg.what, Library.printBundle(msg.getData())));

            ReceiverTask rt = osc.new ReceiverTask(osc);
            Message m = Message.obtain();
            m.copyFrom(msg);
            rt.execute(m);
        }
    }

    private class ReceiverTask extends AsyncTask<Message, Object, Object> {
        private OrchestrationServiceConnection osc;

        private ReceiverTask(OrchestrationServiceConnection osc) {
            this.osc = osc;
        }

        @Override
        protected Object doInBackground(Message... params) {
            // Handle the case of it being the last position here because we don't need to do anything else
            ArrayList<ComponentService> components = osc.cs.getComponentsAL();

            // There should only be one message
            Message m = params[0];
            osc.received[osc.index] = m.getData();

            if (osc.index >= components.size() - 1) {

                osc.doUnbindService();

                if (!osc.test && m.what != ComposableService.MSG_FAIL)
                    osc.registry.compositeSuccess(osc.cs, executionInstance);
                else if (!osc.test)
                    osc.registry.componentCompositeFail(osc.cs, executionInstance, components.get(osc.index), m.getData(), "Failed on the last one, that's not so good");

                return null;
            }

            switch (m.what) {
                case ComposableService.MSG_OBJECT: {
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

                case ComposableService.MSG_LIST: {
                    Log.d(TAG, "Unbinding for list " + components.get(osc.index).getDescription().getName());
                    // Then we need to make it so that it sends one message to register, and then sends another to start processing the list (one object at a time)
                    osc.doUnbindService();

                    osc.incrementIndex();
                    ComponentService next = components.get(osc.index);

                    if (next != null) {
                        osc.message = Message.obtain(null, ComposableService.MSG_LIST);
                        osc.message.replyTo = osc.messageReceiver;
                        osc.message.setData(m.getData());
                        osc.doBindService(next);
                    }
                }
                break;

                case ComposableService.MSG_WAIT:
                    if (LOG) Log.d(TAG, "WAAAAAAAAIIIIIIIIT");
                    // I'm not sure if I actually need to do anything here
                    break;

                case ComposableService.MSG_FAIL:

                    // Unbind the service that failed
                    osc.doUnbindService();

                    // Add the failure to the failure log
                    Bundle b = m.getData();
                    ArrayList<Bundle> dummyList = b.getParcelableArrayList(ComposableService.INPUT);
                    Bundle errorBundle = dummyList.get(0);
                    Registry.getInstance(context).componentCompositeFail(osc.cs, executionInstance, osc.cs.getComponents().get(osc.index), sent[osc.index], errorBundle.getString(ComposableService.ERROR));

                    SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

                    // Sorts out the showing notifications preference.
                    if (prefs.getBoolean(context.getResources().getString(R.string.prefs_notifications), false)) {

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
                    if (LOG) Log.d(TAG, "DEFAULT");
            }
            return null;
        }

        protected void onProgressUpdate(Object... progress) {
            // Work out what to put here
        }

        protected void onPostExecute(Object result) {
            // Work out what to put here
        }

    }
}