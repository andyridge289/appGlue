package com.appglue.engine.description;


import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import com.appglue.Constants;
import com.appglue.description.ServiceDescription;

import java.util.ArrayList;

import static com.appglue.Constants.DESCRIPTION;
import static com.appglue.Constants.ID;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.NAME;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.ENABLED;
import static com.appglue.library.AppGlueConstants.INTERVAL;
import static com.appglue.library.AppGlueConstants.NUMERAL;

public class ComponentService {
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

    public ComponentService(CompositeService composite, ServiceDescription sd, int position) {
        this(sd, position);
        this.composite = composite;
    }

    public ComponentService(long id, ServiceDescription sd, CompositeService cs, int position) {
        this(cs, sd, position);
        this.id = id;
    }

    public CompositeService getComposite() {
        return composite;
    }

    public void setComposite(CompositeService composite) {
        this.composite = composite;
    }

    public long getID() {
        return id;
    }
    public void setID(long id) {
        this.id = id;
    }

    public int getPosition() {
        return position;
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
    public void addInput(ServiceIO input, boolean search) {
        this.inputs.add(input);

        if(search)
            addInputSearch(input);
    }
    public void addInputSearch(ServiceIO input) {
        this.inputSearch.put(input.getID(), input);
    }
    public void removeInputSearch(long id) {
        if(this.inputSearch.get(id) != null) {
            this.inputSearch.delete(id);
        }
    }

    public ArrayList<ServiceIO> getOutputs() {
        return outputs;
    }
    public ServiceIO getOutput(long id) {
        return outputSearch.get(id);
    }
    public void addOutput(ServiceIO output, boolean search) {
        this.outputs.add(output);

        if(search)
            addOutputSearch(output);
    }
    public void addOutputSearch(ServiceIO output) {
        this.outputSearch.put(output.getID(), output);
    }
    public void removeOutputSearch(long id) {
        if(this.outputSearch.get(id) != null) {
            this.outputSearch.delete(id);
        }
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
            if (inputs.get(i).getConnection() != null)
                return true;
        }

        return false;
    }

    public boolean hasOutgoingLinks() {
        for (int i = 0; i < outputs.size(); i++) {
            if (outputs.get(i).getConnection() != null)
                return true;
        }

        return false;
    }

    public boolean equals(Object o) {

        if(o == null) {
            if(LOG) Log.d(TAG, "ComponentService->Equals: null");
            return false;
        }
        if(!(o instanceof ComponentService)) {
            if (LOG) Log.d(TAG, "ComponentService->Equals: Not a ComponentService");
            return false;
        }
        ComponentService other = (ComponentService) o;

        if(this.id != other.getID()) {
            if (LOG) Log.d(TAG, "ComponentService->Equals: id");
            return false;
        }

        if(!this.description.equals(other.getDescription())) {
            if (LOG) Log.d(TAG, "ComponentService->Equals: description");
            return false;
        }

        if(this.composite.getID() != other.getComposite().getID()) {
            if (LOG) Log.d(TAG, "ComponentService->Equals: composite");
            return false;
        }

        if(this.position != other.getPosition()) {
            if (LOG) Log.d(TAG, "ComponentService->Equals: position");
            return false;
        }

        if(this.inputs.size() != other.getInputs().size()) {
            if (LOG) Log.d(TAG, "ComponentService->Equals: num inputs -- " + inputs.size() + " - " + other.getInputs().size());
            return false;
        }

        for(int i = 0; i < inputs.size(); i++) {
            ServiceIO io = this.inputs.get(i);
            if(!io.equals(other.getInput(io.getID()))) {
                if (LOG) Log.d(TAG, "ComponentService->Equals: input " + i);
                return false;
            }
        }

        if(this.outputs.size() != other.getOutputs().size()) {
            if (LOG) Log.d(TAG, "ComponentService->Equals: num outputs " + outputs.size() + " - " + other.getOutputs().size());
            return false;
        }

        for(int i = 0; i < outputs.size(); i++) {
            ServiceIO io = this.outputs.get(i);
            if(!io.equals(other.getOutput(io.getID()))) {
                if (LOG) Log.d(TAG, "ComponentService->Equals: output " + i);
                return false;
            }
        }

        return true;
    }
}
