package com.appglue.engine.description;

import android.support.v4.util.LongSparseArray;

import static com.appglue.library.AppGlueConstants.OR;

import java.util.ArrayList;

public class IOFilter {

    private long id; // We need one of these for the database
    private ComponentService component; // We need a reference to the component that has the description

    private ArrayList<ServiceIO> ios; // References to the IOs that are contained within the filter
    private LongSparseArray<ValueNode> values; // References fo the value nodes

    public IOFilter(ComponentService component) {
        this.id = -1;
        this.component = component;
        values = new LongSparseArray<ValueNode>();
        ios = new ArrayList<ServiceIO>();
    }

    public void addValue(ServiceIO io, IOValue value) {
        if (values.get(io.getID()) == null) {
            // Then we need to create a new one
            ValueNode vn = new ValueNode(io);
            vn.values.add(value);
            ios.add(io);
            values.put(io.getID(), vn);
        } else {
            values.get(io.getID()).values.add(value);
        }
    }

    public ArrayList<ServiceIO> getIOs() {
        return ios;
    }

    public ArrayList<IOValue> getValues(ServiceIO io) {
        return values.get(io.getID()).values;
    }
    public boolean getCondition(ServiceIO io) {
        return values.get(io.getID()).condition;
    }

    private class ValueNode {

        private boolean condition;
        private ServiceIO io;
        private ArrayList<IOValue> values;

        private ValueNode(ServiceIO io) {
            condition = OR; // Default to OR
            this.io = io;
            this.values = new ArrayList<IOValue>();
        }

    }
}
