package com.appglue.engine;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.appglue.MainActivity;
import com.appglue.ComposableService;
import com.appglue.Library;
import com.appglue.R;
import com.appglue.SystemFeature;
import com.appglue.Test;
import com.appglue.description.ServiceDescription;
import com.appglue.description.datatypes.IOType;
import com.appglue.engine.model.ComponentService;
import com.appglue.engine.model.CompositeService;
import com.appglue.engine.model.IOFilter;
import com.appglue.engine.model.IOValue;
import com.appglue.engine.model.ServiceIO;
import com.appglue.library.FilterFactory.FilterValue;
import com.appglue.library.LogItem;
import com.appglue.library.err.OrchestrationException;
import com.appglue.serviceregistry.Registry;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.LOG_EXECUTION_INSTANCE;

public class OrchestrationServiceConnection implements ServiceConnection {

    boolean isBound;
    Messenger messageReceiver;
    private int index;

    private Context context;
    private Message message;

    private CompositeService cs;

    private boolean test;
    private Registry registry;

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

            if (!registry.isTerminated(cs, executionInstance)) {
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

        if (isList) {
            message = Message.obtain(null, ComposableService.MSG_LIST);
        } else {
            message = Message.obtain(null, ComposableService.MSG_OBJECT);
        }

        message.replyTo = messageReceiver;

        ArrayList<Bundle> input = new ArrayList<>();
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

        if (!versionCheck(service)) {
            registry.versionMismatch(cs, executionInstance, service);
            return;
        }

        int missingFeatures = deviceFeatures(service);
        if (missingFeatures > 0) {
            registry.missingFeatures(cs, executionInstance, service, missingFeatures);
            return;
        }

        int execStatus = paramTest(service.getDescription(), context);
        if (execStatus != 0) {
            registry.cantExecute(cs, executionInstance, service, execStatus);
            return;
        }

        if (service.getDescription().hasFlag(ComposableService.FLAG_TRIGGER)) {
            registry.triggerPositionFail(cs, executionInstance, service, messageData, service.getDescription().getName() + " in wrong position");
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
                Pair<ArrayList<Bundle>, ArrayList<Bundle>> filterValues;
                try {
                    filterValues = this.filter2(outputs, cs.getComponents().get(index - 1));
                } catch (OrchestrationException e) {
                    registry.terminate(cs, executionInstance, LogItem.ORCH_FAIL, "Filter failed: " + e.getMessage());
                    return;
                }

                ArrayList<Bundle> retained = filterValues.first;

                if (retained.size() > 0) {
                    messageData.remove(ComposableService.INPUT);
                    messageData.putParcelableArrayList(ComposableService.INPUT, retained);
                } else {
                    ArrayList<Bundle> removed = filterValues.second;
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

    // Test each filter separately
    // Combine them together with the condition applied to the component
    // Test each of the value nodes
    // Combine with AND
    // Test each value within the value node
    // Combine with condition applied to the value node
    private Pair<ArrayList<Bundle>, ArrayList<Bundle>> filter2(ArrayList<Bundle> messageData, ComponentService component) throws OrchestrationException {

        ArrayList<Bundle> retained = new ArrayList<>();
        ArrayList<Bundle> removed = new ArrayList<>();

        for (Bundle bundle : messageData) {
            if (filterTestFilters(bundle, component.getFilterCondition(), component.getFilters())) {
                retained.add(bundle);
            } else {
                removed.add(bundle);
            }
        }

        return new Pair<>(retained, removed);
    }

    // TODO This won't work for triggers

    private boolean versionCheck(ComponentService component) {

        return android.os.Build.VERSION.SDK_INT >= component.getDescription().getMinVersion();
    }

    private int deviceFeatures(ComponentService component) {

        int result = component.getDescription().missingFeaturesMask(context);
        PackageManager packageManager = context.getPackageManager();

        if (component.getDescription().hasFlag(ComposableService.FLAG_NETWORK)) {
            if (!packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY) &&
                !packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI)) {
                result |= SystemFeature.getFeature(SystemFeature.INTERNET).index;
            }
        }

        return result;
    }

    public static int paramTest(ServiceDescription sd, Context context) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int result = 0;

        Resources res = context.getResources();
        if (sd.hasFlag(ComposableService.FLAG_NETWORK)) {
            if (!prefs.getBoolean(res.getString(R.string.prefs_cost), true)) {
                result |= ComposableService.FLAG_MONEY;
            }
            if (!prefs.getBoolean(res.getString(R.string.prefs_network), true)) {
                result |= ComposableService.FLAG_NETWORK;
            }
        }

        if (sd.hasFlag(ComposableService.FLAG_MONEY)) {
            if (!prefs.getBoolean(res.getString(R.string.prefs_cost), true)) {
                result |= ComposableService.FLAG_MONEY;
            }
        }

        if (sd.hasFlag(ComposableService.FLAG_LOCATION)) {
            if (!prefs.getBoolean(res.getString(R.string.prefs_location), true)) {
                result |= ComposableService.FLAG_LOCATION;
            }
        }

        return result;
    }

    private boolean filterTestFilters(Bundle datum, boolean condition, ArrayList<IOFilter> filters) throws OrchestrationException {

        if (filters == null || filters.size() == 0)
            return true;

        // It needs to match all of the elements in the filter
        ArrayList<Boolean> results = new ArrayList<>();
        for (IOFilter filter : filters) {
            results.add(filterTestFilter(datum, filter));
        }

        return conditionCheck(condition, results);
    }

    private boolean conditionCheck(boolean condition, ArrayList<Boolean> results) {

        if (condition) { // AND
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

    private boolean filterTestFilter(Bundle datum, IOFilter filter) throws OrchestrationException {

        // We need to test each of the sets of values that are set in the filter against the thing we have for that IO
        ArrayList<ServiceIO> ios = filter.getIOs();

        boolean fail = false;
        for (ServiceIO io : ios) {

            // Get the actual value for that node
            Object actualValue = datum.get(io.getDescription().getName());

//            Log.d(TAG, String.format("%s - %s", io.getDescription().getName(), actualValue));

            if (!filterTestValues(actualValue, filter.getCondition(io), filter.getValues(io))) {
                if (LOG) Log.d(TAG, "\tfail");
                fail = true;
                break;
            } else {
                if (LOG) Log.d(TAG, "\tsuccess");
            }
        }

        // And then combine them all with an AND
        return !fail;
    }

    private boolean filterTestValues(Object actualValue, boolean condition, ArrayList<IOValue> filterValues) throws OrchestrationException {

        ArrayList<Boolean> results = new ArrayList<>();

        for (IOValue filterValue : filterValues) {
            results.add(filterTestValue(actualValue, filterValue));
        }

        return conditionCheck(condition, results);
    }

    private boolean filterTestValue(Object actualValue, IOValue ioValue) throws OrchestrationException {
        FilterValue fv = ioValue.getCondition();
        Object expectedValue = null;

        if (ioValue.getFilterState() == IOValue.MANUAL) {
            expectedValue = ioValue.getManualValue();
        } else if (ioValue.getFilterState() == IOValue.SAMPLE) {
            expectedValue = ioValue.getSampleValue().getValue();
        }

        if (ioValue == null) {  // Something has gone very very wrong
            Log.e(TAG, "Filter value is dead, you've done something rather stupid");
            return false;
        }

        if (actualValue == null) {
            Log.e(TAG, "No value from the component... What have you done...");
            return false;
        }

        try {
            // This returns whether it PASSES the test, so we need to filter it if it doesn't
            return ((Boolean) fv.method.invoke(null, actualValue, expectedValue));

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

    @SuppressWarnings("unchecked")
    private Bundle mapOutputs(Bundle bundle, ComponentService service) throws OrchestrationException {
        // Get the description of the component to be mapped to
        ServiceDescription sd = service.getDescription();
        if (LOG)
            Log.d(TAG, Thread.currentThread().getName() + ": OrchestrationServiceConnection.mapOutputs(to " + sd.getName() + ") " + System.currentTimeMillis());

        // Get the inputs of the component to be mapped to and set up the other array lists
        ArrayList<ServiceIO> inputs = service.getInputs();
        ArrayList<Bundle> inputList = new ArrayList<>();

        if (bundle != null && bundle.get(ComposableService.INPUT) != null) {

            ArrayList<Bundle> outputList = new ArrayList<>();
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

        // Go through the getInputs and input any manual values that are there
        for (ServiceIO input : inputs) {
            if (input.hasValue()) {

                Log.d(TAG, "Should be getting value for " + input.getDescription().getFriendlyName());

                // Then add it to the input list
                String name = input.getDescription().getName();
                IOType type = input.getDescription().getType();
                IOValue inputValue = input.getValue();
                Object value = null;

                if (inputValue.getFilterState() == IOValue.MANUAL)
                    value = inputValue.getManualValue();
                else if (inputValue.getFilterState() == IOValue.SAMPLE)
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

        private String toastMessage = "";
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

                if (!osc.test && m.what != ComposableService.MSG_FAIL) {
                    registry.componentSuccess(osc.cs, executionInstance, components.get(osc.index), "Success", null);
                    osc.registry.compositeSuccess(osc.cs, executionInstance);

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    if (prefs.getBoolean(context.getResources().getString(R.string.prefs_success), false)) {
                        toastMessage = "Composite \"" + cs.getName() + "\" executed successfully";
                    }

                } else if (!osc.test) {
                    osc.registry.componentCompositeFail(osc.cs, executionInstance, components.get(osc.index), m.getData(), "Failed on the last one, that's not so good");
                }

                return null;
            }

            // TODO The new flags need to be built into the preferences/orchestration
            // TODO Make the failure notification work
            // TODO Put in a check to see which components require what features in a device and don't enable those ones

            switch (m.what) {
                case ComposableService.MSG_OBJECT: {
                    // Got a single object back from a service, send it on to the next one
                    osc.doUnbindService();
                    registry.componentSuccess(osc.cs, executionInstance, components.get(osc.index), "Success", null);
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
                    registry.componentSuccess(osc.cs, executionInstance, components.get(osc.index), "Success", null);
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
                    registry.componentCompositeFail(osc.cs, executionInstance, osc.cs.getComponents().get(osc.index), sent[osc.index], errorBundle.getString(ComposableService.ERROR));

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

                    // Sorts out the showing notifications preference.
                    if (prefs.getBoolean(context.getResources().getString(R.string.prefs_fail), false)) {

                        // Notify the user that there has been a failure
                        NotificationManager n = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);

                        // TODO Tell the main activity what to do when we start with given intent things?
                        Intent intent = new Intent(context, MainActivity.class);
                        intent.putExtra(LOG_EXECUTION_INSTANCE, executionInstance);
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

        // Hopefully this is back on a normal thread
        protected void onPostExecute(Object result) {
            if (!toastMessage.equals("")) {
                Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
            }
        }

    }
}