package com.appglue.engine.model;

import com.appglue.IODescription;
import com.appglue.description.IOType;
import com.orhanobut.logger.Logger;

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

        return !(this.value == null && this.connection == null);
    }

    /**
     * This is because there can only be one input at the moment.
     *
     * @param value the value to set
     */
    public void setValue(IOValue value) {
        this.value = value;
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

    public IOValue getValue() {
        return value;
    }

    public boolean equals(Object o) {

        if (o == null) {
            Logger.d("ServiceIO->Equals: null");
            return false;
        }
        if (!(o instanceof ServiceIO)) {
            Logger.d("ServiceIO->Equals: Not a ServiceIO");
            return false;
        }
        ServiceIO other = (ServiceIO) o;

        if (this.id != other.getID()) {
            Logger.d("ServiceIO->Equals: id");
            return false;
        }

        if (this.component.getID() != other.getComponent().getID()) {
            Logger.d("ServiceIO->Equals: component");
            return false;
        }

        if (this.ioDescription.getID() != other.getDescription().getID()) {
            Logger.d("ServiceIO->Equals: description");
            return false;
        }

        if (this.hasValue()) {
            if (!value.equals(other.getValue())) {
                Logger.d("ServiceIO->Equals: value");
                return false;
            }
        }

        if (this.connection != null || other.getConnection() != null) {
            if ((this.connection == null && other.getConnection() != null) ||
                    (this.connection != null && other.getConnection() == null)) {

                Logger.d("ServiceIO->Equals: connection null " + this.connection + " -- " + other.getConnection());
                return false;
            }

            if (this.connection.getID() != other.getConnection().getID()) {
                Logger.d("ServiceIO->Equals: connection");
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
