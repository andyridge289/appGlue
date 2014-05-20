package com.appglue;

import android.database.Cursor;

import java.util.ArrayList;

import static com.appglue.Constants.ID;
import static com.appglue.Constants.NAME;

public class Tag 
{
	private int id;
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
	
	public Tag(int id, String name)
	{
		this.id = id;
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getId()
	{
		return id;
	}
	
	public static Tag createOneFromCursor(Cursor c)
	{
		int id = c.getInt(c.getColumnIndex(ID));
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
}
