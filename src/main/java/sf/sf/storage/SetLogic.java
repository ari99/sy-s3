package sf.sf.storage;

import java.util.NavigableSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Methods for interacting with MapDB Sets.
 * 
 * @author ari
 *
 */
public class SetLogic {
	private static final Logger logger = LogManager.getLogger(SetLogic.class);
	
	/**
	 * Get objects contained in the DB starting at the start param.
	 * 
	 */
	public static NavigableSet<String> getObjectsFromStart(NavigableSet<String> set, String start){
		if(start == null){
			return set;
		}else{
			//inclusive is false. don't also return nextMarker
			return set.tailSet(start, false);
		}
	}
	

	public static void addIntoObjectsSet(NavigableSet<String> set, String objectString){
		if(set == null){
			logger.debug("Set is null");
		}
		if(objectString == null){
			logger.debug("Object string is null");
		}
		set.add(objectString);
		
	}
	
	public static void removeFromObjectsSet(String setDebugName, NavigableSet<String> set, String objectString){
		boolean removed = set.remove(objectString);
		if(removed){
			logger.debug(objectString + " was successfully removed from "+setDebugName);
		}else{
			logger.debug(objectString + " wasnt found in "+setDebugName);
		}
	}
	
	/**
	 * Clear a DB set.
	 * 
	 */
	public static void clearObjectsSet(NavigableSet<String> set){
		set.clear();
		DbLogic.commitObjectsDb();  
	}
	

}
