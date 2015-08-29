package com.appglue.description;

import android.database.Cursor;

import com.appglue.db.AppGlueDB;
import com.orhanobut.logger.Logger;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.ArrayList;

import static com.appglue.Constants.ID;
import static com.appglue.Constants.NAME;

// FIXME work out how the fuck to do many-many

@Table(databaseName = AppGlueDB.NAME)
public class Tag extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    private long id;

    @Column
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

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
            Logger.d("Tag->Equals: null");
            return false;
        }
        if (!(o instanceof Tag)) {
            Logger.d("Tag->Equals: Not a Tag");
            return false;
        }
        Tag other = (Tag) o;

        if (id == -1) {
            Logger.d("-1 for " + name);
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
