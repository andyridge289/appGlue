package com.appglue.description;

import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;

import static com.appglue.Constants.ID;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.NAME;
import static com.appglue.Constants.TAG;

public class Tag {
    private long id;
    private String name;

    public Tag() {
        this.id = -1;
        this.name = "";
    }

    public Tag(String name) {
        this.id = -1;
        this.name = name;
    }

    public Tag(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public long getID() {
        return id;
    }

    public void setID(long id) {
        this.id = id;
    }

    public static Tag createOneFromCursor(Cursor c) {

        long id = c.getLong(c.getColumnIndex(ID));
        String name = c.getString(c.getColumnIndex(NAME));

        return new Tag(id, name);
    }

    public static ArrayList<Tag> createManyFromCursor(Cursor c) {
        ArrayList<Tag> tags = new ArrayList<>();

        // Assume we're already at the first one

        do {
            tags.add(createOneFromCursor(c));
        }
        while (c.moveToNext());

        return tags;
    }

    public boolean equals(Object o) {
        if (o == null) {
            if (LOG) Log.d(TAG, "Tag->Equals: null");
            return false;
        }
        if (!(o instanceof Tag)) {
            if (LOG) Log.d(TAG, "Tag->Equals: Not a Tag");
            return false;
        }
        Tag other = (Tag) o;

        if (id == -1) {
            Log.d(TAG, "-1 for " + name);
        }

//        if (this.id != other.getID()) {
//            return false;
//        }
//
//        if (!this.name.equals(other.getName())) {
//            return false;
//        }

        return true;
    }
}
