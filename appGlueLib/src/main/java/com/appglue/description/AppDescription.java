package com.appglue.description;

import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;

import static com.appglue.Constants.*;

public class AppDescription 
{
	private String name;
	private String packageName;
	private String developer;
	
	private String iconLocation;
	private String description;
	
	public AppDescription(String name, String packageName, String iconLocation, String description, String developer)
	{
		this(name, packageName);
		this.iconLocation = iconLocation;
		this.description = description;
		this.developer = developer;
	}
	
	public AppDescription(String name, String packageName)
	{
		this.name = name;
		this.packageName = packageName;
		this.iconLocation = null;
		this.description = "";
		this.developer = "";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getIconLocation() {
		return iconLocation;
	}

	public void setIconLocation(String iconLocation) {
		this.iconLocation = iconLocation;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public void setDescription(String description)
	{
		this.description = description;
	}
	
	public String getDeveloper()
	{
		return developer;
	}
	
	public void setDeveloper(String developer)
	{
		this.developer = developer;
	}
	
	public static AppDescription parseFromCursor(Cursor c)
	{
		String name = c.getString(c.getColumnIndex(NAME));
		String packageName = c.getString(c.getColumnIndex(PACKAGENAME));
		String iconLocation = c.getString(c.getColumnIndex(ICON));
		String developerName = c.getString(c.getColumnIndex(DEVELOPER));
		
		int index = c.getColumnIndex(DESCRIPTION);
		String description = index == -1 ? "" : c.getString(c.getColumnIndex(DESCRIPTION));
		
		return new AppDescription(name, packageName, iconLocation, description, developerName);
	}
	
	public static AppDescription parseFromJSON(JSONObject json) throws JSONException
	{
		String name = json.getString(NAME);
		String packagename = json.getString(PACKAGENAME);
		String description = json.getString(DESCRIPTION);
		String developerName = json.getString(DEVELOPER);
		
		return new AppDescription(name, packagename, null, description, developerName);
	}
}
