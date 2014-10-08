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

    public static final int COMBO_OR = 1;
    public static final int COMBO_AND = 2;

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
        if (ioDescription.isInput()) {
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

    public boolean hasValues() {
        return this.values.size() != 0;
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

    public FilterFactory.FilterValue getCondition() {
        if (this.values.isEmpty()) {
            return FilterFactory.NONE;
        }

        return values.get(0).getCondition();
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
}
