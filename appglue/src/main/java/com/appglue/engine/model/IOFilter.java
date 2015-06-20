package com.appglue.engine.model;

import com.appglue.TST;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import static com.appglue.library.AppGlueConstants.OR;

public class IOFilter {

    private long id; // We need one of these for the database
    private ComponentService component; // We need a reference to the component that has the description

    private ArrayList<ServiceIO> ios; // References to the IOs that are contained within the filter

    private TST<ValueNode> values; // References fo the value nodes

    private boolean enabled;

    public IOFilter(ComponentService component) {
        this.id = -1;
        this.component = component;
        values = new TST<>();
        ios = new ArrayList<>();
        this.enabled = true;
    }

    public IOFilter(long id, ComponentService component) {
        this(component);
        this.id = id;
    }

    public void addValue(ServiceIO io, IOValue value) {

        String key = io.getDescription().getName();
        value.setServiceIO(io);

        if (values.get(key) == null) {
            // Then we need to create a new one
            ValueNode vn = new ValueNode(io, this);
            vn.values.add(value);
            ios.add(io);
            values.put(key, vn);
        } else {
            values.get(key).values.add(value);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ArrayList<ServiceIO> getIOs() {
        return ios;
    }

    public ArrayList<IOValue> getValues(ServiceIO io) {

        String key = io.getDescription().getName();
        ValueNode vn = values.get(key);

        if (vn == null) {
            Logger.d("Null for " + key);
            return new ArrayList<>();
        }

        return vn.values;
    }

    public boolean hasValues(ServiceIO io) {
        ValueNode vn = values.get(io.getDescription().getName());
        return vn != null;
    }

    public boolean getCondition(ServiceIO io) {

        ValueNode vn = values.get(io.getDescription().getName());
        if(vn != null) {
            return vn.condition;
        }

        return OR;
    }

    public long getID() {
        return id;
    }

    public ComponentService getComponent() {
        return component;
    }

    public TST<ValueNode> getValues() {
        return values;
    }

    public boolean equals(Object o) {

        if (o == null) {
           Logger.d("IOFilter->Equals: null");
            return false;
        }
        if (!(o instanceof IOFilter)) {
           Logger.d("IOFilter->Equals: Not a ComponentService");
            return false;
        }
        IOFilter other = (IOFilter) o;

        if (this.id != other.getID()) {
           Logger.d("IOFilter->Equals: id");
            return false;
        }

        if (this.component.getID() != other.getComponent().getID()) {
           Logger.d("IOFilter->Equals: component");
            return false;
        }

        if (this.ios.size() != other.getIOs().size()) {
           Logger.d("IOFilter->Equals: ios size: " + ios.size() + " -- " + other.getIOs().size());
            return false;
        }

        for (int i = 0; i < ios.size(); i++) {
            long id = ios.get(i).getID();
            boolean found = false;
            for (int j = 0; j < other.getIOs().size(); j++) {
                if (other.getIOs().get(j).getID() == id) {
                    found = true;
                    break;
                }
            }

            if (!found) {
               Logger.d("IOFilter->Equals: Can't find IO: " + i);
                return false;
            }
        }

        if (values.size() != other.getValues().size()) {
           Logger.d("IOFilter->Equals: value nodes size: " + values.size() + " -- " + other.getValues().size());
            return false;
        }

        ArrayList<String> keys = values.getKeys();
        for (String key : keys) {
            ValueNode vn = values.get(key);
            if (!vn.equals(other.getValues().get(key))) {
               Logger.d("ComponentService->Equals: value node " + key);
                return false;
            }
        }

        return true;
    }

    public void setID(long id) {
        this.id = id;
    }

    public ValueNode getNodeOrCreate(long id, boolean condition, ServiceIO io) {
        String key = io.getDescription().getName();
        if (values.get(key) == null) {
            ValueNode vn = new ValueNode(id, this, condition, io);
            if (!ios.contains(io)) {
                ios.add(io);
            }
            values.put(key, vn);
            return vn;
        } else {
            return values.get(key);
        }
    }

    public void setCondition(ServiceIO io, boolean condition) {
        ValueNode vn = values.get(io.getDescription().getName());
        if (vn != null) {
            vn.condition = condition;
        }
    }

    public class ValueNode {

        private long id;
        private boolean condition;

        private IOFilter filter;
        private ServiceIO io;

        private ArrayList<IOValue> values;

        private ValueNode(ServiceIO io, IOFilter filter) {
            this.id = -1;
            condition = OR; // Default to OR
            this.io = io;
            this.values = new ArrayList<>();
            this.filter = filter;
        }

        public ValueNode(long id, IOFilter ioFilter, boolean condition, ServiceIO io) {
            this(io, ioFilter);
            this.id = id;
            this.condition = condition;
        }

        public boolean equals(Object o) {
            if (o == null) {
               Logger.d("ValueNode->Equals: null");
                return false;
            }
            if (!(o instanceof ValueNode)) {
               Logger.d("ValueNode->Equals: Not a ComponentService");
                return false;
            }
            ValueNode other = (ValueNode) o;

            if (this.id != other.id) {
               Logger.d("ValueNode->Equals: id");
                return false;
            }

            if (this.condition != other.condition) {
               Logger.d("ValueNode->Equals: id");
                return false;
            }

            if (this.filter.getID() != other.getFilter().getID()) {
               Logger.d("ValueNode->Equals: filter");
                return false;
            }

            if (this.io.getID() != other.getIO().getID()) {
               Logger.d("ValueNode->Equals: io");
                return false;
            }

            if (this.values.size() != other.getValues().size()) {
               Logger.d("ValueNode[" + id + "]->Equals: value size " + values.size() + " -- " + other.getValues().size());
                return false;
            }

            for (int i = 0; i < values.size(); i++) {
                IOValue value = values.get(i);
                if (!other.getValues().contains(value)) {
                   Logger.d("ValueNode->Equals: value " + i + " not found in other");
                    return false;
                }
            }

            return true;
        }

        public long getID() {
            return id;
        }

        public boolean getCondition() {
            return condition;
        }

        public ServiceIO getIO() {
            return io;
        }

        public void setID(long id) {
            this.id = id;

            if (component != null) {
                component.rebuildSearch();
            }
        }

        public void add(IOValue value) {
            values.add(value);
        }

        public ArrayList<IOValue> getValues() {
            return values;
        }

        public IOFilter getFilter() {
            return filter;
        }

        @Override
        public String toString() {
            return String.format("[%d]: %b filter(%s) IO(%s)", id, condition, filter, io);
        }
    }
}
