package com.appglue.description;

import com.appglue.IODescription;
import com.appglue.db.AppGlueDB;
import com.orhanobut.logger.Logger;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(databaseName = AppGlueDB.NAME)
public class SampleValue extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    private long id;

    @Column
    private String name;

    @Column
    private String value;

    @Column
    @ForeignKey(references = {
        @ForeignKeyReference(columnName = "ioDescription", columnType = Long.class, fieldIsPrivate = true, foreignColumnName = "id") },
        tableClass = IODescription.class, saveForeignKeyModel = true
    )
    private IODescription ioDescription;

    public SampleValue() {
        this.id = -1;
        this.name = "";
    }

    public SampleValue(long id, String name) {
        this.id = id;
        this.name = name;
        this.value = null;
    }

    public SampleValue(String name, Object value) {
        this.id = -1;
        this.name = name;
        this.value = value.toString();
    }

    public SampleValue(long id, String name, Object value) {
        this.id = id;
        this.name = name;
        this.value = value.toString();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public String toString() {
        return this.name;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setValue(Object value) {
        this.value = value.toString();
    }

    public IODescription getIoDescription() {
        return ioDescription;
    }

    public void setIoDescription(IODescription description) {
        this.ioDescription = description;
    }

    public boolean equals(Object o) {
        if (o == null) {
            Logger.d("IOValue->Equals: null");
            return false;
        }

        if (!(o instanceof SampleValue)) {
            Logger.d("IOValue->Equals: not IOValue");
            return false;
        }

        SampleValue other = (SampleValue) o;

        if (id != other.getId()) {
            Logger.d("IOValue->Equals: id - [" + id + " :: " + other.getId() + "]");
            return false;
        }

        if (!name.equals(other.getName())) {

            Logger.d("IOValue->Equals: name - [" + name + " :: " + other.getName() + "]");
            return false;
        }

        return true;
    }
}
