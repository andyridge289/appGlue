package com.appglue.description;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.util.LongSparseArray;
import android.util.Log;

import com.appglue.TST;

import static com.appglue.Constants.ID;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.NAME;
import static com.appglue.Constants.TAG;

public class Category implements Comparable {

    private long id;
    private String name;
    public int count = 0;

    public Category(String name) {
        this(-1, name);
    }

    public Category(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setID(long id) {
        this.id = id;

        if (Factory.idSearch.get(id) == null) {
            Factory.idSearch.put(id, this);
        }
    }

    public long getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static Category createOneFromCursor(Cursor c) {
        long id = c.getLong(c.getColumnIndex(ID));
        String name = c.getString(c.getColumnIndex(NAME));

        return new Category(id, name);
    }

    public boolean equals(Object o) {
        if(o == null)  {
            if(LOG) Log.d(TAG, "Category->Equals: null");
            return false;
        }

        if(!(o instanceof Category)) {
            if(LOG) Log.d(TAG, "Category->Equals: not ServiceIO");
            return false;
        }

        Category other = (Category) o;

        if(!name.equals(other.getName())) {
            if(LOG) Log.d(TAG, "Category->Equals: name - [" + name + " :: " + other.getName() + "]");
            return false;
        }

        return true;
    }

    @Override
    public int compareTo(@NonNull Object another) {
        if (!(another instanceof Category))
            return 0;

        return ((Category) another).count - count;
    }

    public static class Factory {

        public static final String TRIGGERS = "Triggers";
        public static final String DEVICE_UTILS = "Device Utilities";
        public static final String NETWORK_UTILS = "Network Utilities";
        public static final String MISC = "Miscellaneous";
        public static final String TRAVEL = "Travel";
        public static final String WEARABLE = "Wearable";

        private static TST<Category> nameSearch;
        private static LongSparseArray<Category> idSearch;

        public static Category get(String name) {
            if (nameSearch == null) {
                create();
            }

            return nameSearch.get(name);
        }

        private static void create() {

            nameSearch = new TST<Category>();
            idSearch = new LongSparseArray<Category>();

            nameSearch.put(TRIGGERS, new Category(TRIGGERS));
            nameSearch.put(DEVICE_UTILS, new Category(DEVICE_UTILS));
            nameSearch.put(NETWORK_UTILS, new Category(NETWORK_UTILS));
            nameSearch.put(MISC, new Category(MISC));
            nameSearch.put(TRAVEL, new Category(TRAVEL));
            nameSearch.put(WEARABLE, new Category(WEARABLE));
        }
    }
}
