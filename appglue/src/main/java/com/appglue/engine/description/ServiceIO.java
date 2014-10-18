package com.appglue.engine.description;

import android.util.Log;

import com.appglue.IODescription;
import com.appglue.description.datatypes.IOType;
import com.appglue.library.FilterFactory;

import java.util.ArrayList;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;

public class ServiceIO {
    private long id;
    private ComponentService component;

    private IODescription ioDescription;

    private ServiceIO connection;

    private IOValue value;

    public ServiceIO(ComponentService component, IODescription ioDescription) {
        this.component = component;
        this.ioDescription = ioDescription;
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
        if (component != null) {
            component.rebuildSearch();
        }
    }

    public IODescription getDescription() {
        return ioDescription;
    }

    public ComponentService getComponent() {
        return component;
    }

    public boolean hasValue() {
        return this.value != null;
    }
    public boolean hasValueOrConnection() {

        if(this.value == null && this.connection == null) {
            return false;
        } else {
            // One of them is set
            return true;
        }
    }

    /**
     * This is because there can only be one input at the moment.
     *
     * @param value the value to set
     */
    public void setValue(IOValue value) {
        this.value = value;
    }

    public FilterFactory.FilterValue getCondition() {
        if (this.value == null) {
            return FilterFactory.NONE;
        }

        return value.getCondition();
    }

    public ServiceIO getConnection() {
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

    public IOValue getValue() {
        return value;
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

        if (!other.getValue().equals(value)) {
            if (LOG) Log.d(TAG, "ServiceIO->Equals: value");
            return false;
        }


        if (this.connection != null || other.getConnection() != null) {
            if ((this.connection == null && other.getConnection() != null) ||
                (this.connection != null && other.getConnection() == null)) {
                if (LOG)
                    Log.d(TAG, "ServiceIO->Equals: connection null " + this.connection + " -- " + other.getConnection());
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

    public void clearValue() {
        this.value = null;
    }
}
