package sf.sf;

import java.util.Arrays;
import java.util.List;

/**
 * The parameter and config keys for the app.
 * 
 * @author ari
 *
 */
public class Constants {
	

	
	public static final String COMMANDS = "commands";
	//comma delimeted list of commands in the properties file
	public static final String PROP_COMMANDS = "prop_commands";

	public static final String CLI_PROPERTIES_FILE = "props_file";
	public static final String BUCKET_CONFIG_KEY = "bucket";
	public static final String OBJECT_PATTERN_CONFIG_KEY = "pattern";
	public static final String PREFIX_CONFIG_KEY = "prefix";
	public static final String LAST_MODIFIED_CONFIG_KEY = "last_modified";
	public static final String OBJECT_SIZE_CONFIG_KEY = "object_size";
	public static final String DELIMITER = "delimiter";
	public static final String MAX_KEYS_CONFIG_KEY = "max_keys";
	public static final String MAX_REQUESTS_LIST_OBJECTS= "max_requests_list_objects";
	public static final String MAX_REQUESTS_GET_OBJECTS= "max_requests_get_objects";
	public static final String OBJECT_KEY_CONFIG_KEY = "object_key";
	public static final String OBJECT_DB_STRING_CONFIG_KEY = "object_db_string_key";
	
	public static final String INITIAL_OBJECTS_NAME = "initial_objects_name";
	public static final String OBJECTS_TO_DL_NAME = "objects_to_dl_name";
	public static final String DLED_OBJECTS_NAME = "dled_objects_name";

	public static final String OBJECTS_DB_NAME = "objects_db";
	public static final String ACCESS_KEY_CONFIG_KEY = "access_key";
	public static final String SECRET_KEY_CONFIG_KEY = "secret_key";
	public static final String ENDPOINT_CONFIG_KEY = "endpoint";
	public static final String PORT_CONFIG_KEY = "port";
	public static final String READ_DB_START = "read_db_start"; 
	//used to tell where to start next listobjectsv1 request. aws api param.
	public static final String MARKER_CONFIG_KEY = "next_marker"; 
	public static final String CONTINUATION_TOKEN_CONFIG_KEY = "continuation_token";
	public static final String START_AFTER_CONFIG_KEY = "start_after";
	public static final String NUM_VERTICLES_PER_SLEEP = "num_verticles"; //number of verticles to call before each thread.sleep
	public static final String SLEEP_TIME = "sleep_time"; //milliseconds  - if no value then dont sleep ever
	public static final String USE_SSL="ssl";
	public static final String USE_AUTH="auth";
	
	
	/**
	 * 
	 * End params.
	 * 
	 */
	
	
	/**
	 * Commands.
	 */
	public static final String LIST_BUCKETS_COMMAND = "list_buckets";
	public static final String LIST_OBJECTS_V1_COMMAND = "list_objects_v1";
	public static final String LIST_OBJECTS_V2_COMMAND = "list_objects_v2";
	public static final String GET_SINGLE_OBJECT_COMMAND = "get_single_object";
	public static final String GET_OBJECTS_COMMAND = "get_objects";
	public static final List<String> REQUESTER_SAVER_COMMANDS= 
				Arrays.asList(LIST_OBJECTS_V1_COMMAND, LIST_OBJECTS_V2_COMMAND, GET_SINGLE_OBJECT_COMMAND,
						GET_OBJECTS_COMMAND, LIST_BUCKETS_COMMAND);
	
	public static final String READ_ALL_SETS = "read_all_sets";
	public static final String READ_INITIAL_OBJECTS = "read_initial_objects";
	public static final String READ_OBJECTS_TO_DL = "read_objects_to_dl";
	public static final String READ_DLED_OBJECTS = "read_dled_objects";

	
	public static final String CLEAR_ALL_SETS = "clear_all_sets";
	public static final String CLEAR_INITIAL_OBJECTS = "clear_initial_objects";
	public static final String CLEAR_OBJECTS_TO_DL = "clear_objects_to_dl";
	public static final String CLEAR_DLED_OBJECTS = "clear_dled_objects";

	
	public static final String GET_ALL_NAMES = "get_all_names";
	public static final String DELETE_FIX_DB = "delete_fix_db";
	public static final List<String> READER_COMMANDS = Arrays.asList(
			READ_ALL_SETS, READ_INITIAL_OBJECTS, READ_OBJECTS_TO_DL, READ_DLED_OBJECTS,
			CLEAR_ALL_SETS,CLEAR_INITIAL_OBJECTS, CLEAR_OBJECTS_TO_DL, CLEAR_DLED_OBJECTS,
			DELETE_FIX_DB, GET_ALL_NAMES);

	/**
	 * End commands.
	 */
	
	
	//only used for saving/retrieving objects list json from the db
	public static final String PATH_CONFIG_KEY = "path";
	
	// event bus addresses
	public static final String LIST_OBJECTS_V1_BUS = "listObjectsV1";
	public static final String LIST_OBJECTS_V2_BUS = "listObjectsV2";
	
	
}
