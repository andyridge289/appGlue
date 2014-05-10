package com.appglue;

public class Constants 
{
	public static final boolean LOG = false;
	
	// Broadcast receive-y thing
	public static final String ACTION_COMPOSABLE = "com.appglue.IM_A_COMPOSABLE_SERVICE";
	public static final String ACTION_TRIGGER = "com.appglue.TRIGGER_A_SERVICE_PLEASE";
	
	public static final String TAG = "CompositionTool";
	
	public static final String SERVICE_LIST = "service_list";
	public static final String SERVICE_RETURN = "service_return";
	public static final String SERVICE_CONVERTER = "service_converter";
	
	public static final String KEY_COMPOSITE = "key_composite";
	public static final String KEY_SERVICE_LIST = "key_service_list";
	
//	public static final int STORY_MODE = 109;
	
	public static final int FULL_ALPHA = 360;
	
	public static enum Param
	{
		NUMBER(0, "Number"),
		STRING(1, "String"),
		ONE_SET(2, "One set"),
		MANY_SET(3, "Many set");
		
		public int index;
		public String name;
		
		Param(int index, String name)
		{
			this.index = index;
			this.name = name;
		}
	}
	
	public static enum Requiredness
	{
		MANDATORY(0, "Mandatory"),
		OPTIONAL(1, "Optional");
		
		public int index;
		public String name;
		
		Requiredness(int index, String name)
		{
			this.index = index;
			this.name = name;
		}
	}
	
	public static enum ServiceType
	{
		IN_APP(0, "In app"),
		LOCAL(1, "Local"),
		REMOTE(2, "Remote"),
		ANY(3, "Any"),
		DEVICE(4, "Device");
		
		public int index;
		public String name;
		
		ServiceType(int index, String name)
		{
			this.index = index;
			this.name = name;
		}
	}
	
	public static enum ProcessType
	{
		NORMAL(0, "Normal"),
		CONVERTER(1, "Converter"),
		FILTER(2, "Filter"),
		REST(3, "Rest"),
		TRIGGER(4, "Trigger");
		
		public int index;
		public String name;
		
		ProcessType(int index, String name)
		{
			this.index = index;
			this.name = name;
		}
	}
	
	public static enum Interval
	{
		SECONDS(0, 1, "Second"),
		MINUTES(1, 60, "Minute"),
		HOURS(2, 3600, "Hour"),
		DAYS(3, 86400, "Day");
		
		public int index;
		public int value;
		public String name;
		
		Interval(int index, int value, String name)
		{
			this.index = index;
			this.value = value;
			this.name = name;
		}
	}
	
	public static enum Composition
	{
		// Problems
		EMPTY(0 , "Empty", "The composition is empty", Level.ERROR), // The composition is empty
		NO_MATCH(1, "Input/Output mis-match", "The output of one service doesn't match the input of the next", Level.ERROR), // The output of a service doesn't match the input of the next
		
		// Warnings
		FIRST_INPUT(2, "First service has an input", "The first service in the composition has an input", Level.WARNING),	// The first service has an input
		LAST_OUTPUT(3, "Last service has an output", "The last service in the composition has an output", Level.WARNING),		// The last service has an output 
		NO_INPUT_NOT_FIRST(4, "Middle service doesn't have input", "A service in middle of the composition doesn't have an input", Level.WARNING),
		NO_OUTPUT_NOT_LAST(5, "Middle service doesn't have output", "A service in the middle of the composition doesn't have an output", Level.WARNING);
		
		public int index;
		public String name;
		public String description;
		public Level level;
		
		Composition(int index, String name, String description, Level level)
		{
			this.index = index;
			this.name = name;
			this.description = description;
			this.level = level;
		}
	}
	
	public static enum Level
	{
		ERROR(0, "Error"),
		WARNING(1, "Name"),
		OKAY(2, "Okay");
		
		public int index;
		public String name;
		
		Level(int index, String name)
		{
			this.index = index;
			this.name = name;
		}
	}
	
	public static final String CLASSES = "classes";
	
	// The list of services that have parameters
	public static final String PARAM_SERVICES = "param_services";
	
	// The list of parameters that a service has
	public static final String SERVICE_PARAMS = "service_params";
	
	public static final String TEST = "test";
	public static final String DURATION = "duration";
	public static final String DEFAULT_NAME = "Test Service";
	public static final String RESULT = "result";
	public static final String LAST_CLASSNAME = "last_classname";
	public static final String RUN_NOW = "run_now";
    public static final String ACTIVE_OR_TIMER = "active_or_timer";
    public static final String RUNNING_NOW = "running_now";
	
	public static final String INDEX = "index";
	public static final String IS_LIST = "is_list";
	public static final String DATA = "data";
	
	public static final String INPUTS = "inputs";
	public static final String OUTPUTS = "outputs";
	
	public static final String INPUT_NAME = "input_name";
	public static final String INPUT_TYPE = "input_type";
	public static final String INPUT_DESCRIPTION = "input_description";
	
	public static final String OUTPUT_NAME = "output_name";
	public static final String OUTPUT_TYPE = "output_type";
	public static final String OUTPUT_DESCRIPTION = "output_description";
	
	public static final String MANDATORY = "mandatory";
	public static final String SAMPLES = "samples";
	public static final String SAMPLE_NAME = "sample_name";
	public static final String SAMPLE_VALUE = "sample_value";
	
	public static final String PRIOR = "prior";
	public static final String CURRENT = "current";
	public static final String NEXT = "next";
	
	// The escape sequence
	public static final String DELIMITER = "!!@!!@!!@!!@";
	
	// Database information
	public static final int DB_VERSION = 6;
	public static final String DB_NAME = "ServiceRegistry";
	
	// Database - Composite
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	
	// Database - Atomic
	public static final String CLASSNAME = "classname";
	public static final String PACKAGENAME = "packagename";
	public static final String DEVELOPER = "developer";
	
	public static final String AVG_RATING = "average_rating";
	public static final String NUM_RATINGS = "number_of_ratings";
	public static final String PRICE = "price";
	public static final String SERVICE_TYPE = "service_type";
	public static final String PROCESS_TYPE = "process_type";
	
	public static final String IO_TYPE = "io_type";
	public static final String PARENT_SERVICE = "parent_service";
	public static final String FRIENDLY_NAME = "friendly_name";
	public static final String IO_INDEX = "io_index";
	public static final String I_OR_O = "i_or_o";
	
	// Database - app
	public static final String ICON = "icon";
	public static final String INSTALLED = "installed";
	
	// Database - Composite_has_atomic
	public static final String COMPOSITE_ID = "composite_id";
	public static final String POSITION = "position";
	
	// Database - Parameter
	public static final String PARAM_TYPE = "param_type";
	public static final String PARAM_REQUIREDNESS = "param_priority";
	public static final String POSS_USER = "poss_user";
	public static final String POSS_SYSTEM = "poss_system";
	
	// Database - atomic_has_parameter
	public static final String PARAMETER_ID = "param_id";
	
	public static final String OUTPUT_CLASSNAME = "output_classname";
	public static final String INPUT_CLASSNAME = "input_classname";
	public static final String OUTPUT_IO_ID = "output_io_id";
	public static final String INPUT_IO_ID = "input_io_id";
	
	// Database - Composite_has_parameter
	public static final String USE_DEFAULT = "use_default";
	public static final String VALUE = "value";
	public static final String IS_SET = "is_set"; // This is implicit if there's something in here but we still need the constant
	
	public static final String JSON_APP = "app";
	public static final String JSON_SERVICE_LIST = "services";
	public static final String JSON_SERVICE = "service";
	public static final String JSON_SERVICE_DATA = "service_data";
	
	public static final String JSON_SUCCESS = "success";
	public static final String JSON_ERROR = "err";
	public static final String JSON_ERRNO = "errno";
	public static final String JSON_ERRMSG = "errmsg";
	
	public static final String TAGS = "tags";
	
	public static final int ERR_CLASSNAME = 1;
	public static final int ERR_NOSERVICE = 2;
	
	public static final String DIR_ROOT = "/Composer";
	public static final String DIR_ICON = DIR_ROOT + "/Icons/";
}
