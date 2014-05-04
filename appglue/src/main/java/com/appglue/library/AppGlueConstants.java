package com.appglue.library;

import com.appglue.library.IOFilter.FilterValue;

import static com.appglue.Constants.AVG_RATING;
import static com.appglue.Constants.CLASSNAME;
import static com.appglue.Constants.COMPOSITE_ID;
import static com.appglue.Constants.DESCRIPTION;
import static com.appglue.Constants.DEVELOPER;
import static com.appglue.Constants.FRIENDLY_NAME;
import static com.appglue.Constants.ICON;
import static com.appglue.Constants.ID;
import static com.appglue.Constants.INPUT_CLASSNAME;
import static com.appglue.Constants.INPUT_IO_ID;
import static com.appglue.Constants.INSTALLED;
import static com.appglue.Constants.IO_INDEX;
import static com.appglue.Constants.IO_TYPE;
import static com.appglue.Constants.I_OR_O;
import static com.appglue.Constants.MANDATORY;
import static com.appglue.Constants.NAME;
import static com.appglue.Constants.NUM_RATINGS;
import static com.appglue.Constants.OUTPUT_CLASSNAME;
import static com.appglue.Constants.OUTPUT_IO_ID;
import static com.appglue.Constants.PACKAGENAME;
import static com.appglue.Constants.PARENT_SERVICE;
import static com.appglue.Constants.POSITION;
import static com.appglue.Constants.PRICE;
import static com.appglue.Constants.PROCESS_TYPE;
import static com.appglue.Constants.SAMPLE_VALUE;
import static com.appglue.Constants.SERVICE_TYPE;
import static com.appglue.Constants.VALUE;

public class AppGlueConstants 
{
	public static final String FIRST_WIRING = "first_wiring";
	public static final String SECOND_WIRING = "second_wiring";
	public static final String CREATE_NEW = "create_new";
	public static final String MATCHING = "matching";
	
	// Database tables
	public static final String TBL_COMPONENT = "atomic";
	public static final String TBL_COMPOSITE = "composite";
	public static final String TBL_APP = "app";
	public static final String TBL_COMPOSITE_HAS_COMPONENT = "composite_has_atomic";
	public static final String TBL_FILTER = "filter_values";
	public static final String TBL_IO_SAMPLES = "io_samples";
		
	public static final String TBL_SERVICEIO = "io";
	public static final String TBL_IOTYPE = "input_output";
	public static final String TBL_COMPOSITE_IOCONNECTION = "composite_ioconnections";
	
	public static final String TBL_TAG = "tag";
	public static final String TBL_COMPONENT_HAS_TAG = "component_has_tag";
	
	public static final String TBL_EXECUTION_LOG = "execution_log";
	
	public static final String JUST_A_LIST = "atomic_list";
	
	public static final String TRIGGERS_ONLY = "triggers_only";
	public static final String NO_TRIGGERS = "no_triggers";

    public static final int TEMP_ID = 1;
	
	public static final int SERVICE_REQUEST = 100;
	public static final int PRE_EXEC_PARAMS = 101;
	public static final int EDIT_PARAMS = 102;
	public static final int WIRE_COMPONENTS = 103;
	
	public static final int MARKET_LOOKUP = 104;
	public static final int SUCCESS = 105;
	public static final int BACK_PRESSED = 106;
	public static final int NOT_SET = 107;
	
	public static final int PLAY_SERVICES = 108;
	public static final int STORY_MODE = 109;
	
	public static final FilterValue[] FILTER_STRING_VALUES = new FilterValue[]
	{ 
		IOFilter.STR_EQUALS, IOFilter.STR_NOTEQUALS, IOFilter.STR_CONTAINS
	};
	
	public static final FilterValue[] FILTER_NUMBER_VALUES = new FilterValue[]
	{
		IOFilter.INT_EQUALS, IOFilter.INT_NOTEQUALS, IOFilter.INT_LEQUALS,
		IOFilter.INT_LT, IOFilter.INT_GEQUALS, IOFilter.INT_GT
	};
	
	public static final FilterValue[] FILTER_BOOL_VALUES = new FilterValue[]
	{
		IOFilter.BOOL_EQUALS, IOFilter.BOOL_NOTEQUALS
	};
	
	public static final FilterValue[] FILTER_SET_VALUES = new FilterValue[]
	{
		IOFilter.SET_EQUALS, IOFilter.SET_NOTEQUALS
	};
	
	public static String[] FILTER_BOOL = new String[] { "true", "false" };
	
	public static final String SHOULD_BE_RUNNING = "should_be_running";
	public static final String IS_RUNNING = "running";
	public static final String ACTIVE_OR_TIMER = "should";
	public static final String NUMERAL = "numeral";
	public static final String INTERVAL = "interval";


	public static final String[][] COLS_COMPOSITE = new String[][]
	{
		{ ID, "INTEGER PRIMARY KEY AUTOINCREMENT" },
		{ NAME, "TEXT" },
		{ DESCRIPTION, "TEXT" },
		{ ACTIVE_OR_TIMER, "TINYINT" },
		{ IS_RUNNING, "TINYINT" },
		{ SHOULD_BE_RUNNING, "TINYINT" },
		{ NUMERAL, "INTEGER" },
		{ INTERVAL, "INTEGER" }
	};
	
	public static final String[][] COLS_COMPONENT = new String[][]
	{
		{ CLASSNAME, "TEXT PRIMARY KEY" },
		{ NAME, "TEXT" }, 
		{ PACKAGENAME, "TEXT" }, 
		{ DESCRIPTION, "TEXT" },
		{ AVG_RATING, "FLOAT" }, 
		{ NUM_RATINGS, "INTEGER" }, 
		{ PRICE, "FLOAT" }, 
		{ SERVICE_TYPE, "INTEGER" }, 
		{ PROCESS_TYPE, "INTEGER" }
	};

    public static final String[][] COLS_TAG = new String[][]
    {
        { ID, "INTEGER PRIMARY KEY AUTOINCREMENT" },
        { NAME, "TEXT" }
    };


    public static final String[][] COLS_COMPOSITE_HAS_COMPONENT = new String[][]
    {
        { ID, "INTEGER PRIMARY KEY AUTOINCREMENT" },
        { COMPOSITE_ID, "INTEGER", TBL_COMPOSITE, ID, },
        { CLASSNAME, "TEXT", TBL_COMPONENT, CLASSNAME },
        { POSITION, "INT" }
    };
	
	public static final String[][] COLS_SERVICEIO = new String[][]
	{
		{ ID, "INTEGER PRIMARY KEY AUTOINCREMENT" },
		{ NAME, "TEXT" },
		{ FRIENDLY_NAME, "TEXT" },
		{ IO_INDEX, "INTEGER" },
		{ IO_TYPE, "INTEGER" },
		{ DESCRIPTION, "TEXT" },
		{ PARENT_SERVICE, "TEXT", TBL_COMPONENT, CLASSNAME },
		{ MANDATORY, "TINYINT" },
		{ I_OR_O, "INTEGER" }
	};

    // Links between the output of one component in a composite and the input to another
    public static final String[][] COLS_COMPOSITE_IOCONNECTION = new String[][]
    {
        { ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
        { COMPOSITE_ID, "INTEGER", TBL_COMPOSITE, ID },
        { OUTPUT_CLASSNAME, "TEXT", TBL_COMPONENT, CLASSNAME },
        { OUTPUT_IO_ID, "INTEGER", TBL_SERVICEIO, ID },
        { INPUT_CLASSNAME, "TEXT", TBL_COMPONENT, CLASSNAME },
        { INPUT_IO_ID, "INTEGER", TBL_SERVICEIO, ID }
    };

	public static final String[][] COLS_IOTYPE = new String[][]
	{
		{ ID, "INTEGER PRIMARY KEY AUTOINCREMENT" },
		{ NAME, "TEXT" },
		{ CLASSNAME, "TEXT" }
	};
	
	// Constants for the samples table (and below).
	public static final String SERVICE_IO = "io_id";
	
	// MAke a table for sample values
	public static final String[][] COLS_IO_SAMPLES = new String[][]
	{
		{ ID, "INTEGER PRIMARY KEY AUTOINCREMENT" },
		{ SERVICE_IO, "INTEGER", TBL_SERVICEIO, ID },
		{ NAME, "TEXT" },
		{ VALUE, "TEXT" }
	};
	
	// Constants for the filter table
	public static final String FILTER_CONDITION = "filter_condition";
	public static final String MANUAL_VALUE = "manual_value";	
	public static final String FILTER_STATE = "filter_state";
	
	public static final String[][] COLS_FILTER = new String[][]
	{
		{ ID, "INTEGER PRIMARY KEY AUTOINCREMENT" },
		{ SERVICE_IO, "INTEGER", TBL_SERVICEIO, ID }, // Need the ServiceIO that it relates to, we can use a join to find the type
		{ CLASSNAME, "TEXT", TBL_COMPONENT, CLASSNAME }, // Keep track of the atomic as well just in case!
		{ COMPOSITE_ID, "INTEGER", TBL_COMPOSITE, ID }, // Need to keep track of which composite has these values
		{ FILTER_STATE, "INTEGER" },
		{ MANUAL_VALUE, "TEXT" }, // The value could be anything, better just set it as text so we can do some clever stuff at some point
		{ SAMPLE_VALUE, "INTEGER DEFAULT '-1'" }, // This needs to be a reference to the io value table
		{ FILTER_CONDITION, "INTEGER" }
	};
	
	public static final String[][] COLS_APP = new String[][]
	{
		{ PACKAGENAME, "TEXT" },
		{ NAME, "TEXT" }, 
		{ ICON, "TEXT" },
		{ DESCRIPTION, "TEXT" },
		{ DEVELOPER, "TEXT" },
		{ INSTALLED, "TINYINT" }
	};

	
	public static final String TIME = "time";
	public static final String MESSAGE = "message";
	public static final String LOG_TYPE = "log_type";
	
	public static final String[][] COLS_EXECUTION_LOG = new String[][]
	{
		{ ID, "INTEGER PRIMARY KEY AUTOINCREMENT" },
		{ COMPOSITE_ID, "INTEGER", TBL_COMPOSITE, ID },
		{ CLASSNAME, "TEXT", TBL_COMPONENT, CLASSNAME },
		{ TIME, "TEXT" },
		{ MESSAGE, "TEXT" },
		{ LOG_TYPE, "INTEGER" }
	};
	
	public static final String HAS_INPUTS = "has_inputs";
	public static final String HAS_OUTPUTS = "has_outputs";
	

	
	public static final String TAG_ID = "tag_id";
	
	public static final String[][] COLS_COMPONENT_HAS_TAG = new String[][]
	{
		{ ID, "INTEGER PRIMARY KEY AUTOINCREMENT" },
		{ CLASSNAME, "TEXT" },
		{ TAG_ID, "INTEGER" }
	};
	
	public static final String FIRST = "first";

    public static final String PREFS = "appGlue_prefs";
}
