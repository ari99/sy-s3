package sf.sf.storage;

import java.util.List;
import java.util.NavigableSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import sf.sf.CliOptions;
import sf.sf.Constants;

/**
 * Logic for interacting with the DB. See architecture notes in  
 * 
 * @author ari
 *
 */
public class DbLogic {
	private static final Logger logger = LogManager.getLogger(DbLogic.class);
	/** Holds initial list of object from an AWS ListObjects request */
	private static NavigableSet<String> initialObjects;
	/** Objects left to download. */
	private static NavigableSet<String> objectsToDl;
	/** Objects downloaded. */
	private static NavigableSet<String> dledObjects;

	//The guide says one DB object represents a single transaction under "Transactions".
	/** DB which holds the sets. */
	private static DB objectsDb;

	public static void setupDb(CliOptions cliOptions, List<String> commands){

		String cliObjectsDbName = cliOptions.getObjectsDbName();
		String cliInitialObjectsSetName = cliOptions.getInitialObjectsName();
		String cliObjectsToDlSetName = cliOptions.getObjectsToDlName();
		String cliDledObjectsSetName = cliOptions.getDledObjectsName();

		if(commands.contains(Constants.DELETE_FIX_DB)){
			setUpDeleteFixDb(cliObjectsDbName);
		}else{
			setupNormalDb(cliObjectsDbName, cliInitialObjectsSetName, 
					cliObjectsToDlSetName, cliDledObjectsSetName);
		}


	}
	
	/**
	 * Output how many objects are in each set.
	 */
	public static void logSizes(){
		if(initialObjects !=null){
			logger.debug("Size IntialObjects: "+initialObjects.size());
		}else{
			logger.debug("Size InitialObjects: null");
		}

		if(objectsToDl !=null){
			logger.debug("Size ObjectsToDl: "+objectsToDl.size());
		}else{
			logger.debug("Size ObjectsToDl: null");
		}

		if(objectsToDl !=null){
			logger.debug("Size DledObjects: "+dledObjects.size());
		}else{
			logger.debug("Size DledObjects: null");
		}
	}

	private static void setupNormalDb(String cliObjectsDbName, 
			String cliInitialObjectsSetName,
			String cliObjectsToDlSetName,
			String cliDledObjectsSetName){

		//mmaping is only for 64bit oses, per the quick start
		objectsDb = DBMaker
				.fileDB(cliObjectsDbName)
				.fileMmapEnable()   	    
				.make();

		logger.debug("StorageLogic using: DB: "+ cliObjectsDbName 
				+ " initialObjectsName: "+cliInitialObjectsSetName
				+ " objectsToDlName: "+ cliObjectsToDlSetName
				+ " dledObjectsName: "+ cliDledObjectsSetName);
		
		initialObjects = objectsDb.treeSet(cliInitialObjectsSetName, Serializer.STRING)
				.createOrOpen();
		objectsToDl = objectsDb.treeSet(cliObjectsToDlSetName, Serializer.STRING)
				.createOrOpen();
		dledObjects = objectsDb.treeSet(cliDledObjectsSetName, Serializer.STRING)
				.createOrOpen();
	}
	
	public static DB getDB(){
		return objectsDb;
	}

	public static NavigableSet<String> getInitialObjects(){
		return initialObjects;
	}


	public static NavigableSet<String> getObjectsToDl(){
		return objectsToDl;
	}


	public static NavigableSet<String> getDledObjects(){
		return dledObjects;
	}

	/**
	 * Returns the names of all the sets (tables) in the DB.
	 * 
	 * @return list of set names
	 */
	public static Iterable<String> getAllNames(){
		return objectsDb.getAllNames() ;
	}


	public static void commitObjectsDb(){
		objectsDb.commit();

	}

	public static void closeObjectsDb(){
		objectsDb.close();
	}

	/**
	 * Set up a DB to delete the current one if it gets corrupted.
	 * 
	 * @param objectsDbName
	 */
	public static void setUpDeleteFixDb(String objectsDbName){
		logger.debug("Setting up delete-fix db");
		objectsDb = DBMaker
				.fileDB(objectsDbName)
				.fileMmapEnable()
				.checksumHeaderBypass() 
				.fileDeleteAfterClose()
				.make();

	}
}


