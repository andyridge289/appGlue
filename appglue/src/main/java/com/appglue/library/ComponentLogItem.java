package com.appglue.library;

import android.database.Cursor;
import android.os.Bundle;

import com.appglue.engine.CompositeService;

import static com.appglue.Constants.ID;
import static com.appglue.library.AppGlueConstants.TBL_COMPOSITE_EXECUTION_LOG;

public class ComponentLogItem
{
    private long id;
    private String className;
    private String message;
    private Bundle inputBundle;
    private Bundle outputBundle;
    private int logType;
    private long time;

    public ComponentLogItem(long id, String className, String message, Bundle inputBundle, Bundle outputBundle, int logType, long time) {
        this.id = id;
        this.className = className;
        this.message = message;
        this.inputBundle = inputBundle;
        this.outputBundle = outputBundle;
        this.logType = logType;
        this.time = time;
    }
}
