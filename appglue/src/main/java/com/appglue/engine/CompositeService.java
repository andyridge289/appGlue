package com.appglue.engine;

import android.database.Cursor;

import com.appglue.description.ServiceDescription;
import com.appglue.library.TST;

import java.util.ArrayList;

import static com.appglue.Constants.DESCRIPTION;
import static com.appglue.Constants.ID;
import static com.appglue.Constants.Interval;
import static com.appglue.Constants.NAME;
import static com.appglue.Constants.ProcessType;
import static com.appglue.library.AppGlueConstants.INTERVAL;
import static com.appglue.library.AppGlueConstants.NUMERAL;
import static com.appglue.library.AppGlueConstants.SHOULD_BE_RUNNING;
import static com.appglue.library.AppGlueConstants.TEMP_ID;

public class CompositeService {
    private long id;
    private String name;
    private String description;

    private long numeral;
    private Interval interval;

    private boolean shouldBeRunning;

    public static final int NEW_COMPOSITE_PLACEHOLDER = Integer.MIN_VALUE;

    private ArrayList<ServiceDescription> components;
    private TST<ServiceDescription> componentSearch;

    public CompositeService(boolean temp) {
        if (temp)
            this.id = TEMP_ID;
        else
            this.id = -1;
        this.name = ""; // We know that the name can never be blank so we're good
        this.description = "";
        this.components = new ArrayList<ServiceDescription>();
        this.shouldBeRunning = false;
        this.componentSearch = new TST<ServiceDescription>();
    }

    public CompositeService(String name, String description, ArrayList<ServiceDescription> components) {
        this(false);
        this.name = name;
        this.description = description;
        this.components = components;

        for (ServiceDescription s : components) {
            this.componentSearch.put(s.getClassName(), s);
        }
    }

    public static CompositeService makePlaceholder() {
        return new CompositeService("Nothing", "Nothing", null, false);
    }

    public CompositeService(long id, String name, String description, boolean shouldBeRunning) {
        this(false);
        this.id = id;
        this.name = name;
        this.description = description;
        this.shouldBeRunning = shouldBeRunning;
    }

    public CompositeService(String name, String description, ArrayList<ServiceDescription> services, boolean shouldBeRunning) {
        this(false);
        this.id = (long) CompositeService.NEW_COMPOSITE_PLACEHOLDER;
        this.name = name;
        this.components = services;
        this.description = description;
        this.shouldBeRunning = shouldBeRunning;

        if (services == null)
            return;

        for (ServiceDescription s : services) {
            this.componentSearch.put(s.getClassName(), s);
        }


    }

    public CompositeService(long id, String name, ArrayList<ServiceDescription> services, long numeral, Interval interval) {
        this(false);
        this.id = id;
        this.name = name;
        this.components = services;

        for (ServiceDescription s : services) {
            this.componentSearch.put(s.getClassName(), s);
        }

        this.numeral = numeral;
        this.interval = interval;

        this.shouldBeRunning = false;
    }


    public CompositeService(ArrayList<ServiceDescription> orchestration) {
        this(false);
        // Generate a random name
        this.id = -1;
        this.name = "Random Service";

        this.components = orchestration;

        for (ServiceDescription s : orchestration) {
            this.componentSearch.put(s.getClassName(), s);
        }

        this.shouldBeRunning = false;
    }

    /**
     * @param className The name of the class
     * @return The Component
     */
    public ServiceDescription getComponent(String className) {
        return this.componentSearch.get(className);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addComponent(ServiceDescription service) {
        this.componentSearch.put(service.getClassName(), service);
        this.components.add(service);
    }

    public void addComponent(int position, ServiceDescription component) {
        this.componentSearch.put(component.getClassName(), component);
        this.components.add(position, component);
    }

    public boolean containsComponent(String className) {
        return this.getComponent(className) != null;
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

    public ArrayList<ServiceDescription> getComponents() {
        return components;
    }

    public void setComponents(ArrayList<ServiceDescription> components) {
        this.components = components;
    }

    public boolean isShouldBeRunning() {
        return shouldBeRunning;
    }

    public void setShouldBeRunning(boolean shouldBeRunning) {
        this.shouldBeRunning = shouldBeRunning;
    }

    public boolean containsTrigger() {
        return this.components.get(0).getProcessType() == ProcessType.TRIGGER;
    }

    public void setInfo(String prefix, Cursor c) {

        this.id = c.getLong(c.getColumnIndex(prefix + ID));
        this.name = c.getString(c.getColumnIndex(prefix + NAME));
        this.description = c.getString(c.getColumnIndex(prefix + DESCRIPTION));

        // FIXME WHAT ABOUT ACTIVE_OR_TIMER and IS_RUNNING?

        this.shouldBeRunning = c.getInt(c.getColumnIndex(prefix + SHOULD_BE_RUNNING)) == 1;
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
