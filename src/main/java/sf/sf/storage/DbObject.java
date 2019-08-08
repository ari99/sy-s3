package sf.sf.storage;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import sf.sf.Constants;

/**
 * Serialization logic for an S3 object.
 * 
 * @author ari
 *
 */
public class DbObject implements DbElement {
	private static final Logger logger = LogManager.getLogger(DbObject.class);
	String bucketKey = Constants.BUCKET_CONFIG_KEY;
	String pathKey = Constants.PATH_CONFIG_KEY ;
	//object key is what s3 calls path after bucket
	String objectkeyKey= Constants.OBJECT_KEY_CONFIG_KEY;
	String lastModifiedKey = Constants.LAST_MODIFIED_CONFIG_KEY;
	String objectSizeKey = Constants.OBJECT_SIZE_CONFIG_KEY;
	
	String bucket = null;
	String path = null;
	String objectkey = null;
	String lastModified = null;
	String objectSize = null;
	
	String fullJson = null;
	
	/**
	 * Convert a DbObject to JSON.
	 * 
	 * @return JSON string.
	 */
	private String toFullJson(){
		
		String json ="error, see stack trace";
		ObjectMapper mapper = new ObjectMapper(); 
		Map<String,String> objectData = new HashMap<>();
		objectData.put(this.bucketKey, this.bucket);
		objectData.put(this.pathKey, this.path);
		objectData.put(this.objectkeyKey, this.objectkey);
		objectData.put(this.lastModifiedKey, this.lastModified);
		objectData.put(this.objectSizeKey, this.objectSize);
		try {
			 json = mapper.writeValueAsString(objectData);
			
		} catch (JsonProcessingException e) {
			logger.error("error forming json from hashmap",e);
		}
		
		return json;
	}

	/**
	 * Deserialize from JSON to DbObject.
	 * 
	 * @param json JSON string.
	 */
	public DbObject(String json){
		this.fullJson = json;
		
		ObjectMapper mapper = new ObjectMapper();
		TypeReference<Map<String, String>> typeRef =
				new TypeReference<Map<String, String>>() {};
		try {
			HashMap<String, String> objectMap = mapper.readValue(json, typeRef);
			this.bucket = objectMap.get(this.bucketKey);
			this.objectkey = objectMap.get(this.objectkeyKey);
			this.path = objectMap.get(this.pathKey);
			this.lastModified = objectMap.get(this.lastModifiedKey);
			this.objectSize  = objectMap.get(this.objectSizeKey);
			
		} catch (Exception e) {

			logger.error("error forming hashmap from json",e);
		} 

	}
	
	/**
	 * Create DB object from S3ObjectSummary.
	 * 
	 */
	public DbObject(S3ObjectSummary summary){
		this.bucket = summary.getBucketName();
		this.objectkey = summary.getKey();
		this.lastModified = summary.getLastModified().toString();
		this.objectSize  = Long.toString(summary.getSize());
		this.path  = this.bucket+"/"+this.objectkey;			
		this.fullJson = this.toFullJson();
		
	}
	
	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getObjectkey() {
		return objectkey;
	}

	public void setObjectkey(String objectkey) {
		this.objectkey = objectkey;
	}

	public String getLastModified() {
		return lastModified;
	}

	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}

	public String getObjectSize() {
		return objectSize;
	}

	public void setObjectSize(String objectSize) {
		this.objectSize = objectSize;
	}

	public String getFullJson() {
		return fullJson;
	}

	
	
	
}
