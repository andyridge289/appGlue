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
    private int status;
    private long time;

    public ComponentLogItem(long id, ComponentService component, String message, Bundle inputBundle, Bundle outputBundle, int logType, long time) {
        this.id = id;
        this.component = component;
        this.message = message;
        this.inputBundle = inputBundle;
        this.outputBundle = outputBundle;
        this.status = logType;
        this.time = time;
    }

    public long id() {
        return id;
    }

    public ComponentService getComponent() {
        return component;
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

    public int getStatus() {
        return status;
    }

    public long getTime() {
        return time;
    }
}
