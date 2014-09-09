package com.appglue.description;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.util.LongSparseArray;
import android.util.SparseArray;

import com.appglue.Constants.ProcessType;
import com.appglue.Constants.ServiceType;
import com.appglue.IOValue;
import com.appglue.Review;
import com.appglue.ServiceIO;
import com.appglue.TST;
import com.appglue.Tag;
import com.appglue.datatypes.IOType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.appglue.Constants.AVG_RATING;
import static com.appglue.Constants.CLASSNAME;
import static com.appglue.Constants.DESCRIPTION;
import static com.appglue.Constants.FRIENDLY_NAME;
import static com.appglue.Constants.INPUTS;
import static com.appglue.Constants.INPUT_DESCRIPTION;
import static com.appglue.Constants.INPUT_NAME;
import static com.appglue.Constants.INPUT_TYPE;
import static com.appglue.Constants.JSON_APP;
import static com.appglue.Constants.JSON_SERVICE;
import static com.appglue.Constants.JSON_SERVICE_DATA;
import static com.appglue.Constants.JSON_SERVICE_LIST;
import static com.appglue.Constants.MANDATORY;
import static com.appglue.Constants.NAME;
import static com.appglue.Constants.NUM_RATINGS;
import static com.appglue.Constants.OUTPUTS;
import static com.appglue.Constants.OUTPUT_DESCRIPTION;
import static com.appglue.Constants.OUTPUT_NAME;
import static com.appglue.Constants.OUTPUT_TYPE;
import static com.appglue.Constants.PACKAGENAME;
import static com.appglue.Constants.PRICE;
import static com.appglue.Constants.PROCESS_TYPE;
import static com.appglue.Constants.SAMPLES;
import static com.appglue.Constants.SAMPLE_NAME;
import static com.appglue.Constants.SAMPLE_VALUE;
import static com.appglue.Constants.SERVICE_TYPE;
import static com.appglue.Constants.TAGS;

public class ServiceDescription {

    // The friendly name of the service
    private String name = "";

    // The type of the service - either local or remote
    private ServiceType serviceType = ServiceType.LOCAL;

    // The process type of the service
    private ProcessType processType = ProcessType.NORMAL;

    // The classname of the service itself
    protected String className = "";

    // The package that the service lives in - this should double as what to search for in the market
    private String packageName = "com.appglue";

    // A text description
    private String description = "";

    // The price of the service - this will just be the price of the app
    private double price = 0;

    // Inputs and outputs to/from the service
    private SparseArray<ServiceIO> inputs = new SparseArray<ServiceIO>();
    private SparseArray<ServiceIO> outputs = new SparseArray<ServiceIO>();
    private LongSparseArray<ServiceIO> idSearchInputs = new LongSparseArray<ServiceIO>();
    private LongSparseArray<ServiceIO> idSearchOutputs = new LongSparseArray<ServiceIO>();
    private TST<ServiceIO> nameSearchInputs = new TST<ServiceIO>();
    private TST<ServiceIO> nameSearchOutputs = new TST<ServiceIO>();

    // The average rating of the service
    private double averageReviewRating = 0;

    // The number of ratings/reviews the service has received
    private int numReviews = 0;

    // The reviews that the service has received
    private ArrayList<Review> reviews = new ArrayList<Review>();
    private ArrayList<Tag> tags = new ArrayList<Tag>();

    // Representations of the icon of the service
    private AppDescription app = null;

    private ServiceDescription(
            String packageName, String className, String name,
            String description, double price,
            ArrayList<ServiceIO> inputs, ArrayList<ServiceIO> outputs,
            ServiceType serviceType, ProcessType processType) {
        this.name = name;
        this.className = className;
        this.packageName = packageName;
        this.description = description;
        this.price = price;
        this.averageReviewRating = 0;
        this.numReviews = 0;
        this.reviews = new ArrayList<Review>();

        this.setInputs(inputs, false);
        this.setOutputs(outputs, false);


        this.serviceType = serviceType;
        this.processType = processType;
    }

    public ServiceDescription() {
        this.name = "";
        this.className = "";
        this.packageName = "";
        this.description = "";
        this.price = 0.0f;
        this.averageReviewRating = 0;
        this.numReviews = 0;
        this.reviews = new ArrayList<Review>();

        this.inputs = new SparseArray<ServiceIO>();
        this.outputs = new SparseArray<ServiceIO>();

        this.serviceType = ServiceType.ANY;
        this.processType = ProcessType.NORMAL;
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

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType type) {
        this.serviceType = type;
    }

    public ProcessType getProcessType() {
        return processType;
    }

    public void setProcessType(ProcessType processType) {
        this.processType = processType;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setLocation(String location) {
        this.packageName = location;
    }

    protected void setIsNormal() {
        this.processType = ProcessType.NORMAL;
    }

    public boolean isConverter() {
        return this.processType == ProcessType.CONVERTER;
    }

    public double getPrice() {
        return this.price;
    }

    public boolean hasInputs() {
        return this.inputs != null && this.inputs.size() != 0;

    }

    public boolean hasOutputs() {
        return this.outputs != null && this.outputs.size() != 0;

    }

    public ArrayList<ServiceIO> getInputs() {
        ArrayList<ServiceIO> in = new ArrayList<ServiceIO>();

        for (int i = 0; i < inputs.size(); i++) {
            in.add(inputs.get(i));
        }

        return in;
    }


    public ArrayList<ServiceIO> getOutputs() {
        ArrayList<ServiceIO> out = new ArrayList<ServiceIO>();

        for (int i = 0; i < outputs.size(); i++) {
            out.add(outputs.get(i));
        }

        return out;
    }

    /**
     * This is for filters - I don't think we'll ever need to get the inputs
     *
     * @param outputId The ID of the output to get
     * @return The object representing the output
     */
    public ServiceIO getOutput(long outputId) {
        return idSearchOutputs.get(outputId);
    }
    public ServiceIO getOutput(String outputName) { return nameSearchOutputs.get(outputName); }

    public ServiceIO getInput(long inputId) {
        return idSearchInputs.get(inputId);
    }
    public ServiceIO getInput(String inputName) { return nameSearchInputs.get(inputName); }

    public ServiceIO getIO(long ioId) {

        // First check if it's an input
        ServiceIO io = getInput(ioId);
        if (io != null)
            return io;

        return getOutput(ioId);
    }

    public void setInputs(ArrayList<ServiceIO> inputs, boolean addToSearch) {
        this.inputs = new SparseArray<ServiceIO>();
        this.idSearchInputs = new LongSparseArray<ServiceIO>();

        if (inputs == null)
            return;

        if (inputs != null) {
            for (int i = 0; i < inputs.size(); i++) {
                inputs.get(i).setParent(this);
                this.inputs.put(i, inputs.get(i));
            }
        }

        if (addToSearch) {
            for (ServiceIO in : inputs) {
                idSearchInputs.put(in.getId(), in);
                nameSearchInputs.put(in.getName(), in);
            }
        }
    }

    public void setOutputs(ArrayList<ServiceIO> outputs, boolean addToSearch) {
        this.outputs = new SparseArray<ServiceIO>();
        this.idSearchOutputs = new LongSparseArray<ServiceIO>();

        if (outputs == null)
            return;

        if (outputs != null) {
            for (int i = 0; i < outputs.size(); i++) {
                outputs.get(i).setParent(this);
                this.outputs.put(i, outputs.get(i));
            }
        }

        if (addToSearch) {
            for (ServiceIO out : outputs) {
                idSearchOutputs.put(out.getId(), out);
                nameSearchOutputs.put(out.getName(), out);
            }
        }
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

    public double getAverageRating() {
        return this.averageReviewRating;
    }

    public int getNumReviews() {
        return this.numReviews;
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

        b.putDouble(AVG_RATING, this.averageReviewRating);
        b.putInt(NUM_RATINGS, this.numReviews);
        b.putString(DESCRIPTION, this.description);
        b.putDouble(PRICE, this.price);

        if (this.hasInputs()) {
            b.putString(INPUT_NAME, this.inputs.get(0).getName());
            b.putString(INPUT_TYPE, this.inputs.get(0).getType().getName());
            b.putString(INPUT_DESCRIPTION, this.inputs.get(0).getDescription());
        } else {
            b.putString(INPUT_NAME, "");
            b.putString(INPUT_TYPE, "null");
            b.putString(INPUT_DESCRIPTION, "");
        }

        if (this.hasOutputs()) {
            b.putString(OUTPUT_NAME, this.outputs.get(0).getName());
            b.putString(OUTPUT_TYPE, this.outputs.get(0).getType().getName());
            b.putString(OUTPUT_DESCRIPTION, this.outputs.get(0).getDescription());
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

    public static ProcessType getProcessType(int type) {
        if (type == ProcessType.NORMAL.index)
            return ProcessType.NORMAL;
        else if (type == ProcessType.CONVERTER.index)
            return ProcessType.CONVERTER;
        else if (type == ProcessType.FILTER.index)
            return ProcessType.FILTER;
        else if (type == ProcessType.REST.index)
            return ProcessType.REST;
        else
            return ProcessType.TRIGGER;

    }

    /**
     * ********************************
     * Constructors for ServiceDescription
     * ********************************
     */


    // FIXME Check the IDs of the IOs that are being added, and if they are -1 at any time other than the pre-DB create, refuse!
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
        this.description = c.getString(c.getColumnIndex(prefix + DESCRIPTION));

        this.price = c.getFloat(c.getColumnIndex(prefix + PRICE));

        this.serviceType = ServiceDescription.getServiceType(c.getInt(c.getColumnIndex(prefix + SERVICE_TYPE)));
        this.processType = ServiceDescription.getProcessType(c.getInt(c.getColumnIndex(prefix + PROCESS_TYPE)));

        this.app = new AppDescription();
    }

    public static ServiceDescription clone(ServiceDescription sd) {

        // Do the basics
        ServiceDescription component = new ServiceDescription(sd.getPackageName(), sd.getClassName(), sd.getName(),
                sd.getDescription(), sd.getPrice(), null, null, sd.getServiceType(), sd.getProcessType());

        ArrayList<ServiceIO> inputs = cloneIOs(sd.getInputs());
        ArrayList<ServiceIO> outputs = cloneIOs(sd.getOutputs());

        component.setInputs(inputs, true);
        component.setOutputs(outputs, true);

        return component;
    }

    private static ArrayList<ServiceIO> cloneIOs(ArrayList<ServiceIO> oldList) {

        ArrayList<ServiceIO> newList = new ArrayList<ServiceIO>();

        for (ServiceIO old : oldList) {
            ServiceIO io = new ServiceIO(old.getId(), old.getName(), old.getFriendlyName(), old.getType(),
                    old.getDescription(), old.isMandatory(), old.getSampleValues());
            io.setIndex(old.getIndex());

            newList.add(io);
        }

        return newList;
    }

    public void addIO(ServiceIO io, boolean input, int position) {

        if (input) {
            this.inputs.put(position, io);
            this.idSearchInputs.put(io.getId(), io);
            this.nameSearchInputs.put(io.getName(), io);
        } else {
            this.outputs.put(position, io);
            this.idSearchOutputs.put(io.getId(), io);
            this.nameSearchOutputs.put(io.getName(), io);
        }

        io.setParent(this);
    }

    public static ServiceDescription createFromCursor(Cursor c, String prefix) {
        String packageName = c.getString(c.getColumnIndex(prefix + PACKAGENAME));
        String className = c.getString(c.getColumnIndex(prefix + CLASSNAME));
        String name = c.getString(c.getColumnIndex(prefix + NAME));

        String description = c.getString(c.getColumnIndex(prefix + DESCRIPTION));

        float price = c.getFloat(c.getColumnIndex(prefix + PRICE));

        int serviceType = c.getInt(c.getColumnIndex(prefix + SERVICE_TYPE));
        int processType = c.getInt(c.getColumnIndex(prefix + PROCESS_TYPE));

        return new ServiceDescription(packageName, className, name, description, price, null, null,
                ServiceDescription.getServiceType(serviceType), ServiceDescription.getProcessType(processType));
    }

    // New JSON parsings methods
    private static ArrayList<ServiceIO> parseIOFromNewJSON(JSONArray ioArray, boolean input, ServiceDescription sd) throws JSONException {
        ArrayList<ServiceIO> list = new ArrayList<ServiceIO>();

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
            ArrayList<IOValue> sampleValues = new ArrayList<IOValue>();

            for (int j = 0; j < samples.length(); j++) {
                JSONObject obj = samples.getJSONObject(j);

                String stringValue = obj.getString(SAMPLE_VALUE);
                Object value = type.fromString(stringValue);

                sampleValues.add(new IOValue(obj.getString(SAMPLE_NAME), value));
            }

            list.add(new ServiceIO(-1, ioName, friendlyName, i, type, ioDescription, sd, mandatory, sampleValues));
        }

        return list;
    }

    public static ServiceDescription parseFromNewJSON(JSONObject json, AppDescription app) throws JSONException {
        String packageName = json.getString(PACKAGENAME);
        String className = json.getString(CLASSNAME);
        String name = json.getString(NAME);
        String description = json.getString(DESCRIPTION);
        ProcessType processType = ServiceDescription.getProcessType(json.getInt(PROCESS_TYPE));
        ServiceType serviceType = ServiceType.LOCAL;
        double price = json.getDouble(PRICE);

        ServiceDescription sd = new ServiceDescription(packageName, className, name, description, price, null, null, serviceType, processType);

        ArrayList<ServiceIO> inputs = parseIOFromNewJSON(json.getJSONArray(INPUTS), true, sd);
        ArrayList<ServiceIO> outputs = parseIOFromNewJSON(json.getJSONArray(OUTPUTS), false, sd);

        for (int i = 0; i < inputs.size(); i++)
            inputs.get(i).setIndex(i);

        for (int i = 0; i < outputs.size(); i++)
            outputs.get(i).setIndex(i);

        sd.setInputs(inputs, false);
        sd.setOutputs(outputs, false);

        JSONArray tags = json.getJSONArray(TAGS);
        for (int i = 0; i < tags.length(); i++) {
            String sTag = tags.getString(i);

            // None of these will have and ID at this point because we can't see the database, but they should get one at some point....
            sd.addTag(sTag);
        }

        sd.setApp(app);

        return sd;
    }
}
