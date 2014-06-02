package com.appglue;

import android.database.Cursor;
import android.util.LongSparseArray;

import com.appglue.datatypes.IOType;
import com.appglue.datatypes.Text;
import com.appglue.description.ServiceDescription;

import java.util.ArrayList;

import static com.appglue.Constants.DESCRIPTION;
import static com.appglue.Constants.FRIENDLY_NAME;
import static com.appglue.Constants.IO_INDEX;
import static com.appglue.Constants.I_OR_O;
import static com.appglue.Constants.MANDATORY;
import static com.appglue.Constants.NAME;

public class ServiceIO 
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
	
	private ServiceIO connection;
	private ServiceDescription parent;
	
	// Value if it's an input / filter value if it's an output
	private int filterState = UNFILTERED;
	private Object manualValue; // This is used for outputs on filtering, or its hardcoded value if its an input
	private IOValue chosenSampleValue;

    private LongSparseArray<IOValue> sampleSearch;
    private ArrayList<IOValue> sampleValues;

    private int condition;
	
	private boolean mandatory;
	
	public static final int UNFILTERED = 0;
	public static final int MANUAL_FILTER = 1;
	public static final int SAMPLE_FILTER = 2;
	
	public ServiceIO()
	{
		this.id = -1;
		this.index = -1;
		this.name = "";
		this.friendlyName = "";
		this.type = new Text(); // Just default to text?
		this.description = "";
		this.connection = null;
		this.parent = null;
		this.manualValue = null;
		this.chosenSampleValue = null;
		this.condition = -1;
		this.mandatory = false;

        this.sampleValues = new ArrayList<IOValue>();
        this.sampleSearch = new LongSparseArray<IOValue>();
    }

    public ServiceIO(long id)
    {
        this();
        this.id = id;
    }

    public ServiceIO(String name, String friendlyName, IOType type, String description, boolean mandatory, ArrayList<IOValue> samples) {
		this();
		
		this.name = name;
		this.friendlyName = friendlyName;
		this.type = type;
		this.description = description;
		this.id = -1;
		this.index = -1;
		this.mandatory = mandatory;

        this.sampleValues = samples;
        for (IOValue v : samples) {
            sampleSearch.put(v.id, v);
        }
    }
	
	public ServiceIO(String name, String friendlyName, IOType type, String description, ServiceDescription parent, boolean mandatory, ArrayList<IOValue> sampleValues)
	{
		this(name, friendlyName, type, description, mandatory, sampleValues);
		this.parent = parent;
	}
	
	public ServiceIO(String name, String friendlyName, int index, IOType type, String description, ServiceDescription parent, boolean mandatory, ArrayList<IOValue> sampleValues)
	{
		this(name, friendlyName, type, description, parent, mandatory, sampleValues);
		this.index = index;
	}
	
	public ServiceIO(long id, String name, String friendlyName, int index, IOType type, String description, ServiceDescription parent, boolean mandatory, ArrayList<IOValue> sampleValues)
	{
		this(name, friendlyName, index, type, description, parent, mandatory, sampleValues);
		this.id = id;
	}

    public void setInput(boolean isInput) {
        this.isInput = isInput;
    }

    public boolean isInput() { return this.isInput; }
	
	public long getId()
	{
		return id;
	}
	
	public void setId(long id)
	{
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
	
	public ServiceDescription getParent()
	{
		return parent;
	}
	
	public ServiceIO getConnection()
	{
		return connection;
	}
	
	public void setConnection(ServiceIO connection)
	{
		this.connection = connection;
	}
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getFriendlyName()
	{
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public boolean equals(ServiceIO io)
	{
        return this.description.equals(io.getDescription());

    }
	
	public int isFiltered()
	{
		return filterState;
	}
	
	public void setFilterState(int filterState)
	{
		this.filterState = filterState;
	}
	
	public Object getManualValue()
	{
		if(manualValue == null)
			return "";
		
		return manualValue;
	}

    public Object getValue()
    {
        if(this.isFiltered() == MANUAL_FILTER)
            return this.getManualValue();
        else if(this.isFiltered() == SAMPLE_FILTER)
            return this.getChosenSampleValue();
        else
            return null;
    }
	
	public void setManualValue(Object value)
	{
		this.manualValue = value;
		this.filterState = MANUAL_FILTER;
	}
	
	public IOValue getChosenSampleValue()
	{
		if(chosenSampleValue == null)
			return new IOValue();
		
		return chosenSampleValue;
	}
	
	public void setChosenSampleValue(IOValue value)
	{
		this.chosenSampleValue = value;
		this.filterState = SAMPLE_FILTER;
	}
	
	public int getCondition()
	{
		return condition;
	}
	
	public void setCondition(int condition)
	{
		this.condition = condition;
	}
	
	public boolean hasValue()
	{
        return this.manualValue != null || this.chosenSampleValue != null;
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
