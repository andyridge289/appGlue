package com.appglue.description;

import android.database.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

import static com.appglue.Constants.DESCRIPTION;
import static com.appglue.Constants.DEVELOPER;
import static com.appglue.Constants.ICON;
import static com.appglue.Constants.NAME;
import static com.appglue.Constants.PACKAGENAME;

public class AppDescription {
    private String name;
    private String packageName;
    private String developer;

    private String iconLocation;
    private String description;

    public AppDescription() {
        this.name = "";
        this.packageName = "";
        this.description = "";
        this.developer = "";
        this.iconLocation = "";
    }

    public AppDescription(String name, String packageName, String iconLocation, String description, String developer) {
        this(name, packageName);
        this.iconLocation = iconLocation;
        this.description = description;
        this.developer = developer;
    }

    public AppDescription(String name, String packageName) {
        this();
        this.name = name;
        this.packageName = packageName;
        this.iconLocation = null;
    }

    public void setInfo(String p, Cursor c) {
        this.packageName = c.getString(c.getColumnIndex(p + PACKAGENAME));
        this.name = c.getString(c.getColumnIndex(p + NAME));
        this.iconLocation = c.getString(c.getColumnIndex(p + ICON));
        this.description = c.getString(c.getColumnIndex(p + DESCRIPTION));
        this.developer = c.getString(c.getColumnIndex(p + DEVELOPER));

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public static AppDescription parseFromCursor(Cursor c) {
        String name = c.getString(c.getColumnIndex(NAME));
        String packageName = c.getString(c.getColumnIndex(PACKAGENAME));
        String iconLocation = c.getString(c.getColumnIndex(ICON));
        String developerName = c.getString(c.getColumnIndex(DEVELOPER));

        int index = c.getColumnIndex(DESCRIPTION);
        String description = index == -1 ? "" : c.getString(c.getColumnIndex(DESCRIPTION));

        return new AppDescription(name, packageName, iconLocation, description, developerName);
    }

    public static AppDescription parseFromJSON(JSONObject json) throws JSONException {
        String name = json.getString(NAME);
        String packagename = json.getString(PACKAGENAME);
        String description = json.getString(DESCRIPTION);
        String developerName = json.getString(DEVELOPER);

        return new AppDescription(name, packagename, null, description, developerName);
    }
}
