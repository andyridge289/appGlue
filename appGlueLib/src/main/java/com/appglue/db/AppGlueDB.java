package com.appglue.db;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 *
 */
@Database(name = AppGlueDB.NAME, version = AppGlueDB.VERSION, foreignKeysSupported = true)
public class AppGlueDB {
    public static final String NAME = "AppGlueDB";
    public static final int VERSION = 1;
}
