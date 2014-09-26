package com.appglue.engine.description;

import android.util.Log;

import com.appglue.IODescription;
import com.appglue.description.SampleValue;
import com.appglue.description.datatypes.IOType;
import com.appglue.library.IOFilter;

import java.util.ArrayList;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;

public class ServiceIO
{


    private long id;
    private ComponentService component;

    private IODescription ioDescription;

    private ServiceIO connection;

    private ArrayList<IOValue> values;

    public ServiceIO(ComponentService component, IODescription ioDescription) {
        this.component = component;
        this.ioDescription = ioDescription;
        this.values = new ArrayList<IOValue>();
    }

    public ServiceIO(long id, ComponentService component, IODescription ioDescription) {
        this(component, ioDescription);
        this.id = id;
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
        if (this.values.size() == 0) {
            return null;
        }

        // Just use the first one for now, this method will need to be changed when we enable multiple filters, etc.
        IOValue value = values.get(0);
        if (value.getFilterState() == IOValue.MANUAL_FILTER) {
            return value.getManualValue();
        } else if (value.getFilterState() == IOValue.SAMPLE_FILTER) {
            return value.getSampleValue().getValue();
        }

        // If we get here something has gone a bit wrong
        return null;
    }

    public boolean hasValue() {
        return this.values.size() != 0;
    }

    /**
     * There can be as many filters as you like
     *
     * @param value
     */
    public void addFilter(IOValue value) {
        this.values.add(value);
    }

    /**
     * This is because there can only be one input at the moment.
     *
     * @param value
     */
    public void setValue(IOValue value) {
        this.values.clear();
        this.values.add(value);
    }

    public void setManualValue(Object value) {
        this.values.add(new IOValue(IOFilter.NONE, value, this));
    }

    public SampleValue getChosenSampleValue() {
        if (this.values.size() == 0)
            return null;

        IOValue value = values.get(0);

        if (value.getFilterState() == IOValue.SAMPLE_FILTER)
            return value.getSampleValue();

        return null;
    }

    public void setChosenSampleValue(SampleValue value) {
        this.values.add(new IOValue(IOFilter.NONE, value, this));
    }

    public int getFilterState() {
        if (this.values.isEmpty()) {
            return IOValue.UNFILTERED;
        }

        return values.get(0).getFilterState();
    }

    public IOFilter.FilterValue getCondition() {
        if (this.values.isEmpty()) {
            return IOFilter.NONE;
        }

        return values.get(0).getCondition();
    }

    public Object getManualValue() {
        if (this.values.size() == 0)
            return null;

        IOValue value = values.get(0);

        if (value.getFilterState() == IOValue.MANUAL_FILTER)
            return value.getManualValue();

        return null;
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

    public IOType getType() {
        return this.getDescription().getType();
    }

    public String getTypeName() {
        return getType().getClassName();
    }

    public ArrayList<IOValue> getValues() {
        return values;
    }

    public void setValues(ArrayList<IOValue> values) {
        this.values = values;
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

        if (this.values.size() != other.getValues().size()) {
            if (LOG)
                Log.d(TAG, "ServiceIO->Equals: value size: " + this.values.size() + " -- " + other.getValues().size());
            return false;
        }

        for (int i = 0; i < values.size(); i++) {
            IOValue value = values.get(i);
            if (!other.getValues().contains(value)) {
                if (LOG) Log.d(TAG, "ServiceIO->Equals: value: " + i);
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

        return true;
    }

    public String toString() {
        return this.getID() + ": " + this.getDescription().getName();
    }
}
