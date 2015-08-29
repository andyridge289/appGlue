package com.appglue;

import android.database.Cursor;
import android.support.v4.util.LongSparseArray;

import com.appglue.db.AppGlueDB;
import com.appglue.description.SampleValue;
import com.appglue.description.ServiceDescription;
import com.appglue.description.IOType;
import com.appglue.datatypes.Text;
import com.orhanobut.logger.Logger;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.ArrayList;

import static com.appglue.Constants.DESCRIPTION;
import static com.appglue.Constants.FRIENDLY_NAME;
import static com.appglue.Constants.IO_INDEX;
import static com.appglue.Constants.I_OR_O;
import static com.appglue.Constants.MANDATORY;
import static com.appglue.Constants.NAME;

/**
 * Description of the inputs and outputs of a component
 */

@Table(databaseName = AppGlueDB.NAME)
public class IODescription extends BaseModel
{
//    {CLASSNAME, "TEXT", TBL_SD, CLASSNAME},
// User friendly name of the type

    @Column @PrimaryKey(autoincrement = true) private long id;
    @Column private String name;
    @Column private int index;
    @Column private boolean isInput;
    @Column private String friendlyName;
    @Column private String description;
    @Column private boolean mandatory;
    @Column private String typeName;

    private IOType type;

    @ForeignKey(references = {
        @ForeignKeyReference(columnName = "serviceDescription", columnType = String.class,
                             foreignColumnName = "className", fieldIsPrivate = true)
        },
        saveForeignKeyModel = true,
        tableClass = ServiceDescription.class
    )
    private ServiceDescription parent;

    private LongSparseArray<SampleValue> sampleSearch;
    private ArrayList<SampleValue> sampleValues;

    public IODescription() {
        this.id = -1;
        this.index = -1;
        this.name = "";
        this.friendlyName = "";
        this.type = new Text(); // Just default to text?
        this.description = "";
        this.parent = null;
        this.mandatory = false;

        this.sampleValues = new ArrayList<>();
        this.sampleSearch = new LongSparseArray<>();
    }

    public IODescription(long id) {
        this();
        this.id = id;
    }

    public IODescription(String name) {
        this();
        this.name = name;
    }

    public IODescription(long id, String name, String friendlyName, IOType type, String description, boolean mandatory, ArrayList<SampleValue> samples) {
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

        for (SampleValue v : samples) {
            sampleSearch.put(v.getId(), v);
        }
    }

    public IODescription(long id, String name, String friendlyName, int index, IOType type,
                         String description, ServiceDescription parent, boolean mandatory,
                         ArrayList<SampleValue> sampleValues, boolean isInput) {
        this(id, name, friendlyName, type, description, mandatory, sampleValues);
        this.index = index;
        this.parent = parent;
        this.id = id;
        this.isInput = isInput;
    }

    public void setInput(boolean isInput) {
        this.isInput = isInput;
    }

    public boolean isInput() {
        return this.isInput;
    }
    public boolean getIsInput() { // Need this for dbflow
        return this.isInput;
    }
    public void setIsInput(boolean isInput) { // DBFLOW
        this.isInput = isInput;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setParent(ServiceDescription parent) {
        this.parent = parent;
    }

    public ServiceDescription parent() {
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

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public IOType getType() {
        return type;
    }

    public void setType(IOType type) {
        this.type = type;
    }

    public String getTypeName() {
        return type.getClassName();
    }

    public void setTypeName(String typeName) {
        this.type = IOType.Factory.getType(typeName);
        this.typeName = typeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean equals(Object o) {
        if (o == null) {
            Logger.d("IODescription->Equals: null");
            return false;
        }

        if (!(o instanceof IODescription)) {
            Logger.d("IODescription->Equals: not IODescription");
            return false;
        }

        IODescription other = (IODescription) o;

        if (id != other.getId()) {

            Logger.d("IODescription->Equals: id - [" + id + " :: " + other.getId() + "]");
            return false;
        }

        if (index != other.getIndex()) {
            Logger.d("IODescription->Equals: index");
            return false;
        }

        if (isInput != other.isInput()) {

            Logger.d("IODescription->Equals: is input [" + name + "] " + isInput + " - " + other.isInput());
            return false;
        }

        if (!name.equals(other.getName())) {
            Logger.d("IODescription->Equals: name");
            return false;
        }

        if (!friendlyName.equals(other.getFriendlyName())) {
            Logger.d("IODescription->Equals: friendly name");
            return false;
        }

        if (!type.equals(other.getType())) {
            Logger.d("IODescription->Equals: type");
            return false;
        }

        if (!description.equals(other.getDescription())) {
            Logger.d("IODescription->Equals: description");
            return false;
        }

        if (!parent.getClassName().equals(other.parent.getClassName())) {
            Logger.d("IODescription->Equals: parent");
            return false;
        }

        if (mandatory != other.mandatory) {
            Logger.d("IODescription->Equals: mandatory");
            return false;
        }

        if (this.sampleValues.size() != other.getSampleValues().size()) {
            Logger.d("IODescription->Equals: sample size");
            return false;
        }

        for (int i = 0; i < sampleSearch.size(); i++) {
            SampleValue v = sampleSearch.valueAt(i);

            if (!v.equals(other.getSampleValue(v.getId()))) {

                Logger.d("IODescription->Equals: sample value " + v.getId() + " (index " + i + ")");
                return false;
            }
        }

        return true;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public boolean getMandatory() { // DBFLOW
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public ArrayList<SampleValue> getSampleValues() {
        return sampleValues;
    }
    public SampleValue getSampleValue(long id) {
        return this.sampleSearch.get(id);
    }
    public boolean hasSampleValues() {
        return sampleValues != null && sampleValues.size() > 0;
    }

    public void setSampleValues(ArrayList<SampleValue> values) {
        this.sampleSearch = new LongSparseArray<>();

        for (SampleValue v : values) {
            this.sampleSearch.put(v.getId(), v);
        }
        sampleValues = values;
    }

    public void addSampleValue(SampleValue value) {
        if (sampleSearch == null)
            sampleSearch = new LongSparseArray<>();

        sampleSearch.put(value.getId(), value);

        if (sampleValues == null)
            sampleValues = new ArrayList<>();

        sampleValues.add(value);
    }

    public void setInfo(String prefix, Cursor c) {
        this.setName(c.getString(c.getColumnIndex(prefix + NAME)));
        this.setFriendlyName(c.getString(c.getColumnIndex(prefix + FRIENDLY_NAME)));
        this.setIndex(c.getInt(c.getColumnIndex(prefix + IO_INDEX)));
        this.setDescription(c.getString(c.getColumnIndex(prefix + DESCRIPTION)));
        this.setMandatory(c.getInt(c.getColumnIndex(prefix + MANDATORY)) == 1);
        this.setInput(c.getInt(c.getColumnIndex(prefix + I_OR_O)) == 1);
    }


    public void setID(long id) {
        this.id = id;
    }

    public ServiceDescription getParent() {
        return parent;
    }
}
