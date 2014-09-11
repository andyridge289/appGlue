package com.appglue.description;

import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;

import static com.appglue.Constants.ID;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.NAME;
import static com.appglue.Constants.TAG;

public class Tag 
{
	private long id;
	private String name;
	
	public Tag()
	{
		this.id = -1;
		this.name = "";
	}
	
	public Tag(String name)
	{
		this.id = -1;
		this.name = name;
	}
	
	public Tag(long id, String name)
	{
		this.id = id;
		this.name = name;
	}
	
	public String name()
	{
		return name;
	}
	
	public long id() {
		return id;
	}

    public void setId(long id) {
        this.id = id;
    }
	
	public static Tag createOneFromCursor(Cursor c)
	{
		long id = c.getLong(c.getColumnIndex(ID));
		String name = c.getString(c.getColumnIndex(NAME));
		
		return new Tag(id, name);
	}
	
	public static ArrayList<Tag> createManyFromCursor(Cursor c)
	{
		ArrayList<Tag> tags = new ArrayList<Tag>();
		
		// Assume we're already at the first one
		
		do
		{
			tags.add(createOneFromCursor(c));
		}
		while(c.moveToNext());
		
		return tags;
	}

    public boolean equals(Object o) {

        if(o == null) {
            if(LOG) Log.d(TAG, "Tag->Equals: null");
            return false;
        }
        if(!(o instanceof Tag)) {
            if (LOG) Log.d(TAG, "Tag->Equals: Not a ServiceDescription");
            return false;
        }
        Tag other = (Tag) o;

        if(this.id != other.id()) {
            if (LOG) Log.d(TAG, "Tag->Equals: id: [" + this.id + " - " + other.id() + "]");
            return false;
        }

        if(!this.name.equals(other.name())) {
            if (LOG) Log.d(TAG, "Tag->Equals: name");
            return false;
        }

        return true;
    }
}
