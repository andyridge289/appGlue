package com.appglue.datatypes;

import android.os.Bundle;

import com.appglue.description.IOType;
import com.appgluelib.appgluelib.R;

public class ImageDrawableResource extends IOType {
    public ImageDrawableResource() {
        super();
        this.name = "Image";
        this.className = ImageDrawableResource.class.getCanonicalName();
        this.sensitivity = Sensitivity.NORMAL;
        this.acceptsManual = false;
        this.manualLookup = true;
    }

    @Override
    public Object getFromBundle(Bundle bundle, String key, Object defaultValue) {
        return bundle.getInt(key, R.drawable.ic_help_black_24dp);
    }

    @Override
    public void addToBundle(Bundle b, Object o, String key) {
        b.putInt(key, (Integer) o);
    }


    @Override
    public String toString(Object value) {
        return "" + value;
    }

    public Object fromString(String value) {
        return value;
    }

    public boolean compare(Object a, Object b) {
        return a.equals(b);
    }
}
