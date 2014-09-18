package com.appglue;

import android.database.Cursor;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import com.appglue.description.datatypes.IOType;
import com.appglue.description.datatypes.Text;
import com.appglue.description.IOValue;
import com.appglue.description.ServiceDescription;

import java.util.ArrayList;

import static com.appglue.Constants.DESCRIPTION;
import static com.appglue.Constants.FRIENDLY_NAME;
import static com.appglue.Constants.IO_INDEX;
import static com.appglue.Constants.I_OR_O;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.MANDATORY;
import static com.appglue.Constants.NAME;
import static com.appglue.Constants.TAG;

public class IODescription
{
	private long id;
	
	// The index of the IO in the list of IOs for the SD
	private int index;
    private boolean isInput;
	
	// User friendly name of the type
	private String name;
	private String friendlyName;
	
	// Java canonical name of the class
	private IOType type;
	
	// A user friendly text description of the type
	private String description;

	private ServiceDescription parent;

    private LongSparseArray<IOValue> sampleSearch;
    private ArrayList<IOValue> sampleValues;

	private boolean mandatory;

	public IODescription()
	{
		this.id = -1;
		this.index = -1;
		this.name = "";
		this.friendlyName = "";
		this.type = new Text(); // Just default to text?
		this.description = "";
		this.parent = null;
		this.mandatory = false;

        this.sampleValues = new ArrayList<IOValue>();
        this.sampleSearch = new LongSparseArray<IOValue>();
    }

    public IODescription(long id)
    {
        this();
        this.id = id;
    }

    public IODescription(String name) {
        this();
        this.name = name;
    }

    public IODescription(long id, String name, String friendlyName, IOType type, String description, boolean mandatory, ArrayList<IOValue> samples) {
        this(id);

        this.name = name;
		this.friendlyName = friendlyName;
		this.type = type;
		this.description = description;
		this.index = -1;
		this.mandatory = mandatory;

        this.sampleValues = samples;

        if (samples == null)
            return;

        for (IOValue v : samples) {
            sampleSearch.put(v.id, v);
        }
    }

    public IODescription(long id, String name, String friendlyName, int index, IOType type,
                         String description, ServiceDescription parent, boolean mandatory,
                         ArrayList<IOValue> sampleValues, boolean isInput) {
        this(id, name, friendlyName, type, description, mandatory, sampleValues);
        this.index = index;
        this.parent = parent;
        this.id = id;
        this.isInput = isInput;
	}

    public void setInput(boolean isInput) {
        this.isInput = isInput;
    }

    public boolean isInput() { return this.isInput; }
	
	public long getID()
	{
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public int getIndex()
	{
		return index;
	}
	
	public void setIndex(int index)
	{
		this.index = index;
	}
	
	public void setParent(ServiceDescription parent)
	{
		this.parent = parent;
	}
	
	public ServiceDescription parent()
	{
		return parent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getFriendlyName() {
		return friendlyName;
	}
	
	public void setFriendlyName(String friendlyName)
	{
		this.friendlyName = friendlyName;
	}

	public IOType getType() {
		return type;
	}
	public void setType(IOType type) {
		this.type = type;
	}

	public String description() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public boolean equals(Object o)
	{
        if(o == null)  {
            if(LOG) Log.d(TAG, "IODescription->Equals: null");
            return false;
        }

        if(!(o instanceof IODescription)) {
            if(LOG) Log.d(TAG, "IODescription->Equals: not IODescription");
            return false;
        }

        IODescription other = (IODescription) o;

        if(id != other.getID()) {
            if(LOG) Log.d(TAG, "IODescription->Equals: id - [" + id + " :: " + other.getID() + "]");
            return false;
        }

        if(index != other.getIndex()) {
            if(LOG) Log.d(TAG, "IODescription->Equals: index");
            return false;
        }

        if(isInput != other.isInput()) {
            if(LOG) Log.d(TAG, "IODescription->Equals: is input [" + name + "] " + isInput + " - " + other.isInput());
            return false;
        }

        if(!name.equals(other.getName())) {
            if(LOG) Log.d(TAG, "IODescription->Equals: name");
            return false;
        }

        if(!friendlyName.equals(other.getFriendlyName())) {
            if(LOG) Log.d(TAG, "IODescription->Equals: friendly name");
            return false;
        }

        if(!type.equals(other.getType())) {
            if(LOG) Log.d(TAG, "IODescription->Equals: type");
            return false;
        }

        if(!description.equals(other.description())) {
            if(LOG) Log.d(TAG, "IODescription->Equals: description");
            return false;
        }

        if(!parent.getClassName().equals(other.parent.getClassName())) {
            if(LOG) Log.d(TAG, "IODescription->Equals: parent");
            return false;
        }

        if(mandatory != other.mandatory) {
            if(LOG) Log.d(TAG, "IODescription->Equals: mandatory");
            return false;
        }

        if (this.sampleValues.size() != other.getSampleValues().size()) {
            if(LOG) Log.d(TAG, "IODescription->Equals: sample size");
            return false;
        }

        for(int i = 0; i < sampleSearch.size(); i++) {
            IOValue v = sampleSearch.valueAt(i);

            if(!v.equals(other.getSampleValue(v.getID()))) {
                if(LOG) Log.d(TAG, "IODescription->Equals: sample value " + v.getID() + " (index " + i + ")");
                return false;
            }
        }

        return true;
    }
	
	public boolean isMandatory()
	{
		return mandatory;
	}
	
	public void setMandatory(boolean mandatory)
	{
		this.mandatory = mandatory;
	}
	
	public ArrayList<IOValue> getSampleValues()
	{
        return sampleValues;
    }

    public IOValue getSampleValue(long id) {
        return this.sampleSearch.get(id);
    }

    public void setSampleValues(ArrayList<IOValue> values)
	{
        this.sampleSearch = new LongSparseArray<IOValue>();

        for (IOValue v : values) {
            this.sampleSearch.put(v.id, v);
        }
        sampleValues = values;
    }
	
	public void addSampleValue(IOValue value)
	{
        if (sampleSearch == null)
            sampleSearch = new LongSparseArray<IOValue>();

        sampleSearch.put(value.id, value);

        if (sampleValues == null)
            sampleValues = new ArrayList<IOValue>();

        sampleValues.add(value);
    }

    public void setInfo(String prefix, Cursor c)
    {
        this.setName(c.getString(c.getColumnIndex(prefix + NAME)));
        this.setFriendlyName(c.getString(c.getColumnIndex(prefix + FRIENDLY_NAME)));
        this.setIndex(c.getInt(c.getColumnIndex(prefix + IO_INDEX)));
        this.setDescription(c.getString(c.getColumnIndex(prefix + DESCRIPTION)));
        this.setMandatory(c.getInt(c.getColumnIndex(prefix + MANDATORY)) == 1);
        this.setInput(c.getInt(c.getColumnIndex(prefix + I_OR_O)) == 1);
    }
	
	
}
