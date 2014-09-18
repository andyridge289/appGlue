package com.appglue;

public class Constants 
{
    public static final boolean LOG = false;

    // Broadcast receive-y thing
	public static final String ACTION_COMPOSABLE = "com.appglue.IM_A_COMPOSABLE_SERVICE";
	public static final String ACTION_TRIGGER = "com.appglue.TRIGGER_A_SERVICE_PLEASE";
	
	public static final String TAG = "appGlue";
	
	public static final String SERVICE_LIST = "service_list";
	public static final String SERVICE_RETURN = "service_return";
	public static final String SERVICE_CONVERTER = "service_converter";
	
	public static final String KEY_COMPOSITE = "key_composite";
	public static final String KEY_SERVICE_LIST = "key_service_list";
	
	public static final int FULL_ALPHA = 360;
	
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
        NONE(0, 0, "None"),
		SECONDS(1, 1, "Second"),
		MINUTES(2, 60, "Minute"),
		HOURS(3, 3600, "Hour"),
		DAYS(4, 86400, "Day");
		
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

	public static final String DURATION = "duration";
	public static final String RESULT = "result";
	public static final String RUN_NOW = "run_now";

	public static final String INDEX = "index";
	public static final String IS_LIST = "is_list";
	public static final String DATA = "data";
	
	public static final String INPUTS = "getInputs";
	public static final String OUTPUTS = "getOutputs";
	
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

    // These sort out the service descriptions too
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";

    // Database - Atomic
	public static final String CLASSNAME = "classname";
	public static final String PACKAGENAME = "packagename";
	public static final String DEVELOPER = "developer";

	public static final String SERVICE_TYPE = "service_type";
	public static final String PROCESS_TYPE = "process_type";
	
	public static final String IO_TYPE = "io_type";
	public static final String FRIENDLY_NAME = "friendly_name";
	public static final String IO_INDEX = "io_index";
	public static final String I_OR_O = "i_or_o";
	
	// Database - app
	public static final String ICON = "icon";
	public static final String INSTALLED = "installed";
	
	// Database - Composite_has_atomic
	public static final String POSITION = "position";


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
