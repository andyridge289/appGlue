package com.appglue.serviceregistry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.util.LongSparseArray;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;

import com.appglue.ComposableService;
import com.appglue.Constants.Interval;
import com.appglue.Constants.ProcessType;
import com.appglue.IODescription;
import com.appglue.TST;
import com.appglue.description.AppDescription;
import com.appglue.description.IOValue;
import com.appglue.description.ServiceDescription;
import com.appglue.description.Tag;
import com.appglue.description.datatypes.IOType;
import com.appglue.engine.description.ComponentService;
import com.appglue.engine.description.CompositeService;
import com.appglue.engine.description.ServiceIO;
import com.appglue.library.AppGlueLibrary;
import com.appglue.library.ComponentLogItem;
import com.appglue.library.LogItem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import static com.appglue.Constants.CLASSNAME;
import static com.appglue.library.AppGlueConstants.COMPOSITE_ID;
import static com.appglue.Constants.DESCRIPTION;
import static com.appglue.Constants.DEVELOPER;
import static com.appglue.Constants.FRIENDLY_NAME;
import static com.appglue.Constants.ICON;
import static com.appglue.Constants.ID;
import static com.appglue.Constants.INPUT_IO_ID;
import static com.appglue.Constants.INSTALLED;
import static com.appglue.Constants.IO_INDEX;
import static com.appglue.Constants.IO_TYPE;
import static com.appglue.Constants.I_OR_O;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.MANDATORY;
import static com.appglue.Constants.NAME;
import static com.appglue.Constants.OUTPUT_IO_ID;
import static com.appglue.Constants.PACKAGENAME;
import static com.appglue.Constants.POSITION;
import static com.appglue.Constants.PROCESS_TYPE;
import static com.appglue.Constants.SAMPLE_VALUE;
import static com.appglue.Constants.SERVICE_TYPE;
import static com.appglue.Constants.TAG;
import static com.appglue.Constants.VALUE;
import static com.appglue.library.AppGlueConstants.ACTIVE_OR_TIMER;
import static com.appglue.library.AppGlueConstants.COLS_APP;
import static com.appglue.library.AppGlueConstants.COLS_COMPONENT;
import static com.appglue.library.AppGlueConstants.COLS_COMPONENT_HAS_TAG;
import static com.appglue.library.AppGlueConstants.COLS_COMPOSITE;
import static com.appglue.library.AppGlueConstants.COLS_COMPOSITE_EXECUTION_LOG;
import static com.appglue.library.AppGlueConstants.COLS_IOCONNECTION;
import static com.appglue.library.AppGlueConstants.COLS_EXECUTION_LOG;
import static com.appglue.library.AppGlueConstants.COLS_SERVICEIO;
import static com.appglue.library.AppGlueConstants.COLS_IOTYPE;
import static com.appglue.library.AppGlueConstants.COLS_IO_DESCRIPTION;
import static com.appglue.library.AppGlueConstants.COLS_IO_SAMPLES;
import static com.appglue.library.AppGlueConstants.COLS_SD;
import static com.appglue.library.AppGlueConstants.COLS_TAG;
import static com.appglue.library.AppGlueConstants.COMPONENT_ID;
import static com.appglue.library.AppGlueConstants.DB_NAME;
import static com.appglue.library.AppGlueConstants.DB_VERSION;
import static com.appglue.library.AppGlueConstants.ENABLED;
import static com.appglue.library.AppGlueConstants.END_TIME;
import static com.appglue.library.AppGlueConstants.EXECUTION_INSTANCE;
import static com.appglue.library.AppGlueConstants.FILTER_CONDITION;
import static com.appglue.library.AppGlueConstants.FILTER_STATE;
import static com.appglue.library.AppGlueConstants.INDEX_COMPONENT_HAS_TAG;
import static com.appglue.library.AppGlueConstants.INDEX_COMPOSITE_HAS_COMPONENT;
import static com.appglue.library.AppGlueConstants.INDEX_IOCONNECTION;
import static com.appglue.library.AppGlueConstants.INDEX_EXECUTION_LOG;
import static com.appglue.library.AppGlueConstants.INDEX_FILTER;
import static com.appglue.library.AppGlueConstants.INDEX_IO_DESCRIPTION;
import static com.appglue.library.AppGlueConstants.INDEX_IO_SAMPLES;
import static com.appglue.library.AppGlueConstants.INPUT_DATA;
import static com.appglue.library.AppGlueConstants.INPUT_ID;
import static com.appglue.library.AppGlueConstants.INTERVAL;
import static com.appglue.library.AppGlueConstants.IO_DESCRIPTION_ID;
import static com.appglue.library.AppGlueConstants.IX_COMPONENT_HAS_TAG;
import static com.appglue.library.AppGlueConstants.IX_COMPOSITE_HAS_COMPONENT;
import static com.appglue.library.AppGlueConstants.IX_IOCONNECTION;
import static com.appglue.library.AppGlueConstants.IX_EXECUTION_LOG;
import static com.appglue.library.AppGlueConstants.IX_SERVICEIO;
import static com.appglue.library.AppGlueConstants.IX_IO_DESCRIPTION;
import static com.appglue.library.AppGlueConstants.IX_IO_SAMPLES;
import static com.appglue.library.AppGlueConstants.LOG_TYPE;
import static com.appglue.library.AppGlueConstants.MANUAL_VALUE;
import static com.appglue.library.AppGlueConstants.MESSAGE;
import static com.appglue.library.AppGlueConstants.NUMERAL;
import static com.appglue.library.AppGlueConstants.OUTPUT_ID;
import static com.appglue.library.AppGlueConstants.OUTPUT_DATA;
import static com.appglue.library.AppGlueConstants.SINK_IO;
import static com.appglue.library.AppGlueConstants.SOURCE_IO;
import static com.appglue.library.AppGlueConstants.START_TIME;
import static com.appglue.library.AppGlueConstants.TAG_ID;
import static com.appglue.library.AppGlueConstants.TBL_APP;
import static com.appglue.library.AppGlueConstants.TBL_COMPONENT;
import static com.appglue.library.AppGlueConstants.TBL_COMPOSITE;
import static com.appglue.library.AppGlueConstants.TBL_COMPOSITE_EXECUTION_LOG;
import static com.appglue.library.AppGlueConstants.TBL_IOCONNECTION;
import static com.appglue.library.AppGlueConstants.TBL_EXECUTION_LOG;
import static com.appglue.library.AppGlueConstants.TBL_SERVICEIO;
import static com.appglue.library.AppGlueConstants.TBL_IOTYPE;
import static com.appglue.library.AppGlueConstants.TBL_IO_SAMPLE;
import static com.appglue.library.AppGlueConstants.TBL_SD;
import static com.appglue.library.AppGlueConstants.TBL_SD_HAS_TAG;
import static com.appglue.library.AppGlueConstants.TBL_IO_DESCRIPTION;
import static com.appglue.library.AppGlueConstants.TBL_TAG;
import static com.appglue.library.AppGlueConstants.TERMINATED;
import static com.appglue.library.AppGlueConstants.TIME;

public class LocalDBHandler extends SQLiteOpenHelper {
    private TST<AppDescription> appMap;

//    private LongSparseArray<CompositeService> compositeMap;
    private TST<ServiceDescription> componentMap;
    private LongSparseArray<IODescription> ioMap;

    // These are the ones that get cached immediately for speed-ness.
    private LongSparseArray<IOType> typeMap;
    private LongSparseArray<Tag> tagMap;

    private ArrayList<String> queries = new ArrayList<String>();

    private Object compositeLock = new Object();

    /**
     * Creates a new class to handle all the database crap
     *
     * @param context The context we need to create the stuff
     */
    public LocalDBHandler(Context context, Registry registry) {
        super(context, DB_NAME, null, DB_VERSION);

        appMap = new TST<AppDescription>();
        componentMap = new TST<ServiceDescription>();

//        compositeMap = new LongSparseArray<CompositeService>();
        ioMap = new LongSparseArray<IODescription>();
        typeMap = new LongSparseArray<IOType>();
        tagMap = new LongSparseArray<Tag>();

        cacheIOTypes();
        cacheTags();

        // Recreate the database every time for now while we are testing
//        recreate();
    }

    // FIXME Probably want to lock some of the entries/exists

    @Override
    /**
     * Automagically called when the database needs to be created, usually on an upgrade
     *
     * @param db The database to be edited
     */
    public void onCreate(SQLiteDatabase db) {
        create(db);
    }

    /**
     * Indicate the database should be created again
     */
    public void recreate() {
        SQLiteDatabase db = this.getWritableDatabase();
        onCreate(db);
    }


    @Override
    /**
     * Called when the database needs to be upgraded
     *
     * @param db            The database to be upgraded
     * @param oldVersion    The old version number of the database
     * @param newVersion    The new version number of the database
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }


    /**
     * Create all of the tables that we need to create
     * Atomic
     * Atomic_has_io
     * Parameter
     * Atomic_has_parameter
     * Composite
     * Composite_has_atomic
     * Composite_has_parameter
     *
     * @param db The database that everything should be created in
     */
    private void create(SQLiteDatabase db) {

        execSQL(db, String.format("DROP TABLE IF EXISTS %s", TBL_COMPOSITE));
        execSQL(db, AppGlueLibrary.createTableString(TBL_COMPOSITE, COLS_COMPOSITE));

        execSQL(db, String.format("DROP TABLE IF EXISTS %s", TBL_APP));
        execSQL(db, AppGlueLibrary.createTableString(TBL_APP, COLS_APP));

        execSQL(db, String.format("DROP TABLE IF EXISTS %s", TBL_SD));
        execSQL(db, AppGlueLibrary.createTableString(TBL_SD, COLS_SD));

        execSQL(db, String.format("DROP TABLE IF EXISTS %s", TBL_IOTYPE));
        execSQL(db, AppGlueLibrary.createTableString(TBL_IOTYPE, COLS_IOTYPE));

        execSQL(db, String.format("DROP TABLE IF EXISTS %s", TBL_TAG));
        execSQL(db, AppGlueLibrary.createTableString(TBL_TAG, COLS_TAG));

        // references Component
        execSQL(db, String.format("DROP TABLE IF EXISTS %s", TBL_IO_DESCRIPTION));
        execSQL(db, AppGlueLibrary.createTableString(TBL_IO_DESCRIPTION, COLS_IO_DESCRIPTION));

        // references ServiceIO
        execSQL(db, String.format("DROP TABLE IF EXISTS %s", TBL_IO_SAMPLE));
        execSQL(db, AppGlueLibrary.createTableString(TBL_IO_SAMPLE, COLS_IO_SAMPLES));

        // references Composite and component
        execSQL(db, String.format("DROP TABLE IF EXISTS %s", TBL_COMPONENT));
        execSQL(db, AppGlueLibrary.createTableString(TBL_COMPONENT, COLS_COMPONENT));

        execSQL(db, String.format("DROP TABLE IF EXISTS %s", TBL_COMPOSITE_EXECUTION_LOG));
        execSQL(db, AppGlueLibrary.createTableString(TBL_COMPOSITE_EXECUTION_LOG, COLS_COMPOSITE_EXECUTION_LOG));

        execSQL(db, String.format("DROP TABLE IF EXISTS %s", TBL_EXECUTION_LOG));
        execSQL(db, AppGlueLibrary.createTableString(TBL_EXECUTION_LOG, COLS_EXECUTION_LOG));

        // references Component and Tag
        execSQL(db, String.format("DROP TABLE IF EXISTS %s", TBL_SD_HAS_TAG));
        execSQL(db, AppGlueLibrary.createTableString(TBL_SD_HAS_TAG, COLS_COMPONENT_HAS_TAG));

        // references Component, composite and ServiceIO
        execSQL(db, String.format("DROP TABLE IF EXISTS %s", TBL_IOCONNECTION));
        execSQL(db, AppGlueLibrary.createTableString(TBL_IOCONNECTION, COLS_IOCONNECTION));

        execSQL(db, String.format("DROP TABLE IF EXISTS %s", TBL_SERVICEIO));
        execSQL(db, AppGlueLibrary.createTableString(TBL_SERVICEIO, COLS_SERVICEIO));

        db.execSQL(AppGlueLibrary.createIndexString(TBL_SERVICEIO, IX_SERVICEIO, INDEX_FILTER));
        db.execSQL(AppGlueLibrary.createIndexString(TBL_IOCONNECTION, IX_IOCONNECTION, INDEX_IOCONNECTION));
        db.execSQL(AppGlueLibrary.createIndexString(TBL_IO_DESCRIPTION, IX_IO_DESCRIPTION, INDEX_IO_DESCRIPTION));
        db.execSQL(AppGlueLibrary.createIndexString(TBL_SD_HAS_TAG, IX_COMPONENT_HAS_TAG, INDEX_COMPONENT_HAS_TAG));
        db.execSQL(AppGlueLibrary.createIndexString(TBL_EXECUTION_LOG, IX_EXECUTION_LOG, INDEX_EXECUTION_LOG));
        db.execSQL(AppGlueLibrary.createIndexString(TBL_COMPONENT, IX_COMPOSITE_HAS_COMPONENT, INDEX_COMPOSITE_HAS_COMPONENT));
        db.execSQL(AppGlueLibrary.createIndexString(TBL_IO_SAMPLE, IX_IO_SAMPLES, INDEX_IO_SAMPLES));

        postCreateInsert(db);
    }

    private void execSQL(SQLiteDatabase db, String q) {
        queries.add(q + ";\n");
        db.execSQL(q);
    }

    private Cursor rawQuery(SQLiteDatabase db, String q, String[] selectionArgs) {
        queries.add(q + "; \n");
        return db.rawQuery(q, selectionArgs);
    }

    public void dumpSQLLog() throws IOException {
        File out = new File(Environment.getExternalStorageDirectory(), "appgluedb.txt");
        FileWriter f = new FileWriter(out);

        for (String s : queries) {
            f.write(s);
        }

        f.flush();
        f.close();
        Log.e(TAG, "Dumped");
    }

    /**
     * Anything that needs to be put in the database without making a component do it.
     * IO Samples:
     * Boolean
     *
     * Also initialise the temporary composite
     *
     * @param db The Database
     */
    private void postCreateInsert(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        cv.put(IO_DESCRIPTION_ID, -1);
        cv.put(NAME, "True");
        cv.put(VALUE, true);
        db.insertOrThrow(TBL_IO_SAMPLE, null, cv);

        cv = new ContentValues();
        cv.put(IO_DESCRIPTION_ID, -1);
        cv.put(NAME, "False");
        cv.put(VALUE, false);
        db.insertOrThrow(TBL_IO_SAMPLE, null, cv);

        // This is where we initialise the temporary Composite
        cv = new ContentValues();
        cv.put(ID, CompositeService.TEMP_ID);
        cv.put(NAME, CompositeService.TEMP_NAME);
        cv.put(DESCRIPTION, CompositeService.TEMP_DESCRIPTION);
        cv.put(ACTIVE_OR_TIMER, 1);
        cv.put(ENABLED, 1);
        cv.put(NUMERAL, 0);
        cv.put(INTERVAL, Interval.NONE.index);
        long id = db.insertOrThrow(TBL_COMPOSITE, null, cv);
        if (id != 1) {
            Log.e(TAG, "The temp has been inserted somewhere that isn't 1. This is a problem");
        }
    }

    public CompositeService saveTempAsComposite(String name, CompositeService temp) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(NAME, name);
        cv.put(DESCRIPTION, "");
        cv.put(ACTIVE_OR_TIMER, temp.containsTrigger());
        cv.put(ENABLED, temp.isEnabled());
        cv.put(NUMERAL, temp.getNumeral());
        cv.put(INTERVAL, temp.getInterval().index);

        long id = db.insertOrThrow(TBL_COMPOSITE, null, cv);

        cv = new ContentValues();
        cv.put(COMPOSITE_ID, id);

        String[] tables = new String[]{TBL_COMPONENT, TBL_EXECUTION_LOG, TBL_IOCONNECTION, TBL_SERVICEIO
        };

        for (String table : tables)
            db.update(table, cv, COMPOSITE_ID + " = ?", new String[]{ "" + CompositeService.TEMP_ID });

        return getComposite(id);
    }

    /**
     * Delete all of the stuff in the other tables that references the TEMP.
     */
    public CompositeService resetTemp() {

        SQLiteDatabase db = this.getWritableDatabase();

        int num = db.delete(TBL_COMPONENT, COMPOSITE_ID + " = ?", new String[]{ "" + CompositeService.TEMP_ID });
        if(LOG) Log.d(TAG, "Reset temp: " + num + " deleted from component");

        num = db.delete(TBL_SERVICEIO, COMPOSITE_ID + " = ?", new String[]{ "" + CompositeService.TEMP_ID });
        if(LOG) Log.d(TAG, "Reset temp: " +num + " deleted from service IO");

        num = db.delete(TBL_IOCONNECTION, COMPOSITE_ID + " = ?", new String[]{ "" + CompositeService.TEMP_ID });
        if(LOG) Log.d(TAG, "Reset temp: " +num + " deleted from io connection");

        num = db.delete(TBL_EXECUTION_LOG, COMPOSITE_ID + " = ?", new String[]{ "" + CompositeService.TEMP_ID });
        if(LOG) Log.d(TAG, "Reset temp: " +num + " deleted from execution log");

        num = db.delete(TBL_COMPOSITE_EXECUTION_LOG, COMPOSITE_ID + " = ?", new String[]{ "" + CompositeService.TEMP_ID });
        if(LOG) Log.d(TAG, "Reset temp: " +num + " deleted from composite execution log");

        return getComposite(CompositeService.TEMP_ID);
    }

    /**
     * Adds a new atomic service for the given service description
     *
     * @param sd The object representation of the component
     * @return The getID of the inserted component
     */
    public ServiceDescription addServiceDescription(ServiceDescription sd) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NAME, sd.getName());
        values.put(CLASSNAME, sd.getClassName());
        values.put(PACKAGENAME, sd.getPackageName());
        values.put(DESCRIPTION, sd.getDescription());

        values.put(SERVICE_TYPE, sd.getServiceType().index);
        values.put(PROCESS_TYPE, sd.getProcessType().index);

        int inputSuccess = 0;
        int outputSuccess = 0;

        try {
            db.insertOrThrow(TBL_SD, null, values);

            if(sd.getApp() != null) {
                // Try to get the app out of the database
                AppDescription app = getAppDescription(sd.getApp().getPackageName());
                if(app == null) {
                    addAppDescription(sd.getApp());
                }
            }

            if (sd.hasInputs()) {
                if (adIODescription(sd.getInputs(), true, sd.getClassName()))
                    inputSuccess = 1;
                else
                    inputSuccess = -1;
            }

            if (sd.hasOutputs()) {
                if (adIODescription(sd.getOutputs(), false, sd.getClassName()))
                    outputSuccess = 1;
                else
                    outputSuccess = -1;
            }

            if (sd.hasTags()) {
                addTagsForComponent(sd);
            }
        } catch (SQLiteConstraintException e) {
            // This means that that key is already in the database
            return null;
        }

        if (inputSuccess == -1)
            Log.e(TAG, "Failed to add getInputs for " + sd.getClassName());

        if (outputSuccess == -1)
            Log.e(TAG, "Failed to add getOutputs for " + sd.getClassName());

        return sd;
    }

    // Add the sample values to the database
    private boolean adIODescription(ArrayList<IODescription> ios, boolean input, String className) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean failures = false;

        for (IODescription io : ios) {
            ContentValues values = new ContentValues();

            values.put(NAME, io.getName());
            values.put(FRIENDLY_NAME, io.getFriendlyName());
            values.put(IO_INDEX, io.getIndex());
            values.put(DESCRIPTION, io.description());

            if (!input)
                values.put(MANDATORY, 0);
            else
                values.put(MANDATORY, io.isMandatory() ? 1 : 0);

            IOType type = io.getType();
            if (type.getID() == -1) // Then it hasn't been validated, see if it's in the database
            {
                long ioId = this.getInputOutputIfExists(type.getName(), type.getClass().getCanonicalName()).getID();
                values.put(IO_TYPE, ioId);
                type.setID(ioId);
            } else {
                values.put(IO_TYPE, type.getID());
            }

            // So now we have the IO Type ID
            values.put(CLASSNAME, className);
            values.put(I_OR_O, input ? 1 : 0);

            long ioId = db.insertOrThrow(TBL_IO_DESCRIPTION, null, values);
            io.setId(ioId);

            if (ioId == -1) {
                failures = true;
            } else {
                boolean sampleSuccess = addSampleValues(ioId, io);
                if (!sampleSuccess)
                    failures = true;
            }
        }

        return !failures;
    }

    private boolean addSampleValues(long ioId, IODescription io) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean win = true;

        ArrayList<IOValue> samples = io.getSampleValues();

        for (IOValue sample : samples) {
            ContentValues cv = new ContentValues();
            cv.put(IO_DESCRIPTION_ID, ioId);
            cv.put(NAME, sample.name);

            // The value could be anything really - This might be working but I'm really not sure....
            String stringValue = io.getType().toString(sample.value);
            cv.put(VALUE, stringValue);

            long sampleId = db.insertOrThrow(TBL_IO_SAMPLE, null, cv);
            sample.setID(sampleId);

            if (sampleId == -1)
                win = false;
        }

        return win;
    }

    private IOValue getIOValue(long sampleValue, IODescription io) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.query(TBL_IO_SAMPLE, null,
                ID + " = ?", new String[]{"" + sampleValue},
                null, null, null, null);


        if (c == null) {
            Log.e(TAG, "Unable to get io value: " + io.getFriendlyName());
            return null;
        }

        if (c.getCount() == 0) {
            Log.e(TAG, "No rows for that id: " + sampleValue + " .... How have you managed that?");
            return null;
        }

        c.moveToFirst();

        String name = c.getString(c.getColumnIndex(NAME));
        String stringValue = c.getString(c.getColumnIndex(VALUE));

        //  Work out value of thing
        IOType type = io.getType();

        // Getting the value back
        Object value = type.fromString(stringValue);
        return new IOValue(sampleValue, name, value);
    }

    public ArrayList<ServiceDescription> getComponentsForApp(String packageName) {
        ArrayList<ServiceDescription> components = new ArrayList<ServiceDescription>();

        SQLiteDatabase db = this.getReadableDatabase();

        String query = String.format("SELECT * FROM %s WHERE %s = \"%s\"", TBL_SD, PACKAGENAME, packageName);
        Cursor c = rawQuery(db, query, null);

        if (c == null || c.getCount() == 0) {
            // All has failed
            return components;
        }

        c.moveToFirst();

        do {
            ServiceDescription sd = ServiceDescription.createFromCursor(c, "");
            sd.setInputs(getServiceIOs(sd, true));
            sd.setOutputs(getServiceIOs(sd, false));
            components.add(sd);
        }
        while (c.moveToNext());
        c.close();

        return components;
    }

    private ArrayList<IODescription> getServiceIOs(ServiceDescription sd, boolean input) {
        ArrayList<IODescription> ios = new ArrayList<IODescription>();

        SQLiteDatabase db = this.getReadableDatabase();
        String textInput = input ? "1" : "0";
        Cursor c = db.query(TBL_IO_DESCRIPTION, null,
                CLASSNAME + " = ? AND " + I_OR_O + " = ?", new String[]{sd.getClassName(), textInput},
                null, null, null, null);

        if (c == null) {
            Log.e(TAG, String.format("getServiceIO() %s %s: Cursor dead", sd.getClassName(), "" + input));
            return null;
        }

        if (c.getCount() == 0) {
            return ios;
        }

        c.moveToFirst();

        do {
            long id = c.getLong(c.getColumnIndex(ID));
            String name = c.getString(c.getColumnIndex(NAME));
            String friendlyName = c.getString(c.getColumnIndex(FRIENDLY_NAME));
            int index = c.getInt(c.getColumnIndex(IO_INDEX));
            boolean mandatory = c.getInt(c.getColumnIndex(MANDATORY)) == 1;

            IOType type = getIOType(c.getLong(c.getColumnIndex(IO_TYPE)));
            String description = c.getString(c.getColumnIndex(DESCRIPTION));

            IODescription io = new IODescription(id, name, friendlyName, index, type, description, sd, mandatory, new ArrayList<IOValue>(), input);
            getSampleValues(io);

            ios.add(io);
        }
        while (c.moveToNext());
        c.close();

        return ios;
    }

    private void getSampleValues(IODescription io) {
        SQLiteDatabase db = this.getReadableDatabase();
        String q = String.format(Locale.ENGLISH, "SELECT * FROM %s WHERE %s = %d", TBL_IO_SAMPLE, IO_DESCRIPTION_ID, io.getID());

        Cursor c = rawQuery(db, q, null);

        if (c == null) {
            Log.e(TAG, "Getting sample values fail, or there aren't any...");
            return;
        }

        if (c.getCount() == 0) {
            // This should just mean that there aren't any
            return;
        }

        c.moveToFirst();

        do {
            // Build and set up the IO values
            long id = c.getLong(c.getColumnIndex(ID));
            String name = c.getString(c.getColumnIndex(NAME));
            String stringValue = c.getString(c.getColumnIndex(VALUE));

            IOType type = io.getType();

            IOValue value = new IOValue(id, name);
            if (type.equals(IOType.Factory.getType(IOType.Factory.NUMBER)))
                value.setValue(Integer.parseInt(stringValue));
            else if (type.equals(IOType.Factory.getType(IOType.Factory.BOOLEAN)))
                value.setValue(Boolean.parseBoolean(stringValue));
            else // It's probably just a string, so a string representation will do...
                value.setValue(stringValue);

            io.addSampleValue(value);
        }
        while (c.moveToNext());
        c.close();
    }

    /**
     * We only need the intermediary table for going the other way I think!!
     *
     * @param packageName The name of the package to check for apps
     * @return An object representing the app description
     */
    private AppDescription getAppDescription(String packageName) {
        if (appMap.contains(packageName))
            return appMap.get(packageName);

        SQLiteDatabase db = this.getReadableDatabase();
        String query = String.format("SELECT * FROM %s WHERE %s = \"%s\"", TBL_APP, PACKAGENAME, packageName);
        Cursor c = rawQuery(db, query, null);

        if (c == null)
            return null;

        if (c.getCount() == 0)
            return null;

        c.moveToFirst();

        AppDescription app = AppDescription.parseFromCursor(c);
        appMap.put(packageName, app);
        return app;
    }

    private long addAppDescription(AppDescription app) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PACKAGENAME, app.getPackageName());
        values.put(NAME, app.getName());
        values.put(ICON, app.iconLocation());
        values.put(DESCRIPTION, app.getDescription());
        values.put(DEVELOPER, app.getDeveloper());
        values.put(INSTALLED, 1);

        return db.insertOrThrow(TBL_APP, null, values);
    }


    /**
     * Returns an IO if it exists, adds it if it doesn't
     *
     * @param name      The name of the IOType to be searched for/added
     * @param className The name of the class
     * @return the IO Type
     */
    public IOType getInputOutputIfExists(String name, String className) {
        if (className.equals("")) {
            return null;
        }

        IOType type = getIOType(className);

        if (type == null) {
            long id = addInputOutput(name, className);
            type = getIOType(id);
        }


        return type;
    }


    /**
     * Returns an IOType
     *
     * @param ioId The ID of the IOType to be got
     * @return The IOType
     */
    public IOType getIOType(long ioId) {
        // Get it from the cache if it's in there
        if (typeMap.get(ioId) != null)
            return typeMap.get(ioId);

        SQLiteDatabase db = this.getWritableDatabase();

        String sql = String.format("SELECT * FROM %s WHERE %s = %s", TBL_IOTYPE, ID, "" + ioId);
        Cursor c = rawQuery(db, sql, null);

        if (c == null) {
            Log.e(TAG, String.format("getIOType() %d: Cursor dead", ioId));
            return null;
        }

        if (c.getCount() == 0) {
            return null;
        }

        c.moveToFirst();

        long id = c.getLong(c.getColumnIndex(ID));
        String className = c.getString(c.getColumnIndex(CLASSNAME));
        c.close();

        IOType type = IOType.Factory.getType(className);
        type.setID(id);

        return type;
    }


    /**
     * Gets an IOType, given its classname
     *
     * @param inputClassName The name of the class of the IOType
     * @return The IOType
     */
    public IOType getIOType(String inputClassName) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.query(TBL_IOTYPE, new String[]{ID, NAME, CLASSNAME}, CLASSNAME + " = ?",
                            new String[]{ "" + inputClassName }, null, null, null);

        if (c == null) {
            Log.e(TAG, String.format("getIOType() %s: Cursor dead", inputClassName));
            return null;
        }

        if (c.getCount() == 0) {
            return null;
        }

        c.moveToFirst();

        long id = c.getLong(c.getColumnIndex(ID));
        String className = c.getString(c.getColumnIndex(CLASSNAME));

        IOType type = IOType.Factory.getType(className);
        type.setID(id);
        return type; //new IOType(getID, name, className);
    }


    /**
     * Adds an IOType with the given class name
     *
     * @param name      The name of the IOType to be added
     * @param className the class name of the IO to be added
     * @return The ID of the inserted IOType
     */
    public long addInputOutput(String name, String className) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NAME, name);
        values.put(CLASSNAME, className);

        return db.insertOrThrow(TBL_IOTYPE, null, values);
    }

    public CompositeService addComposite(CompositeService cs) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NAME, cs.getName());
        values.put(DESCRIPTION, cs.getDescription());
        values.put(ENABLED, cs.isEnabled());
        values.put(ACTIVE_OR_TIMER, 0);
        values.put(INTERVAL, cs.getInterval().index);
        values.put(NUMERAL, cs.getNumeral());

        long compositeId = db.insertOrThrow(TBL_COMPOSITE, null, values);
        cs.setID(compositeId);

        SparseArray<ComponentService> components = cs.getComponents();

        deleteAllComponents(cs);
        for (int i = 0; i < components.size(); i++) {
            long linkId = this.addComponent(components.get(i));
            components.get(i).setID(linkId);
        }

        // We can only setup all the IO connections when all of the components and serviceIOs have IDs!
        boolean connectionSuccess = addIOConnections(cs);

        if(!connectionSuccess) {
            Log.e(TAG, "Failed at connecting all of the service IOs for " + cs.getID() + ": " + cs.getName());
        }

        return cs;
    }

    /**
     *
     * @param cs
     * @return
     */
    private boolean addIOConnections(CompositeService cs) {
        SQLiteDatabase db = this.getWritableDatabase();

        boolean success = true;

        for(int i = 0; i < cs.getComponents().size(); i++) {
            ComponentService component = cs.getComponents().valueAt(i);

            for(int j = 0; j < component.getOutputs().size(); j++) {
                ServiceIO io = component.getOutputs().get(j);

                if(!io.hasConnection() || io.getID() == -1)
                    continue;

                ContentValues values = new ContentValues();
                values.put(COMPOSITE_ID, cs.getID());
                values.put(SOURCE_IO, io.getID());
                values.put(SINK_IO, io.getConnection().getID());

                // We aren't keeping track of the IDs for this
                long id = db.insertOrThrow(TBL_IOCONNECTION, null, values);
                if(id == -1)
                    success = false;
            }
        }

        return success;
    }

    public CompositeService updateComposite(CompositeService composite) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (composite.getID() == -1) {
            // This is a new one and it needs to be added from scratch
            return addComposite(composite);
        } else {

            ContentValues values = new ContentValues();
            values.put(NAME, composite.getName());
            values.put(DESCRIPTION, composite.getDescription());
            values.put(ENABLED, composite.isEnabled());
            values.put(NUMERAL, composite.getNumeral());
            values.put(INTERVAL, composite.getInterval().index);
            values.put(ACTIVE_OR_TIMER, composite.containsTrigger());

            int ret = db.update(TBL_COMPOSITE, values, ID + " = ?", new String[]{ "" + composite.getID() });
            if(ret != 1) {
                Log.e(TAG, "Updated " + ret + " values for " + composite.getID() + " (" + composite.getName() + ")");
            }

            SparseArray<ComponentService> components = composite.getComponents();

            for(int i = 0; i < components.size(); i++) {
                ComponentService component = updateComponent(components.valueAt(i));
                if(!component.equals(components.valueAt(i))) {
                    Log.e(TAG, "Component " + components.valueAt(i).getID() + " update failed for " + composite.getID());
                }
            }

            updateWiring(composite);
            return getComposite(composite.getID());
        }
    }

    private ComponentService updateComponent(ComponentService component) {

        SQLiteDatabase db = this.getWritableDatabase();

        if(component.getID() == -1) {
            // This is a new one and it needs to be added from scratch
            return getComponent(addComponent(component), component.getComposite());
        } else {

            ContentValues values = new ContentValues();
            values.put(COMPOSITE_ID, component.getComposite().getID());
            values.put(CLASSNAME, component.getDescription().getClassName());
            values.put(POSITION, component.getPosition());

            int ret = db.update(TBL_COMPONENT, values, ID + " = ?", new String[]{ "" + component.getID() });
            if(ret != 1) {
                Log.e(TAG, "Updated " + ret + " values for " + component.getID() + " (" + component.getDescription().getName() + ")");
            }

            ArrayList<ServiceIO> inputs = component.getInputs();
            for (int i = 0; i < inputs.size(); i++) {
                ServiceIO io = updateServiceIO(inputs.get(i));
                if(!io.equals(inputs.get(i))) {
                    Log.e(TAG, "IO " + inputs.get(i).getID() + " update failed for " + component.getID());
                }
            }

            ArrayList<ServiceIO> outputs = component.getOutputs();
            for (int i = 0; i < outputs.size(); i++) {
                ServiceIO io = updateServiceIO(outputs.get(i));
                if(!io.equals(outputs.get(i))) {
                    Log.e(TAG, "IO " + outputs.get(i).getID() + " update failed for " + component.getID());
                }
            }
        }

        return getComponent(component.getID(), component.getComposite());
    }

    private ServiceIO updateServiceIO(ServiceIO io) {

        SQLiteDatabase db = this.getWritableDatabase();

        if(io.getID() == -1) {
            // This is a new one and it needs to be added from scratch
            addServiceIO(io);
            return io;
        } else {

            ContentValues values = new ContentValues();
            values.put(COMPONENT_ID, io.getComponent().getID());
            values.put(COMPOSITE_ID, io.getComponent().getComposite().getID());
            values.put(IO_DESCRIPTION_ID, io.getDescription().getID());
            values.put(FILTER_STATE, io.getFilterState());
            values.put(FILTER_CONDITION, io.getCondition());

            if(io.getManualValue() != null) {
                String manualValue = io.getDescription().getType().toString(io.getManualValue());
                values.put(MANUAL_VALUE, manualValue);
            } else {
                values.putNull(MANUAL_VALUE);
            }

            if(io.getChosenSampleValue() != null) {
                values.put(SAMPLE_VALUE, io.getChosenSampleValue().getID());
            }

            int ret = db.update(TBL_SERVICEIO, values, ID + " = ?", new String[]{ "" + io.getID() });

            if(ret != 1) {
                Log.e(TAG, "Updated " + ret + " values for " + io.getID() + " (" + io.getDescription().getName() + ")");
            }

            return io;
        }
    }

    public void updateWiring(CompositeService cs) {
        // Just delete them all and then re-add them all again - this seems easiest?
        SQLiteDatabase db = this.getWritableDatabase();
        int delCount = db.delete(TBL_IOCONNECTION, COMPOSITE_ID + "=" + cs.getID(), null);

        if (LOG) Log.d(TAG, "DBUPDATE: Deleted " + delCount + " rows for " + cs.getName());

        // This could be zero to be fair
        addIOConnections(cs);
    }

    public boolean deleteComposite(CompositeService cs) {
        long id = cs.getID();

        SQLiteDatabase db = this.getWritableDatabase();
        int cStatus = db.delete(TBL_COMPOSITE, ID + "=?", new String[]{"" + id});
        int chcStatus = db.delete(TBL_COMPONENT, ID + "=?", new String[]{"" + id});
        int cioStatus = db.delete(TBL_IOCONNECTION, ID + "=?", new String[]{"" + id});
        int fStatus = db.delete(TBL_SERVICEIO, ID + "=?", new String[]{"" + id});

        return cStatus == -1 || chcStatus == -1 || cioStatus == -1 || fStatus == -1;
    }

    public ArrayList<CompositeService> getIntendedRunningComposites() {
        ArrayList<CompositeService> serviceList = new ArrayList<CompositeService>();
        String query = String.format(Locale.getDefault(), "SELECT * FROM %s WHERE %s = %d", TBL_COMPOSITE, ACTIVE_OR_TIMER, 1);

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = rawQuery(db, query, null);

        if (c == null) {
            Log.e(TAG, "Running cursor dead - getIntendedRunningComposites");
            return serviceList;
        }

        if (c.getCount() == 0) {
            return serviceList;
        }

        if (c.moveToFirst()) {
            do {
                long id = c.getLong(c.getColumnIndex(ID));
                String name = c.getString(c.getColumnIndex(NAME));
                long numeral = c.getLong(c.getColumnIndex(NUMERAL));
                int intervalIndex = c.getInt(c.getColumnIndex(INTERVAL));
                Interval interval = Interval.values()[intervalIndex];

                CompositeService cs = getComposite(id);
                serviceList.add(new CompositeService(id, name, cs.getComponents(), numeral, interval));
            }
            while (c.moveToNext());
            c.close();
        }

        return serviceList;
    }

    /**
     * Returns whether it should be running, and then whether it is running
     *
     * @param id The getID of the composite to find out about the running status
     * @return The running status of the composite
     */
    public Boolean compositeEnabled(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        if (id == -1) {
            // ID not set, don't really know what to do here
            return null;
        }

        Cursor c = db.query(TBL_COMPOSITE, new String[]{ACTIVE_OR_TIMER}, ID + "=?", new String[]{"" + id}, null, null, null);
        if (c == null)
            return false;

        c.moveToFirst();

        long should = c.getLong(c.getColumnIndex(ACTIVE_OR_TIMER));

        return should == 1;
    }

    /**
     * Sets whether the composite service should be running or not
     *
     * @param id      The getID of the composite
     * @param running the new active status
     * @return The success of this?
     */
    public boolean setCompositeActive(long id, long running) {
        return setCompositeRunning(id, running, ACTIVE_OR_TIMER);
    }

    /**
     * So we don't have such code duplication with two methods doing essentially
     * the same thing.
     *
     * @param id      The getID of the composite
     * @param running The running status of the composite
     * @param type    Custom type
     * @return The status
     */
    private boolean setCompositeRunning(long id, long running, String type) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (id == -1) {
            // Id not set, don't really know what to do here
            return false;
        }

        String strFilter = ID + "=" + id;
        ContentValues args = new ContentValues();
        args.put(type, running);
        int retval = db.update(TBL_COMPOSITE, args, strFilter, null);

        return retval != 0;
    }


    /**
     * ********************************************************************************
     * Link from composite service to atomic service stuff
     * ********************************************************************************
     */

    private int deleteAllComponents(CompositeService cs) {

        if(cs.getID() == -1)
            return 0;

        SQLiteDatabase db = this.getWritableDatabase();
        int num = db.delete(TBL_COMPONENT, COMPOSITE_ID + " = ? ", new String[]{"" + cs.getID()});

        SparseArray<ComponentService> components = cs.getComponents();
        for(int i = 0; i < components.size(); i++) {
            ComponentService component = components.valueAt(i);
            if(component.getID() == -1)
                continue;

            num += db.delete(TBL_SERVICEIO, COMPONENT_ID + " = ?", new String[]{ "" + component.getID() } );

            for(ServiceIO io : component.getInputs()) {
                if(io.getID() == -1)
                    continue;

                num += db.delete(TBL_IOCONNECTION, SINK_IO + " = ?", new String[]{ "" + io.getID() });
            }


            for(ServiceIO io : component.getOutputs()) {
                if(io.getID() == -1)
                    continue;

                num += db.delete(TBL_IOCONNECTION, SOURCE_IO + " = ?", new String[]{ "" + io.getID() });
            }
        }

        return num;
    }

    public long addComponent(ComponentService component) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COMPOSITE_ID, component.getComposite().getID());
        values.put(CLASSNAME, component.getDescription().getClassName());
        values.put(POSITION, component.getPosition());

        long componentId = db.insertOrThrow(TBL_COMPONENT, null, values);
        component.setID(componentId);

        ArrayList<ServiceIO> inputs = component.getInputs();
        for (int i = 0; i < inputs.size(); i++) {
            long ioId = addServiceIO(inputs.get(i));
            inputs.get(i).setID(ioId);
        }

        ArrayList<ServiceIO> outputs = component.getOutputs();
        for (int i = 0; i < component.getOutputs().size(); i++) {
            long ioId = addServiceIO(outputs.get(i));
            outputs.get(i).setID(ioId);
        }

        return componentId;
    }

    public long addServiceIO(ServiceIO io) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COMPONENT_ID, io.getComponent().getID());
        values.put(COMPOSITE_ID, io.getComponent().getComposite().getID());
        values.put(IO_DESCRIPTION_ID, io.getDescription().getID());
        values.put(FILTER_STATE, io.getFilterState());
        values.put(FILTER_CONDITION, io.getCondition());

        if(io.getManualValue() != null) {
            String manualValue = io.getDescription().getType().toString(io.getManualValue());
            values.put(MANUAL_VALUE, manualValue);
        } else {
            values.putNull(MANUAL_VALUE);
        }

        if(io.getChosenSampleValue() != null) {
            values.put(SAMPLE_VALUE, io.getChosenSampleValue().getID());
        }

        long id = db.insertOrThrow(TBL_SERVICEIO, null, values);
        if(id == -1) {
            Log.e(TAG, "Failed to add IO for " + io.getComponent().getID() + ": " + io.getDescription().getFriendlyName());
            return id;
        }

        io.setID(id);
        return id;
    }

    public boolean compositeExistsWithName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = String.format("SELECT * FROM %s WHERE %s = \"%s\"", TBL_COMPOSITE,
                NAME, name);

        Cursor c = rawQuery(db, query, null);

        if (c == null) {
            Log.e(TAG, String.format("Cursor dead compositeExistsWithName %s", name));
            return false;
        }

        return c.getCount() != 0;

    }

    public boolean setTimerDuration(long compositeId, long numeral, Interval interval) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(NUMERAL, numeral);
        values.put(INTERVAL, interval.index);

        String strFilter = ID + "=" + compositeId;
        int retval = db.update(TBL_COMPOSITE, values, strFilter, null);

        return retval == 1;
    }

    public Pair<Long, Interval> getTimerDuration(long compositeId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = String.format("SELECT * FROM %s WHERE %s = %s", TBL_COMPOSITE, ID, compositeId);

        Cursor c = rawQuery(db, query, null);

        if (c == null) {
            Log.e(TAG, "Cursor null getTimerDuration " + compositeId);
            return null;
        }

        if (c.getCount() == 0) {
            Log.e(TAG, "No rows getTimerDuration " + compositeId);
            return null;
        }

        c.moveToFirst();

        long numeral = c.getLong(c.getColumnIndex(NUMERAL));
        int intervalValue = c.getInt(c.getColumnIndex(INTERVAL));

        Interval interval;

        if (intervalValue == Interval.SECONDS.index) {
            interval = Interval.SECONDS;
        } else if (intervalValue == Interval.MINUTES.index) {
            interval = Interval.MINUTES;
        } else if (intervalValue == Interval.HOURS.index) {
            interval = Interval.HOURS;
        } else {
            interval = Interval.DAYS;
        }

        return new Pair<Long, Interval>(numeral, interval);
    }

    public boolean updateFiltersAndValues(CompositeService cs) {
        SQLiteDatabase db = this.getWritableDatabase();
        SparseArray<ComponentService> components = cs.getComponents();
        int runningFailures = 0;

        // Delete all of the filters for that particular composite
        int numRemoved = db.delete(TBL_SERVICEIO, COMPOSITE_ID + " = ?", new String[]{"" + cs.getID()});
        if (LOG)
            Log.d(TAG, "Removed " + numRemoved + " from Filter for " + cs.getID() + "(" + cs.getName() + ")");

        for (int i = 0; i < components.size(); i++) {
            // Do the filter conditions on the getOutputs first
            ComponentService c = components.valueAt(i);
            ArrayList<ServiceIO> inputs = c.getInputs();
            for (ServiceIO o : inputs) {
                if (o.getFilterState() != ServiceIO.UNFILTERED) {

                    ContentValues values = new ContentValues();

                    values.put(COMPOSITE_ID, cs.getID());
                    values.put(COMPONENT_ID, c.getID());
                    values.put(IO_DESCRIPTION_ID, o.getDescription().getID());

                    // Set the filter state
                    values.put(FILTER_STATE, o.getFilterState());

                    values.put(MANUAL_VALUE, o.getManualValue().toString());
                    values.put(SAMPLE_VALUE, o.getChosenSampleValue().id);
                    values.put(FILTER_CONDITION, o.getCondition());

                    long ret = db.insertOrThrow(TBL_SERVICEIO, null, values);
                    if (ret == -1) {
                        runningFailures++;
                    }
                }
            }


            // Do the filter conditions on the getOutputs first
            ArrayList<ServiceIO> outputs = c.getOutputs();
            for (ServiceIO o : outputs) {
                if (o.getFilterState() != ServiceIO.UNFILTERED) {
                    ContentValues values = new ContentValues();

                    // Set the filter state
                    values.put(FILTER_STATE, o.getFilterState());
                    values.put(COMPOSITE_ID, cs.getID());
                    values.put(IO_DESCRIPTION_ID, o.getDescription().getID());
                    values.put(COMPONENT_ID, c.getID());

                    values.put(MANUAL_VALUE, o.getDescription().getType().toString(o.getManualValue()));
                    values.put(SAMPLE_VALUE, o.getChosenSampleValue().id);

                    values.put(FILTER_CONDITION, o.getCondition());

                    long ret = db.insertOrThrow(TBL_SERVICEIO, null, values);

                    if (ret == -1) {
                        runningFailures++;
                    }
                }
            }
        }

        return runningFailures == 0;
    }

    public ArrayList<CompositeService> getExamples(String componentName) {
        // Implement some examples of composites that can be used
        // Implement this?
        return new ArrayList<CompositeService>();
    }


    public AppDescription getApp(String packageName) {
        String query = String.format("SELECT * FROM %s WHERE %s = \"%s\"", TBL_APP, PACKAGENAME, packageName);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = rawQuery(db, query, null);

        if (c == null) {
            return null;
        }

        if (c.getCount() == 0) {
            return null;
        }

        c.moveToFirst();

        return AppDescription.parseFromCursor(c);
    }

    public boolean setAppInstalled(String packageName, boolean b) {
        SQLiteDatabase db = this.getWritableDatabase();

        String strFilter = PACKAGENAME + "='" + packageName + "'";
        ContentValues args = new ContentValues();

        args.put(INSTALLED, b ? 1 : 0);

        int retval = db.update(TBL_APP, args, strFilter, null);

        if (retval == 0) {
            Log.e(TAG, "Failed to update service " + packageName);
            return false;
        } else {
            Log.e(TAG, "Updated service " + packageName);
            return true;
        }
    }

    public long startComposite(CompositeService composite) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(COMPOSITE_ID, composite.getID());
        cv.put(START_TIME, System.currentTimeMillis());

        return db.insert(TBL_COMPOSITE_EXECUTION_LOG, null, cv);
    }

    public boolean addToLog(CompositeService composite, long executionInstance, ComponentService component, String message, Bundle inputData, Bundle outputData, int status) {
        SQLiteDatabase db = this.getWritableDatabase();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        Date date = new Date();

        ContentValues values = new ContentValues();

        values.put(COMPOSITE_ID, composite == null ? -1 : composite.getID());
        values.put(EXECUTION_INSTANCE, executionInstance);
        values.put(COMPONENT_ID, component.getID());

        values.put(MESSAGE, message);
        values.put(TIME, sdf.format(date));
        values.put(LOG_TYPE, status);
        values.put(TIME, System.currentTimeMillis());

        if(inputData != null) {
            ArrayList<Bundle> data = inputData.getParcelableArrayList(ComposableService.INPUT);

            if(data != null) {
                StringBuilder dataString = new StringBuilder("[");

                for (int i = 0; i < data.size(); i++) {

                    if (i > 0)
                        dataString.append(",");

                    dataString.append(AppGlueLibrary.bundleToJSON(data.get(i), component.getDescription(), true));
                }
                dataString.append("]");
                values.put(INPUT_DATA, dataString.toString());
            } else {
                values.put(INPUT_DATA, "");
            }
        } else {
            values.put(INPUT_DATA, "");
        }

        if(outputData != null) {
            ArrayList<Bundle> data = outputData.getParcelableArrayList(ComposableService.INPUT);

            if(data != null) {
                StringBuilder dataString = new StringBuilder("[");

                for (int i = 0; i < data.size(); i++) {

                    if (i > 0)
                        dataString.append(",");

                    dataString.append(AppGlueLibrary.bundleToJSON(data.get(i), component.getDescription(), true));
                }
                dataString.append("]");
                values.put(OUTPUT_DATA, dataString.toString());
            } else {
                values.put(OUTPUT_DATA, "");
            }
        } else {
            values.put(OUTPUT_DATA, "");
        }

        long id = db.insertOrThrow(TBL_EXECUTION_LOG, null, values);
        return id != -1;
    }

    public ArrayList<LogItem> getLog() {

        ArrayList<LogItem> logs = new ArrayList<LogItem>();
        SQLiteDatabase db = this.getReadableDatabase();

        String compositeLogCols = AppGlueLibrary.buildGetAllString(TBL_COMPOSITE_EXECUTION_LOG, COLS_COMPOSITE_EXECUTION_LOG);
        String logCols = AppGlueLibrary.buildGetAllString(TBL_EXECUTION_LOG, COLS_EXECUTION_LOG);

        String q = String.format("SELECT %s, %s FROM %s " +
                        "LEFT JOIN %s ON %s.%s = %s.%s " +
                        "ORDER BY %s.%s DESC",
                compositeLogCols, logCols, TBL_COMPOSITE_EXECUTION_LOG,
                TBL_EXECUTION_LOG, TBL_COMPOSITE_EXECUTION_LOG, ID, TBL_EXECUTION_LOG, EXECUTION_INSTANCE,
                TBL_COMPOSITE_EXECUTION_LOG, ID);

        Cursor c = db.rawQuery(q, null);

        if(c == null){
            Log.e(TAG, "Cursor null for " + q);
            return logs;
        }

        if(c.getCount() == 0) {
            return logs;
        }

        LogItem current = null;
        c.moveToFirst();

        do {
            long id = c.getLong(c.getColumnIndex(TBL_COMPOSITE_EXECUTION_LOG + "_" + ID));
            if (current == null || current.getID() != id) {

                String message = c.getString(c.getColumnIndex(TBL_COMPOSITE_EXECUTION_LOG + "_" + MESSAGE));
                long startTime = c.getLong(c.getColumnIndex(TBL_COMPOSITE_EXECUTION_LOG + "_" + START_TIME));
                long endTime = c.getLong(c.getColumnIndex(TBL_COMPOSITE_EXECUTION_LOG + "_" + END_TIME));
                int status = c.getInt(c.getColumnIndex(TBL_COMPOSITE_EXECUTION_LOG + "_" + LOG_TYPE));

                long compositeId = c.getLong(c.getColumnIndex(TBL_COMPOSITE_EXECUTION_LOG + "_" + COMPOSITE_ID));
                CompositeService cs = getComposite(compositeId);

                current = new LogItem(id, cs, startTime, endTime, message, status);
                logs.add(current);
            }

            long componentLogId = c.getLong(c.getColumnIndex(TBL_EXECUTION_LOG + "_" + ID));
            long componentId = c.getLong(c.getColumnIndex(TBL_EXECUTION_LOG + "_" + COMPONENT_ID));
            ComponentService component = current.getComposite().getComponent(componentId);

            String componentMsg = c.getString(c.getColumnIndex(TBL_EXECUTION_LOG + "_" + MESSAGE));
            String inputString = c.getString(c.getColumnIndex(TBL_EXECUTION_LOG + "_" + INPUT_DATA));
            String outputString = c.getString(c.getColumnIndex(TBL_EXECUTION_LOG + "_" + OUTPUT_DATA));

            Bundle inputBundle = null;
            if(!inputString.equals("")) {
                inputBundle = AppGlueLibrary.JSONToBundle(inputString, component.getDescription(), true);
            }

            Bundle outputBundle = null;
            if(!outputString.equals("")) {
                outputBundle = AppGlueLibrary.JSONToBundle(outputString, component.getDescription(), false);
            }

            int componentStatus = c.getInt(c.getColumnIndex(TBL_EXECUTION_LOG + "_" + LOG_TYPE));
            long time = c.getLong(c.getColumnIndex(TBL_EXECUTION_LOG + "_" + TIME));
            ComponentLogItem cli = new ComponentLogItem(componentLogId, component, componentMsg, inputBundle, outputBundle, componentStatus, time);
            current.addComponentLog(cli);

        } while(c.moveToNext());
        c.close();

        return logs;
    }

    public ArrayList<LogItem> getLog(CompositeService cs) {

        ArrayList<LogItem> logs = new ArrayList<LogItem>();
        SQLiteDatabase db = this.getReadableDatabase();

        String compositeLogCols = AppGlueLibrary.buildGetAllString(TBL_COMPOSITE_EXECUTION_LOG, COLS_COMPOSITE_EXECUTION_LOG);
        String logCols = AppGlueLibrary.buildGetAllString(TBL_EXECUTION_LOG, COLS_EXECUTION_LOG);

        String q = String.format("SELECT %s, %s FROM %s " +
                        "LEFT JOIN %s ON %s.%s = %s.%s " +
                        "WHERE %s.%s = %d " +
                        "ORDER BY %s.%s DESC",
                compositeLogCols, logCols, TBL_COMPOSITE_EXECUTION_LOG,
                TBL_EXECUTION_LOG, TBL_COMPOSITE_EXECUTION_LOG, ID, TBL_EXECUTION_LOG, EXECUTION_INSTANCE,
                TBL_COMPOSITE_EXECUTION_LOG, COMPOSITE_ID, cs.getID(),
                TBL_COMPOSITE_EXECUTION_LOG, ID);

        Cursor c = db.rawQuery(q, null);

        if(c == null){
            Log.e(TAG, "Cursor null for " + q);
            return logs;
        }

        if(c.getCount() == 0) {
            return logs;
        }

        LogItem current = null;
        c.moveToFirst();

        do {
            long id = c.getLong(c.getColumnIndex(TBL_COMPOSITE_EXECUTION_LOG + "_" + ID));
            if (current == null || current.getID() != id) {

                long logCompositeId = c.getLong(c.getColumnIndex(TBL_COMPOSITE_EXECUTION_LOG + "_" + COMPOSITE_ID));
                if(logCompositeId != cs.getID()) {
                    Log.e(TAG, "log composite id isn't what we asked for, this is a problem..");
                    return logs;
                }

                String message = c.getString(c.getColumnIndex(TBL_COMPOSITE_EXECUTION_LOG + "_" + MESSAGE));
                long startTime = c.getLong(c.getColumnIndex(TBL_COMPOSITE_EXECUTION_LOG + "_" + START_TIME));
                long endTime = c.getLong(c.getColumnIndex(TBL_COMPOSITE_EXECUTION_LOG + "_" + END_TIME));
                int status = c.getInt(c.getColumnIndex(TBL_COMPOSITE_EXECUTION_LOG + "_" + LOG_TYPE));

                current = new LogItem(id, cs, startTime, endTime, message, status);
                logs.add(current);
            }

            long componentLogId = c.getLong(c.getColumnIndex(TBL_EXECUTION_LOG + "_" + ID));
            long componentId = c.getLong(c.getColumnIndex(TBL_EXECUTION_LOG + "_" + COMPONENT_ID));
            ComponentService component = cs.getComponent(componentId);

            String componentMsg = c.getString(c.getColumnIndex(TBL_EXECUTION_LOG + "_" + MESSAGE));

            String inputString = c.getString(c.getColumnIndex(TBL_EXECUTION_LOG + "_" + INPUT_DATA));
            String outputString = c.getString(c.getColumnIndex(TBL_EXECUTION_LOG + "_" + OUTPUT_DATA));

            Bundle inputBundle = null;
            if(!inputString.equals("")) {
                inputBundle = AppGlueLibrary.JSONToBundle(inputString, component.getDescription(), true);
            }

            Bundle outputBundle = null;
            if(!outputString.equals("")) {
                outputBundle = AppGlueLibrary.JSONToBundle(outputString, component.getDescription(), false);
            }

            int componentStatus = c.getInt(c.getColumnIndex(TBL_EXECUTION_LOG + "_" + LOG_TYPE));
            long time = c.getLong(c.getColumnIndex(TBL_EXECUTION_LOG + "_" + TIME));
            ComponentLogItem cli = new ComponentLogItem(componentLogId, component, componentMsg, inputBundle, outputBundle, componentStatus, time);
            current.addComponentLog(cli);

        } while(c.moveToNext());
        c.close();

        return logs;
    }

    public void addTag(Tag t) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(NAME, t.name());

        long id = db.insertOrThrow(TBL_TAG, null, cv);
        t.setID(id);
    }

    public Tag getTag(long id) {
        if (tagMap.get(id) != null)
            return tagMap.get(id);

        SQLiteDatabase db = this.getReadableDatabase();

        // Should probably do something clever than just equals - consider lower case too
        Cursor c = db.query(TBL_TAG, null,
                ID + " = ?", new String[]{"" + id},
                null, null, null, null);

        if (c == null) {
            Log.e(TAG, "Dead cursor: tag exists " + id);
            return null;
        }

        if (c.getCount() == 0) {
            // Insert it?
            return new Tag();
        }

        c.moveToFirst(); // There better not be more than one...

        return Tag.createOneFromCursor(c);
    }

    public Tag getTag(Tag t) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Should probably do something clever than just equals - consider lower case too
        Cursor c = db.query(TBL_TAG, null,
                NAME + " = ?", new String[]{t.name()},
                null, null, null, null);

        if (c == null) {
            Log.e(TAG, "Dead cursor: get tag " + t.name());
            return null;
        }

        if (c.getCount() == 0) {
            // Insert it?
            addTag(t);
            return t;
        }

        c.moveToFirst(); // There better not be more than one...

        return Tag.createOneFromCursor(c);
    }

    public boolean addTagsForComponent(ServiceDescription component) {
        SQLiteDatabase db = this.getWritableDatabase();

        boolean allWin = true;
        ArrayList<Tag> tags = component.getTags();

        for (Tag t : tags) {
            if (t.id() == -1) {
                // Then we need to get an ID (and maybe insert it)
                getTag(t);
            }

            ContentValues cv = new ContentValues();
            cv.put(TAG_ID, t.id());
            cv.put(CLASSNAME, component.getClassName());

            long id = db.insertOrThrow(TBL_SD_HAS_TAG, null, cv);
            t.setID(id);

            if (id == -1)
                allWin = false;
        }

        return allWin;
    }

    public ArrayList<Tag> getTagsForComponent(String className) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Tag> tags = new ArrayList<Tag>();

        Cursor c = db.query(TBL_SD_HAS_TAG, null,
                CLASSNAME + " = ?", new String[]{className},
                null, null, null, null);

        if (c == null) {
            Log.e(TAG, "Cursor dead for getTagsForComponent: " + className);
            return tags;
        }

        if (c.getCount() == 0) {
            return tags;
        }

        c.moveToFirst();

        do {
            long tagId = c.getLong(c.getColumnIndex(TAG_ID));
            Tag t = getTag(tagId);
            tags.add(t);
        }
        while (c.moveToNext());
        c.close();

        return tags;
    }

    public boolean isEnabled(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.query(TBL_COMPOSITE, new String[]{ENABLED},
                ID + " = ?", new String[]{"" + id},
                null, null, null, null);

        if (c == null)
            return false;

        if (c.getCount() != 1)
            return false;

        c.moveToFirst();

        int ret = c.getInt((c.getColumnIndex(ENABLED)));

        return ret == 1;
    }


    public ArrayList<ServiceDescription> getMatchingForIOs(ServiceDescription component, boolean inputs) {
        ArrayList<IODescription> ios = inputs ? component.getInputs() : component.getOutputs();

        HashMap<String, ServiceDescription> components = new HashMap<String, ServiceDescription>();
        HashMap<String, Long> types = new HashMap<String, Long>();
        for (IODescription io : ios) {
            IOType type = io.getType();
            if (!types.containsKey(type.getClassName()))
                types.put(type.getClassName(), type.getID());
        }

        Set<String> keys = types.keySet();

        for (String s : keys) {
            Long id = types.get(s);

            if (id == -1) {
                // A Need to get the ID of the type
                id = this.getIOType(s).getID();
            }

            // Get all the getInputs that use that type - we only want the IDs at this stage
            // We've got getInputs from the component parameter, so we need to get the getOutputs of the other one. Or vice versa
            ArrayList<String> componentNames = this.getComponentIdsForType(id, !inputs);
            for (String name : componentNames) {
                if (!components.containsKey(name)) {
                    components.put(name, getServiceDescription(name));
                }
            }
        }

        ArrayList<ServiceDescription> c = new ArrayList<ServiceDescription>();
        keys = components.keySet();
        for (String s : keys) {
            c.add(components.get(s));
        }

        return c;
    }

    private ArrayList<String> getComponentIdsForType(long id, boolean inputs) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> ioIds = new ArrayList<String>();

        int ioNum = inputs ? 1 : 0;

        Cursor c = db.query(TBL_IO_DESCRIPTION, new String[]{CLASSNAME},
                IO_TYPE + " = ? AND " + I_OR_O + " = ?", new String[]{"" + id, "" + ioNum},
                null, null, null, null);

        if (c == null) {
            Log.e(TAG, "Cursor dead for getIOsForType(" + id + "," + inputs + ")");
            return ioIds;
        }

        if (c.getCount() == 0) {
            return ioIds;
        }

        c.moveToFirst();

        do {
            ioIds.add(c.getString(c.getColumnIndex(CLASSNAME)));
        }
        while (c.moveToNext());
        c.close();

        return ioIds;
    }

    /**
     * ****************
     * Get stuff (joins)
     * ****************
     */

    public ArrayList<CompositeService> getComposites(boolean includeTemp) {
        ArrayList<CompositeService> composites = new ArrayList<CompositeService>();

        String compositeCols = AppGlueLibrary.buildGetAllString(TBL_COMPOSITE, COLS_COMPOSITE);
        String componentCols = AppGlueLibrary.buildGetAllString(TBL_COMPONENT, COLS_COMPONENT);
        String whereClause = includeTemp ? "" : " where " + TBL_COMPOSITE + "." + ID + " <> " + CompositeService.TEMP_ID;

        String query = String.format("SELECT %s FROM %s" +
                        " LEFT JOIN %s ON %s.%s = %s.%s" +
                        " %s %s",
                new StringBuilder(compositeCols).append(",").append(componentCols),
                TBL_COMPOSITE,
                TBL_COMPONENT, TBL_COMPOSITE, ID, TBL_COMPONENT, COMPOSITE_ID,
                whereClause,
                "ORDER BY " + POSITION
        );

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = rawQuery(db, query, null);

        if (c == null) {
            Log.e(TAG, "Cursor is null for getting composite: " + query);
            return composites;
        }

        if (c.getCount() == 0) {
            return composites;
        }

        c.moveToFirst();



        do {
            composites.add(getComposite(c.getLong(c.getColumnIndex(TBL_COMPOSITE + "_" + ID))));
        }
        while (c.moveToNext());
        c.close();

        return composites;
    }

    public CompositeService getComposite(long id) { // FIXME This should use the getComposites(ArrayList) but only have one thing in the list. Thus removing code duplication
        String compositeCols = AppGlueLibrary.buildGetAllString(TBL_COMPOSITE, COLS_COMPOSITE);
        String compositeComponentCols = AppGlueLibrary.buildGetAllString(TBL_COMPONENT, COLS_COMPONENT);
        String whereClause = " where " + TBL_COMPOSITE + "." + ID + " = " + id;

        String query = String.format("SELECT %s FROM %s" +
                        " LEFT JOIN %s ON %s.%s = %s.%s" +
                        " %s %s",
                new StringBuilder(compositeCols).append(",").append(compositeComponentCols),
                TBL_COMPOSITE,
                TBL_COMPONENT, TBL_COMPOSITE, ID, TBL_COMPONENT, COMPOSITE_ID,
                whereClause,
                "ORDER BY " + POSITION
        );

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = rawQuery(db, query, null);


        if (c == null) {
            Log.e(TAG, "Cursor is null for getting composite: " + query);
            return null;
        }

        if (c.getCount() == 0) {
            return null;
        }

        c.moveToFirst();

        CompositeService currentComposite = null;

        do {

            if (currentComposite == null) {

                currentComposite = new CompositeService(false);
                currentComposite.setInfo(TBL_COMPOSITE + "_", c);
            }

            long componentId = c.getLong(c.getColumnIndex(TBL_COMPONENT + "_" + ID));
            if (componentId == -1) {
                String row = DatabaseUtils.dumpCurrentRowToString(c);
                Log.e(TAG, "No component: " + row);
                continue;
            }

            ComponentService component = getComponent(componentId, currentComposite);
            if(component != null)
                currentComposite.addComponent(component, component.getPosition());
        }
        while (c.moveToNext());
        c.close();

        // At this stage we have all of the components, so link everything up
        connectComponentsForComposite(currentComposite);
        return currentComposite;
    }

    private boolean connectComponentsForComposite(CompositeService composite) {

        String query = String.format("SELECT * FROM %s " +
                "WHERE %s = %d",
                TBL_IOCONNECTION, COMPOSITE_ID, composite.getID());

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = rawQuery(db, query, null);

        if (c == null) {
            Log.e(TAG, "Cursor is null for getting composite: " + query);
            return false;
        }

        if (c.getCount() == 0) {
            return false;
        }

        c.moveToFirst();
        boolean allSuccess = true;

        do {
            long compositeId = c.getLong(c.getColumnIndex(COMPOSITE_ID));
            if(compositeId != composite.getID()) {
                Log.e(TAG, "IO Connect fail: composite IDs don't match. Expected " + composite.getID() + " and got " + compositeId);
                allSuccess = false;
                continue;
            }

            long sourceId = c.getLong(c.getColumnIndex(SOURCE_IO));
            long sinkId = c.getLong(c.getColumnIndex(SINK_IO));

            ServiceIO source = composite.getOutput(sourceId);
            ServiceIO sink = composite.getInput(sinkId);

            source.setConnection(sink);
            sink.setConnection(source);

        } while (c.moveToNext());
        c.close();

        return allSuccess;
    }

    private void connectComponents(Cursor c, CompositeService cs, String prefix) {

        long csId = c.getLong(c.getColumnIndex(prefix + COMPOSITE_ID));

        if (csId == 0) {
            // Presumably this means that there aren't any components?
            Log.d(TAG, "Dead CS id (for " + cs.getID() + ")");
            return;
        }

        if (csId != cs.getID() || csId == 0) { // 0 means it's not there, right?
            Log.d(TAG, "cs ID mis-match: " + csId + " -- " + cs.getID());
            return;
        }

        long outputId = c.getLong(c.getColumnIndex(prefix + OUTPUT_ID));
        ComponentService out = cs.getComponent(outputId);
        if (out == null) {
            Log.d(TAG, "Out null for " + outputId);
            return;
        }

        long inputId = c.getLong(c.getColumnIndex(prefix + INPUT_ID));
        ComponentService in = cs.getComponent(inputId);
        if (in == null) {
            Log.d(TAG, "In null for " + inputId);
            return;
        }

        long outputIoId = c.getLong(c.getColumnIndex(prefix + OUTPUT_IO_ID));
        ServiceIO output = out.getOutput(outputIoId);

        long inputIoId = c.getLong(c.getColumnIndex(prefix + INPUT_IO_ID));
        ServiceIO input = in.getInput(inputIoId);

        // If we've got to here then everything should have worked. Make a two-way link
        output.setConnection(input);
        input.setConnection(output);
    }

    private void filterComponents(Cursor c, CompositeService cs, String prefix) {

        long csId = c.getLong(c.getColumnIndex(prefix + COMPOSITE_ID));
        if (csId != cs.getID())
            return;

        long componentId = c.getLong(c.getColumnIndex(prefix + COMPONENT_ID));
        ComponentService component = cs.getComponent(componentId);
        if (component == null)
            return;

        long ioId = c.getLong(c.getColumnIndex(prefix + IO_DESCRIPTION_ID));
        ServiceIO io = component.getIO(ioId);
        if (io == null)
            return;

        int filterState = c.getInt(c.getColumnIndex(prefix + FILTER_STATE));
        int filterCondition = c.getInt(c.getColumnIndex(prefix + FILTER_CONDITION));

        switch (filterState) {

            case ServiceIO.SAMPLE_FILTER: // Then we need to look up a sample value
                long sampleId = c.getLong(c.getColumnIndex(prefix + SAMPLE_VALUE));
                IOValue sample = io.getDescription().getSampleValue(sampleId);
                io.setChosenSampleValue(sample);
                io.setCondition(filterCondition);
                break;

            case ServiceIO.MANUAL_FILTER: // Then we need to look up the manual value
                String textValue = c.getString(c.getColumnIndex(prefix + MANUAL_VALUE));
                io.setManualValue(io.getDescription().getType().fromString(textValue));
                io.setCondition(filterCondition);
                break;

            default:
                // Do nothing, I think, either it's none of our values, or it's unfiltered.
        }
    }


    public ArrayList<CompositeService> componentAtPosition(String className, int position) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<CompositeService> composites = new ArrayList<CompositeService>();

        String sql = String.format("SELECT DISTINCT %s FROM %s WHERE %s = '%s' AND %s = %s", COMPOSITE_ID, TBL_COMPONENT, CLASSNAME, className, POSITION, position);
        Cursor c = rawQuery(db, sql, null);

        if (c == null) {
            Log.e(TAG, String.format("Cursor dead for atomicAtPosition: %s %d", className, position));
            return null;
        }

        if (c.getCount() == 0) {
            return composites;
        }

        c.moveToFirst();

        do {
            long id = c.getLong(c.getColumnIndex(COMPOSITE_ID));
            composites.add(this.getComposite(id));
        }
        while (c.moveToNext());
        c.close();

        return composites;
    }

    public ArrayList<ComponentService> getComponents(String className, int position) {

        SQLiteDatabase db = this.getReadableDatabase();

        String query  = String.format("SELECT %s FROM %s WHERE %s = \"%s\" AND %s = %d",
                ID, TBL_COMPONENT, CLASSNAME, className, POSITION, position);
        Cursor c = rawQuery(db, query, null);

        if (c == null) {
            Log.e(TAG, "Cursor is null for getting composite: " + query);
            return null;
        }

        if (c.getCount() == 0) {
            return null;
        }

        c.moveToFirst();

        ArrayList<ComponentService> components = new ArrayList<ComponentService>();

        do {

            long componentId = c.getLong(c.getColumnIndex(ID));
            components.add(getComponent(componentId, null));

        } while (c.moveToNext());
        c.close();

        return components;
    }

    public ComponentService getComponent(long id, CompositeService cs) {

        String componentCols = AppGlueLibrary.buildGetAllString(TBL_COMPONENT, COLS_COMPONENT);
        String ioCols = AppGlueLibrary.buildGetAllString(TBL_SERVICEIO, COLS_SERVICEIO);
        String whereClause = " where " + TBL_COMPONENT + "." + ID + " = " + id;

        String query = String.format("SELECT %s FROM %s" +
                        " LEFT JOIN %s ON %s.%s = %s.%s" +
                        " %s",
                new StringBuilder(componentCols).append(",").append(ioCols),
                TBL_COMPONENT,
                TBL_SERVICEIO, TBL_COMPONENT, ID, TBL_SERVICEIO, COMPONENT_ID,
                whereClause
        );

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = rawQuery(db, query, null);

        if (c == null) {
            Log.e(TAG, "Cursor is null for getting composite: " + query);
            return null;
        }

        if (c.getCount() == 0) {
            return null;
        }

        c.moveToFirst();

        ComponentService currentComponent = null;

        do {

            long componentId = c.getLong(c.getColumnIndex(TBL_COMPONENT + "_" + ID));
            if(currentComponent == null || currentComponent.getID() != componentId) {

                // We need to set up a new one
                int position = c.getInt(c.getColumnIndex(TBL_COMPONENT + "_" + POSITION));
                ServiceDescription sd = getServiceDescription(c.getString(c.getColumnIndex(TBL_COMPONENT + "_" + CLASSNAME)));

                currentComponent = new ComponentService(id, sd, cs, position);
            }

            long ioId = c.getLong(c.getColumnIndex(TBL_SERVICEIO + "_" + ID));
            long ioComponentId = c.getLong(c.getColumnIndex(TBL_SERVICEIO + "_" + COMPONENT_ID));
            long ioDescriptionId = c.getLong(c.getColumnIndex(TBL_SERVICEIO + "_" + IO_DESCRIPTION_ID));
            int filterState = c.getInt(c.getColumnIndex(TBL_SERVICEIO + "_" + FILTER_STATE));
            int filterCondition = c.getInt(c.getColumnIndex(TBL_SERVICEIO + "_" + FILTER_CONDITION));

            int manualIndex = c.getColumnIndex(TBL_SERVICEIO + "_" + MANUAL_VALUE);
            String manualValue = c.isNull(manualIndex) ? null : c.getString(manualIndex);

            long sampleId = c.getLong(c.getColumnIndex(TBL_SERVICEIO + "_" + SAMPLE_VALUE));

            if(ioComponentId != componentId) {
                if(ioComponentId > 0)
                    Log.w(TAG, "Component IDs don't match for ServiceIOs: expected" + componentId + " and got " + ioComponentId);
                continue;
            }

            // We need to check this because they might genuinely use the empty string as a parameter.

            IODescription iod = getIODescription(ioDescriptionId);
            ServiceIO io = new ServiceIO(ioId, currentComponent, iod);
            io.setFilterState(filterState);
            io.setCondition(filterCondition);

            if(manualValue != null) {
                io.setManualValue(iod.getType().fromString(manualValue));
            }

            if(sampleId > 0) {
                IOValue chosenSample = getIOValue(sampleId, iod);
                io.setChosenSampleValue(chosenSample);
            }

        } while (c.moveToNext());

        return currentComponent;
    }

    private IODescription getIODescription(long id) {

        if(ioMap.get(id) != null) {
            return ioMap.get(id);
        }

        if (id < 1) {
            Log.e(TAG, "ID is too small");
            return null;
        }

        String ioCols = AppGlueLibrary.buildGetAllString(TBL_IO_DESCRIPTION, COLS_IO_DESCRIPTION);
        String ioSamples = AppGlueLibrary.buildGetAllString(TBL_IO_SAMPLE, COLS_IO_SAMPLES);

        String query = String.format("SELECT %s from %s " +
                        "LEFT JOIN %s ON %s.%s = %s.%s " +
                        "WHERE %s.%s = %d",
                ioCols + ", " + ioSamples, TBL_IO_DESCRIPTION,
                TBL_IO_SAMPLE, TBL_IO_DESCRIPTION, ID, TBL_IO_SAMPLE, IO_DESCRIPTION_ID,
                TBL_IO_DESCRIPTION, ID, id
        );

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = rawQuery(db, query, null);

        if (c == null) {
            Log.e(TAG, "Cursor dead " + query);
            return null;
        }

        if (c.getCount() == 0) {
            return null;
        }

        IODescription currentIO = null;

        c.moveToFirst();

        do {

            long ioId = c.getLong(c.getColumnIndex(TBL_IO_DESCRIPTION + "_" + ID));
            long ioTypeId = c.getLong(c.getColumnIndex(TBL_IO_DESCRIPTION + "_" + IO_TYPE));

            if (ioTypeId != 0) {

                if (ioMap.get(ioId) != null) {
                    currentIO = ioMap.get(ioId);
                } else if (currentIO == null || currentIO.getID() != ioId) {

                    // If it doesn't exist, then use the old one
                    currentIO = new IODescription(ioId);
                    currentIO.setInfo(TBL_IO_DESCRIPTION + "_", c);
                    currentIO.setType(getIOType(ioTypeId));
                    ioMap.put(ioId, currentIO);
                }

                long sampleId = c.getLong(c.getColumnIndex(TBL_IO_SAMPLE + "_" + ID));

                if (sampleId != 0) {

                    String sampleName = c.getString(c.getColumnIndex(TBL_IO_SAMPLE + "_" + NAME));
                    String strValue = c.getString(c.getColumnIndex(TBL_IO_SAMPLE + "_" + VALUE));

                    if (sampleName == null)
                        Log.d(TAG, "Getting sample " + sampleId + ": " + sampleName + DatabaseUtils.dumpCurrentRowToString(c));

                    // When we make the sample it might need to be converted to be the right type of object?
                    Object value = currentIO.getType().fromString(strValue);
                    currentIO.addSampleValue(new IOValue(sampleId, sampleName, value));
                }
            }
        } while(c.moveToNext());
        c.close();

        return currentIO;
    }

    public ServiceDescription getServiceDescription(String className) {
        if (componentMap.contains(className)) {
            return componentMap.get(className);
        }

        if (className == null) {
            Log.e(TAG, "Classname is null..");
            return null;
        }

        String componentCols = AppGlueLibrary.buildGetAllString(TBL_SD, COLS_SD);
        String ioCols = AppGlueLibrary.buildGetAllString(TBL_IO_DESCRIPTION, COLS_IO_DESCRIPTION);

        String query = String.format("SELECT %s FROM %s " +
                        "LEFT JOIN %s ON %s.%s = %s.%s " +
                        "LEFT JOIN %s ON %s.%s = %s.%s " +
                        " %s",
                componentCols + "," + ioCols,
                TBL_SD,
                TBL_IO_DESCRIPTION, TBL_SD, CLASSNAME, TBL_IO_DESCRIPTION, CLASSNAME,
                TBL_APP, TBL_SD, PACKAGENAME, TBL_APP, PACKAGENAME,
                "WHERE " + TBL_SD + "_" + CLASSNAME + " = \"" + className + "\""
        );

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = rawQuery(db, query, null);

        if (c == null) {
            Log.e(TAG, "Cursor dead " + query);
            return null;
        }

        if (c.getCount() == 0) {
            return null;
        }

        ServiceDescription currentComponent = null;

        c.moveToFirst();

        do {
            if (currentComponent == null) {

                // Create a new component based on this info
                currentComponent = new ServiceDescription();
                currentComponent.setInfo(TBL_SD + "_", c);
                currentComponent.setApp(getApp(c.getString(c.getColumnIndex(TBL_SD + "_" + PACKAGENAME))));

                // It shouldn't already be in there
                componentMap.put(className, currentComponent);
            }

            long ioId = c.getLong(c.getColumnIndex(TBL_IO_DESCRIPTION + "_" + ID));
            IODescription io = getIODescription(ioId);

            currentComponent.addIO(io, io.isInput(), io.getIndex());

            if (!currentComponent.hasTags()) {
                Cursor c2 = rawQuery(db, String.format("SELECT * FROM %s WHERE %s = '%s'",
                        TBL_SD_HAS_TAG, CLASSNAME,
                        currentComponent.getClassName()), null);

                if (c2 == null) {
                    Log.e(TAG, "Tag cursor dead for getting components w/ join");
                    continue;
                }

                if (c2.getCount() == 0) // This is fine
                    continue;

                c2.moveToFirst();

                do {
                    currentComponent.addTag(getTag(c2.getInt(c2.getColumnIndex(TAG_ID))));
                } while (c2.moveToNext());
                c2.close();
            }
        }
        while (c.moveToNext());
        c.close();

        return currentComponent;
    }

    public ArrayList<ServiceDescription>    getServiceDescriptions(ProcessType processType) {
        String componentCols = AppGlueLibrary.buildGetAllString(TBL_SD, COLS_SD);
        String ioCols = AppGlueLibrary.buildGetAllString(TBL_IO_DESCRIPTION, COLS_IO_DESCRIPTION);
        String ioSamples = AppGlueLibrary.buildGetAllString(TBL_IO_SAMPLE, COLS_IO_SAMPLES);

        String args = "";

        if (processType != null)
            args = " WHERE " + TBL_SD + "_" + PROCESS_TYPE + " = " + processType;


        String query = String.format("SELECT %s FROM %s " +
                        "LEFT JOIN %s ON %s.%s = %s.%s " +
                        "LEFT JOIN %s ON %s.%s = %s.%s " +
                        "LEFT JOIN %s ON %s.%s = %s.%s " +
                        " %s",
                componentCols + "," + ioCols + "," + ioSamples,
                TBL_SD,
                TBL_IO_DESCRIPTION, TBL_SD, CLASSNAME, TBL_IO_DESCRIPTION, CLASSNAME,
                TBL_APP, TBL_SD, PACKAGENAME, TBL_APP, PACKAGENAME,
                TBL_IO_SAMPLE, TBL_IO_DESCRIPTION, ID, TBL_IO_SAMPLE, IO_DESCRIPTION_ID,
                args
        );

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = rawQuery(db, query, null);
        ArrayList<ServiceDescription> components = new ArrayList<ServiceDescription>();

        if (c == null) {
            Log.e(TAG, "Cursor dead " + query);
            return components;
        }

        if (c.getCount() == 0) {
            Log.e(TAG, "Cursor empty, this seems unlikely: " + query);
            return components;
        }

        ServiceDescription currentComponent = null;
        IODescription currentIO = null;

        c.moveToFirst();

        do {
            String className = c.getString(c.getColumnIndex(TBL_SD + "_" + CLASSNAME));

            if (currentComponent == null || !currentComponent.getClassName().equals(className)) {

                if (componentMap.contains(className))
                    currentComponent = componentMap.get(className);
                else {
                    // Create a new component based on this info
                    currentComponent = new ServiceDescription();
                    currentComponent.setInfo(TBL_SD + "_", c);
                    // It shouldn't already be in there
                    componentMap.put(className, currentComponent);
                }

                if (!components.contains(currentComponent))
                    components.add(currentComponent);
            }

            long ioId = c.getLong(c.getColumnIndex(TBL_IO_DESCRIPTION + "_" + ID));
            long ioTypeId = c.getLong(c.getColumnIndex(TBL_IO_DESCRIPTION + "_" + IO_TYPE));

            if (ioTypeId != 0) {

                if (ioMap.get(ioId) != null) {
                    currentIO = ioMap.get(ioId);
                    currentComponent.addIO(currentIO, currentIO.isInput(), currentIO.getIndex());
                } else if (currentIO == null || currentIO.getID() != ioId) {

                    // If it doesn't exist, then use the old one
                    currentIO = new IODescription(ioId);
                    currentIO.setInfo(TBL_IO_DESCRIPTION + "_", c);
                    currentIO.setType(getIOType(ioTypeId));
                    currentComponent.addIO(currentIO, currentIO.isInput(), currentIO.getIndex());
                    ioMap.put(ioId, currentIO);
                }

                long sampleId = c.getLong(c.getColumnIndex(TBL_IO_SAMPLE + "_" + ID));
                String sampleName = c.getString(c.getColumnIndex(TBL_IO_SAMPLE + "_" + NAME));
                String strValue = c.getString(c.getColumnIndex(TBL_IO_SAMPLE + "_" + VALUE));

                if (sampleName != null && strValue != null) {
                    Object value = currentIO.getType().fromString(strValue);
                    currentIO.addSampleValue(new IOValue(sampleId, sampleName, value));
                }
            }

            if (!currentComponent.hasTags()) {
                Cursor c2 = rawQuery(db, String.format("SELECT * FROM %s WHERE %s = '%s'",
                        TBL_SD_HAS_TAG, CLASSNAME,
                        currentComponent.getClassName()), null);

                if (c2 == null) {
                    Log.e(TAG, "Tag cursor dead for getting components w/ join");
                    continue;
                }

                if (c2.getCount() == 0) // This is fine
                    continue;

                c2.moveToFirst();

                do {
                    currentComponent.addTag(getTag(c2.getLong(c2.getColumnIndex(TAG_ID))));
                } while (c2.moveToNext());
                c2.close();
            }


        }
        while (c.moveToNext());
        c.close();

        return components;
    }


    /**
     * ******************************
     * Below here is the auto-caching stuff
     */

    private void cacheIOTypes() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = rawQuery(db, String.format("SELECT * FROM %s", TBL_IOTYPE), null);

        if (c == null) {
            Log.e(TAG, "Dead cursor: Caching all the types");
            return;
        }

        if (c.getCount() == 0) {
            Log.w(TAG, "Empty cursor: Caching all the types");
            return;
        }

        c.moveToFirst();

        do {
            String className = c.getString(c.getColumnIndex(CLASSNAME));
            IOType type = IOType.Factory.getType(className);
            typeMap.put(type.getID(), type);
        } while (c.moveToNext());

        c.close();
    }

    private void cacheTags() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = rawQuery(db, String.format("SELECT * FROM %s", TBL_TAG), null);

        if (c == null) {
            Log.e(TAG, "Dead cursor: Caching all the tags");
            return;
        }

        if (c.getCount() == 0) {
            // Insert it?
            Log.w(TAG, "Empty cursor: Caching all the tags");
            return;
        }

        c.moveToFirst();

        do {
            Tag tag = Tag.createOneFromCursor(c);
            tagMap.put(tag.id(), tag);
        }
        while (c.moveToNext());
        c.close();
    }

    public boolean isInstanceRunning(long compositeId, long executionInstance) {
        // FIXME Do this
        // Look for a row with the instance and the composite getID. If the timestamp is set then we're good. otherwise not.

        SQLiteDatabase db = this.getReadableDatabase();

        String query = String.format("SELECT %s FROM %s WHERE %s = %d",
                TERMINATED, TBL_COMPOSITE_EXECUTION_LOG, EXECUTION_INSTANCE, executionInstance);

        return false;
    }

    public long isCompositeRunning(long compositeId) {
        // Look through all the rows for that compositeId, and see if there are any instances with the timestamp not set
        // Return the instance ID so that we can control it
        return -1L;
    }

    public boolean isTerminated(CompositeService composite, long executionInstance) {

        SQLiteDatabase db = this.getReadableDatabase();

        String query = String.format("SELECT %s FROM %s " +
                "WHERE %s = %d " +
                "AND %s = %d",
                TERMINATED, TBL_COMPOSITE_EXECUTION_LOG,
                COMPOSITE_ID, composite.getID(),
                ID, executionInstance);

        Cursor c = db.rawQuery(query, null);

        if (c == null) {
            Log.e(TAG, "Dead cursor: No composite to check termination");
            return true;
        }

        if (c.getCount() == 0) {
            Log.w(TAG, "Empty cursor: Teminated?");
            return true;
        }

        c.moveToFirst();
        boolean terminate = c.getInt(c.getColumnIndex(TERMINATED)) == 1;

        c.close();
        return terminate;
    }

    public boolean terminate(CompositeService composite, long executionInstance, int status, String message) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(TERMINATED, 1);
        cv.put(END_TIME, System.currentTimeMillis());
        cv.put(LOG_TYPE, status);
        cv.put(MESSAGE, message);

        int num = db.update(TBL_COMPOSITE_EXECUTION_LOG, cv,
                COMPOSITE_ID + " = ? AND " + ID + " = ?",
                new String[]{"" + composite.getID(), "" + executionInstance});
        return num == 1;
    }
}