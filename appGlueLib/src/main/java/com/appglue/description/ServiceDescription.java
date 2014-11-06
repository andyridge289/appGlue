package com.appglue.description;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.LongSparseArray;
import android.util.Log;
import android.util.SparseArray;

import com.appglue.Constants.ServiceType;
import com.appglue.IODescription;
import com.appglue.TST;
import com.appglue.description.datatypes.IOType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.appglue.Constants.CATEGORIES;
import static com.appglue.Constants.CLASSNAME;
import static com.appglue.Constants.DESCRIPTION;
import static com.appglue.Constants.FEATURES;
import static com.appglue.Constants.FRIENDLY_NAME;
import static com.appglue.Constants.INPUTS;
import static com.appglue.Constants.INPUT_DESCRIPTION;
import static com.appglue.Constants.INPUT_NAME;
import static com.appglue.Constants.INPUT_TYPE;
import static com.appglue.Constants.JSON_APP;
import static com.appglue.Constants.JSON_SERVICE;
import static com.appglue.Constants.JSON_SERVICE_DATA;
import static com.appglue.Constants.JSON_SERVICE_LIST;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.MANDATORY;
import static com.appglue.Constants.MIN_VERSION;
import static com.appglue.Constants.NAME;
import static com.appglue.Constants.SHORT_NAME;
import static com.appglue.Constants.OUTPUTS;
import static com.appglue.Constants.OUTPUT_DESCRIPTION;
import static com.appglue.Constants.OUTPUT_NAME;
import static com.appglue.Constants.OUTPUT_TYPE;
import static com.appglue.Constants.PACKAGENAME;
import static com.appglue.Constants.FLAGS;
import static com.appglue.Constants.SAMPLES;
import static com.appglue.Constants.SAMPLE_NAME;
import static com.appglue.Constants.SAMPLE_VALUE;
import static com.appglue.Constants.SERVICE_TYPE;
import static com.appglue.Constants.TAG;
import static com.appglue.Constants.TAGS;

public class ServiceDescription {

    // The friendly name of the service
    private String name = "";
    private String shortName = "";

    // The type of the service - either local or remote
//    private ServiceType serviceType = ServiceType.IN_APP;
    private int flags;

    // The classname of the service itself
    protected String className = "";

    // The package that the service lives in - this should double as what to search for in the market
    private String packageName = "com.appglue";

    // A text description
    private String description = "";

    private int minVersion = Build.VERSION_CODES.ICE_CREAM_SANDWICH;

    // Inputs and getOutputs to/from the service
    private SparseArray<IODescription> inputs = new SparseArray<IODescription>();
    private SparseArray<IODescription> outputs = new SparseArray<IODescription>();
    private LongSparseArray<IODescription> idSearchInputs = new LongSparseArray<IODescription>();
    private LongSparseArray<IODescription> idSearchOutputs = new LongSparseArray<IODescription>();
    private TST<IODescription> nameSearchInputs = new TST<IODescription>();
    private TST<IODescription> nameSearchOutputs = new TST<IODescription>();

    private ArrayList<Tag> tags = new ArrayList<Tag>();
    private ArrayList<Category> categories = new ArrayList<Category>();

    // Representations of the icon of the service
    private AppDescription app = null;
    private int featuresRequired = 0;

    private ServiceDescription(
            String packageName, String className, String name, String shortName,
            String description,
            ArrayList<IODescription> inputs, ArrayList<IODescription> outputs,
            ServiceType serviceType, int flags, int version, int features) {
        this.name = name;
        this.className = className;
        this.packageName = packageName;
        this.description = description;
        this.shortName = shortName;

        this.setInputs(inputs);
        this.setOutputs(outputs);

//        this.serviceType = serviceType;
        this.flags = flags;
        this.minVersion = version;
        this.featuresRequired = features;
    }

    public ServiceDescription() {
        this.name = "";
        this.className = "";
        this.packageName = "";
        this.description = "";

        this.inputs = new SparseArray<IODescription>();
        this.outputs = new SparseArray<IODescription>();

//        this.serviceType = ServiceType.ANY;
        this.flags = 0;
    }

    public String getClassName() {
        return this.className;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

//    public ServiceType getServiceType() {
//        return serviceType;
//    }
//
//    public void setServiceType(ServiceType type) {
//        this.serviceType = type;
//    }

    public boolean hasFlag(int flag) {
        return (this.flags & flag) == flag;
    }

    public int getFlags() {
        return flags;
    }
    public void setFlags(int flags) {
        this.flags = flags;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setLocation(String location) {
        this.packageName = location;
    }

    public boolean hasInputs() {
        return this.inputs != null && this.inputs.size() != 0;

    }

    public boolean hasOutputs() {
        return this.outputs != null && this.outputs.size() != 0;

    }

    public ArrayList<IODescription> getInputs() {
        ArrayList<IODescription> in = new ArrayList<IODescription>();

        for (int i = 0; i < inputs.size(); i++) {
            in.add(inputs.get(i));
        }

        return in;
    }


    public ArrayList<IODescription> getOutputs() {
        ArrayList<IODescription> out = new ArrayList<IODescription>();

        for (int i = 0; i < outputs.size(); i++) {
            out.add(outputs.get(i));
        }

        return out;
    }

    /**
     * This is for filters - I don't think we'll ever need to get the getInputs
     *
     * @param outputId The ID of the output to get
     * @return The object representing the output
     */
    public IODescription getOutput(long outputId) {
        return idSearchOutputs.get(outputId);
    }

    public IODescription getOutput(String outputName) {
        return nameSearchOutputs.get(outputName);
    }

    public IODescription getInput(long inputId) {
        return idSearchInputs.get(inputId);
    }

    public IODescription getInput(String inputName) {
        return nameSearchInputs.get(inputName);
    }

    public IODescription getIO(long ioId) {

        // First check if it's an input
        IODescription io = getInput(ioId);
        if (io != null)
            return io;

        return getOutput(ioId);
    }

    public void setInputs(ArrayList<IODescription> inputs) {
        this.inputs = new SparseArray<IODescription>();
        this.idSearchInputs = new LongSparseArray<IODescription>();
        this.nameSearchInputs = new TST<IODescription>();

        if (inputs == null)
            return;

        if (inputs != null) {
            for (int i = 0; i < inputs.size(); i++) {
                inputs.get(i).setParent(this);
                this.inputs.put(i, inputs.get(i));
            }
        }

        for (IODescription in : inputs) {
            if (idSearchInputs.get(in.getID()) == null)
                idSearchInputs.put(in.getID(), in);

            if (nameSearchInputs.get(in.getName()) == null)
                nameSearchInputs.put(in.getName(), in);
        }
    }

    public void setOutputs(ArrayList<IODescription> outputs) {
        this.outputs = new SparseArray<IODescription>();
        this.idSearchOutputs = new LongSparseArray<IODescription>();

        if (outputs == null)
            return;

        if (outputs != null) {
            for (int i = 0; i < outputs.size(); i++) {
                outputs.get(i).setParent(this);
                this.outputs.put(i, outputs.get(i));
            }
        }

        for (IODescription out : outputs) {
            if (idSearchOutputs.get(out.getID()) == null)
                idSearchOutputs.put(out.getID(), out);

            if (nameSearchOutputs.get(out.getName()) == null)
                nameSearchOutputs.put(out.getName(), out);
        }
    }

    public AppDescription getApp() {
        return this.app;
    }

    public void setApp(AppDescription app) {
        this.app = app;
    }

    public ArrayList<Tag> getTags() {
        return this.tags;
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public void addTag(String name) {
        this.tags.add(new Tag(name));
    }

    public void addCategory(Category category) {
        this.categories.add(category);
    }
    public void addCategory(String name) {
        this.categories.add(Category.Factory.get(name));
    }

    public void addTags(ArrayList<Tag> tags) {
        this.tags.addAll(tags);
    }

    public boolean hasTags() {
        return this.tags.size() > 0;
    }

    public Bundle toBundle() {
        Bundle b = new Bundle();

        b.putString(PACKAGENAME, this.packageName);
        b.putString(CLASSNAME, this.className);
        b.putString(NAME, this.name);

        b.putString(DESCRIPTION, this.description);

        if (this.hasInputs()) {
            b.putString(INPUT_NAME, this.inputs.get(0).getName());
            b.putString(INPUT_TYPE, this.inputs.get(0).getType().getName());
            b.putString(INPUT_DESCRIPTION, this.inputs.get(0).description());
        } else {
            b.putString(INPUT_NAME, "");
            b.putString(INPUT_TYPE, "null");
            b.putString(INPUT_DESCRIPTION, "");
        }

        if (this.hasOutputs()) {
            b.putString(OUTPUT_NAME, this.outputs.get(0).getName());
            b.putString(OUTPUT_TYPE, this.outputs.get(0).getType().getName());
            b.putString(OUTPUT_DESCRIPTION, this.outputs.get(0).description());
        } else {
            b.putString(OUTPUT_NAME, "");
            b.putString(OUTPUT_TYPE, "null");
            b.putString(OUTPUT_DESCRIPTION, "");
        }

        return b;
    }

    public static ServiceType getServiceType(int type) {
        if (type == ServiceType.IN_APP.index)
            return ServiceType.IN_APP;
        else if (type == ServiceType.LOCAL.index)
            return ServiceType.LOCAL;
        else if (type == ServiceType.REMOTE.index)
            return ServiceType.REMOTE;
        else
            return ServiceType.ANY;
    }

    @Override
    public boolean equals(Object o) {

        if (o == null) {
            if (LOG) Log.d(TAG, "ServiceDescription->Equals: null");
            return false;
        }
        if (!(o instanceof ServiceDescription)) {
            if (LOG) Log.d(TAG, "ServiceDescription->Equals: Not a ServiceDescription");
            return false;
        }
        ServiceDescription other = (ServiceDescription) o;

        if (!this.className.equals(other.getClassName())) {
            return false;
        }

        if (!this.name.equals(other.getName())) {
            if (LOG) Log.d(TAG, "ServiceDescription->Equals: name");
            return false;
        }

        if (!this.shortName.equals(other.getShortName())) {
            if (LOG)
                Log.d(TAG, "ServiceDescription->Equals: shortname " + shortName + " - " + other.getShortName());
            return false;
        }

        if (this.flags != other.getFlags()) {
            if (LOG) Log.d(TAG, "ServiceDescription->Equals: process type");
            return false;
        }

        if (this.minVersion != other.getMinVersion()) {
            if (LOG) Log.d(TAG, "ServiceDescription->Equals: min version");
            return false;
        }

        if (this.featuresRequired != other.getFeaturesRequired()) {
            if (LOG) Log.d(TAG, "ServiceDescription->Equals: features required");
            return false;
        }

        if (!this.packageName.equals(other.getPackageName())) {
            if (LOG) Log.d(TAG, "ServiceDescription->Equals: package name");
            return false;
        }

        if (!this.description.equals(other.getDescription())) {
            if (LOG) Log.d(TAG, "ServiceDescription->Equals: description");
            return false;
        }

        if (!this.app.equals(other.getApp())) {
            if (LOG) Log.d(TAG, "ServiceDescription->Equals: app");
            return false;
        }

        if (this.inputs.size() != other.getInputs().size()) {
            if (LOG)
                Log.d(TAG, "ServiceDescription->Equals: Inputs size -- " + inputs.size() + " - " + other.getInputs().size());
            return false;
        }

        for (int i = 0; i < inputs.size(); i++) {
            if (!inputs.valueAt(i).equals(other.getInput(inputs.valueAt(i).getID()))) {
                if (LOG) Log.d(TAG, "ServiceDescription->Equals: input " + i);
                return false;
            }
        }

        if (this.outputs.size() != other.getOutputs().size()) {
            if (LOG)
                Log.d(TAG, "ServiceDescription->Equals: Outputs size -- " + outputs.size() + " - " + other.getOutputs().size());
            return false;
        }

        for (int i = 0; i < outputs.size(); i++) {
            if (!outputs.valueAt(i).equals(other.getOutput(outputs.valueAt(i).getID()))) {
                if (LOG) Log.d(TAG, "ServiceDescription->Equals: output " + i);
                return false;
            }
        }

        ArrayList<Tag> otherTags = other.getTags();
        if (tags.size() != otherTags.size()) {
            if (LOG) Log.d(TAG, "ServiceDescription->Equals: tag sizes don't match");
            return false;
        }

        for (int i = 0; i < tags.size(); i++) {

            boolean found = false;
            for (Tag otherTag : otherTags) {
                if (tags.get(i).equals(otherTag)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                if (LOG) Log.d(TAG, "ServiceDescription->Equals: tag " + i);
                return false;
            }
        }

        // Need to do equals for categories
        ArrayList<Category> otherCategories = other.getCategories();
        if (categories.size() != otherCategories.size()) {
            if (LOG) Log.d(TAG, "ServiceDescription->Equals: category sizes don't match");
            return false;
        }

        for (int i = 0; i < categories.size(); i++) {

            boolean found = false;
            for (Category otherCat : otherCategories) {
                if (categories.get(i).equals(otherCat)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                if (LOG) Log.d(TAG, "ServiceDescription->Equals: category " + i);
                return false;
            }
        }

        return true;
    }

    /**
     * ********************************
     * Constructors for ServiceDescription
     * ********************************
     */


    public static ArrayList<ServiceDescription> parseServices(String jsonString, Context context, AppDescription appDescription) throws JSONException {
        ArrayList<ServiceDescription> services = new ArrayList<ServiceDescription>();

        JSONObject json = new JSONObject(jsonString);

        AppDescription app;

        if (appDescription == null) {
            JSONObject jsonApp = json.getJSONObject(JSON_APP);
            app = AppDescription.parseFromJSON(jsonApp);
        } else {
            app = appDescription;
        }

        JSONArray serviceList = json.getJSONArray(JSON_SERVICE_LIST);


        for (int i = 0; i < serviceList.length(); i++) {
            if (!serviceList.getJSONObject(i).has(JSON_SERVICE)) {
                continue;
            }


            JSONObject service = serviceList.getJSONObject(i).getJSONObject(JSON_SERVICE);

            if (!service.has(JSON_SERVICE_DATA))
                continue;

            JSONObject serviceData = service.getJSONObject(JSON_SERVICE_DATA);

            ServiceDescription sd = ServiceDescription.parseFromNewJSON(serviceData, app);

            services.add(sd);
        }

        return services;
    }

    public void setInfo(String prefix, Cursor c) {
        this.packageName = c.getString(c.getColumnIndex(prefix + PACKAGENAME));
        this.className = c.getString(c.getColumnIndex(prefix + CLASSNAME));
        this.name = c.getString(c.getColumnIndex(prefix + NAME));
        this.shortName = c.getString(c.getColumnIndex(prefix + SHORT_NAME));
        this.description = c.getString(c.getColumnIndex(prefix + DESCRIPTION));

//        this.serviceType = ServiceDescription.getServiceType(c.getInt(c.getColumnIndex(prefix + SERVICE_TYPE)));
        this.flags = c.getInt(c.getColumnIndex(prefix + FLAGS));
        this.featuresRequired = c.getInt(c.getColumnIndex(prefix + FEATURES));
        this.minVersion = c.getInt(c.getColumnIndex(prefix + MIN_VERSION));

        this.app = new AppDescription();
    }

    public void addIO(IODescription io, boolean input, int position) {

        if (input) {
            this.inputs.put(position, io);
            this.idSearchInputs.put(io.getID(), io);
            this.nameSearchInputs.put(io.getName(), io);
        } else {
            this.outputs.put(position, io);
            this.idSearchOutputs.put(io.getID(), io);
            this.nameSearchOutputs.put(io.getName(), io);
        }

        io.setParent(this);
    }

    public static ServiceDescription createFromCursor(Cursor c, String prefix) {
        String packageName = c.getString(c.getColumnIndex(prefix + PACKAGENAME));
        String className = c.getString(c.getColumnIndex(prefix + CLASSNAME));
        String name = c.getString(c.getColumnIndex(prefix + NAME));
        String shortName = c.getString(c.getColumnIndex(prefix + SHORT_NAME));

        String description = c.getString(c.getColumnIndex(prefix + DESCRIPTION));

        int serviceType = c.getInt(c.getColumnIndex(prefix + SERVICE_TYPE));
        int flags = c.getInt(c.getColumnIndex(prefix + FLAGS));
        int version = c.getInt(c.getColumnIndex(prefix + MIN_VERSION));
        int features = c.getInt(c.getColumnIndex(prefix + FEATURES));

        return new ServiceDescription(packageName, className, name, shortName, description, null, null,
                ServiceDescription.getServiceType(serviceType), flags, version, features);
    }

    // New JSON parsings methods
    private static ArrayList<IODescription> parseIOFromNewJSON(JSONArray ioArray, boolean input, ServiceDescription sd) throws JSONException {
        ArrayList<IODescription> list = new ArrayList<IODescription>();

        for (int i = 0; i < ioArray.length(); i++) {
            JSONObject io = ioArray.getJSONObject(i);

            String ioName = input ? io.getString(INPUT_NAME) : io.getString(OUTPUT_NAME);
            String friendlyName = io.getString(FRIENDLY_NAME);
            String className = input ? io.getString(CLASSNAME) : io.getString(CLASSNAME);
            boolean mandatory = input && io.getBoolean(MANDATORY);

            IOType type = IOType.Factory.getType(className);
            String ioDescription = input ? io.getString(INPUT_DESCRIPTION) : io.getString(OUTPUT_DESCRIPTION);

            // Get the sample values for the thing
            JSONArray samples = io.getJSONArray(SAMPLES);
            ArrayList<SampleValue> sampleValues = new ArrayList<SampleValue>();

            for (int j = 0; j < samples.length(); j++) {
                JSONObject obj = samples.getJSONObject(j);

                String stringValue = obj.getString(SAMPLE_VALUE);
                Object value = type.fromString(stringValue);

                sampleValues.add(new SampleValue(-1, obj.getString(SAMPLE_NAME), value));
            }

            list.add(new IODescription(-1, ioName, friendlyName, i, type, ioDescription, sd, mandatory, sampleValues, input));
        }

        return list;
    }

    public static ServiceDescription parseFromNewJSON(JSONObject json, AppDescription app) throws JSONException {
        String packageName = json.getString(PACKAGENAME);
        String className = json.getString(CLASSNAME);
        String name = json.getString(NAME);
        String shortName = json.getString(SHORT_NAME);
        String description = json.getString(DESCRIPTION);

        int flags = json.getInt(FLAGS);
        int version = json.getInt(MIN_VERSION);
        int features = json.getInt(FEATURES);

        ServiceType serviceType = ServiceType.LOCAL;

        ServiceDescription sd = new ServiceDescription(packageName, className, name, shortName, description, null, null, serviceType, flags, version, features);

        ArrayList<IODescription> inputs = parseIOFromNewJSON(json.getJSONArray(INPUTS), true, sd);
        ArrayList<IODescription> outputs = parseIOFromNewJSON(json.getJSONArray(OUTPUTS), false, sd);

        for (int i = 0; i < inputs.size(); i++)
            inputs.get(i).setIndex(i);

        for (int i = 0; i < outputs.size(); i++)
            outputs.get(i).setIndex(i);

        sd.setInputs(inputs);
        sd.setOutputs(outputs);

        JSONArray tags = json.getJSONArray(TAGS);
        for (int i = 0; i < tags.length(); i++) {
            String sTag = tags.getString(i);

            // None of these will have and ID at this point because we can't see the database, but they should get one at some point....
            sd.addTag(sTag);
        }

        JSONArray cats = json.getJSONArray(CATEGORIES);
        for (int i = 0; i < cats.length(); i++) {
            String sCat = cats.getString(i);
            sd.addCategory(sCat);
        }

        sd.setApp(app);

        return sd;
    }

    public String getShortName() {
        return shortName;
    }

    public ArrayList<Category> getCategories() {
        return categories;
    }

    public boolean hasCategories() {
        return categories.size() > 0;
    }

    public int getMinVersion() {
        return minVersion;
    }

    public int getFeaturesRequired() {
        return featuresRequired;
    }
}
