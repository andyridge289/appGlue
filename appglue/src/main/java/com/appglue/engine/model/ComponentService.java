package com.appglue.engine.model;


import android.support.v4.util.LongSparseArray;

import com.appglue.TST;
import com.appglue.description.ServiceDescription;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;

public class ComponentService {
    private long id;
    private ServiceDescription description;

    private CompositeService composite;
    private int mPosition;

    private ArrayList<ServiceIO> inputs;
    private ArrayList<ServiceIO> outputs;

    private LongSparseArray<ServiceIO> inputSearch;
    private LongSparseArray<ServiceIO> outputSearch;

    private TST<ServiceIO> inputNameSearch;
    private TST<ServiceIO> outputNameSearch;

    private ArrayList<IOFilter> mFilters;
    private LongSparseArray<IOFilter> mFilterSearch;

    private boolean and; // Everything must have the same relation for now

    private ArrayList<Observer> mObservers;
    private final Object mObserverLock = new Object();

    public interface Observer {
        void onComponentPositionChanged(int oldPosition, int newPosition);
        void onFilterAdded(IOFilter filter);
        void onFilterRemoved(IOFilter filer);
    }

    public ComponentService() {

        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();

        this.id = -1;
        this.description = null;
        this.composite = null;
        this.mPosition = -1;

        this.inputSearch = new LongSparseArray<>();
        this.outputSearch = new LongSparseArray<>();

        this.inputNameSearch = new TST<>();
        this.outputNameSearch = new TST<>();

        this.mFilters = new ArrayList<>();
        this.mFilterSearch = new LongSparseArray<>();

        mObservers = new ArrayList<>();
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

        this.mPosition = position;
    }

    public ComponentService(CompositeService composite, ServiceDescription sd, int position) {
        this(sd, position);
        this.composite = composite;
    }

    public ComponentService(long id, ServiceDescription sd, CompositeService cs, int position) {
        this(cs, sd, position);
        this.id = id;
    }

    public void addObserver(Observer o) {
        synchronized (mObserverLock) {
            if (!mObservers.contains(o)) {
                mObservers.add(o);
            }
        }
    }

    public void removeObserver(Observer o) {
        synchronized (mObserverLock) {
            if (mObservers.contains(o)) {
                mObservers.remove(o);
            }
        }
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
        return mPosition;
    }

    public ServiceDescription getDescription() {
        return description;
    }

    public boolean hasInputs() {

        for (int i = 0; i < this.inputs.size(); i++) {
            if (inputs.get(i).hasConnection()) {
                return true;
            }
        }

        return false;
    }
    public ArrayList<ServiceIO> getInputs() {
        return inputs;
    }

    public ArrayList<ServiceIO> getDisconnectedInputs() {
        ArrayList<ServiceIO> in = new ArrayList<>();

        // Hopefully this should preserve the order
        for (ServiceIO input : inputs) {
            if (input.getConnection() == null && input.getValue() == null) {
                in.add(input);
            }
        }

        return in;
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

    public boolean hasOutputs() {

        for (int i = 0; i < this.outputs.size(); i++) {
            if (outputs.get(i).hasConnection()) {
                return true;
            }
        }

        return false;
    }
    public ArrayList<ServiceIO> getOutputs() {
        return outputs;
    }

    public ArrayList<ServiceIO> getDisconnectedOutputs() {
        ArrayList<ServiceIO> out = new ArrayList<>();

        // Hopefully this should preserve the order
        for (ServiceIO output : outputs) {
            if (output.getConnection() == null) {
                out.add(output);
            }
        }

        return out;
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
        mFilters.add(filter);
        if(filter.getID() != -1) {
            mFilterSearch.put(filter.getID(), filter);
        }

        synchronized (mObserverLock) {
            for(Observer o : mObservers) {
                o.onFilterAdded(filter);
            }
        }
    }
    public ArrayList<IOFilter> getFilters() {
        return mFilters;
    }
    public IOFilter getFilter(long id) {
        return mFilterSearch.get(id);
    }
    public boolean hasFilters() {
        return mFilters != null && mFilters.size() > 0;
    }
    public void removeFilter(IOFilter filter) {
        this.mFilters.remove(filter);
        this.mFilterSearch.remove(filter.getID());

        synchronized (mObserverLock) {
            for (Observer o : mObservers) {
                o.onFilterRemoved(filter);
            }
        }
    }

    public void setPosition(int position) {
        int oldPosition = this.mPosition;
        this.mPosition = position;

        synchronized (mObserverLock) {
            for (Observer o : mObservers) {
                o.onComponentPositionChanged(oldPosition, mPosition);
            }
        }
    }

    public void setFilterCondition(boolean and) {
        this.and = and;
    }
    public boolean getFilterCondition() {
        return and;
    }
    public boolean equals(Object o) {

        if(o == null) {
            Logger.d("ComponentService->Equals: null");
            return false;
        }
        if(!(o instanceof ComponentService)) {
            Logger.d("ComponentService->Equals: Not a ComponentService");
            return false;
        }
        ComponentService other = (ComponentService) o;

        if(this.id != other.getID()) {
            Logger.d("ComponentService->Equals: id");
            return false;
        }

        if(!this.description.equals(other.getDescription())) {
            Logger.d("ComponentService->Equals: description");
            return false;
        }

        if(this.composite.getID() != other.getComposite().getID()) {
            Logger.d("ComponentService->Equals: composite");
            return false;
        }

        if(this.mPosition != other.getPosition()) {
            Logger.d("ComponentService->Equals: mPosition");
            return false;
        }

        if(this.inputs.size() != other.getInputs().size()) {
            Logger.d("ComponentService->Equals: num inputs -- " + inputs.size() + " - " + other.getInputs().size());
            return false;
        }

        for(int i = 0; i < inputs.size(); i++) {
            ServiceIO io = this.inputs.get(i);
            if(!io.equals(other.getInput(io.getID()))) {
                Logger.d("ComponentService->Equals: input " + i);
                return false;
            }
        }

        if(this.outputs.size() != other.getOutputs().size()) {
            Logger.d("ComponentService->Equals: num outputs " + outputs.size() + " - " + other.getOutputs().size());
            return false;
        }

        for(int i = 0; i < outputs.size(); i++) {
            ServiceIO io = this.outputs.get(i);
            if(!io.equals(other.getOutput(io.getID()))) {
                Logger.d("ComponentService->Equals: output " + i);
                return false;
            }
        }

        if (this.and != other.getFilterCondition()) {
            Logger.d("ComponentService->Equals: filter condition (and) -- " + this.and + other.getFilterCondition());
            return false;
        }

        if (this.mFilters.size() != other.getFilters().size()) {
            Logger.d("ComponentService->Equals: num mFilters -- " + mFilters.size() + " - " + other.getFilters().size());
            return false;
        }

        for (int i = 0; i < mFilters.size(); i++) {
            IOFilter f = other.getFilter(mFilters.get(i).getID());
            if(!mFilters.get(i).equals(f)) {
                Logger.d("ComponentService->Equals: filter " + i);
                return false;
            }
        }

        return true;
    }

    public void setFilters(ArrayList<IOFilter> filters) {
        this.mFilters = filters;
        this.mFilterSearch.clear();
        for (IOFilter filter : filters) {
            mFilterSearch.put(filter.getID(), filter);
        }
    }

    public void rebuildSearch() {

        inputNameSearch = new TST<>();
        outputNameSearch = new TST<>();

        inputSearch.clear();
        outputSearch.clear();

        mFilterSearch.clear();

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

        for (IOFilter filter : mFilters) {
            if (filter.getID() != -1) {
                mFilterSearch.put(filter.getID(), filter);
            }
        }
    }
}
