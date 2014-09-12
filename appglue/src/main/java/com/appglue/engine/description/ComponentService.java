package com.appglue.engine.description;


import android.support.v4.util.LongSparseArray;

import com.appglue.description.ServiceDescription;

import java.util.ArrayList;

public class ComponentService
{
    private long id;
    private ServiceDescription description;

    private CompositeService composite;
    private int position;

    private ArrayList<ServiceIO> inputs;
    private ArrayList<ServiceIO> outputs;

    private LongSparseArray<ServiceIO> inputSearch;
    private LongSparseArray<ServiceIO> outputSearch;

    public ComponentService() {

        this.inputs = new ArrayList<ServiceIO>();
        this.outputs = new ArrayList<ServiceIO>();

        this.id = -1;
        this.description = null;
        this.composite = null;
        this.position = -1;
        this.inputSearch = new LongSparseArray<ServiceIO>();
        this.outputSearch = new LongSparseArray<ServiceIO>();
    }

    public ComponentService(ServiceDescription description, int position) {
        this();
        this.description = description;
        this.position = position;
    }

    public void setComposite(CompositeService composite) {
        this.composite = composite;
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
