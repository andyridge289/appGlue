package com.appglue.description.datatypes;

import android.os.Bundle;

import com.appgluelib.appgluelib.R;

public class ImageDrawableResource extends IOType {
    public ImageDrawableResource() {
        super();
        this.name = "Image";
        this.className = ImageDrawableResource.class.getCanonicalName();
        this.sensitivity = Sensitivity.NORMAL;
    }

    @Override
    public Object getFromBundle(Bundle bundle, String key, Object defaultValue) {
        return bundle.getInt(key, R.drawable.ic_launcher);
    }

    @Override
    public void addToBundle(Bundle b, Object o, String key) {
        b.putInt(key, (Integer) o);
    }


    @Override
    public String toString(Object value) {
        return (String) value;
    }

    public Object fromString(String value) {
        return value;
    }

    public boolean compare(Object a, Object b) {
        return a.equals(b);
    }
}