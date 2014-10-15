package com.appglue.engine.description;


import android.support.v4.util.LongSparseArray;
import android.util.Log;

import com.appglue.TST;
import com.appglue.description.ServiceDescription;

import java.util.ArrayList;

import static com.appglue.Constants.LOG;
import static com.appglue.Constants.TAG;

public class ComponentService {
    private long id;
    private ServiceDescription description;

    private CompositeService composite;
    private int position;

    private ArrayList<ServiceIO> inputs;
    private ArrayList<ServiceIO> outputs;

    private LongSparseArray<ServiceIO> inputSearch;
    private LongSparseArray<ServiceIO> outputSearch;

    private TST<ServiceIO> inputNameSearch;
    private TST<ServiceIO> outputNameSearch;

    private ArrayList<IOFilter> filters;
    private LongSparseArray<IOFilter> filterSearch;

    private boolean and; // Everything must have the same relation for now

    public ComponentService() {

        this.inputs = new ArrayList<ServiceIO>();
        this.outputs = new ArrayList<ServiceIO>();

        this.id = -1;
        this.description = null;
        this.composite = null;
        this.position = -1;

        this.inputSearch = new LongSparseArray<ServiceIO>();
        this.outputSearch = new LongSparseArray<ServiceIO>();

        this.inputNameSearch = new TST<ServiceIO>();
        this.outputNameSearch = new TST<ServiceIO>();

        this.filters = new ArrayList<IOFilter>();
        this.filterSearch = new LongSparseArray<IOFilter>();
    }

    public ComponentService(ServiceDescription description, int position) {
        this();
        this.description = description;

        for(int i = 0; i < description.getInputs().size(); i++) {
            ServiceIO in = new ServiceIO(this, description.getInputs().get(i));
            this.addInput(in, false);
        }

        for (int i = 0; i < description.getOutputs().size(); i++) {
            ServiceIO out = new ServiceIO(this, description.getOutputs().get(i));
            this.addOutput(out, false);
        }

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

        if (this.composite != null) {
            this.composite.rebuildSearch();
        }
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
    public ServiceIO getInput(String name) {
        return inputNameSearch.get(name);
    }
    public void addInput(ServiceIO input, boolean search) {
        this.inputs.add(input);
        this.inputNameSearch.put(input.getDescription().getName(), input);
        if(search)
            addInputSearch(input);
    }
    public void addInputSearch(ServiceIO input) {
        this.inputSearch.put(input.getID(), input);

    }

    public ArrayList<ServiceIO> getOutputs() {
        return outputs;
    }
    public ServiceIO getOutput(long id) {
        return outputSearch.get(id);
    }
    public ServiceIO getOutput(String name) {
        return outputNameSearch.get(name);
    }
    public void addOutput(ServiceIO output, boolean search) {
        this.outputs.add(output);
        this.outputNameSearch.put(output.getDescription().getName(), output);
        if(search)
            addOutputSearch(output);
    }
    public void addOutputSearch(ServiceIO output) {
        this.outputSearch.put(output.getID(), output);
    }


    public ServiceIO getIO(long id) {
        ServiceIO io = getInput(id);

        if(io == null) {
            io = getOutput(id);
        }

        return io;
    }

    public ServiceIO getIO(String name) {
        ServiceIO io = getInput(name);
        if (io == null) {
            io = getOutput(name);
        }
        return io;
    }

    public void addFilter(IOFilter filter) {
        filters.add(filter);
        if(filter.getID() != -1) {
            filterSearch.put(filter.getID(), filter);
        }

        // TODO What about adding them from the database.
    }
    public ArrayList<IOFilter> getFilters() {
        return filters;
    }
    public IOFilter getFilter(long id) {
        return filterSearch.get(id);
    }
    public boolean hasFilters() {
        return filters != null && filters.size() > 0;

    }
    public void removeFilter(IOFilter filter) {
        this.filters.remove(filter);
        this.filterSearch.remove(filter.getID());
    }

    public void setFilterCondition(boolean and) {
        this.and = and;
    }
    public boolean getFilterCondition() {
        return and;
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

        if (this.and != other.getFilterCondition()) {
            if (LOG) Log.d(TAG, "ComponentService->Equals: filter condition (and) -- " + this.and + other.getFilterCondition());
            return false;
        }

        if (this.filters.size() != other.getFilters().size()) {
            if (LOG) Log.d(TAG, "ComponentService->Equals: num filters -- " + filters.size() + " - " + other.getFilters().size());
            return false;
        }

        for (int i = 0; i < filters.size(); i++) {
            IOFilter f = other.getFilter(filters.get(i).getID());
            if(!filters.get(i).equals(f)) {
                if (LOG) Log.d(TAG, "ComponentService->Equals: filter " + i);
                return false;
            }
        }

        return true;
    }

    public void setFilters(ArrayList<IOFilter> filters) {
        this.filters = filters;
        this.filterSearch.clear();
        for (IOFilter filter : filters) {
            filterSearch.put(filter.getID(), filter);
        }
    }

    public void rebuildSearch() {

        inputNameSearch = new TST<ServiceIO>();
        outputNameSearch = new TST<ServiceIO>();

        inputSearch.clear();
        outputSearch.clear();

        filterSearch.clear();

        for (ServiceIO io : inputs) {
            if (io.getID() != -1) {
                inputSearch.put(io.getID(), io);
            }
            inputNameSearch.put(io.getDescription().getName(), io);
        }

        for (ServiceIO io : outputs) {
            if (io.getID() != -1) {
                outputSearch.put(io.getID(), io);
            }
            outputNameSearch.put(io.getDescription().getName(), io);
        }

        for (IOFilter filter : filters) {
            if (filter.getID() != -1) {
                filterSearch.put(filter.getID(), filter);
            }
        }
    }
}
