package sf.sf.storage;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import sf.sf.Constants;

/**
 * Serialization logic for an S3 prefix.
 * 
 * @author ari
 *
 */
public class DbPrefix implements DbElement{
	private static final Logger logger = LogManager.getLogger(DbPrefix.class);
	String bucketKey = Constants.BUCKET_CONFIG_KEY;
	String prefixKey= Constants.PREFIX_CONFIG_KEY;
	String pathKey = Constants.PATH_CONFIG_KEY ;

	String prefix = null;
	String bucket = null;
	String path = null;
	String fullJson = null;
	
	/**
	 * Create DbPrefix from json string.
	 * 
	 */
	public DbPrefix(String json){
		this.fullJson = json;

		ObjectMapper mapper = new ObjectMapper();
		TypeReference<Map<String, String>> typeRef =
				new TypeReference<Map<String, String>>() {};
		try {
			HashMap<String, String> objectMap = mapper.readValue(json, typeRef);
			this.bucket = objectMap.get(this.bucketKey);
			this.prefix = objectMap.get(this.prefixKey);
			this.path = objectMap.get(this.pathKey);
			
		} catch (Exception e) {

			logger.error("error forming dbprefix hashmap from json",e);
		} 

	}
	
	public DbPrefix(String prefix, String bucket){
		this.prefix = prefix;
		this.bucket = bucket;
		this.path  = this.bucket+"/"+this.prefix;			
		this.fullJson = this.toFullJson();
	}
	/**
	 * Create json representation of DbPrefix.
	 * 
	 * @return string json.
	 */
	private String toFullJson(){
		String json = "Error , see stack trace";
		ObjectMapper mapper = new ObjectMapper(); 
		Map<String,String> objectData = new HashMap<>();
		objectData.put(this.bucketKey, this.bucket);
		objectData.put(this.pathKey, this.path);
		objectData.put(this.prefixKey, this.prefix);
		try {
			 json = mapper.writeValueAsString(objectData);
			
		} catch (JsonProcessingException e) {
			logger.error("error forming json from hashmap",e);
		}
		
		return json;
		
	}
	

	@Override
	public String getFullJson() {
		return this.fullJson;
	}

	@Override
	public String getPath() {
		return this.path;
	}

}
