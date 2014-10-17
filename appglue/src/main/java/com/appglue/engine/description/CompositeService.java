package com.appglue.engine.description;

import android.database.Cursor;
import android.support.v4.util.LongSparseArray;
import android.util.Log;
import android.util.SparseArray;

import com.appglue.ComposableService;
import com.appglue.description.ServiceDescription;

import java.util.ArrayList;

import static com.appglue.Constants.DESCRIPTION;
import static com.appglue.Constants.ID;
import static com.appglue.Constants.Interval;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.NAME;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.ENABLED;

public class CompositeService {
    private long id;
    private String name;
    private String description;

    private Schedule scheduleMode;

    private long numeral;
    private Interval interval;

    private int hours;
    private int minutes;

    private boolean enabled;

    public static final int NEW_COMPOSITE_PLACEHOLDER = Integer.MIN_VALUE;

    public static final int TEMP_ID = 1;
    public static final String TEMP_NAME = "temp";
    public static final String TEMP_DESCRIPTION = "This is the temporary composite";

    public enum Schedule {
        NONE(0, "None"),
        INTERVAL(1, "Interval"),
        TIME(2, "Time");

        public int index;
        public String name;

        Schedule(int index, String name) {
            this.index = index;
            this.name = name;
        }
    }

    private SparseArray<ComponentService> components;
    private LongSparseArray<ComponentService> componentSearch;

    private final Object lock = new Object();

    public CompositeService() {
        this.id = -1;
        this.name = "";
        this.description = "";
        this.numeral = 0;
        this.interval = Interval.NONE;
        this.enabled = true;
        this.scheduleMode = Schedule.NONE;
        this.hours = -1;
        this.minutes = -1;
        this.components = new SparseArray<ComponentService>();
        this.componentSearch = new LongSparseArray<ComponentService>();
    }

    public CompositeService(boolean temp) {
        this();
        if (temp) {
            this.id = TEMP_ID;
            this.name = TEMP_NAME;
            this.description = TEMP_DESCRIPTION;
        }
    }

    public CompositeService(String name, String description, ArrayList<ComponentService> services) {
        this(false);
        this.name = name;
        this.description = description;
        this.components = new SparseArray<ComponentService>();

        if(services != null) {
            for (int i = 0; i < services.size(); i++) {
                components.put(i, services.get(i));
                services.get(i).setComposite(this);
            }

            for (ComponentService comps : services) {
                this.componentSearch.put(comps.getID(), comps);
            }
        }
    }

    /**
     * When the ID of a component changes, we need to rebuild the search array
     */
    public void rebuildSearch() {
        this.componentSearch.clear();
        for (int i = 0; i < components.size(); i++) {
            ComponentService component = components.valueAt(i);
            if (component.getID() != -1) {
                componentSearch.put(component.getID(), component);
            }
        }
    }

    public static CompositeService makePlaceholder() {
        return new CompositeService("Nothing", "Nothing", null, false);
    }

    public CompositeService(long id, String name, String description, boolean enabled) {
        this(false);
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
    }

    public CompositeService(String name, String description, SparseArray<ComponentService> services, boolean enabled) {
        this(false);
        this.id = (long) CompositeService.NEW_COMPOSITE_PLACEHOLDER;
        this.name = name;
        this.components = services;
        this.description = description;
        this.enabled = enabled;

        if(services != null) {
            for (int i = 0; i < services.size(); i++) {
                ComponentService component = services.valueAt(i);
                componentSearch.put(component.getID(), component);
                services.get(i).setComposite(this);
            }
        }
    }

    public CompositeService(ArrayList<ComponentService> services) {
        this(false);
        // Generate a random name
        this.id = -1;
        this.name = "Random Service";

        if(services != null) {
            this.components = new SparseArray<ComponentService>();
            for (int i = 0; i < services.size(); i++) {
                ComponentService cs = services.get(i);
                components.put(i, cs);
                cs.setComposite(this);
                this.componentSearch.put(cs.getID(), cs);
            }
        }

        this.enabled = false;
    }

    public ComponentService getComponent(long id) {
        return this.componentSearch.get(id);
    }

    public ComponentService getComponent(int position) {
        if (this.components == null) {
            return null;
        }

        if (components.size() < position) {
            return null;
        }

        return components.get(position);
    }

    public void removeComponent(ComponentService component) {
        this.components.remove(component.getPosition());
        this.componentSearch.remove(component.getID());

        // Remove all of the connections for this and others
        for (ServiceIO input : component.getInputs()) {
            if (input.hasConnection()) {
                ServiceIO other = input.getConnection();
                other.setConnection(null);
                input.setConnection(null);
            }
        }

        // Remove all of the connections for this and others
        for (ServiceIO output : component.getOutputs()) {
            if (output.hasConnection()) {
                ServiceIO other = output.getConnection();
                other.setConnection(null);
                output.setConnection(null);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addAtEnd(ComponentService component) {

        this.components.put(components.size(), component);
        component.setComposite(this);
        if(LOG) Log.d(TAG, String.format("Adding %s to %s at end (%d)", component.getDescription().getClassName(), name, components.size() - 1));
    }

    public void addComponent(ServiceDescription sd, int position) {
        ComponentService component = new ComponentService(sd, position);
        synchronized (lock) {
            components.put(position, component);
            component.setComposite(this);
        }
    }

    public void addComponent(ComponentService component, int position) {

        if(components.get(position) == null) {
            // If there isn't a component at that position, then add one
            synchronized(lock) {
                components.put(position, component);
                component.setComposite(this);
                if (component.getID() != -1)
                    componentSearch.put(component.getID(), component);
            }
        } else {
            // If there is a component at that position, we need to move everything back
            ComponentService replacee = components.get(position);
            components.put(position, component);
            addComponent(replacee, position + 1);
        }
    }

    public boolean containsComponent(long componentId) {
        return this.getComponent(componentId) != null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getID() {
        return id;
    }

    public void setID(long id) {
        this.id = id;
    }

    public long getNumeral() {
        return numeral;
    }

    public void setNumeral(long numeral) {
        this.numeral = numeral;
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    public ArrayList<ComponentService> getComponentsAL() {
        ArrayList<ComponentService> comps = new ArrayList<ComponentService>();
        for(int i = 0 ; i < components.size(); i++) {
            int k = components.keyAt(i);
            ComponentService v = components.get(k);
            comps.add(k, v);
        }

        return comps;
    }

    public SparseArray<ComponentService> getComponents() {
        return components;
    }

    public ArrayList<ComponentService> getComponents(String className) {
        ArrayList<ComponentService> matching = new ArrayList<ComponentService>();
        for(int i = 0; i < components.size(); i++) {
            ComponentService component = components.valueAt(i);
            if(component.getDescription().getClassName().equals(className))
                matching.add(component);
        }

        return matching;
    }

    public void setComponents(SparseArray<ComponentService> components) {
        this.components = components;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean containsTrigger() {
        return this.components.get(0).getDescription().hasFlag(ComposableService.FLAG_TRIGGER);
    }

    public ServiceIO getInput(long id) {
        for(int i = 0; i < components.size(); i++) {
            ServiceIO in = components.valueAt(i).getInput(id);
            if(in != null)
                return in;
        }

        return null;
    }

    public ServiceIO getOutput(long id) {
        for(int i = 0; i < components.size(); i++) {
            ServiceIO in = components.valueAt(i).getOutput(id);
            if(in != null)
                return in;
        }

        return null;
    }

    public void setInfo(String prefix, Cursor c) {

        this.id = c.getLong(c.getColumnIndex(prefix + ID));
        this.name = c.getString(c.getColumnIndex(prefix + NAME));
        this.description = c.getString(c.getColumnIndex(prefix + DESCRIPTION));

        this.enabled = c.getInt(c.getColumnIndex(prefix + ENABLED)) == 1;
//        this.numeral = c.getInt(c.getColumnIndex(prefix + NUMERAL));

//        int intervalValue = c.getInt(c.getColumnIndex(prefix + INTERVAL));
//        this.interval = Interval.values()[intervalValue];

//        int scheduleValue = c.getInt(c.getColumnIndex(prefix + SCHEDULED));
//        this.scheduleMode = Schedule.values()[scheduleValue];

//        this.hours = c.getInt(c.getColumnIndex(prefix + HOURS));
//        this.minutes = c.getInt(c.getColumnIndex(prefix + MINUTES));
    }

    public int size() {
        return components.size();
    }

    public boolean equals(Object o) {

        if(o == null) {
            if(LOG) Log.d(TAG, "CompositeService->Equals: null");
            return false;
        }
        if(!(o instanceof CompositeService)) {
            if (LOG) Log.d(TAG, "CompositeService->Equals: Not a CompositeService");
            return false;
        }
        CompositeService other = (CompositeService) o;

        if(this.id != other.getID()) {
            if (LOG) Log.d(TAG, "CompositeService->Equals: id");
            return false;
        }

        if(!this.name.equals(other.getName())) {
            if (LOG) Log.d(TAG, "CompositeService->Equals: name " + name + " - " + other.getName());
            return false;
        }

        if(!this.description.equals(other.getDescription())) {
            if (LOG) Log.d(TAG, "CompositeService->Equals: description: " + description + " - " +
                                other.getDescription());
            return false;
        }

        if(this.numeral != other.getNumeral()) {
            if (LOG) Log.d(TAG, "CompositeService->Equals: numeral: " + this.numeral + " - " + other.getNumeral());
            return false;
        }

        if(this.interval.index != other.getInterval().index) {
            if (LOG) Log.d(TAG, "CompositeService->Equals: interval: " + this.interval.index + " - " +
                                other.getInterval().index);
            return false;
        }

        if(this.enabled != other.isEnabled()) {
            if (LOG) Log.d(TAG, "CompositeService->Equals: enabled");
            return false;
        }

        if (this.hours != other.getHours()) {
            if (LOG) Log.d(TAG, "CompositeService->Equals: hours " + hours + " - " + other.getHours());
            return false;
        }

        if(this.minutes != other.getMinutes()) {
            if (LOG) Log.d(TAG, "CompositeService->Equals: minutes " + minutes + " - " + other.getMinutes());
            return false;
        }

        if(!this.scheduleMode.equals(other.getScheduleMode())) {
            if (LOG) Log.d(TAG, "CompositeService->Equals: schedule " + scheduleMode.name + " - " + other.getScheduleMode().name);
            return false;
        }

        if(this.components.size() != other.getComponents().size()) {
            if (LOG) Log.d(TAG, "CompositeService->Equals: not same num components: " +
                components.size() + " - " + other.getComponents().size());
            return false;
        }

        for(int i = 0; i < components.size(); i++) {
            ComponentService component = components.valueAt(i);
            if(!component.equals(other.getComponent(component.getID()))) {
                if (LOG) Log.d(TAG, "CompositeService->Equals: component " + component.getID() + ": " + i);
                return false;
            }
        }

        return true;
    }

    public Schedule getScheduleMode() {
        return scheduleMode;
    }

    public int getScheduleIndex() {
        return scheduleMode.index;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }
}
