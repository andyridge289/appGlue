package com.appglue.library;

import com.appglue.library.FilterFactory.FilterValue;

import static com.appglue.Constants.CLASSNAME;
import static com.appglue.Constants.DESCRIPTION;
import static com.appglue.Constants.DEVELOPER;
import static com.appglue.Constants.FEATURES;
import static com.appglue.Constants.FLAGS;
import static com.appglue.Constants.FRIENDLY_NAME;
import static com.appglue.Constants.ICON;
import static com.appglue.Constants.ID;
import static com.appglue.Constants.INSTALLED;
import static com.appglue.Constants.IO_INDEX;
import static com.appglue.Constants.IO_TYPE;
import static com.appglue.Constants.I_OR_O;
import static com.appglue.Constants.MANDATORY;
import static com.appglue.Constants.NAME;
import static com.appglue.Constants.SHORT_NAME;
import static com.appglue.Constants.PACKAGENAME;
import static com.appglue.Constants.POSITION;
import static com.appglue.Constants.SAMPLE_VALUE;
import static com.appglue.Constants.SERVICE_TYPE;
import static com.appglue.Constants.VALUE;
import static com.appglue.Constants.MIN_VERSION;

public class AppGlueConstants {

    public static final boolean AND = true;
    public static final boolean OR = false;

    public static final String CREATE_NEW = "create_new";
    public static final String EDIT_EXISTING = "edit_existing";
    public static final String MATCHING = "matching";
    public static final String TEST = "test";
    public static final String MODE = "mode";

    public static final String LOG_EXECUTION_INSTANCE = "log_id";

    // SharedPreferences
    public static final String PREFS_HIDDEN = "prefs_hidden";
    public static final String RUN_BEFORE = "run_before";
    public static final String P_DISCLAIMER = "p_disclaimer";

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
    public static final String TBL_CATEGORY = "category";
    public static final String TBL_SD_HAS_CATEGORY = "sd_has_category";

    // 'dynamic' database tables
    public static final String TBL_COMPOSITE = "composite";
    public static final String TBL_COMPONENT = "component";
    public static final String TBL_SERVICEIO = "serviceio";
    public static final String TBL_IOCONNECTION = "ioconnections";
    public static final String TBL_IOVALUE = "iovalue";
    public static final String TBL_IOFILTER = "iofilter";
    public static final String TBL_VALUENODE = "valuenode";
    public static final String TBL_SCHEDULE = "schedule";

    public static final String TBL_COMPOSITE_EXECUTION_LOG = "compositeexecutionlog";
    public static final String TBL_EXECUTION_LOG = "executionlog";

    public static final String JUST_A_LIST = "atomic_list";

    public static final String TRIGGERS_ONLY = "triggers_only";

    public static final int BASE_ALPHA = 2;
    public static final int FULL_ALPHA = 360;

    public static final FilterValue[] FILTER_STRING_VALUES = new FilterValue[]{
            FilterFactory.STR_EQUALS, FilterFactory.STR_NOTEQUALS, FilterFactory.STR_CONTAINS
    };

    public static final FilterValue[] FILTER_NUMBER_VALUES = new FilterValue[]{
            FilterFactory.INT_EQUALS, FilterFactory.INT_NOTEQUALS, FilterFactory.INT_LEQUALS,
            FilterFactory.INT_LT, FilterFactory.INT_GEQUALS, FilterFactory.INT_GT
    };

    public static final FilterValue[] FILTER_BOOL_VALUES = new FilterValue[]{
            FilterFactory.BOOL_EQUALS, FilterFactory.BOOL_NOTEQUALS
    };

    public static final FilterValue[] FILTER_SET_VALUES = new FilterValue[]{
            FilterFactory.SET_EQUALS, FilterFactory.SET_NOTEQUALS
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
                    { CLASSNAME, "TEXT PRIMARY KEY"},
                    { NAME, "TEXT"},
                    { SHORT_NAME, "TEXT"},
                    { PACKAGENAME, "TEXT"},
                    { DESCRIPTION, "TEXT"},
                    { SERVICE_TYPE, "INTEGER"},
                    { FLAGS, "INTEGER"},
                    { MIN_VERSION, "INTEGER"},
                    { FEATURES, "INTEGER" }
            };
    public static final String IX_SD = "index_servicedescription";
    public static final String[] INDEX_SD = new String[] {
            PACKAGENAME
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

    public static final String IX_IO_DESCRIPTION = "index_iodescription";
    public static final String[] INDEX_IO_DESCRIPTION = new String[]{
            IO_TYPE
    };

    public static final String[][] COLS_TAG = new String[][]
            {
                    {ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
                    {NAME, "TEXT"}
            };

    public static final String[][] COLS_IOTYPE = new String[][]{
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

    public static final String[][] COLS_CATEGORY = new String[][] {
            { ID, "INTEGER PRIMARY KEY AUTOINCREMENT" },
            { NAME, "TEXT" }
    };

    public static final String CATEGORY_ID = "category_id";

    public static final String[][] COLS_SD_HAS_CATEGORY = new String[][] {
            { ID, "INTEGER PRIMARY KEY AUTOINCREMENT" },
            { CLASSNAME, "TEXT" },
            { CATEGORY_ID, "INTEGER" }
    };

    // Database - Composite
    public static final String ENABLED = "enabled";

    // 'dynamic' database tables
    public static final String[][] COLS_COMPOSITE = new String[][]
            {
                    {ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
                    {NAME, "TEXT"},
                    {DESCRIPTION, "TEXT"},
                    {ENABLED, "TINYINT"},
            };

    public static final String COMPOSITE_ID = "composite_id";

    public static final String[][] COLS_COMPONENT = new String[][]
            {
                    {ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
                    {COMPOSITE_ID, "INTEGER", TBL_COMPOSITE, ID},
                    {CLASSNAME, "TEXT", TBL_SD, CLASSNAME},
                    {POSITION, "INT"}
            };

    public static final String[][] FK_COMPONENT = new String[][]{
            {COMPOSITE_ID, TBL_COMPOSITE, ID}
    };

    public static final String IX_COMPOSITE_HAS_COMPONENT = "index_composite_component";
    public static final String[] INDEX_COMPOSITE_HAS_COMPONENT = new String[]{
            COMPOSITE_ID, CLASSNAME
    };

    public static final String NUMERAL = "numeral";
    public static final String INTERVAL = "interval";
    public static final String LAST_EXECUTE = "last_executed";
    public static final String SCHEDULE_TYPE = "schedule_type";

    public static final String TIME_PERIOD = "time_period";
    public static final String DAY_OF_WEEK = "day_of_week";
    public static final String DAY_OF_MONTH = "day_of_month";
    public static final String MINUTE = "minute";
    public static final String HOUR = "hour";
    public static final String NEXT_EXECUTE = "next_execute";
    public static final String IS_SCHEDULED = "is_scheduled";
    public static final String EXECUTION_NUM = "execution_num";

    public static final String[][] COLS_SCHEDULE = new String[][]{
            {ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
            {COMPOSITE_ID, "INTEGER", TBL_COMPOSITE, ID},
            {ENABLED, "TINYINT"},
            {IS_SCHEDULED, "TINYINT"},
            {SCHEDULE_TYPE, "INTEGER"},
            {NUMERAL, "INTEGER"},
            {INTERVAL, "INTEGER"},
            {TIME_PERIOD, "INTEGER"},
            {DAY_OF_WEEK, "INTEGER"},
            {DAY_OF_MONTH, "INTEGER"},
            {HOUR, "INTEGER"},
            {MINUTE, "INTEGER"},
            {LAST_EXECUTE, "INTEGER"},
            {NEXT_EXECUTE, "INTEGER"},
            {EXECUTION_NUM, "INTEGER"}
    };

    public static final String COMPONENT_ID = "component_id";

    //    // Constants for the filter table
    public static final String FILTER_CONDITION = "filter_condition";
    public static final String MANUAL_VALUE = "manual_value";
    public static final String FILTER_STATE = "filter_state";

    public static final String[][] COLS_SERVICEIO = new String[][]{
            {ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
            {COMPONENT_ID, "INTEGER", TBL_COMPONENT, ID},
            {COMPOSITE_ID, "INTEGER", TBL_COMPOSITE, ID}, // Keep track of this to make life easier later
            {IO_DESCRIPTION_ID, "INTEGER", TBL_IO_DESCRIPTION, ID}
    };

    public static final String[][] FK_SERVICEIO = new String[][]{
            {COMPONENT_ID, TBL_COMPONENT, ID},
            {COMPOSITE_ID, TBL_COMPOSITE, ID}
    };

    public static final String IX_SERVICEIO = "index_filter";
    public static final String[] INDEX_SERVICEIO = new String[]{
            COMPONENT_ID, COMPOSITE_ID, IO_DESCRIPTION_ID
    };

    public static final String IO_ID = "serviceio_id";
    public static final String VALUE_NODE_ID = "value_node_id";

    public static final String[][] COLS_IOVALUE = new String[][]{
            {ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
            {IO_ID, "INTEGER", TBL_SERVICEIO, ID},
            {COMPOSITE_ID, "INTEGER", TBL_COMPOSITE, ID},
            {VALUE_NODE_ID, "INTEGER"},
            {FILTER_STATE, "INTEGER"},
            {FILTER_CONDITION, "INTEGER"},
            {ENABLED, "TINYINT"},
            {MANUAL_VALUE, "TEXT DEFAULT NULL"}, // The value could be anything, better just set it as text so we can do some clever stuff at some point
            {SAMPLE_VALUE, "INTEGER DEFAULT '-1'", TBL_IO_SAMPLE, ID},
    };

    public static final String[][] FK_IOVALUE = new String[][]{
            {IO_ID, TBL_SERVICEIO, ID},
            {COMPOSITE_ID, TBL_COMPOSITE, ID},
            {VALUE_NODE_ID, TBL_VALUENODE, ID}
    };

    public static final String IX_IOVALUE = "index_iovalue";
    public static final String[] INDEX_IOVALUE = new String[]{
            IO_ID, COMPOSITE_ID, SAMPLE_VALUE
    };

    public static final String[][] COLS_IOFILTER = new String[][]{
            {ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
            {COMPONENT_ID, "INTEGER"}
    };

    public static final String[][] FK_IOFILTER = new String[][]{
            {COMPONENT_ID, TBL_COMPONENT, ID}
    };

    public static final String IX_IOFILTER = "index_iofilter";
    public static final String[] INDEX_IOFILTER = new String[]{
            COMPONENT_ID
    };

    public static final String FILTER_ID = "filter_id";
    public static final String CONDITION = "condition";

    public static final String[][] COLS_VALUENODE = new String[][]{
            {ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
            {FILTER_ID, "INTEGER"},
            {CONDITION, "TINYINT"},
            {IO_ID, "INTEGER"}
    };

    public static final String[][] FK_VALUENODE = new String[][]{
            {IO_ID, TBL_SERVICEIO, ID},
            {FILTER_ID, TBL_IOFILTER, ID}
    };

    public static final String IX_VALUENODE = "index_valuenode";
    public static final String[] INDEX_VALUENODE = new String[]{
            IO_ID, FILTER_ID
    };

    public static final String SOURCE_IO = "source_io";
    public static final String SINK_IO = "sink_io";

    public static final String[][] COLS_IOCONNECTION = new String[][]{
            {ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
            {COMPOSITE_ID, "INTEGER", TBL_COMPOSITE, ID},
            {SOURCE_IO, "INTEGER", TBL_SERVICEIO, ID},
            {SINK_IO, "INTEGER", TBL_SERVICEIO, ID}
    };

    public static final String[][] FK_IOCONNECTION = new String[][]{
            {COMPOSITE_ID, TBL_COMPOSITE, ID},
            {SOURCE_IO, TBL_SERVICEIO, ID},
            {SINK_IO, TBL_SERVICEIO, ID}
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
                    {ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
                    {COMPOSITE_ID, "INTEGER"},
                    {START_TIME, "INTEGER"},
                    {END_TIME, "INTEGER"},
                    {LOG_TYPE, "INTEGER"},
                    {TERMINATED, "TINYINT DEFAULT 0"},
                    {MESSAGE, "TEXT"}
            };

    public static final String[][] FK_COMPOSITE_EXECUTION_LOG = new String[][]{
            {COMPOSITE_ID, TBL_COMPOSITE, ID}
    };

    public static final String TIME = "time";

    public static final String OUTPUT_DATA = "output_data";
    public static final String INPUT_DATA = "input_data";
    public static final String EXECUTION_INSTANCE = "execution_instance";

    public static final String[][] COLS_EXECUTION_LOG = new String[][]
            {
                    {ID, "INTEGER PRIMARY KEY AUTOINCREMENT"},
                    {COMPOSITE_ID, "INTEGER", TBL_COMPOSITE, ID},
                    {EXECUTION_INSTANCE, "INTEGER", TBL_COMPOSITE_EXECUTION_LOG, ID},
                    {COMPONENT_ID, "INTEGER", TBL_COMPONENT, ID},
                    {MESSAGE, "TEXT"},
                    {INPUT_DATA, "BLOB"},
                    {OUTPUT_DATA, "BLOB"},
                    {LOG_TYPE, "INTEGER"},
                    {FLAGS, "INTEGER"},
                    {TIME, "INTEGER"}
            };

    public static final String[][] FK_EXECUTION_LOG = new String[][]{
            {COMPOSITE_ID, TBL_COMPOSITE, ID},
            {EXECUTION_INSTANCE, TBL_COMPOSITE_EXECUTION_LOG, ID},
            {COMPONENT_ID, TBL_COMPONENT, ID}
    };

    public static final String IX_EXECUTION_LOG = "index_execution_log";
    public static final String[] INDEX_EXECUTION_LOG = new String[]{
            COMPOSITE_ID, COMPONENT_ID, EXECUTION_INSTANCE
    };

    public static final String HAS_INPUTS = "has_inputs";
    public static final String HAS_OUTPUTS = "has_outputs";

    public static final String FIRST = "first";
}