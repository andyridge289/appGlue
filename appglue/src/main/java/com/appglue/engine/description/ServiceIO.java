package com.appglue.engine.description;

import android.util.Log;

import com.appglue.IODescription;
import com.appglue.description.IOValue;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;

public class ServiceIO
{
    public static final int UNFILTERED = 0;
    public static final int MANUAL_FILTER = 1;
    public static final int SAMPLE_FILTER = 2;

    private long id;
    private ComponentService component;

    private IODescription ioDescription;

    private ServiceIO connection;

    private int filterState = UNFILTERED;

    private Object manualValue; // This is used for getOutputs on filtering, or its hardcoded value if its an input
    private IOValue chosenSampleValue;

    private int condition;

    public ServiceIO(ComponentService component, IODescription ioDescription) {
        this.component = component;
        this.ioDescription = ioDescription;

        if(ioDescription.isInput()) {
            component.addInput(this, false);
        } else {
            component.addOutput(this, false);
        }
    }

    public ServiceIO(long id, ComponentService component, IODescription ioDescription) {
        this.id = id;
        this.component = component;

        this.ioDescription = ioDescription;

        if(ioDescription.isInput()) {
            component.addInput(this, true);
        } else {
            component.addOutput(this, true);
        }

    }

    public long getID() {
        return id;
    }

    public void setID(long id) {

        this.id = id;

        // IF we are setting the ID we also need to update where it is in the component, if it was there at all
        if(ioDescription.isInput()) {
            component.removeInputSearch(id);
            component.addInputSearch(this);
        } else {
            component.removeOutputSearch(id);
            component.addOutputSearch(this);
        }
    }

    public IODescription getDescription() {
        return ioDescription;
    }

    public ComponentService getComponent() {
        return component;
    }

    public Object getValue() {
        if(this.getFilterState() == MANUAL_FILTER)
            return this.getManualValue();
        else if(this.getFilterState() == SAMPLE_FILTER)
            return this.getChosenSampleValue();
        else
            return null;
    }
    public boolean hasValue()
    {
        return this.manualValue != null || this.chosenSampleValue != null;
    }

    public void setManualValue(Object value) {
        this.manualValue = value;
        this.filterState = MANUAL_FILTER;
    }

    public IOValue getChosenSampleValue() {
        return chosenSampleValue;
    }
    public void setChosenSampleValue(IOValue value) {
        this.chosenSampleValue = value;
        this.filterState = SAMPLE_FILTER;
    }

    public int getFilterState()
    {
        return filterState;
    }
    public void setFilterState(int filterState)
    {
        this.filterState = filterState;
    }

    public Object getManualValue() {
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

    public ServiceIO getConnection()
    {
        return connection;
    }
    public void setConnection(ServiceIO other) {
        this.connection = other;
    }
    public boolean hasConnection() {
        return this.connection != null;
    }

    public boolean equals(Object o) {

        if (o == null) {
            if (LOG) Log.d(TAG, "ServiceIO->Equals: null");
            return false;
        }
        if (!(o instanceof ServiceIO)) {
            if (LOG) Log.d(TAG, "ServiceIO->Equals: Not a ServiceIO");
            return false;
        }
        ServiceIO other = (ServiceIO) o;

        if (this.id != other.getID()) {
            if (LOG) Log.d(TAG, "ServiceIO->Equals: id");
            return false;
        }

        if (this.component.getID() != other.getComponent().getID()) {
            if (LOG) Log.d(TAG, "ServiceIO->Equals: component");
            return false;
        }

        if (this.ioDescription.getID() != other.getDescription().getID()) {
            if (LOG) Log.d(TAG, "ServiceIO->Equals: description");
            return false;
        }

        if (this.filterState != other.getFilterState()) {
            if (LOG) Log.d(TAG, "ServiceIO->Equals: filter state");
            return false;
        }

        if (this.condition != other.getCondition()) {
            if (LOG) Log.d(TAG, "ServiceIO->Equals: condition");
            return false;
        }

        if(this.chosenSampleValue != null || other.getChosenSampleValue() != null) {

            if ((this.chosenSampleValue == null && other.getChosenSampleValue() != null) ||
                    (this.chosenSampleValue != null && other.getChosenSampleValue() == null)) {
                if (LOG)  Log.d(TAG, "ServiceIO->Equals: chosen sample null " + this.chosenSampleValue + " -- " + other.getChosenSampleValue());
                return false;
            } else if (!this.chosenSampleValue.equals(other.getChosenSampleValue())) {
                if (LOG)
                    Log.d(TAG, "ServiceIO->Equals: chosen sample value (" + ioDescription.getFriendlyName() + ")");
                return false;
            }
        }

        if (this.connection != null || other.getConnection() != null) {
            if ((this.connection == null && other.getConnection() != null) ||
                (this.connection != null && other.getConnection() == null)) {
                if (LOG) Log.d(TAG, "ServiceIO->Equals: connection null " + this.connection + " -- " + other.getConnection());
                return false;
            }

            if (this.connection.getID() != other.getConnection().getID()) {
                if (LOG) Log.d(TAG, "ServiceIO->Equals: connection");
                return false;
            }
        }

        if(this.manualValue != null || other.getManualValue() != null) {

            if((this.manualValue == null && other.getManualValue() != null) ||
                    (this.manualValue != null && other.getManualValue() == null)) {
                if (LOG) Log.d(TAG, "ServiceIO->Equals: manual value null " + this.manualValue + " -- " + other.getManualValue());
                return false;
            }

            boolean same = this.getDescription().getType().compare(this.manualValue, other.getManualValue());
            if (!same) {
                if (LOG) Log.d(TAG, "ServiceIO->Equals: manual");
                return false;
            }
        }

        return true;
    }
}
