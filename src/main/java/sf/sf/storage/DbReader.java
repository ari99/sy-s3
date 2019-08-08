package sf.sf.storage;

import java.util.NavigableSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sf.sf.CliOptions;

/**
 * Methods for reading from the MapDB sets.
 * 
 * @author ari
 *
 */
public class DbReader {

	private static final Logger logger = LogManager.getLogger(DbReader.class);		
	CliOptions cliOptions;

	public DbReader(CliOptions cliOptions) {
		this.cliOptions = cliOptions;
	}


	public void readSetCommand(NavigableSet<String> set){
		if(cliOptions.getReadDbStart() != null && !cliOptions.getReadDbStart().isEmpty()){
			this.readSetWithStart(cliOptions.getReadDbStart(), set);
		}else{
			this.readSet(set);
		}
	}

	public void readSet( NavigableSet<String> set){

		for(String str: set){
			logger.debug("found in db: {}", str);
		}
	}


	public void readSetWithStart(String start,  NavigableSet<String> set){
		NavigableSet<String> dbObjects = SetLogic.getObjectsFromStart(set, start);

		for(String str: dbObjects){
			logger.debug("found in db: {}", str);
		}

	}

	public void getAllNames(){
		Iterable<String> names = DbLogic.getAllNames();
		for(String name : names){
			logger.debug("name in db: "+name);
		}
	}

	public void readAllSets() {
		logger.debug("--------------------------------------");
		logger.debug("--Initial objects---");
		logger.debug("--------------------------------------");

		this.readSetCommand(DbLogic.getInitialObjects());
		logger.debug("--------------------------------------");
		logger.debug("--Objects to DL---");
		logger.debug("--------------------------------------");

		this.readSetCommand(DbLogic.getObjectsToDl());
		logger.debug("--------------------------------------");
		logger.debug("--Dled objects---");
		logger.debug("--------------------------------------");

		this.readSetCommand(DbLogic.getDledObjects());
	}

}
