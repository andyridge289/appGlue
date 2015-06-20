package com.appglue.description;

import com.orhanobut.logger.Logger;

public class SampleValue {
    private long id;

    private String name;
    private Object value;

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
        this.value = value;
    }

    public SampleValue(long id, String name, Object value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    public long getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public String toString() {
        return this.name;
    }

    public void setID(long id) {
        this.id = id;
    }

    public void setValue(Object value) {
        this.value = value;
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

        if (id != other.getID()) {
            Logger.d("IOValue->Equals: id - [" + id + " :: " + other.getID() + "]");
            return false;
        }

        if (!name.equals(other.getName())) {

            Logger.d("IOValue->Equals: name - [" + name + " :: " + other.getName() + "]");
            return false;
        }

        return true;
    }
}
