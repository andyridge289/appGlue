package com.appglue.engine.description;


import android.support.v4.util.LongSparseArray;

import com.appglue.description.ServiceDescription;

import java.util.ArrayList;

public class ComponentService
{
    private long id;
    private ServiceDescription description;

    private long compositeId;
    private int position;

    private ArrayList<ServiceIO> inputs;
    private ArrayList<ServiceIO> outputs;

    private LongSparseArray<ServiceIO> inputSearch;
    private LongSparseArray<ServiceIO> outputSearch;

    public ComponentService(ServiceDescription description) {
        this.description = description;
    }

    public long id() {
        return id;
    }

    public ServiceDescription getDescription() {
        return description;
    }

    public ArrayList<ServiceIO> getInputs() {
        return inputs;
    }

    public ServiceIO getInput(long id) {
        return inputSearch.get(id);
    }

    public ArrayList<ServiceIO> getOutputs() {
        return outputs;
    }

    public ServiceIO getOutput(long id) {
        return outputSearch.get(id);
    }

    public ServiceIO getIO(long id) {
        ServiceIO io = getInput(id);

        if(io == null) {
            io = getOutput(id);
        }

        return io;
    }

    public boolean hasIncomingLinks() {
        for (int i = 0; i < inputs.size(); i++) {
            if (inputs.get(i).connection() != null)
                return true;
        }

        return false;
    }

    public boolean hasOutgoingLinks() {
        for (int i = 0; i < outputs.size(); i++) {
            if (outputs.get(i).connection() != null)
                return true;
        }

        return false;
    }
}
