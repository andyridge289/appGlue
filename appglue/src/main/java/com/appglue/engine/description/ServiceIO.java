package com.appglue.engine.description;

import com.appglue.IODescription;
import com.appglue.description.IOValue;

public class ServiceIO
{
    public static final int UNFILTERED = 0;
    public static final int MANUAL_FILTER = 1;
    public static final int SAMPLE_FILTER = 2;

    private long id;
    private ComponentService parentComponent;

    private IODescription io;

    private ServiceIO connection;

    private int filterState = UNFILTERED;

    private Object manualValue; // This is used for outputs on filtering, or its hardcoded value if its an input
    private IOValue chosenSampleValue;

    private int condition;

    public ServiceIO(long id, ComponentService parentComponent, IODescription io) {
        this.id = id;
        this.parentComponent = parentComponent;
        this.io = io;
    }

    public long id() {
        return id;
    }

    public IODescription description() {
        return io;
    }

    public ComponentService component() {
        return parentComponent;
    }

    public Object getValue()
    {
        if(this.isFiltered() == MANUAL_FILTER)
            return this.getManualValue();
        else if(this.isFiltered() == SAMPLE_FILTER)
            return this.getChosenSampleValue();
        else
            return null;
    }

    public void setManualValue(Object value)
    {
        this.manualValue = value;
        this.filterState = MANUAL_FILTER;
    }

    public IOValue getChosenSampleValue()
    {
        if(chosenSampleValue == null)
            return new IOValue();

        return chosenSampleValue;
    }

    public void setChosenSampleValue(IOValue value)
    {
        this.chosenSampleValue = value;
        this.filterState = SAMPLE_FILTER;
    }

    public int isFiltered()
    {
        return filterState;
    }

    public void setFilterState(int filterState)
    {
        this.filterState = filterState;
    }

    public Object getManualValue()
    {
        if(manualValue == null)
            return "";

        return manualValue;
    }

    public int getCondition()
    {
        return condition;
    }

    public void setCondition(int condition)
    {
        this.condition = condition;
    }

    public boolean hasValue()
    {
        return this.manualValue != null || this.chosenSampleValue != null;
    }

    public ServiceIO connection()
    {
        return connection;
    }

    public void setConnection(ServiceIO connection)
    {
        this.connection = connection;
    }
}
