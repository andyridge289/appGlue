package com.appglue.library;

import android.os.Bundle;

import com.appglue.engine.description.ComponentService;

public class ComponentLogItem
{
    private long id;
    private ComponentService component;
    private String message;
    private Bundle inputBundle;
    private Bundle outputBundle;
    private int logType;
    private long time;

    public ComponentLogItem(long id, ComponentService component, String message, Bundle inputBundle, Bundle outputBundle, int logType, long time) {
        this.id = id;
        this.component = component;
        this.message = message;
        this.inputBundle = inputBundle;
        this.outputBundle = outputBundle;
        this.logType = logType;
        this.time = time;
    }

    public long id() {
        return id;
    }

    public String getClassName() {
        return component.getDescription().getClassName();
    }

    public String getMessage() {
        return message;
    }

    public Bundle getInputBundle() {
        return inputBundle;
    }

    public Bundle getOutputBundle() {
        return outputBundle;
    }

    public int getType() {
        return logType;
    }

    public long time() {
        return time;
    }
}
