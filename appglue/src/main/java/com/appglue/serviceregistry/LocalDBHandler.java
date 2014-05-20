package com.appglue.serviceregistry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Pair;

import com.appglue.Constants.Interval;
import com.appglue.Constants.ProcessType;
import com.appglue.IOValue;
import com.appglue.ServiceIO;
import com.appglue.Tag;
import com.appglue.datatypes.IOType;
import com.appglue.description.AppDescription;
import com.appglue.description.ServiceDescription;
import com.appglue.engine.CompositeService;
import com.appglue.library.AppGlueLibrary;
import com.appglue.library.LogItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import static com.appglue.Constants.AVG_RATING;
import static com.appglue.Constants.CLASSNAME;
import static com.appglue.Constants.COMPOSITE_ID;
import static com.appglue.Constants.DB_NAME;
import static com.appglue.Constants.DB_VERSION;
import static com.appglue.Constants.DESCRIPTION;
import static com.appglue.Constants.FRIENDLY_NAME;
import static com.appglue.Constants.ICON;
import static com.appglue.Constants.ID;
import static com.appglue.Constants.INPUT_CLASSNAME;
import static com.appglue.Constants.INPUT_IO_ID;
import static com.appglue.Constants.INSTALLED;
import static com.appglue.Constants.IO_INDEX;
import static com.appglue.Constants.IO_TYPE;
import static com.appglue.Constants.I_OR_O;
import static com.appglue.Constants.LOG;
import static com.appglue.Constants.MANDATORY;
import static com.appglue.Constants.NAME;
import static com.appglue.Constants.NUM_RATINGS;
import static com.appglue.Constants.OUTPUT_CLASSNAME;
import static com.appglue.Constants.OUTPUT_IO_ID;
import static com.appglue.Constants.PACKAGENAME;
import static com.appglue.Constants.POSITION;
import static com.appglue.Constants.PRICE;
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
import static com.appglue.library.AppGlueConstants.COLS_COMPOSITE_HAS_COMPONENT;
import static com.appglue.library.AppGlueConstants.COLS_COMPOSITE_IOCONNECTION;
import static com.appglue.library.AppGlueConstants.COLS_EXECUTION_LOG;
import static com.appglue.library.AppGlueConstants.COLS_FILTER;
import static com.appglue.library.AppGlueConstants.COLS_IOTYPE;
import static com.appglue.library.AppGlueConstants.COLS_IO_SAMPLES;
import static com.appglue.library.AppGlueConstants.COLS_SERVICEIO;
import static com.appglue.library.AppGlueConstants.COLS_TAG;
import static com.appglue.library.AppGlueConstants.FILTER_CONDITION;
import static com.appglue.library.AppGlueConstants.FILTER_STATE;
import static com.appglue.library.AppGlueConstants.INTERVAL;
import static com.appglue.library.AppGlueConstants.IS_RUNNING;
import static com.appglue.library.AppGlueConstants.LOG_TYPE;
import static com.appglue.library.AppGlueConstants.MANUAL_VALUE;
import static com.appglue.library.AppGlueConstants.MESSAGE;
import static com.appglue.library.AppGlueConstants.NUMERAL;
import static com.appglue.library.AppGlueConstants.SERVICE_IO;
import static com.appglue.library.AppGlueConstants.SHOULD_BE_RUNNING;
import static com.appglue.library.AppGlueConstants.TAG_ID;
import static com.appglue.library.AppGlueConstants.TBL_APP;
import static com.appglue.library.AppGlueConstants.TBL_COMPONENT;
import static com.appglue.library.AppGlueConstants.TBL_COMPONENT_HAS_TAG;
import static com.appglue.library.AppGlueConstants.TBL_COMPOSITE;
import static com.appglue.library.AppGlueConstants.TBL_COMPOSITE_HAS_COMPONENT;
import static com.appglue.library.AppGlueConstants.TBL_COMPOSITE_IOCONNECTION;
import static com.appglue.library.AppGlueConstants.TBL_EXECUTION_LOG;
import static com.appglue.library.AppGlueConstants.TBL_FILTER;
import static com.appglue.library.AppGlueConstants.TBL_IOTYPE;
import static com.appglue.library.AppGlueConstants.TBL_IO_SAMPLES;
import static com.appglue.library.AppGlueConstants.TBL_SERVICEIO;
import static com.appglue.library.AppGlueConstants.TBL_TAG;
import static com.appglue.library.AppGlueConstants.TEMP_ID;
import static com.appglue.library.AppGlueConstants.TIME;

public class LocalDBHandler extends SQLiteOpenHelper
{
	private HashMap<String, AppDescription> appMap;

    private HashMap<Long, CompositeService> compositeMap;
	private HashMap<String, ServiceDescription> componentMap;
    private LongSparseArray<ServiceIO> ioMap;

    // These are the ones that get cached immediately for speed-ness.
    private LongSparseArray<IOType> typeMap;
    private LongSparseArray<Tag> tagMap;

	/**
	 * Creates a new class to handle all the database crap
	 * 
	 * @param context The context we need to create the stuff
	 */
	public LocalDBHandler(Context context)
	{
		super(context, DB_NAME, null, DB_VERSION);

		appMap = new HashMap<String, AppDescription>();
        componentMap = new HashMap<String, ServiceDescription>();
        ioMap = new LongSparseArray<ServiceIO>();

        typeMap = new LongSparseArray<IOType>();
        tagMap = new LongSparseArray<Tag>();

        cacheIOTypes();
        cacheTags();
		
		// Recreate the database every time for now while we are testing
		recreate();
	}
	

	@Override 
	/**
	 * Automagically called when the database needs to be created, usually on an upgrade
	 * 
	 * @param db The database to be edited
	 */
	public void onCreate(SQLiteDatabase db) 
	{
		create(db);
	}

    @Override
    public void onConfigure(SQLiteDatabase db)
    {
        db.setForeignKeyConstraintsEnabled(true);
    }
	
	
	/**
	 * Indicate the database should be created again
	 */
	public void recreate()
	{
		SQLiteDatabase db = this.getWritableDatabase();
		onCreate(db);
	}
	
	
	@Override
	/**
	 * Called when the database needs to be upgraded
	 * 
	 * @param db			The database to be upgraded
	 * @param oldVersion	The old version number of the database
	 * @param newVersion	The new version number of the database
	 */
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		onCreate(db);
	}
	
	
	/**
	 * Create all of the tables that we need to create
	 * 	Atomic
	 * 	Atomic_has_io
	 * 	Parameter
	 * 	Atomic_has_parameter
	 * 	Composite
	 * 	Composite_has_atomic
	 * 	Composite_has_parameter
	 * 
	 * @param db The database that everything should be created in
	 */
	private void create(SQLiteDatabase db)
	{
        // None of these should have foreign keys
		db.execSQL(String.format("DROP TABLE IF EXISTS %s", TBL_COMPOSITE));
		db.execSQL(AppGlueLibrary.createTableString(TBL_COMPOSITE, COLS_COMPOSITE));

        db.execSQL(String.format("DROP TABLE IF EXISTS %s", TBL_APP));
        db.execSQL(AppGlueLibrary.createTableString(TBL_APP, COLS_APP));

		db.execSQL(String.format("DROP TABLE IF EXISTS %s", TBL_COMPONENT));
		db.execSQL(AppGlueLibrary.createTableString(TBL_COMPONENT, COLS_COMPONENT));

        db.execSQL(String.format("DROP TABLE IF EXISTS %s", TBL_IOTYPE));
        db.execSQL(AppGlueLibrary.createTableString(TBL_IOTYPE, COLS_IOTYPE));

        db.execSQL(String.format("DROP TABLE IF EXISTS %s", TBL_TAG));
        db.execSQL(AppGlueLibrary.createTableString(TBL_TAG, COLS_TAG));

        // references Component
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", TBL_SERVICEIO));
        db.execSQL(AppGlueLibrary.createTableString(TBL_SERVICEIO, COLS_SERVICEIO));

        // references ServiceIO
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", TBL_IO_SAMPLES));
        db.execSQL(AppGlueLibrary.createTableString(TBL_IO_SAMPLES, COLS_IO_SAMPLES));

        // references Composite and component
		db.execSQL(String.format("DROP TABLE IF EXISTS %s", TBL_COMPOSITE_HAS_COMPONENT));
		db.execSQL(AppGlueLibrary.createTableString(TBL_COMPOSITE_HAS_COMPONENT, COLS_COMPOSITE_HAS_COMPONENT));

        db.execSQL(String.format("DROP TABLE IF EXISTS %s", TBL_EXECUTION_LOG));
        db.execSQL(AppGlueLibrary.createTableString(TBL_EXECUTION_LOG, COLS_EXECUTION_LOG));

        // references Component and Tag
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", TBL_COMPONENT_HAS_TAG));
        db.execSQL(AppGlueLibrary.createTableString(TBL_COMPONENT_HAS_TAG, COLS_COMPONENT_HAS_TAG));

        // references Component, composite and ServiceIO
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", TBL_COMPOSITE_IOCONNECTION));
        db.execSQL(AppGlueLibrary.createTableString(TBL_COMPOSITE_IOCONNECTION, COLS_COMPOSITE_IOCONNECTION));

     	db.execSQL(String.format("DROP TABLE IF EXISTS %s", TBL_FILTER));
		db.execSQL(AppGlueLibrary.createTableString(TBL_FILTER, COLS_FILTER));
		
		postCreateInsert(db);
	}	
	
	/**
	 * Anything that needs to be put in the database without making a component do it.
	 * IO Samples:
	 * 	Boolean
     *
     * 	Also initialise the temporary composite
	 * 
	 * @param db The Database
	 */
	private void postCreateInsert(SQLiteDatabase db)
	{
		ContentValues cv = new ContentValues();
		cv.put(NAME, "True");
		cv.put(VALUE, true);
		db.insert(TBL_IO_SAMPLES, null, cv);
		
		cv = new ContentValues();
		cv.put(NAME, "False");
		cv.put(VALUE, false);
		db.insert(TBL_IO_SAMPLES, null, cv);

        // This is where we initialise the temporary Composite
        cv = new ContentValues();
        cv.put(ID, TEMP_ID);
        cv.put(NAME, "temp");
        cv.put(DESCRIPTION, "This is ALWAYS the temporary composite");
        cv.put(ACTIVE_OR_TIMER, 1);
        cv.put(IS_RUNNING, 0);
        cv.put(SHOULD_BE_RUNNING, 1);
        cv.put(NUMERAL, -1);
        cv.put(INTERVAL, -1);
        db.insert(TBL_COMPOSITE, null, cv);
	}

    public void saveTemp(String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(NAME, name);
        cv.put(DESCRIPTION, "You haven't entered a description yet");
        cv.put(ACTIVE_OR_TIMER, 1);
        cv.put(IS_RUNNING, 0);
        cv.put(SHOULD_BE_RUNNING, 1);
        cv.put(NUMERAL, -1);
        cv.put(INTERVAL, -1);
        long id = db.insert(TBL_COMPOSITE, null, cv);

        cv = new ContentValues();
        cv.put(COMPOSITE_ID, id);

        String[] tables = new String[] { TBL_COMPOSITE_HAS_COMPONENT, TBL_EXECUTION_LOG, TBL_COMPOSITE_IOCONNECTION, TBL_FILTER };

        for(String table : tables)
            db.update(table, cv, COMPOSITE_ID + " = ?", new String[]{ "" + TEMP_ID });
    }

    /**
     * Delete all of the stuff in the other tables that references the TEMP.
     */
    public void resetTemp()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] sqls = new String[]{
            String.format("DELETE FROM `%s` WHERE %s = %d", TBL_COMPOSITE_HAS_COMPONENT, COMPOSITE_ID, TEMP_ID),
            String.format("DELETE FROM `%s` WHERE %s = %d", TBL_EXECUTION_LOG, COMPOSITE_ID, TEMP_ID),
            String.format("DELETE FROM `%s` WHERE %s = %d", TBL_COMPOSITE_IOCONNECTION, COMPOSITE_ID, TEMP_ID),
            String.format("DELETE FROM `%s` WHERE %s = %d", TBL_FILTER, COMPOSITE_ID, TEMP_ID)
        };

        for(String sql : sqls)
            db.rawQuery(sql, null);
    }
	
	/**
	 * Adds a new atomic service for the given service description
	 * 
	 * @param sd The object representation of the component
	 * @return The id of the inserted component
	 */
	public long addComponent(ServiceDescription sd)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(NAME, sd.getName());
		values.put(CLASSNAME, sd.getClassName());
		values.put(PACKAGENAME, sd.getPackageName());
		values.put(DESCRIPTION, sd.getDescription());
		
		values.put(SERVICE_TYPE, sd.getServiceType().index);
		values.put(PROCESS_TYPE, sd.getProcessType().index);
		
		values.put(AVG_RATING, sd.getAverageRating());
		values.put(NUM_RATINGS, sd.getNumReviews());
		values.put(PRICE, sd.getPrice());
		
		long retval;
		int inputSuccess = 0;
		int outputSuccess = 0;
		
		try
		{
			retval = db.insertOrThrow(TBL_COMPONENT, null, values);
			
			addApp(sd);
			
			
			if(sd.hasInputs())
			{
				if(addServiceIO(sd.getInputs(), true, sd.getClassName()))
					inputSuccess = 1;
				else
					inputSuccess = -1;
			}
			
			if(sd.hasOutputs())
			{
				if(addServiceIO(sd.getOutputs(), false, sd.getClassName()))
					outputSuccess = 1;
				else
					outputSuccess = -1;
			}
			
			if(sd.hasTags())
			{
				addTagsForComponent(sd);
			}
		}
		catch(SQLiteConstraintException e)
		{
			// This means that that key is already in the database
			retval = 0;
		}
		
		if(inputSuccess == -1)
			Log.e(TAG, "Failed to add inputs for " + sd.getClassName());
		
		if(outputSuccess == -1)
			Log.e(TAG, "Failed to add outputs for " + sd.getClassName());
		
		return retval;
	}
	
	// Add the sample values to the database
	private boolean addServiceIO(ArrayList<ServiceIO> ios, boolean input, String className)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		boolean failures = false;

        for (ServiceIO io1 : ios) {
            ContentValues values = new ContentValues();

            values.put(NAME, io1.getName());
            values.put(FRIENDLY_NAME, io1.getFriendlyName());
            values.put(IO_INDEX, io1.getIndex());
            values.put(DESCRIPTION, io1.getDescription());

            if (!input)
                values.put(MANDATORY, false);
            else
                values.put(MANDATORY, io1.isMandatory());

            IOType type = io1.getType();
            if (type.getID() == -1) // Then it hasn't been validated, see if it's in the database
            {
                long inputId = this.getInputOutputIfExists(type.getName(), type.getClass().getCanonicalName()).getID();

                if(inputId == -1)
                {
                    Log.e(TAG, "ID is -1 for " + io1.getName());
                }

                values.put(IO_TYPE, inputId);
            } else {
                values.put(IO_TYPE, type.getID());
            }

            // So now we have the IO Type ID
            values.put(CLASSNAME, className);
            values.put(I_OR_O, input ? 1 : 0);

            long ioId = db.insertOrThrow(TBL_SERVICEIO, null, values);
            if (ioId == -1) {
                failures = true;
            } else {
                boolean sampleSuccess = addSampleValues(ioId, io1);
                if (!sampleSuccess)
                    failures = true;
            }
        }
		
		return !failures;
	}
	
	private boolean addSampleValues(long ioId, ServiceIO io)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		boolean win = true;
		
		ArrayList<IOValue> samples = io.getSampleValues();

        for (IOValue sample : samples) {
            ContentValues cv = new ContentValues();
            cv.put(SERVICE_IO, ioId);
            cv.put(NAME, sample.name);

            // The value could be anything really - This might be working but I'm really not sure....
            String stringValue = io.getType().toString(io.getValue());
            cv.put(VALUE, stringValue);

            long sampleId = db.insertOrThrow(TBL_IO_SAMPLES, null, cv);
            if (sampleId == -1)
                win = false;
        }
		
		return win;
	}
	
	private IOValue getIOValue(long sampleValue, ServiceIO io)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor c = db.query(TBL_IO_SAMPLES, null,
	 			ID + " = ?", new String[] { "" + sampleValue },
				null, null, null, null);
		
		
		if(c == null)
		{
			Log.e(TAG, "Unable to get io value: " + io.getFriendlyName());
			return null;
		}

		if(c.getCount() == 0)
		{
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
		return new IOValue(name, value);
	}
	
	private boolean addApp(ServiceDescription service)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		String sql = String.format("SELECT * FROM %s WHERE %s = \"%s\"", TBL_APP, PACKAGENAME, service.getPackageName());
		Cursor c = db.rawQuery(sql, null);
		AppDescription app = service.getApp();
		
		if(c == null)
			return false;
		
		if(c.getCount() == 0)
		{
			// Then we need to insert it
			db = this.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(PACKAGENAME, app.getPackageName());
			values.put(NAME, app.getName());
			values.put(ICON, app.getIconLocation());
			values.put(DESCRIPTION, app.getDescription());
			values.put(INSTALLED, 1);
			
			db.insert(TBL_APP, null, values);
		}
		
		// If it gets this far then we're good
		return true;
	}
	
	public ArrayList<ServiceDescription> getComponentsForApp(String packageName)
	{
		ArrayList<ServiceDescription> components = new ArrayList<ServiceDescription>();
		
		SQLiteDatabase db = this.getReadableDatabase();
		
		String query = String.format("SELECT * FROM %s WHERE %s = \"%s\"", TBL_COMPONENT, PACKAGENAME, packageName);
		Cursor c = db.rawQuery(query, null);
		
		if(c == null || c.getCount() == 0)
		{
			// All has failed
			return components;
		}
		
		c.moveToFirst();
		
		do
		{
			ServiceDescription sd = ServiceDescription.createFromCursor(c, "");
			sd.setInputs(getServiceIOs(sd, true));
			sd.setOutputs(getServiceIOs(sd, false));
			components.add(sd);
		}
		while(c.moveToNext());
        c.close();

		return components;
	}

	/**
	 * Updates the service. It's been added after a broadcast, and is now being updated
	 * after more information about it has been looked up.
	 *
	 * @param service 	The description of the service containing the new information
	 * @return			An indication as to whether it has been successful or not
	 */
	public boolean updateServiceFromLookup(ServiceDescription service)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		String strFilter = CLASSNAME + "='" + service.getClassName() + "'";
		ContentValues args = new ContentValues();

		args.put(AVG_RATING, service.getAverageRating());
		args.put(NUM_RATINGS, service.getNumReviews());
		args.put(PRICE, service.getPrice());

		int retval = db.update(TBL_COMPONENT, args, strFilter, null);

		if(retval == 0)
		{
			Log.e(TAG, "Failed to update service " + service.getClassName());
			return false;
		}
		else
		{
			if(LOG) Log.d(TAG, "Updated service " + service.getClassName());
			return true;
		}
	}

	private ArrayList<ServiceIO> getServiceIOs(ServiceDescription sd, boolean input)
	{
		ArrayList<ServiceIO> ios = new ArrayList<ServiceIO>();

		SQLiteDatabase db = this.getReadableDatabase();
		String textInput = input ? "1" : "0";
		Cursor c = db.query(TBL_SERVICEIO, null,
	 			CLASSNAME + " = ? AND " + I_OR_O + " = ?", new String[] { sd.getClassName(),  textInput},
				null, null, null, null);

		if(c == null)
		{
			Log.e(TAG, String.format("getServiceIO() %s %s: Cursor dead", sd.getClassName(), "" + input));
			return null;
		}

		if(c.getCount() == 0)
		{
			return ios;
		}

		c.moveToFirst();

		do
		{
			long id = c.getLong(c.getColumnIndex(ID));
			String name = c.getString(c.getColumnIndex(NAME));
			String friendlyName = c.getString(c.getColumnIndex(FRIENDLY_NAME));
			int index = c.getInt(c.getColumnIndex(IO_INDEX));
			boolean mandatory = c.getInt(c.getColumnIndex(MANDATORY)) == 1;

            if(c.getLong(c.getColumnIndex(IO_TYPE)) == -1)
            {
                Log.w(TAG, DatabaseUtils.dumpCurrentRowToString(c));
                Log.e(TAG, "ID is empty?? " + name);
            }

			IOType type = getIOType(c.getLong(c.getColumnIndex(IO_TYPE)));
			String description = c.getString(c.getColumnIndex(DESCRIPTION));

			ServiceIO io = new ServiceIO(id, name, friendlyName, index, type, description, sd, mandatory, new ArrayList<IOValue>());
			getSampleValues(io);

			ios.add(io);
		}
		while(c.moveToNext());
		c.close();

		return ios;
	}

	private void getSampleValues(ServiceIO io)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		String q = String.format(Locale.ENGLISH, "SELECT * FROM %s WHERE %s = %d", TBL_IO_SAMPLES, SERVICE_IO, io.getId());

		Cursor c = db.rawQuery(q, null);

		if(c == null)
		{
			Log.e(TAG, "Getting sample values fail, or there aren't any...");
			return;
		}

		if(c.getCount() == 0)
		{
			// This should just mean that there aren't any
			return;
		}

		c.moveToFirst();

		do
		{
			// Build and set up the IO values
			long id = c.getLong(c.getColumnIndex(ID));
			String name = c.getString(c.getColumnIndex(NAME));
			String stringValue = c.getString(c.getColumnIndex(VALUE));

			IOType type = io.getType();

			IOValue value = new IOValue(id, name);
			if(type.equals(IOType.Factory.getType(IOType.Factory.NUMBER)))
				value.setValue(Integer.parseInt(stringValue));
			else if(type.equals(IOType.Factory.getType(IOType.Factory.BOOLEAN)))
				value.setValue(Boolean.parseBoolean(stringValue));
			else // It's probably just a string, so a string representation will do...
				value.setValue(stringValue);

			io.addSampleValue(value);
		}
		while(c.moveToNext());
        c.close();
	}

	/**
	 * We only need the intermediary table for going the other way I think!!
	 *
	 * @param packageName The name of the package to check for apps
	 * @return An object representing the app description
	 */
	private AppDescription getAppForService(String packageName)
	{
		if(appMap.containsKey(packageName))
			return appMap.get(packageName);

		SQLiteDatabase db = this.getReadableDatabase();
		String query = String.format("SELECT * FROM %s WHERE %s = \"%s\"", TBL_APP, PACKAGENAME, packageName);
		Cursor c = db.rawQuery(query, null);

		if(c == null)
			return null;

		if(c.getCount() == 0)
			return null;

		c.moveToFirst();

		AppDescription app = AppDescription.parseFromCursor(c);
		appMap.put(packageName, app);
		return app;
	}




	/**
	 * Returns an IO if it exists, adds it if it doesn't
	 *
	 * @param name	The name of the IOType to be searched for/added
     * @param className The name of the class
	 * @return				the IO Type
	 */
	public IOType getInputOutputIfExists(String name, String className)
	{
		if(className.equals(""))
		{
			return null;
		}

		IOType type = getIOType(className);

		if(type == null)
		{
			long id = addInputOutput(name, className);

            if(id == -1)
            {
                Log.w(TAG, "ID -1 for " + name + ", " + className);
            }

			type = getIOType(id);
		}


		return type;
	}


	/**
	 * Returns an IOType
	 *
	 * @param ioId	The ID of the IOType to be got
	 * @return		The IOType
	 */
	public IOType getIOType(long ioId)
	{
        // Get it from the cache if it's in there
        if(typeMap.get(ioId) != null)
            return typeMap.get(ioId);

		SQLiteDatabase db = this.getWritableDatabase();

		String sql = String.format("SELECT * FROM %s WHERE %s = %s", TBL_IOTYPE, ID, "" + ioId);
		Cursor c = db.rawQuery(sql, null);

		if(c == null)
		{
			Log.e(TAG, String.format("getIOType() %d: Cursor dead", ioId));
			return null;
		}

		if(c.getCount() == 0)
		{
			Log.e(TAG, String.format("getIOType() %d: Cursor empty", ioId));
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
	 * @param inputClassName	The name of the class of the IOType
	 * @return				The IOType
	 */
	public IOType getIOType(String inputClassName) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.query(TBL_IOTYPE, new String[]{ID, NAME, CLASSNAME}, CLASSNAME + "=?", new String[]{"" + inputClassName}, null, null, null);

        if (c == null) {
            Log.e(TAG, String.format("getIOType() %s: Cursor dead", inputClassName));
            return null;
        }

		if(c.getCount() == 0) {
            if(LOG) Log.w(TAG, String.format("getIOType() %s: Cursor empty", inputClassName));
            return null;
        }

		c.moveToFirst();

		long id = c.getLong(c.getColumnIndex(ID));
		String className = c.getString(c.getColumnIndex(CLASSNAME));

		IOType type = IOType.Factory.getType(className);
		type.setID(id);
		return type; //new IOType(id, name, className);
	}


	/**
	 * Adds an IOType with the given class name
	 *
	 * @param name	The name of the IOType to be added
     * @param className the class name of the IO to be added
	 * @return				The ID of the inserted IOType
	 */
	public long addInputOutput(String name, String className)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(NAME, name);
		values.put(CLASSNAME, className);

		return db.insert(TBL_IOTYPE, null, values);
	}

	public long updateComposite(CompositeService cs)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		if(cs.getId() == -1)
		{
			// This is a new one and it needs to be added from scratch
			ContentValues values = new ContentValues();
			values.put(NAME, "");
			values.put(DESCRIPTION, "");
			values.put(IS_RUNNING, 0);
            values.put(SHOULD_BE_RUNNING, 1);
			values.put(ACTIVE_OR_TIMER, 0);

			ArrayList<ServiceDescription> components = cs.getComponents();
			if(components.size() == 0)
				return -1;

			if(components.get(0).getProcessType() == ProcessType.TRIGGER)
				values.put(ACTIVE_OR_TIMER, 1);
			else
				values.put(ACTIVE_OR_TIMER, 0);

			long compositeId = db.insert(TBL_COMPOSITE, null, values);
			cs.setId(compositeId);

            deleteAllComponents(cs.getId());
			for(int i = 0; i < components.size(); i++)
			{
				long linkId = this.addCompositeHasAtomic(compositeId, components.get(i).getClassName(), i);

				if(linkId == -1)
					Log.e(TAG, "Failed to add " + components.get(i).getClassName() + " to " + cs.getId());
				else if(LOG)
					Log.d(TAG, "Added component: " + components.get(i).getClassName() + " to " + cs.getId());
			}

			// Now we need to connect the ServiceIOs
			boolean ioSuccess = addIOConnections(compositeId, components);
			boolean filterValueSuccess = updateFiltersAndValues(cs);

			if(!ioSuccess)
			{
				Log.e(TAG, "Composite save LOSE -- IO");
				return -1;
			}
			else if(!filterValueSuccess)
			{
				Log.e(TAG, "Composite save LOSE -- IO");
				return -1;
			}
			else
			{
				if(LOG) Log.d(TAG, "Successfully saved " + cs.getId() + "("  + cs.getName() + ")");
				return cs.getId();
			}
		}
		else
		{
			// We've already saved it once, so we just need to add everything else
			ContentValues values = new ContentValues();

			Log.e(TAG, "Setting name to" + cs.getName());
			values.put(NAME, cs.getName());
			values.put(DESCRIPTION, cs.getDescription());

			ArrayList<ServiceDescription> components = cs.getComponents();
			if(components.size() == 0)
				return -1;

			if(components.get(0).getProcessType() == ProcessType.TRIGGER)
				values.put(ACTIVE_OR_TIMER, 1);
			else
				values.put(ACTIVE_OR_TIMER, 0);

			int ret = db.update(TBL_COMPOSITE, values, ID + " = ?", new String[]{ "" + cs.getId() });
			if(LOG)
				if(ret == 0)
					Log.d(TAG, "Nothing to update for " + cs.getId() + "(" + cs.getName() + ")");
				else
					Log.d(TAG, "Updated " + ret + " rows for " + cs.getId() + "(" + cs.getName() + ")");

			// Clear the atomic table for that component, then add all the components
            deleteAllComponents(cs.getId());
			for(int i = 0; i < components.size(); i++)
			{
				long linkId = this.addCompositeHasAtomic(cs.getId(), components.get(i).getClassName(), i);

				if(linkId == -1)
					Log.e(TAG, "Failed to add " + components.get(i).getClassName() + " to " + cs.getId());
				else if(LOG)
					Log.d(TAG, "Added component: " + components.get(i).getClassName() + " to " + cs.getId());
			}

			// Clear the IO connections then add them all
			db.delete(TBL_COMPOSITE_IOCONNECTION, COMPOSITE_ID + " = ?", new String[]{"" + cs.getId()});

			boolean ioSuccess = addIOConnections(cs.getId(), components);
			boolean filterValueSuccess = updateFiltersAndValues(cs);

			if(!ioSuccess)
			{
				Log.e(TAG, "Composite save LOSE -- IO");
				return -1;
			}
			else if(!filterValueSuccess)
			{
				Log.e(TAG, "Composite save LOSE -- IO");
				return -1;
			}
			else
			{
				if(LOG) Log.d(TAG, "Successfully updated " + cs.getId() + "("  + cs.getName() + ")");
				return cs.getId();
			}
		}
	}

	private boolean addIOConnections(long compositeId, ArrayList<ServiceDescription> services)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		// If there's only one service we don't need to do anything
		if(services.size() <= 1)
			return true;

		boolean allSuccess = true;

		// Loop through the inputs to the second service in each pair, and then add a connection for the relevant output to the relevant input
        for (ServiceDescription current : services) {
            // Loop through the current inputs
            ArrayList<ServiceIO> currentInputs = current.getInputs();
            for (ServiceIO input : currentInputs) {
                ServiceIO output = input.getConnection();

                if (output == null) // Currently nothing is mandatory...
                    continue;

                ContentValues values = new ContentValues();
                values.put(COMPOSITE_ID, compositeId);
                values.put(INPUT_CLASSNAME, current.getClassName());
                values.put(INPUT_IO_ID, input.getId());
                values.put(OUTPUT_IO_ID, output.getId());
                values.put(OUTPUT_CLASSNAME, output.getParent().getClassName());

                long connectionId = db.insert(TBL_COMPOSITE_IOCONNECTION, null, values);

                if (connectionId == -1) {
                    Log.e(TAG, compositeId + ": Failed to set link between " + current.getClassName() +
                            " and " + output.getParent().getClassName());
                    allSuccess = false;
                }
            }
        }

		return allSuccess;
	}

	/***
	 * Get all of the composites!
	 *
	 * @return All of the composites
     * @param includeTemp whether the temp should be included or not
	 */
	public ArrayList<CompositeService> getComposites(boolean includeTemp)
	{
		ArrayList<CompositeService> composites = new ArrayList<CompositeService>();
		SQLiteDatabase db = this.getWritableDatabase();

		Cursor c;

        if(includeTemp)
            c = db.query(TBL_COMPOSITE, new String[] { ID }, null, null, null, null, null);
        else
            c = db.query(TBL_COMPOSITE, new String[] { ID }, ID + " <> ?", new String[] { "" + TEMP_ID }, null, null, null);

		if(c == null)
			return composites;

		if(c.getCount() == 0)
			return composites;

		if(c.moveToFirst())
		{
			do
			{
				long id = c.getLong(c.getColumnIndex(ID));


				composites.add(getComposite(id));
			}
			while(c.moveToNext());
            c.close();
		}

		return composites;
	}


	/**
	 * Get one of the composites
	 *
	 * @param id The id of the composite
	 * @return The composite
	 */
	public CompositeService getComposite(long id)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor c = db.query(TBL_COMPOSITE,
							new String[]{ ID, NAME, DESCRIPTION, ACTIVE_OR_TIMER },
							ID + " = ?", new String[]{ "" + id },
							null, null, null, null);

		if(c == null)
		{
			return null;
		}

		if(c.getCount() == 0)
		{
			return null;
		}

		if(!c.moveToFirst())
			return null;

		String name = c.getString(c.getColumnIndex(NAME));
		String description = c.getString(c.getColumnIndex(DESCRIPTION));
		int active = c.getInt(c.getColumnIndex(ACTIVE_OR_TIMER));

		CompositeService cs = new CompositeService(id, name, description, active == 1);

		setupComponents(cs);

		return cs;
	}

	/**
	 * Sort out the components of the composite
	 *
	 * @param cs The composite inside which the composites exist
	 */
	public void setupComponents(CompositeService cs)
	{
		ArrayList<ServiceDescription> components = new ArrayList<ServiceDescription>();
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor c = db.query(TBL_COMPOSITE_HAS_COMPONENT, null,
				 			COMPOSITE_ID + " = ?", new String[] { "" + cs.getId() },
							null, null, POSITION, null);

		if(c == null)
		{
			Log.e(TAG, "Error, trying to get components but didn't really manage it");
			return;
		}

		if(c.getCount() == 0) // This is fine, there just aren't any
		{
			if(LOG) Log.d(TAG, "No components... " + cs.getId());
			return;
		}

		c.moveToFirst();

		do
		{
			// Make a component from the stuff we get back and then add it at the right position
			int position = c.getInt(c.getColumnIndex(POSITION));

			components.add(position, getComponentForComposite(c.getString(c.getColumnIndex(CLASSNAME))));
		}
		while(c.moveToNext());
        c.close();

		cs.setComponents(components);

		// We should have all of the components set by this point, now do the connections
		setupConnections(cs);

		// Then get all the filter information for those components
		getFiltersAndValues(cs);
	}

	/**
	 * Gets the atomic service with the given ID
	 *
	 * @param className	The name of the class of the service to get
	 * @return 		The service with the given ID
	 */
	public ServiceDescription getComponentForComposite(String className)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.query(TBL_COMPONENT, null,
	 			CLASSNAME + " = ?", new String[] { className },
				null, null, null, null);


		if(c == null)
		{
			return null;
		}

		if(c.getCount() == 0)
		{
			return null;
		}

		c.moveToFirst();

		ServiceDescription sd = ServiceDescription.createFromCursor(c, "");

		ArrayList<ServiceIO> inputs = getServiceIOs(sd, true);
		sd.setInputs(inputs);

		ArrayList<ServiceIO> outputs = getServiceIOs(sd, false);
		sd.setOutputs(outputs);

		AppDescription app = getAppForService(sd.getPackageName());
		if(app != null)
			sd.setApp(app);

		return sd;
	}

	public void setupConnections(CompositeService cs)
	{
		String query = String.format("SELECT * FROM %s WHERE %s = %s",
				TBL_COMPOSITE_IOCONNECTION, COMPOSITE_ID, "" + cs.getId());

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(query, null);

		if(c == null)
			return;

		if(c.getCount() == 0)
			return;

		c.moveToFirst();

		do
		{
			// Get the output atomic
			ServiceDescription first = null;
			String firstClassName = c.getString(c.getColumnIndex(OUTPUT_CLASSNAME));
			for(ServiceDescription service : cs.getComponents())
			{
				if(service.getClassName().equals(firstClassName))
				{
					first = service;
				}
			}

			if(first == null)
				continue;

			// Get the ID of the input atomic
			ServiceDescription second = null;
			String secondClassName = c.getString(c.getColumnIndex(INPUT_CLASSNAME));
			for(ServiceDescription service : cs.getComponents())
			{
				if(service.getClassName().equals(secondClassName))
				{
					second = service;
				}
			}

			if(second == null)
				continue;

			// Get the ID of the serviceIO output
			ArrayList<ServiceIO> outputs = first.getOutputs();
			ServiceIO output = null;
			long outputId = c.getLong(c.getColumnIndex(OUTPUT_IO_ID));
			for(ServiceIO io : outputs)
			{
				if(io.getId() == outputId)
				{
					output = io;
				}
			}

			if(output == null)
				continue;

			// Get the ID of the serviceIO input
			ArrayList<ServiceIO> inputs = second.getInputs();
			ServiceIO input = null;
			long inputId = c.getLong(c.getColumnIndex(INPUT_IO_ID));
			for(ServiceIO io : inputs)
			{
				if(io.getId() == inputId)
				{
					input = io;
				}
			}

			if(input == null)
				continue;

			// Connect the input to the output and vice versa
			input.setConnection(output);
			output.setConnection(input);

		}
		while(c.moveToNext());
        c.close();
	}

	public boolean updateWiring(CompositeService cs)
	{
		// Just delete them all and then re-add them all again - this seems easiest?
		SQLiteDatabase db = this.getWritableDatabase();
		int delCount = db.delete(TBL_COMPOSITE_IOCONNECTION, COMPOSITE_ID + "=" + cs.getId(), null);

		if(LOG) Log.d(TAG, "DBUPDATE: Deleted " + delCount + " rows for " + cs.getName());

		// This could be zero to be fair
		addIOConnections(cs.getId(), cs.getComponents());

		return true;
	}

	public boolean deleteComposite(CompositeService cs)
	{
		long id = cs.getId();

		SQLiteDatabase db = this.getWritableDatabase();
		int cStatus = db.delete(TBL_COMPOSITE, ID + "=?", new String[] { "" + id });
		int chcStatus = db.delete(TBL_COMPOSITE_HAS_COMPONENT, ID +"=?", new String[]{ "" + id });
		int cioStatus = db.delete(TBL_COMPOSITE_IOCONNECTION, ID + "=?", new String[] { "" + id });
		int fStatus = db.delete(TBL_FILTER, ID + "=?", new String[] { "" + id });

        return cStatus == -1 || chcStatus == -1 || cioStatus == -1 || fStatus == -1;
	}

	public ArrayList<CompositeService> getIntendedRunningComposites()
	{
		ArrayList<CompositeService> serviceList = new ArrayList<CompositeService>();
		String query = String.format(Locale.getDefault(), "SELECT * FROM %s WHERE %s = %d", TBL_COMPOSITE, ACTIVE_OR_TIMER, 1);

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor c = db.rawQuery(query, null);

		if(c == null)
		{
			Log.e(TAG, "Running cursor dead - getIntendedRunningComposites");
			return serviceList;
		}

		if(c.getCount() == 0)
		{
			return serviceList;
		}

		if(c.moveToFirst())
		{
			do
			{
				long id = c.getLong(c.getColumnIndex(ID));
				String name = c.getString(c.getColumnIndex(NAME));
				long numeral = c.getLong(c.getColumnIndex(NUMERAL));
				int intervalIndex = c.getInt(c.getColumnIndex(INTERVAL));
				Interval interval;

				if(intervalIndex == Interval.SECONDS.index)
				{
					interval = Interval.SECONDS;
				}
				else if(intervalIndex == Interval.MINUTES.index)
				{
					interval = Interval.MINUTES;
				}
				else if(intervalIndex == Interval.HOURS.index)
				{
					interval = Interval.HOURS;
				}
				else
				{
					interval = Interval.DAYS;
				}

				CompositeService cs = getComposite(id);
				serviceList.add(new CompositeService(id, name, cs.getComponents(), numeral, interval));
			}
			while(c.moveToNext());
            c.close();
		}

		return serviceList;
	}

	/**
	 * Returns whether it should be running, and then whether it is running
	 *
	 * @param id The id of the composite to find out about the running status
	 * @return The running status of the composite
	 */
	public Pair<Boolean, Boolean> compositeRunning(long id)
	{
		SQLiteDatabase db = this.getReadableDatabase();

		if(id == -1)
		{
			// ID not set, don't really know what to do here
			return null;
		}

		Cursor c = db.query(TBL_COMPOSITE, new String[]{ ACTIVE_OR_TIMER, IS_RUNNING }, ID + "=?", new String[] { "" + id }, null, null, null);

		if(c == null)
			return new Pair<Boolean, Boolean>(false, false);

		c.moveToFirst();

		long is = c.getLong(c.getColumnIndex(IS_RUNNING));
		long should = c.getLong(c.getColumnIndex(ACTIVE_OR_TIMER));

		return new Pair<Boolean, Boolean>((should == 1), (is == 1));
	}

	/**
	 * Set whether a composite service is currently running or not
	 *
	 * @param id The id of the composite
	 * @param running The new running status
	 * @return The success?
	 */
	public boolean setCompositeIsRunning(long id, long running)
	{
		return setCompositeRunning(id, running, IS_RUNNING);
	}


	/**
	 * Sets whether the composite service should be running or not
	 *
	 * @param id The id of the composite
	 * @param running the new active status
	 * @return The success of this?
	 */
	public boolean setCompositeActive(long id, long running)
	{
		return setCompositeRunning(id, running, ACTIVE_OR_TIMER);
	}

	/**
	 * So we don't have such code duplication with two methods doing essentially
	 * the same thing.
	 *
	 * @param id The id of the composite
	 * @param running The running status of the composite
	 * @param type Custom type
	 * @return The status
	 */
	private boolean setCompositeRunning(long id, long running, String type)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		if(id == -1)
		{
			// Id not set, don't really know what to do here
			return false;
		}

		String strFilter = ID + "=" + id;
		ContentValues args = new ContentValues();
		args.put(type, running);
		int retval = db.update(TBL_COMPOSITE, args, strFilter, null);

        return retval != 0;
	}


	/***********************************************************************************
	 * Link from composite service to atomic service stuff
	 **********************************************************************************/

    private int deleteAllComponents(long compositeId)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TBL_COMPOSITE_HAS_COMPONENT, COMPOSITE_ID + " = ? ", new String[]{ "" + compositeId });
    }

	public long addCompositeHasAtomic(long compositeId, String className, int position)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(COMPOSITE_ID, compositeId);
		values.put(CLASSNAME, className);
		values.put(POSITION, position);

		return db.insert(TBL_COMPOSITE_HAS_COMPONENT, null, values);
	}

	public boolean compositeExistsWithName(String name)
	{
		SQLiteDatabase db = this.getReadableDatabase();

		String query = String.format("SELECT * FROM %s WHERE %s = \"%s\"", TBL_COMPOSITE,
				NAME, name);

		Cursor c = db.rawQuery(query, null);

		if(c == null)
		{
			Log.e(TAG, String.format("Cursor dead compositeExistsWithName %s", name));
			return false;
		}

        return c.getCount() != 0;

    }

	public boolean setTimerDuration(long compositeId, long numeral, Interval interval)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();

		values.put(NUMERAL, numeral);
		values.put(INTERVAL, interval.index);

		String strFilter = ID + "=" + compositeId;
		int retval = db.update(TBL_COMPOSITE, values, strFilter, null);

        return retval == 1;
	}

	public Pair<Long, Interval> getTimerDuration(long compositeId)
	{
		SQLiteDatabase db = this.getReadableDatabase();

		String query = String.format("SELECT * FROM %s WHERE %s = %s", TBL_COMPOSITE, ID, compositeId);

		Cursor c = db.rawQuery(query, null);

		if(c == null)
		{
			Log.e(TAG, "Cursor null getTimerDuration " + compositeId);
			return null;
		}

		if(c.getCount() == 0)
		{
			Log.e(TAG, "No rows getTimerDuration " + compositeId);
			return null;
		}

		c.moveToFirst();

		long numeral = c.getLong(c.getColumnIndex(NUMERAL));
		int intervalValue = c.getInt(c.getColumnIndex(INTERVAL));

		Interval interval;

		if(intervalValue == Interval.SECONDS.index)
		{
			interval = Interval.SECONDS;
		}
		else if(intervalValue == Interval.MINUTES.index)
		{
			interval = Interval.MINUTES;
		}
		else if(intervalValue == Interval.HOURS.index)
		{
			interval = Interval.HOURS;
		}
		else
		{
			interval = Interval.DAYS;
		}

		return new Pair<Long, Interval>(numeral, interval);
	}

	public boolean updateFiltersAndValues(CompositeService cs)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		ArrayList<ServiceDescription> components = cs.getComponents();
		int runningFailures = 0;

		// Delete all of the filters for that particular composite
		int numRemoved = db.delete(TBL_FILTER, COMPOSITE_ID + " = ?", new String[] { "" + cs.getId() });
		if(LOG) Log.d(TAG, "Removed " + numRemoved + " from Filter for " + cs.getId() + "("  + cs.getName() + ")");

        for (ServiceDescription c : components) {
            // Do the filter conditions on the outputs first
            ArrayList<ServiceIO> inputs = c.getInputs();
            for (ServiceIO o : inputs) {
                if (o.isFiltered() != ServiceIO.UNFILTERED) {
                    ContentValues values = new ContentValues();

                    // Set the filter state
                    values.put(FILTER_STATE, o.isFiltered());
                    values.put(COMPOSITE_ID, cs.getId());
                    values.put(SERVICE_IO, o.getId());
                    values.put(CLASSNAME, c.getClassName());

                    values.put(MANUAL_VALUE, o.getManualValue().toString());

                    values.put(SAMPLE_VALUE, o.getChosenSampleValue().id);

                    values.put(FILTER_CONDITION, o.getCondition());

                    long ret = db.insert(TBL_FILTER, null, values);

                    if (ret == -1) {
                        runningFailures++;
                    }
                }
            }


            // Do the filter conditions on the outputs first
            ArrayList<ServiceIO> outputs = c.getOutputs();
            for (ServiceIO o : outputs) {
                if (o.isFiltered() != ServiceIO.UNFILTERED) {
                    ContentValues values = new ContentValues();

                    // Set the filter state
                    values.put(FILTER_STATE, o.isFiltered());
                    values.put(COMPOSITE_ID, cs.getId());
                    values.put(SERVICE_IO, o.getId());
                    values.put(CLASSNAME, c.getClassName());

                    values.put(MANUAL_VALUE, o.getType().toString(o.getManualValue()));
                    values.put(SAMPLE_VALUE, o.getChosenSampleValue().id);

                    values.put(FILTER_CONDITION, o.getCondition());

                    long ret = db.insert(TBL_FILTER, null, values);

                    if (ret == -1) {
                        runningFailures++;
                    }
                }
            }
        }

		return runningFailures == 0;
	}

	/**
	 * This one needs to set all of the values out of the database for a Composite
	 * that's already been got.
	 *
	 * @param cs The composite to get the filter values for
	 */
	public void getFiltersAndValues(CompositeService cs)
	{
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor c = db.query(TBL_FILTER, null,
	 			COMPOSITE_ID + " = ?", new String[] { ""+  cs.getId() },
				null, null, null, null);

		if(c == null)
		{
			Log.e(TAG, String.format("Cursor dead for getFilterValues: %s", cs.getName()));
			return ;
		}

		if(c.getCount() == 0)
		{
			// This shouldn't be a problem, it probably just means that there are no filters for that composite
			return;
		}

		c.moveToFirst();

		do
		{
			// Need to know which atomic in the composite we're operating on
			ServiceDescription component = cs.getComponent(c.getString(c.getColumnIndex(CLASSNAME)));

			if(component == null)
				continue; // I don't feel like this should happen but you never know...

			ServiceIO io = component.getIO(c.getLong(c.getColumnIndex(SERVICE_IO)));

			if(io == null)
				continue; // Ditto

			// Try to set both of the values in case they've saved an old one

			long sampleValue = c.getLong(c.getColumnIndex(SAMPLE_VALUE));
			if(sampleValue != -1)
			{
				IOValue ioValue = getIOValue(sampleValue, io);
				io.setChosenSampleValue(ioValue);
			}

			String manualValue = c.getString(c.getColumnIndex(MANUAL_VALUE));

			// A the re-loading of values needs to use the new method
			io.setManualValue(io.getType().fromString(manualValue));

			// Set the filter state afterwards to override it to whatever they wanted
			int filterState = c.getInt(c.getColumnIndex(FILTER_STATE));
			io.setFilterState(filterState);

			io.setCondition(c.getInt(c.getColumnIndex(FILTER_CONDITION)));
		}
		while(c.moveToNext());
        c.close();
	}


	public ArrayList<CompositeService> atomicAtPosition(String className, int position)
	{
		SQLiteDatabase db = this.getReadableDatabase();

		ArrayList<CompositeService> composites = new ArrayList<CompositeService>();

		String sql = String.format("SELECT DISTINCT %s FROM %s WHERE %s = '%s' AND %s = %s", COMPOSITE_ID, TBL_COMPOSITE_HAS_COMPONENT, CLASSNAME, className, POSITION, position);

		Cursor c = db.rawQuery(sql, null);

		if(c == null)
		{
			Log.e(TAG, String.format("Cursor dead for atomicAtPosition: %s %d", className, position));
			return null;
		}

		if(c.getCount() == 0)
		{
			return null;
		}

		c.moveToFirst();

		do
		{
			long id = c.getLong(c.getColumnIndex(COMPOSITE_ID));
			composites.add(this.getComposite(id));
		}
		while(c.moveToNext());
        c.close();

		return composites;
	}


	public ArrayList<CompositeService> getExamples(String componentName)
	{
		// Implement some examples of composites that can be used
		// Implement this?
		return new ArrayList<CompositeService>();
	}


	public AppDescription getApp(String packageName)
	{
		String query = String.format("SELECT * FROM %s WHERE %s = \"%s\"", TBL_APP, PACKAGENAME, packageName);

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(query, null);

		if(c == null)
		{
			return null;
		}

		if(c.getCount() == 0)
		{
			return null;
		}

		c.moveToFirst();

		return AppDescription.parseFromCursor(c);
	}

	public boolean setAppInstalled(String packageName, boolean b)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		String strFilter = PACKAGENAME + "='" + packageName + "'";
		ContentValues args = new ContentValues();

		args.put(INSTALLED, b ? 1 : 0);

		int retval = db.update(TBL_APP, args, strFilter, null);

		if(retval == 0)
		{
			Log.e(TAG, "Failed to update service " + packageName);
			return false;
		}
		else
		{
			Log.e(TAG, "Updated service " + packageName);
			return true;
		}
	}


	public boolean addToLog(long compositeId, String className, String message, int status)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
		Date date = new Date();

		ContentValues values = new ContentValues();
		values.put(COMPOSITE_ID, compositeId);
		values.put(CLASSNAME, className);
		values.put(MESSAGE, message);
		values.put(TIME, sdf.format(date));
		values.put(LOG_TYPE, status);

		long id = db.insert(TBL_EXECUTION_LOG, null, values);
		return id != -1;
	}

	public ArrayList<LogItem> getLog()
	{
		ArrayList<LogItem> items = new ArrayList<LogItem>();

		String query = String.format("SELECT * FROM %s", TBL_EXECUTION_LOG);

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(query, null);

		if(c == null)
		{
			return null;
		}

		if(c.getCount() == 0)
		{
			return null;
		}

		c.moveToFirst();

		do
		{
			items.add(new LogItem(c));
		}
		while(c.moveToNext());
        c.close();

		return items;
	}

	public Tag addTag(String name)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues cv = new ContentValues();
		cv.put(NAME, name);

		long id = db.insert(TBL_TAG, null, cv);
		return new Tag(id, name);
	}

	public Tag getTag(long id)
	{
        if(tagMap.get(id) != null)
            return tagMap.get(id);

		SQLiteDatabase db = this.getReadableDatabase();

		// Should probably do something clever than just equals - consider lower case too
		Cursor c = db.query(TBL_TAG, null,
	 			ID + " = ?", new String[] { "" + id },
				null, null, null, null);

		if(c == null)
		{
			Log.e(TAG, "Dead cursor: tag exists " + id);
			return null;
		}

		if(c.getCount() == 0)
		{
			// Insert it?
			return new Tag();
		}

		c.moveToFirst(); // There better not be more than one...

		return Tag.createOneFromCursor(c);
	}

	public Tag getTag(String name)
	{
		SQLiteDatabase db = this.getReadableDatabase();

		// Should probably do something clever than just equals - consider lower case too
		Cursor c = db.query(TBL_TAG, null,
	 			NAME + " = ?", new String[] { name },
				null, null, null, null);

		if(c == null)
		{
			Log.e(TAG, "Dead cursor: get tag " + name);
			return null;
		}

		if(c.getCount() == 0)
		{
			// Insert it?
			return addTag(name);
		}

		c.moveToFirst(); // There better not be more than one...

		return Tag.createOneFromCursor(c);
	}

	public boolean addTagsForComponent(ServiceDescription component)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		boolean allWin = true;
		ArrayList<Tag> tags = component.getTags();

        for (Tag t : tags) {
            if (t.getId() == -1) {
                // Then we need to get an ID (and maybe insert it)
                t = getTag(t.getName());
            }

            ContentValues cv = new ContentValues();
            cv.put(TAG_ID, t.getId());
            cv.put(CLASSNAME, component.getClassName());

            long id = db.insert(TBL_COMPONENT_HAS_TAG, null, cv);

            if (id == -1)
                allWin = false;
        }

		return allWin;
	}

	public ArrayList<Tag> getTagsForComponent(String className)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		ArrayList<Tag> tags = new ArrayList<Tag>();

		Cursor c = db.query(TBL_COMPONENT_HAS_TAG, null,
	 			CLASSNAME + " = ?", new String[] { className },
				null, null, null, null);

		if(c == null)
		{
			Log.e(TAG, "Cursor dead for getTagsForComponent: " + className);
			return tags;
		}

		if(c.getCount() == 0)
		{
			return tags;
		}

		c.moveToFirst();

		do
		{

			long tagId = c.getLong(c.getColumnIndex(TAG_ID));
			Tag t = getTag(tagId);
			tags.add(t);
		}
		while(c.moveToNext());
		c.close();

		return tags;
	}

	public boolean shouldBeRunning(long id)
	{
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor c = db.query(TBL_COMPOSITE, new String[]{ SHOULD_BE_RUNNING },
	 			ID + " = ?", new String[] { "" + id },
				null, null, null, null);

		if(c == null)
			return false;

		if(c.getCount() != 1)
			return false;

		c.moveToFirst();

		int ret = c.getInt((c.getColumnIndex(SHOULD_BE_RUNNING)));

		return ret == 1;
	}


	public ArrayList<ServiceDescription> getMatchingForIOs(ServiceDescription component, boolean inputs)
	{
		ArrayList<ServiceIO> ios = inputs ? component.getInputs() : component.getOutputs();

		HashMap<String, ServiceDescription> components =  new HashMap<String, ServiceDescription>();
		HashMap<String, Long> types = new HashMap<String, Long>();
        for (ServiceIO io : ios) {
            IOType type = io.getType();
            if (!types.containsKey(type.getClassName()))
                types.put(type.getClassName(), type.getID());
        }

		Set<String> keys = types.keySet();

		for(String s : keys)
		{
			Long id = types.get(s);

			if(id == -1)
			{
				// A Need to get the ID of the type
				id = this.getIOType(s).getID();
			}

			// Get all the inputs that use that type - we only want the IDs at this stage
			// We've got inputs from the component parameter, so we need to get the outputs of the other one. Or vice versa
			ArrayList<String> componentNames = this.getComponentIdsForType(id, !inputs);
            for (String name : componentNames) {
                if (!components.containsKey(name)) {
                    components.put(name, getComponent(name));
                }
            }
		}

		ArrayList<ServiceDescription> c = new ArrayList<ServiceDescription>();
		keys = components.keySet();
		for(String s : keys)
		{
			c.add(components.get(s));
		}

		return c;
	}

	private ArrayList<String> getComponentIdsForType(long id, boolean inputs)
	{
		SQLiteDatabase db = this.getReadableDatabase();
		ArrayList<String> ioIds = new ArrayList<String>();

		int ioNum = inputs ? 1 : 0;

		Cursor c = db.query(TBL_SERVICEIO, new String[] { CLASSNAME },
	 			IO_TYPE + " = ? AND " + I_OR_O + " = ?", new String[] { "" + id, "" + ioNum },
				null, null, null, null);

		if(c == null)
		{
			Log.e(TAG, "Cursor dead for getIOsForType(" + id + "," + inputs + ")");
			return ioIds;
		}

		if(c.getCount() == 0)
		{
			return ioIds;
		}

		c.moveToFirst();

		do
		{
			ioIds.add(c.getString(c.getColumnIndex(CLASSNAME)));
		}
		while(c.moveToNext());
        c.close();

		return ioIds;
	}

    /*******************
     * Get stuff (joins)
     ******************/

    public ArrayList<CompositeService> getCompositesJoin(boolean includeTemp)
    {
        return new ArrayList<CompositeService>();
    }

    public CompositeService getCompositeJoin(long compositeId)
    {
        return new CompositeService(false);
    }

    public ServiceDescription getComponent(String className)
    {
        if(componentMap.containsKey(className)) {
            return componentMap.get(className);
        }

        String componentCols = AppGlueLibrary.buildGetAllString(TBL_COMPONENT, COLS_COMPONENT);
        String ioCols = AppGlueLibrary.buildGetAllString(TBL_SERVICEIO, COLS_SERVICEIO);
        String ioSamples = AppGlueLibrary.buildGetAllString(TBL_IO_SAMPLES, COLS_IO_SAMPLES);

        String query = String.format("SELECT %s FROM %s " +
                        "LEFT JOIN %s ON %s.%s = %s.%s " +
                        "LEFT JOIN %s ON %s.%s = %s.%s " +
                        "ORDER BY %s" +
                        " %s",
                componentCols + "," + ioCols + "," + ioSamples,
                TBL_COMPONENT,
                TBL_SERVICEIO, TBL_COMPONENT, CLASSNAME, TBL_SERVICEIO, CLASSNAME,
                TBL_IO_SAMPLES, TBL_SERVICEIO, ID, TBL_IO_SAMPLES, SERVICE_IO,
                TBL_SERVICEIO + "_" + IO_INDEX,
                "WHERE " + TBL_COMPONENT + "_" + CLASSNAME + " = \"" + className + "\"");

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(query, null);

        if(c == null)
        {
            Log.e(TAG, "Cursor dead " + query);
            return null;
        }

        if(c.getCount() == 0) {
            Log.e(TAG, "Cursor empty, this should be impossible: " + query);
            return null;
        }

        ServiceDescription currentComponent = null;
        ServiceIO currentIO = null;

        c.moveToFirst();

        do
        {
           if(currentComponent == null) {

                // Create a new component based on this info
                currentComponent = new ServiceDescription();
                currentComponent.setInfo(TBL_COMPONENT + "_", c);

                // It shouldn't already be in there
                componentMap.put(className, currentComponent);
            }

            long ioId = c.getLong(c.getColumnIndex(TBL_SERVICEIO + "_" + ID));
            long ioTypeId = c.getLong(c.getColumnIndex(TBL_SERVICEIO + "_" + IO_TYPE));

            if(ioMap.get(ioId) != null) {
                currentIO = ioMap.get(ioId);
                currentComponent.addIO(currentIO, currentIO.isInput(), currentIO.getIndex());
            } else if(currentIO == null || currentIO.getId() != ioId) {

                // If it doesn't exist, then use the old one
                currentIO = new ServiceIO(ioId);
                currentIO.setInfo(TBL_SERVICEIO + "_", c);
                currentIO.setType(getIOType(ioTypeId));
                currentComponent.addIO(currentIO, currentIO.isInput(), currentIO.getIndex());
                ioMap.put(ioId, currentIO);
            }

            long sampleId = c.getLong(c.getColumnIndex(TBL_IO_SAMPLES + "_" + ID));

            if(sampleId != -1)
            {
                String sampleName = c.getString(c.getColumnIndex(TBL_IO_SAMPLES + "_" + NAME));
                String strValue = c.getString(c.getColumnIndex(TBL_IO_SAMPLES + "_" + VALUE));

                // When we make the sample it might need to be converted to be the right type of object?
                Object value = currentIO.getType().fromString(strValue);
                currentIO.addSampleValue(new IOValue(sampleId, sampleName, value));
            }

            if(!currentComponent.hasTags())
            {
                Cursor c2 = db.rawQuery(String.format("SELECT * FROM %s WHERE %s = '%s'",
                        TBL_COMPONENT_HAS_TAG, CLASSNAME,
                        currentComponent.getClassName()), null);

                if(c2 == null)
                {
                    Log.e(TAG, "Tag cursor dead for getting components w/ join");
                    continue;
                }

                if(c2.getCount() == 0) // This is fine
                    continue;

                c2.moveToFirst();

                do {
                    currentComponent.addTag(getTag(c2.getInt(c2.getColumnIndex(TAG_ID))));
                } while(c2.moveToNext());
                c2.close();
            }
        }
        while(c.moveToNext());
        c.close();

        return currentComponent;
    }

    public ArrayList<ServiceDescription> getComponents(ProcessType processType)
    {
        String componentCols = AppGlueLibrary.buildGetAllString(TBL_COMPONENT, COLS_COMPONENT);
        String ioCols = AppGlueLibrary.buildGetAllString(TBL_SERVICEIO, COLS_SERVICEIO);
        String ioSamples = AppGlueLibrary.buildGetAllString(TBL_IO_SAMPLES, COLS_IO_SAMPLES);

        String args = "";

        if(processType != null)
            args = " WHERE " + TBL_COMPONENT + "_" + PROCESS_TYPE + " = " + processType;


        String query = String.format("SELECT %s FROM %s " +
            "LEFT JOIN %s ON %s.%s = %s.%s " +
            "LEFT JOIN %s ON %s.%s = %s.%s " +
                "ORDER BY %s" +
                " %s",
                componentCols + "," + ioCols + "," + ioSamples,
                TBL_COMPONENT,
                TBL_SERVICEIO, TBL_COMPONENT, CLASSNAME, TBL_SERVICEIO, CLASSNAME,
                TBL_IO_SAMPLES, TBL_SERVICEIO, ID, TBL_IO_SAMPLES, SERVICE_IO,
                TBL_SERVICEIO + "_" + IO_INDEX,
                args);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(query, null);
        ArrayList<ServiceDescription> components = new ArrayList<ServiceDescription>();

        if(c == null)
        {
            Log.e(TAG, "Cursor dead " + query);
            return components;
        }

        if(c.getCount() == 0) {
            Log.e(TAG, "Cursor empty, this seems unlikely: " + query);
            return components;
        }

        ServiceDescription currentComponent = null;
        ServiceIO currentIO = null;

        c.moveToFirst();

        do
        {
            String className = c.getString(c.getColumnIndex(TBL_COMPONENT + "_" + CLASSNAME));

            if(componentMap.containsKey(className))  {
                currentComponent = componentMap.get(className);
                components.add(currentComponent);
            } else if(currentComponent == null || !currentComponent.getClassName().equals(className)) {

                // Create a new component based on this info
                currentComponent = new ServiceDescription();
                currentComponent.setInfo(TBL_COMPONENT + "_", c);
                components.add(currentComponent);

                // It shouldn't already be in there
                componentMap.put(className, currentComponent);
            }

            long ioId = c.getLong(c.getColumnIndex(TBL_SERVICEIO + "_" + ID));
            long ioTypeId = c.getLong(c.getColumnIndex(TBL_SERVICEIO + "_" + IO_TYPE));

            if(ioMap.get(ioId) != null) {
                currentIO = ioMap.get(ioId);
                currentComponent.addIO(currentIO, currentIO.isInput(), currentIO.getIndex());
            } else if(currentIO == null || currentIO.getId() != ioId) {

                // If it doesn't exist, then use the old one
                currentIO = new ServiceIO(ioId);
                currentIO.setInfo(TBL_SERVICEIO + "_", c);
                currentIO.setType(getIOType(ioTypeId));
                currentComponent.addIO(currentIO, currentIO.isInput(), currentIO.getIndex());
                ioMap.put(ioId, currentIO);
            }

            long sampleId = c.getLong(c.getColumnIndex(TBL_IO_SAMPLES + "_" + ID));

            if(sampleId != -1)
            {
                String sampleName = c.getString(c.getColumnIndex(TBL_IO_SAMPLES + "_" + NAME));
                String strValue = c.getString(c.getColumnIndex(TBL_IO_SAMPLES + "_" + VALUE));

                // When we make the sample it might need to be converted to be the right type of object?
                Object value = currentIO.getType().fromString(strValue);
                currentIO.addSampleValue(new IOValue(sampleId, sampleName, value));
            }

            if(!currentComponent.hasTags())
            {
                Cursor c2 = db.rawQuery(String.format("SELECT * FROM %s WHERE %s = '%s'",
                                        TBL_COMPONENT_HAS_TAG, CLASSNAME,
                                        currentComponent.getClassName()), null);

                if(c2 == null)
                {
                    Log.e(TAG, "Tag cursor dead for getting components w/ join");
                    continue;
                }

                if(c2.getCount() == 0) // This is fine
                    continue;

                c2.moveToFirst();

                do {
                    currentComponent.addTag(getTag(c2.getLong(c2.getColumnIndex(TAG_ID))));
                } while(c2.moveToNext());
                c2.close();
            }
        }
        while(c.moveToNext());
        c.close();

        Log.e(TAG, "Got all components");
        return components;
    }


    /*********************************
     * Below here is the auto-caching stuff
     */

    private void cacheIOTypes()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(String.format("SELECT * FROM %s", TBL_IOTYPE), null);

        if(c == null)
        {
            Log.e(TAG, "Dead cursor: Caching all the types");
            return;
        }

        if(c.getCount() == 0)
        {
            Log.w(TAG, "Empty cursor: Caching all the tags");
            return;
        }

        c.moveToFirst();

        do {

            String className = c.getString(c.getColumnIndex(CLASSNAME));
            IOType type = IOType.Factory.getType(className);
            typeMap.put(type.getID(), type);

        } while(c.moveToNext());

        c.close();
    }

    private void cacheTags()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(String.format("SELECT * FROM %s", TBL_TAG), null);

        if(c == null)
        {
            Log.e(TAG, "Dead cursor: Caching all the tags");
            return;
        }

        if(c.getCount() == 0)
        {
            // Insert it?
            Log.w(TAG, "Empty cursor: Caching all the tags");
            return;
        }

        c.moveToFirst();

        do
        {
            Tag tag = Tag.createOneFromCursor(c);
            tagMap.put(tag.getId(), tag);
        }
        while(c.moveToNext());
        c.close();
    }
}