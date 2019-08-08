package sf.sf;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import sf.sf.command.S3Command;

/** 
 * Used by jCommander to create the parameters and commands for the app.
 * 
 * @author ari
 *
 */
@Parameters(separators = "=")
public class CliOptions {
	

	public List<String> getAllCommands(){
		// Commands can come from:
		// 1) the first parameters passed into the command line. they have no key, for example:
		// myjar command1 command2 -someKey=someVal  . The commands are "command1 command2".
		// These get put into "arguments".
		// 2) The command key/value passed into the command line. "-commands=val". This gets
		// put into "commands".
		// 3) The "propcommands" key/val in the properties file. This gets put into the "propCommands" var.
		// The commands from the commandline should overwrite all the commands in the properties file
		
		List<String> a = new ArrayList<String>();
		if((arguments == null || arguments.isEmpty()) && 
				(commands == null || commands.isEmpty())){
			a.addAll(this.getPropCommands());
		}else{
			a.addAll(this.getCommands());
			a.addAll(arguments);
		}
		return a;
		
	}
	
	//Command line commands
	@Parameter(description = "Commands to run. ")
	private List<String> arguments = new ArrayList<>();
	public List<String> getArguments() {
		return arguments;
	}
	
	// Properties file commands
	@Parameter(names="-"+Constants.PROP_COMMANDS)
	private String propCommands;	
	public List<String> getPropCommands(){
		if(this.propCommands != null){
			return Arrays.asList(propCommands.split(",")).stream()
				.map((String x) -> x.trim()).collect(Collectors.toList());
		} else {
			return new ArrayList<String>();
		}
	}

	// Command line key/value param or properties file key/value default value
	@Parameter(names="-"+Constants.COMMANDS)
	private  String commands;	
	public List<String> getCommands(){
		if(this.commands != null){
			return Arrays.asList(commands.split(",")).stream()
				.map((String x) -> x.trim()).collect(Collectors.toList());
		} else {
			return new ArrayList<String>();
		}
	}
	

	


	@Parameter(names="-"+Constants.MAX_REQUESTS_LIST_OBJECTS)
	private Integer maxRequestsListObjects;
	public Integer getMaxRequestsListObjects(){
		return this.maxRequestsListObjects;
	}

	@Parameter(names="-"+Constants.MAX_REQUESTS_GET_OBJECTS)
	private Integer maxRequestsGetObjects;
	public Integer getMaxRequestsGetObjects(){
		return this.maxRequestsGetObjects;
	}
	
	
	@Parameter(names="-"+Constants.NUM_VERTICLES_PER_SLEEP)
	private Integer numVerticlesPerSleep;
	public Integer getNumVerticlesPerSleep() {
		return numVerticlesPerSleep;
	}
	
	
	@Parameter(names="-"+Constants.SLEEP_TIME)
	private Integer sleepTime;
	public Integer getSleepTime() {
		return sleepTime;
	}
	
	@Parameter(names="-"+Constants.READ_DB_START)
	private String readDbStart;
	public String getReadDbStart() {
		return readDbStart;
	}
	
	@Parameter(names="-"+Constants.USE_SSL)
	private Boolean useSsl;
	public Boolean getUseSsl() {
		return useSsl;
	}
	
	@Parameter(names="-"+Constants.USE_AUTH)
	private Boolean useAuth;
	public Boolean getUseAuth() {
		return useAuth;
	}
	
	/* isn't being used yet
	@Parameter(names="-"+Constants.CONTINUATION_TOKEN_CONFIG_KEY)
	private String continuationToken;
	public String getContinuationToken() {
		return continuationToken;
	}*/
	
	@Parameter(names="-"+Constants.START_AFTER_CONFIG_KEY)
	private String startAfter;
	public String getStartAfter() {
		return startAfter;
	}
	
	
	@Parameter(names="-"+Constants.ACCESS_KEY_CONFIG_KEY)
	private String accessKey;
	public String getAccessKey() {
		return accessKey;
	}

	@Parameter(names="-"+Constants.SECRET_KEY_CONFIG_KEY)
	private String secretKey;
	public String getSecretKey() {
		return secretKey;
	}

	@Parameter(names="-"+Constants.ENDPOINT_CONFIG_KEY)
	private String endpoint;
	public String getEndpoint() {
		return endpoint;
	}
	
	@Parameter(names="-"+Constants.PORT_CONFIG_KEY)
	private Integer port;
	public Integer getPort() {
		return port;
	}



	//REQUIRED for list_objects, not get_objects 
	@Parameter(names="-"+Constants.BUCKET_CONFIG_KEY)
	private String bucket;
	public String getBucket() {
		return bucket;
	}


	// Used in both list_objects and get_object
	@Parameter(names="-"+Constants.OBJECT_PATTERN_CONFIG_KEY)
	private String pattern;
	public String getPattern() {
		return pattern;
	}

	
	
	@Parameter(names="-"+Constants.DELIMITER)
	private String delimiter;
	public String getDelimiter(){
		return delimiter;
	}


	@Parameter(names="-"+Constants.PREFIX_CONFIG_KEY)
	private String prefix;
	public String getPrefix() {
		return prefix;
	}

	
	@Parameter(names="-"+Constants.MAX_KEYS_CONFIG_KEY)
	private Integer maxKeys;
	public Integer getMaxKeys() {
		return maxKeys;
	}
	
	//For get_single_object
	@Parameter(names="-"+Constants.OBJECT_KEY_CONFIG_KEY)
	private String objectKey;
	public String getObjectKey() {
		return objectKey;
	}


	@Parameter(names="-"+Constants.INITIAL_OBJECTS_NAME)
	private String initialObjectsName = "initial_objects"; // Default
	public String getInitialObjectsName() {
		return initialObjectsName;
	}

	@Parameter(names="-"+Constants.OBJECTS_TO_DL_NAME)
	private String objectsToDlName = "objects_to_dl"; // Default
	public String getObjectsToDlName() {
		return objectsToDlName;
	}

	@Parameter(names="-"+Constants.DLED_OBJECTS_NAME)
	private String dledObjectsName = "dled_objects"; // Default
	public String getDledObjectsName() {
		return dledObjectsName;
	}

	
	
	@Parameter(names="-"+Constants.OBJECTS_DB_NAME)
	private String objectsDbName = "objectsDb.db"; // Default
	public String getObjectsDbName() {
		return objectsDbName;
	}
	
	
	@Parameter(names="-"+Constants.CLI_PROPERTIES_FILE)
	private String propsFile = "./jc.properties"; // Default
	public String getPropsFile() {
		return propsFile;
	}
	
	public void setPropsFile(String propsFile){
		this.propsFile = propsFile;
	}
	
	public boolean numRequestsIsLessThanMaxRequestsGetObjects(){
		 if(this.getMaxRequestsGetObjects() == null ||
				   S3Command.numRequests <= this.getMaxRequestsGetObjects()){
			 return true;
		 }else{
			 return false;
		 }
	}
	
	public boolean numRequestsIsLessThanMaxRequestsListObjects(){
		 if(this.getMaxRequestsListObjects() == null ||
				   S3Command.numRequests <= this.getMaxRequestsListObjects()){
			 return true;
		 }else{
			 return false;
		 }
	}
	
}
