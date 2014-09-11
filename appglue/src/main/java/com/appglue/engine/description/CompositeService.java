package com.appglue.engine.description;

import android.database.Cursor;
import android.support.v4.util.LongSparseArray;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;

import static com.appglue.Constants.TAG;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.DESCRIPTION;
import static com.appglue.Constants.ID;
import static com.appglue.Constants.Interval;
import static com.appglue.Constants.NAME;
import static com.appglue.Constants.ProcessType;
import static com.appglue.library.AppGlueConstants.INTERVAL;
import static com.appglue.library.AppGlueConstants.NUMERAL;
import static com.appglue.library.AppGlueConstants.ENABLED;
import static com.appglue.library.AppGlueConstants.TEMP_ID;

public class CompositeService {
    private long id;
    private String name;
    private String description;

    private long numeral;
    private Interval interval;

    private boolean enabled;

    public static final int NEW_COMPOSITE_PLACEHOLDER = Integer.MIN_VALUE;

    private SparseArray<ComponentService> components;
    private LongSparseArray<ComponentService> componentSearch;

    public CompositeService(boolean temp) {
        if (temp)
            this.id = TEMP_ID;
        else
            this.id = -1;
        this.name = ""; // We know that the name can never be blank so we're good
        this.description = "";
        this.components = new SparseArray<ComponentService>();
        this.enabled = false;
        this.componentSearch = new LongSparseArray<ComponentService>();
    }

    public CompositeService(String name, String description, ArrayList<ComponentService> services) {
        this(false);
        this.name = name;
        this.description = description;

        this.components = new SparseArray<ComponentService>();
        for(int i = 0; i < services.size(); i++) {
            components.put(i, services.get(i));
        }

        for (ComponentService comps : services) {
            this.componentSearch.put(comps.id(), comps);
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

        for(int i = 0; i < services.size(); i++) {
            ComponentService cs = services.valueAt(i);
            componentSearch.put(cs.id(), cs);
        }
    }

    public CompositeService(long id, String name, SparseArray<ComponentService> services, long numeral, Interval interval) {
        this(false);
        this.id = id;
        this.name = name;
        this.components = services;

        this.numeral = numeral;
        this.interval = interval;

        this.enabled = false;

        for(int i = 0; i < services.size(); i++) {
            ComponentService cs = services.valueAt(i);
            componentSearch.put(cs.id(), cs);
        }
    }


    public CompositeService(ArrayList<ComponentService> services) {
        this(false);
        // Generate a random name
        this.id = -1;
        this.name = "Random Service";

        this.components = new SparseArray<ComponentService>();
        for(int i = 0; i < services.size(); i++) {
            components.put(i, services.get(i));
        }

        for (ComponentService cs : services) {
            this.componentSearch.put(cs.id(), cs);
        }

        this.enabled = false;
    }

    public ComponentService getComponent(long id) {
        return this.componentSearch.get(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addAtEnd(ComponentService component) {

        this.components.put(components.size(), component);
        if(LOG) Log.d(TAG, String.format("Adding %s to %s at end (%d)", component.description().className(), name, components.size() - 1));
    }

    public void addComponent(int position, ComponentService component) {

        if(components.get(position) == null) {
            // If there isn't a component at that position, then add one
            components.put(position, component);
        } else {
            // If there is a component at that position, we need to move everything back
            ComponentService replacee = components.get(position);
            components.put(position, component);
            addComponent(position + 1, replacee);
        }
    }

    public boolean containsComponent(long componentId) {
        return this.getComponent(componentId) != null;
    }

    public void resetOrchestration() {
        this.components.clear();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
        return this.components.get(0).description().getProcessType() == ProcessType.TRIGGER;
    }

    public void setInfo(String prefix, Cursor c) {

        this.id = c.getLong(c.getColumnIndex(prefix + ID));
        this.name = c.getString(c.getColumnIndex(prefix + NAME));
        this.description = c.getString(c.getColumnIndex(prefix + DESCRIPTION));

        // FIXME WHAT ABOUT ACTIVE_OR_TIMER and IS_RUNNING?

        this.enabled = c.getInt(c.getColumnIndex(prefix + ENABLED)) == 1;
        this.numeral = c.getInt(c.getColumnIndex(prefix + NUMERAL));

        int intervalValue = c.getInt(c.getColumnIndex(prefix + INTERVAL));
        Interval interval;

        if (intervalValue == Interval.SECONDS.index) {
            interval = Interval.SECONDS;
        } else if (intervalValue == Interval.MINUTES.index) {
            interval = Interval.MINUTES;
        } else if (intervalValue == Interval.HOURS.index) {
            interval = Interval.HOURS;
        } else {
            interval = Interval.DAYS;
        }

        this.interval = interval;
    }

    public int size() {
        return components.size();
    }
}
