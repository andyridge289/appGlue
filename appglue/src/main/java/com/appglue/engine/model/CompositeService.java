package com.appglue.engine.model;

import android.database.Cursor;
import android.support.v4.util.LongSparseArray;
import android.util.Log;
import android.util.SparseArray;

import com.appglue.ComposableService;
import com.appglue.R;

import java.util.ArrayList;
import java.util.Arrays;

import static com.appglue.Constants.DESCRIPTION;
import static com.appglue.Constants.ID;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.NAME;
import static com.appglue.Constants.TAG;
import static com.appglue.library.AppGlueConstants.ENABLED;

public class CompositeService {

    private long id;
    private String name;
    private String description;

    private boolean enabled;

    public static final int TEMP_ID = 1;
    public static final String TEMP_NAME = "temp";
    public static final String TEMP_DESCRIPTION = "This is the temporary composite";

    private SparseArray<ComponentService> components;
    private LongSparseArray<ComponentService> componentSearch;
    private final Object mComponentLock = new Object();

    private ArrayList<Observer> mObservers;
    private final Object mObserverLock = new Object();

    public interface Observer {
        public void onComponentAdded(ComponentService component, int position);
        public void onComponentRemoved(ComponentService component);
        public void onComponentsSwapped(ComponentService first, ComponentService second,
                                        int firstNewPosition, int secondNewPosition);
        public void onStartExecute();
        public void onStopExecute();
    }

    public CompositeService() {
        this.id = -1;
        this.name = "";
        this.description = "";
        this.enabled = true;
        this.components = new SparseArray<ComponentService>();
        this.componentSearch = new LongSparseArray<ComponentService>();
        mObservers = new ArrayList<Observer>();
    }

    public CompositeService(boolean temp) {
        this();
        if (temp) {
            this.id = TEMP_ID;
            this.name = TEMP_NAME;
            this.description = TEMP_DESCRIPTION;
        }
    }

    public CompositeService(String name, String description, ArrayList<ComponentService> services) {
        this(false);
        this.name = name;
        this.description = description;
        this.components = new SparseArray<ComponentService>();

        if (services != null) {
            for (int i = 0; i < services.size(); i++) {
                components.put(i, services.get(i));
                services.get(i).setComposite(this);
            }

            for (ComponentService comps : services) {
                this.componentSearch.put(comps.getID(), comps);
            }
        }
    }

    public CompositeService(long id, String name, String description, boolean enabled) {
        this(false);
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
    }

    public CompositeService(String name, String description, SparseArray<ComponentService> services, boolean enabled) {
        this(false);
        this.id = -1;
        this.name = name;
        this.components = services;
        this.description = description;
        this.enabled = enabled;

        if (services != null) {
            for (int i = 0; i < services.size(); i++) {
                ComponentService component = services.valueAt(i);
                componentSearch.put(component.getID(), component);
                services.get(i).setComposite(this);
            }
        }
    }

    public CompositeService(ArrayList<ComponentService> services) {
        this(false);
        // Generate a random name
        this.id = -1;
        this.name = "Random Service";

        if (services != null) {
            this.components = new SparseArray<ComponentService>();
            for (int i = 0; i < services.size(); i++) {
                ComponentService cs = services.get(i);
                components.put(i, cs);
                cs.setComposite(this);
                this.componentSearch.put(cs.getID(), cs);
            }
        }

        this.enabled = false;
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

    /**
     * When the ID of a component changes, we need to rebuild the search array
     */
    public void rebuildSearch() {
        synchronized (mComponentLock) {
            this.componentSearch.clear();
            for (int i = 0; i < components.size(); i++) {
                ComponentService component = components.valueAt(i);
                if (component.getID() != -1) {
                    componentSearch.put(component.getID(), component);
                }
            }
        }
    }

    public ComponentService getComponent(long id) {
        synchronized (mComponentLock) {
            return this.componentSearch.get(id);
        }
    }
    public ComponentService getComponent(int position) {
        synchronized (mComponentLock) {
            if (this.components == null) {
                return null;
            }

            if (components.size() < position) {
                return null;
            }

            return components.get(position);
        }
    }

    public static final int PRE_CONNECTIONS = 0x1;
    public static final int POST_CONNECTIONS = 0x2;
    public static final int FILTERS = 0x4;

    public int isMovable(int index) {

        int result = 0;

        ComponentService component = components.get(index);

        if (component.hasInputs()) {
            result |= PRE_CONNECTIONS;
        }

        if (component.hasOutputs()) {
            result |= POST_CONNECTIONS;
        }

        if (component.hasFilters()) {
            result |= FILTERS;
        }

        return result;
    }

    public void remove(int index) {


        remove(components.get(index));
    }
    public void remove(ComponentService component) {
        synchronized (mComponentLock) {
            this.components.remove(component.getPosition());
            this.componentSearch.remove(component.getID());

            // Remove all of the connections for this and others
            for (ServiceIO input : component.getInputs()) {
                if (input.hasConnection()) {
                    ServiceIO other = input.getConnection();
                    other.setConnection(null);
                    input.setConnection(null);
                }
            }

            // Remove all of the connections for this and others
            for (ServiceIO output : component.getOutputs()) {
                if (output.hasConnection()) {
                    ServiceIO other = output.getConnection();
                    other.setConnection(null);
                    output.setConnection(null);
                }
            }
        }

        int position = component.getPosition();
        synchronized (mObserverLock) {
            for (Observer o : mObservers) {
                o.onComponentRemoved(component);
            }
        }

        for (int i = position + 1; i < components.size(); i++) {
            ComponentService toMove = components.get(i);
            components.put(i - 1, components.get(i));
            toMove.setPosition(i - 1);
            Log.d(TAG, "Putting " + (i) + " in " + (i - 1));
        }

        this.components.remove(position);
        this.componentSearch.remove(component.getID());
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void addComponent(ComponentService component, int position) {

        synchronized (mComponentLock) {
            if (components.get(position) == null) {
                // If there isn't a component at that position, then add one
                components.put(position, component);
                component.setComposite(this);
                if (component.getID() != -1)
                    componentSearch.put(component.getID(), component);

            } else {
                // If there is a component at that position, we need to move everything back
                ComponentService replacee = components.get(position);
                components.put(position, component);
                addComponent(replacee, position + 1);
            }
        }

        synchronized (mObserverLock) {
            for (Observer o : mObservers) {
                o.onComponentAdded(component, position);
            }
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getID() {
        return id;
    }

    public void setID(long id) {
        this.id = id;
    }

    public ArrayList<ComponentService> getComponentsAL() {
        ComponentService[] comps = new ComponentService[components.size()];
        for (int i = 0; i < components.size(); i++) {
            int k = components.keyAt(i);
            ComponentService v = components.get(k);
            comps[k] = v;
        }

        return new ArrayList<ComponentService>(Arrays.asList(comps));
    }

    public SparseArray<ComponentService> getComponents() {
        return components;
    }

    public ArrayList<ServiceIO> getMandatoryInputs() {

        ArrayList<ServiceIO> mandatories = new ArrayList<ServiceIO>();

        for (int i = 0; i < components.size(); i++) {
            ComponentService component = components.valueAt(i);

            for (ServiceIO io : component.getInputs()) {
                if (io.getDescription().isMandatory()) {
                    mandatories.add(io);
                }
            }
        }

        return mandatories;
    }

    public ArrayList<ComponentService> getComponents(String className) {
        ArrayList<ComponentService> matching = new ArrayList<ComponentService>();
        for (int i = 0; i < components.size(); i++) {
            ComponentService component = components.valueAt(i);
            if (component.getDescription().getClassName().equals(className))
                matching.add(component);
        }

        return matching;
    }

    public void setComponents(SparseArray<ComponentService> components) {
        this.components = components;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean containsTrigger() {
        return this.components.get(0).getDescription().hasFlag(ComposableService.FLAG_TRIGGER);
    }

    public ServiceIO getInput(long id) {
        for (int i = 0; i < components.size(); i++) {
            ServiceIO in = components.valueAt(i).getInput(id);
            if (in != null)
                return in;
        }

        return null;
    }

    public ServiceIO getOutput(long id) {
        for (int i = 0; i < components.size(); i++) {
            ServiceIO in = components.valueAt(i).getOutput(id);
            if (in != null)
                return in;
        }

        return null;
    }

    public void setInfo(String prefix, Cursor c) {

        this.id = c.getLong(c.getColumnIndex(prefix + ID));
        this.name = c.getString(c.getColumnIndex(prefix + NAME));
        this.description = c.getString(c.getColumnIndex(prefix + DESCRIPTION));

        this.enabled = c.getInt(c.getColumnIndex(prefix + ENABLED)) == 1;
    }

    public int size() {
        return components.size();
    }

    public void swap(int a, int b) {

        Log.d(TAG, String.format("Pre-swap: %s -> %s", components.get(a).getDescription().getName(),
                                                       components.get(b).getDescription().getName()));

        ComponentService ath = components.get(a);
        ComponentService bth = components.get(b);

        bth.setPosition(a);
        ath.setPosition(b);

        components.put(a, bth);
        components.put(b, ath);

        synchronized (mObserverLock) {
            for (Observer o : mObservers) {
                o.onComponentsSwapped(ath, bth, b, a);
            }
        }

        Log.d(TAG, String.format("Post-swap: %s -> %s", components.get(a).getDescription().getName(),
                components.get(b).getDescription().getName()));
    }

    public boolean equals(Object o) {

        if (o == null) {
            if (LOG) Log.d(TAG, "CompositeService->Equals: null");
            return false;
        }
        if (!(o instanceof CompositeService)) {
            if (LOG) Log.d(TAG, "CompositeService->Equals: Not a CompositeService");
            return false;
        }
        CompositeService other = (CompositeService) o;

        if (this.id != other.getID()) {
            if (LOG) Log.d(TAG, "CompositeService->Equals: id");
            return false;
        }

        if (!this.name.equals(other.getName())) {
            if (LOG) Log.d(TAG, "CompositeService->Equals: name " + name + " - " + other.getName());
            return false;
        }

        if (!this.description.equals(other.getDescription())) {
            if (LOG) Log.d(TAG, "CompositeService->Equals: description: " + description + " - " +
                    other.getDescription());
            return false;
        }

        if (this.components.size() != other.getComponents().size()) {
            if (LOG) Log.d(TAG, "CompositeService->Equals: not same num components: " +
                    components.size() + " - " + other.getComponents().size());
            return false;
        }

        for (int i = 0; i < components.size(); i++) {
            ComponentService component = components.valueAt(i);
            if (!component.equals(other.getComponent(component.getID()))) {
                if (LOG)
                    Log.d(TAG, "CompositeService->Equals: component " + component.getID() + ": " + i);
                return false;
            }
        }

        return true;
    }

    public int getColour(boolean dark) {
        if (!this.isEnabled() || this.id == -1)
            return R.color.card_disabled;

        // The first id is 2, but the index should be 0
        int index = (int) (id - 2);

        if (dark) {
            return COMPOSITE_COLOURS[index % COMPOSITE_COLOURS.length];
        } else {
            return COMPOSITE_COLOURS_LIGHT[index % COMPOSITE_COLOURS_LIGHT.length];
        }
    }

    public static final int[] COMPOSITE_COLOURS = new int[]{
            R.color.material_deeppurple,
            R.color.material_indigo,
            R.color.material_blue,
            R.color.material_lightblue,
            R.color.material_cyan,
            R.color.material_teal,
            R.color.material_green,
            R.color.material_lime,
            R.color.material_yellow,
            R.color.material_amber,
            R.color.material_orange,
            R.color.material_deeporange,
            R.color.material_red,
            R.color.material_pink,
            R.color.material_purple,
    };

    public static final int[] COMPOSITE_COLOURS_LIGHT = new int[]{
            R.color.material_deeppurple200,
            R.color.material_indigo200,
            R.color.material_blue200,
            R.color.material_lightblue200,
            R.color.material_cyan200,
            R.color.material_teal200,
            R.color.material_green200,
            R.color.material_lime200,
            R.color.material_yellow200,
            R.color.material_amber200,
            R.color.material_orange200,
            R.color.material_deeporange200,
            R.color.material_red200,
            R.color.material_pink200,
            R.color.material_purple200,
    };

    public boolean canEnable() {
        ArrayList<ServiceIO> m = this.getMandatoryInputs();
        boolean allSet = true;
        for (ServiceIO aM : m) {
            if (aM.getValue() == null && aM.getConnection() == null) {
                allSet = false;
                break;
            }
        }

        return allSet;
    }
}
