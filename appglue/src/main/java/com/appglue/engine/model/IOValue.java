package com.appglue.engine.model;

import com.appglue.description.SampleValue;
import com.appglue.library.FilterFactory;
import com.orhanobut.logger.Logger;

public class IOValue {

    public static final int UNFILTERED = -1;
    public static final int MANUAL = 0;
    public static final int SAMPLE = 1;

    private long id = -1; // It'll be going in the database so we'll need an ID at some point

    private int filterState;

    private FilterFactory.FilterValue condition;

    private ServiceIO io;

    private Object manualValue;
    private SampleValue sampleValue;
    private boolean enabled;

    public IOValue(ServiceIO io) {
        this.condition = null;
        this.manualValue = null;
        this.io = io;
        this.filterState = UNFILTERED;
        this.enabled = true;
    }

    public IOValue(FilterFactory.FilterValue condition, Object manualValue, ServiceIO io) {
        this(io);
        this.condition = condition;
        this.manualValue = manualValue;
        this.filterState = MANUAL;
        this.enabled = true;
    }

    public IOValue(FilterFactory.FilterValue condition, SampleValue sampleValue, ServiceIO io) {
        this(io);
        this.condition = condition;
        this.sampleValue = sampleValue;
        this.filterState = SAMPLE;
        this.enabled = true;
    }

    public IOValue(long id, ServiceIO io, int filterState, FilterFactory.FilterValue condition, Object manualValue, SampleValue sample, boolean enabled) {
        this(io);
        this.id = id;
        this.filterState = filterState;
        this.condition = condition;
        this.manualValue = manualValue;
        this.sampleValue = sample;
        this.enabled = enabled;
    }

    public long getID() {
        return id;
    }

    public void setID(long id) {
        this.id = id;
    }

    public int getFilterState() {
        return filterState;
    }

    public void setFilterState(int filterState) {
        this.filterState = filterState;
    }

    public FilterFactory.FilterValue getCondition() {
        return condition;
    }

    public void setCondition(FilterFactory.FilterValue condition) {
        this.condition = condition;
    }

    public Object getManualValue() {
        return manualValue;
    }

    public void setManualValue(Object manualValue) {
        this.manualValue = manualValue;
    }

    public SampleValue getSampleValue() {
        return sampleValue;
    }

    public void setSampleValue(SampleValue sampleValue) {
        this.sampleValue = sampleValue;
    }

    public ServiceIO getServiceIO() {
        return io;
    }

    public void setServiceIO(ServiceIO io) {
        this.io = io;
    }

    public boolean equals(Object o) {
        if (o == null) {
            Logger.d("IOValue->Equals: null");
            return false;
        }
        if (!(o instanceof IOValue)) {
            Logger.d("IOValue->Equals: Not an IOValue");
            return false;
        }
        IOValue other = (IOValue) o;

        if (this.id != other.getID()) {
            Logger.d("IOValue->Equals: id");
            return false;
        }

        if (this.filterState != other.getFilterState()) {
            Logger.d("IOValue->Equals: filter state");
            return false;
        }

        if (this.condition != other.getCondition()) {
            Logger.d("IOValue->Equals: condition");
            return false;
        }
        if (this.enabled != other.isEnabled()) {
            Logger.d("IOValue->Equals: enabled");
            return false;
        }

        if (this.manualValue != null) {
            if (!this.manualValue.equals(other.getManualValue())) {
                Logger.d("IOValue->Equals: manual value");
                return false;
            }
        }

        if (this.sampleValue != null) {
            if (this.sampleValue.getID() != other.getSampleValue().getID()) {
                Logger.d("IOValue->Equals: sample value");
                return false;
            }
        }

        return true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
