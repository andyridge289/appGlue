package com.appglue.library;

import com.appglue.library.IOFilter.FilterValue;

import static com.appglue.Constants.CLASSNAME;
import static com.appglue.Constants.DESCRIPTION;
import static com.appglue.Constants.DEVELOPER;
import static com.appglue.Constants.FRIENDLY_NAME;
import static com.appglue.Constants.ICON;
import static com.appglue.Constants.ID;
import static com.appglue.Constants.INSTALLED;
import static com.appglue.Constants.IO_INDEX;
import static com.appglue.Constants.IO_TYPE;
import static com.appglue.Constants.I_OR_O;
import static com.appglue.Constants.MANDATORY;
import static com.appglue.Constants.NAME;
import static com.appglue.Constants.PACKAGENAME;
import static com.appglue.Constants.POSITION;
import static com.appglue.Constants.PROCESS_TYPE;
import static com.appglue.Constants.SAMPLE_VALUE;
import static com.appglue.Constants.SERVICE_TYPE;
import static com.appglue.Constants.VALUE;

public class AppGlueConstants {
    public static final String CREATE_NEW = "create_new";
    public static final String MATCHING = "matching";
    public static final String TEST = "test";
    public static final String MODE = "mode";

    // Database information
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "appGlue";



    // 'static' database tables
    public static final String TBL_APP = "app";
    public static final String TBL_SD = "servicedescription";
    public static final String TBL_IO_DESCRIPTION = "iodescription";
    public static final String TBL_IOTYPE = "inputoutputtype";
    public static final String TBL_TAG = "tag";
    public static final String TBL_SD_HAS_TAG = "sdhastag";
    public static final String TBL_IO_SAMPLE = "iosamples";

    // 'dynamic' database tables
    public static final String TBL_COMPOSITE = "composite";
    public static final String TBL_COMPONENT = "component";
    public static final String TBL_SERVICEIO = "serviceio";
    public static final String TBL_IOCONNECTION = "ioconnections";

    public static final String TBL_COMPOSITE_EXECUTION_LOG = "compositeexecutionlog";
    public static final String TBL_EXECUTION_LOG = "executionlog";

    public static final String JUST_A_LIST = "atomic_list";

    public static final String TRIGGERS_ONLY = "triggers_only";
    public static final String NO_TRIGGERS = "no_triggers";

    // Values for calling activities and such
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

    public static String[] FILTER_BOOL = new String[]{"true", "false"};

    // 'static' database tables
    public static final String[][] COLS_APP = new String[][]
            {
                    {PACKAGENAME, "TEXT"},
                    {NAME, "TEXT"},
                    {ICON, "TEXT"},
                    {DESCRIPTION, "TEXT"},
                    {DEVELOPER, "TEXT"},
                    {INSTALLED, "TINYINT"}
            };

    public static final String[][] COLS_SD = new String[][]
            {
                    {CLASSNAME, "TEXT PRIMARY KEY"},
                    {NAME, "TEXT"},
                    {PACKAGENAME, "TEXT"},
                    {DESCRIPTION, "TEXT"},
                    {SERVICE_TYPE, "INTEGER"},
                    {PROCESS_TYPE, "INTEGER"}
            };

    public static final String[][] COLS_IO_DESCRIPTION = new String[][]
            {
                    {ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
                    {NAME, "TEXT"},
                    {FRIENDLY_NAME, "TEXT"},
                    {IO_INDEX, "INTEGER"},
                    {IO_TYPE, "INTEGER"},
                    {DESCRIPTION, "TEXT"},
                    {CLASSNAME, "TEXT", TBL_SD, CLASSNAME},
                    {MANDATORY, "TINYINT"},
                    {I_OR_O, "TINYINT"}
            };

    public static final String IX_IO_DESCRIPTION = "index_service_io";
    public static final String[] INDEX_IO_DESCRIPTION = new String[]{
            IO_TYPE
    };

    public static final String[][] COLS_TAG = new String[][]
            {
                    {ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
                    {NAME, "TEXT"}
            };

    public static final String[][] COLS_IOTYPE = new String[][]
            {
                    {ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
                    {NAME, "TEXT"},
                    {CLASSNAME, "TEXT"}
            };

    // Constants for the samples table (and below).
    public static final String IO_DESCRIPTION_ID = "io_description_id";

    // MAke a table for sample values
    public static final String[][] COLS_IO_SAMPLES = new String[][]
    {
        {ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
        {IO_DESCRIPTION_ID, "INTEGER"},
        {NAME, "TEXT"},
        {VALUE, "TEXT"}
    };

    public static final String IX_IO_SAMPLES = "index_io_samples";
    public static final String[] INDEX_IO_SAMPLES = new String[]{
            IO_DESCRIPTION_ID
    };

    public static final String TAG_ID = "tag_id";

    public static final String[][] COLS_COMPONENT_HAS_TAG = new String[][]
            {
                    {ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
                    {CLASSNAME, "TEXT"},
                    {TAG_ID, "INTEGER"}
            };

    public static final String IX_COMPONENT_HAS_TAG = "index_component_has_tag";
    public static final String[] INDEX_COMPONENT_HAS_TAG = new String[]{
            CLASSNAME, TAG_ID
    };

    // Database - Composite
    public static final String ENABLED = "enabled";
    public static final String SCHEDULED = "scheduled";
    public static final String NUMERAL = "numeral";
    public static final String INTERVAL = "interval";
    public static final String HOURS = "hours";
    public static final String MINUTES = "minutes";

    // 'dynamic' database tables
    public static final String[][] COLS_COMPOSITE = new String[][]
    {
        { ID, "INTEGER PRIMARY KEY AUTOINCREMENT" },
        { NAME, "TEXT" },
        { DESCRIPTION, "TEXT" },
        { SCHEDULED, "TINYINT" },
        { ENABLED, "TINYINT" },
        { NUMERAL, "INTEGER" },
        { INTERVAL, "INTEGER" },
        { HOURS, "INTEGER" },
        { MINUTES, "INTEGER" }
    };

    public static final String COMPOSITE_ID = "composite_id";

    public static final String[][] COLS_COMPONENT = new String[][]
            {
                    {ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
                    {COMPOSITE_ID, "INTEGER", TBL_COMPOSITE, ID,},
                    {CLASSNAME, "TEXT", TBL_SD, CLASSNAME},
                    {POSITION, "INT"}
            };

    public static final String IX_COMPOSITE_HAS_COMPONENT = "index_composite_component";
    public static final String[] INDEX_COMPOSITE_HAS_COMPONENT = new String[]{
            COMPOSITE_ID, CLASSNAME
    };

    public static final String OUTPUT_ID = "output_id";
    public static final String INPUT_ID = "input_id";

    public static final String COMPONENT_ID = "component_id";

    //    // Constants for the filter table
    public static final String FILTER_CONDITION = "filter_condition";
    public static final String MANUAL_VALUE = "manual_value";
    public static final String FILTER_STATE = "filter_state";

    public static final String[][] COLS_SERVICEIO = new String[][] {
        { ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
        { COMPONENT_ID, "INTEGER", TBL_COMPONENT, ID},
        { COMPOSITE_ID, "INTEGER", TBL_COMPOSITE, ID}, // Keep track of this to make life easier later
        { IO_DESCRIPTION_ID, "INTEGER", TBL_IO_DESCRIPTION, ID},
        { FILTER_STATE, "INTEGER" },
        { FILTER_CONDITION, "INTEGER"},
        { MANUAL_VALUE, "TEXT DEFAULT NULL"}, // The value could be anything, better just set it as text so we can do some clever stuff at some point
        { SAMPLE_VALUE, "INTEGER DEFAULT '-1'", TBL_IO_SAMPLE, ID},
    };

    public static final String IX_SERVICEIO = "index_filter";
    public static final String[] INDEX_FILTER = new String[]{
        COMPONENT_ID, COMPOSITE_ID, IO_DESCRIPTION_ID, SAMPLE_VALUE
    };

    public static final String SOURCE_IO = "source_io";
    public static final String SINK_IO = "sink_io";

    public static final String[][] COLS_IOCONNECTION = new String[][] {
        { ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
        { COMPOSITE_ID, "INTEGER", TBL_COMPOSITE, ID},
        { SOURCE_IO, "INTEGER", TBL_SERVICEIO, ID },
        { SINK_IO, "INTEGER", TBL_SERVICEIO, ID }
    };

    public static final String IX_IOCONNECTION = "index_composite_ioconnection";
    public static final String[] INDEX_IOCONNECTION = new String[]{
        SOURCE_IO, SINK_IO
    };

    public static final String START_TIME = "start_time";
    public static final String END_TIME = "end_time";
    public static final String LOG_TYPE = "log_type";
    public static final String MESSAGE = "message";
    public static final String TERMINATED = "terminate";

    public static final String[][] COLS_COMPOSITE_EXECUTION_LOG = new String[][]
    {
        { ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
        { COMPOSITE_ID, "INTEGER"},
        { START_TIME, "INTEGER"},
        { END_TIME, "INTEGER"},
        { LOG_TYPE, "INTEGER"},
        { TERMINATED, "TINYINT DEFAULT 0" },
        { MESSAGE, "TEXT"}
    };

    public static final String TIME = "time";

    public static final String OUTPUT_DATA = "output_data";
    public static final String INPUT_DATA = "input_data";
    public static final String EXECUTION_INSTANCE = "execution_instance";

    public static final String[][] COLS_EXECUTION_LOG = new String[][]
    {
        { ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
        { COMPOSITE_ID, "INTEGER", TBL_COMPOSITE, ID},
        { EXECUTION_INSTANCE, "INTEGER", TBL_COMPOSITE_EXECUTION_LOG, ID},
        { COMPONENT_ID, "INTEGER", TBL_COMPONENT, ID },
        { MESSAGE, "TEXT"},
        { INPUT_DATA, "BLOB"},
        { OUTPUT_DATA, "BLOB"},
        { LOG_TYPE, "INTEGER"},
        { TIME, "INTEGER"}
    };

    public static final String IX_EXECUTION_LOG = "index_execution_log";
    public static final String[] INDEX_EXECUTION_LOG = new String[]{
            COMPOSITE_ID, COMPONENT_ID, EXECUTION_INSTANCE
    };

    public static final String HAS_INPUTS = "has_inputs";
    public static final String HAS_OUTPUTS = "has_outputs";

    public static final String FIRST = "first";
    public static final String PREFS = "appGlue_prefs";
}