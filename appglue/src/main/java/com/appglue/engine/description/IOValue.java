package com.appglue.engine.description;

import android.util.Log;

import com.appglue.description.SampleValue;
import com.appglue.library.FilterFactory;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;

public class IOValue {

    public static final int UNFILTERED = -1;
    public static final int MANUAL_FILTER = 0;
    public static final int SAMPLE_FILTER = 1;

    private long id; // It'll be going in the database so we'll need an ID at some point

    private int filterState;

    private FilterFactory.FilterValue condition;

    private ServiceIO io;

    private Object manualValue;
    private SampleValue sampleValue;

    public IOValue(FilterFactory.FilterValue condition, Object manualValue, ServiceIO io) {
        this.condition = condition;
        this.manualValue = manualValue;
        this.io = io;
        this.filterState = MANUAL_FILTER;
    }

    public IOValue(FilterFactory.FilterValue condition, SampleValue sampleValue, ServiceIO io) {
        this.condition = condition;
        this.sampleValue = sampleValue;
        this.io = io;
        this.filterState = SAMPLE_FILTER;
    }

    public IOValue(long id, int filterState, FilterFactory.FilterValue condition, Object manualValue, SampleValue sample) {
        this.id = id;
        this.filterState = filterState;
        this.condition = condition;
        this.manualValue = manualValue;
        this.sampleValue = sample;
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
            if (LOG) Log.d(TAG, "IOValue->Equals: null");
            return false;
        }
        if (!(o instanceof IOValue)) {
            if (LOG) Log.d(TAG, "IOValue->Equals: Not an IOValue");
            return false;
        }
        IOValue other = (IOValue) o;

        if (this.id != other.getID()) {
            if (LOG) Log.d(TAG, "IOValue->Equals: id");
            return false;
        }

        if (this.filterState != other.getFilterState()) {
            if (LOG) Log.d(TAG, "IOValue->Equals: filter state");
            return false;
        }

        if (this.condition != other.getCondition()) {
            if (LOG) Log.d(TAG, "IOValue->Equals: condition");
            return false;
        }

        if (this.manualValue != null) {
            if (!this.manualValue.equals(other.getManualValue())) {
                if (LOG) Log.d(TAG, "IOValue->Equals: manual value");
                return false;
            }
        }

        if (this.sampleValue != null) {
            if (this.sampleValue.getID() != other.getSampleValue().getID()) {
                if (LOG) Log.d(TAG, "IOValue->Equals: sample value");
                return false;
            }
        }

        return true;
    }
}
