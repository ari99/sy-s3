package sf.sf.consumer;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.AntPathMatcher;

import com.amazonaws.services.s3.model.S3ObjectSummary;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import sf.sf.command.S3Command;
import sf.sf.storage.DbElement;
import sf.sf.storage.DbLogic;
import sf.sf.storage.DbObject;
import sf.sf.storage.DbPrefix;
import sf.sf.storage.SetLogic;
import sf.sf.CliOptions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
 
/**
 * Superclass for list_objects consumers.
 * The list objects consumer classes consume from the vertx event bus.
 * 
 * @author ari
 *
 */
public abstract class ListObjectsConsumer implements Handler<Message<String>>{
    private static final Logger logger = LogManager.getLogger(ListObjectsConsumer.class);

	protected Vertx vertx;
	protected CliOptions cliOptions;
	protected Future<String> commandDoneFuture;

	public ListObjectsConsumer(Vertx vertx,
			CliOptions cliOptions, Future<String> commandDoneFuture){
		this.cliOptions = cliOptions;
		this.vertx = vertx;
		this.commandDoneFuture = commandDoneFuture;
	}
	
	/**
	 * Messages from the event bus are consumed through this method.
	 * The AWS API response is contained in the event param.
	 * The event is published in {@link sf.sf.verticle.ListObjectVerticle#handleResponse}.
	 * 
	 * @see sf.sf.verticle.ListObjectVerticle#handleResponse 
	 */
	@Override
	public void handle(Message<String> event) {
		
		String responseBody = event.body();

		if(responseBody.contains("<Error>")){
			logger.debug("Error xml response from s3 for ListObjects V2 or V1 ");
			logger.debug(responseBody);
			 this.failListObjects("Error xml response from s3");

		}else{
			this.handleResponseBody(responseBody);
		}	
	}
	
	protected abstract void handleResponseBody(String responseBody);
	
	/**
	 * Save list of objects into db by converting them 
	 * into {@link DbElement} and sending them
	 * to {@link sf.sf.consumer.ListObjectsConsumer#saveDbElements}.
	 * 
	 */
	public void handleObjectSummarys(List<S3ObjectSummary> summarys){
		//https://jankotek.gitbooks.io/mapdb/content/quick-start/
		//https://github.com/tuplejump/MapDB/tree/master/src/test/java/examples
	    //http://stackoverflow.com/questions/24715971/how-to-retrieve-data-from-mapdb-database-without-override-it-every-time
		//indextreelist example
		//https://github.com/grozeille/test-mapdb/blob/master/src/main/java/org/grozeille/StreamController.java
		// S3ObjectSummary can't be serialized directly
		
		List<DbElement> dbObjects = summarys.stream()
							.map((S3ObjectSummary summary) -> {
								return new DbObject(summary);
								
							})
							.collect(Collectors.toList());
		this.saveDbElements(dbObjects);
	}
	
	/**
	 * Save list of prefixes returned by AWS into mapdb 
	 * by converting them into {@link sf.sf.storage.DbElement} and sending them
	 * to {@link sf.sf.consumer.ListObjectsConsumer#saveDbElements}.
	 * 
	 */
	public void handleCommonPrefixes(List<String> prefixes, String bucket){
		List<DbElement> dbPrefixes = 
				prefixes.stream().map( (String prefix) ->{
					return new DbPrefix(prefix, bucket);
				})
				.collect(Collectors.toList());
									
								
		this.saveDbElements(dbPrefixes);
	}
	
	
	/**
	 * Save list of {@link sf.sf.storage.DbElement}.
	 * 
	 */
	private void saveDbElements(List<DbElement> dbElements){
		String includePattern =  this.cliOptions.getPattern(); 
		AntPathMatcher matcher = new AntPathMatcher();
		
		for(DbElement dbElement : dbElements){
			if(includePattern == null || matcher.match(includePattern.trim(), dbElement.getPath()) ){
				this.saveToDbSets(dbElement.getFullJson());
			}else {
				logger.trace("Path: {} does not match {}",  dbElement.getPath(), includePattern);
			}
		}
	}
	
	/**
	 * Insert into initialObjects set and objectsToDel set in db.
	 * 
	 */
	private void saveToDbSets(String json){
		SetLogic.addIntoObjectsSet(DbLogic.getInitialObjects() , json);
		SetLogic.addIntoObjectsSet(DbLogic.getObjectsToDl() , json);
	}
	/**
	 * Send a fail message to commandDoneFuture and complete it.
	 */
	protected void failListObjects(String message){
		   this.commandDoneFuture.complete("+++ list objects failed in consumer: "+message);
	}
	
	/**
	 * Complete commandDoneFuture with a success message.
	 */
	protected void succeedListObjects(){
		 logger.debug("listing not truncated or numRequests: "+S3Command.numRequests
				 +" greater than max: "
				 + this.cliOptions.getMaxRequestsListObjects()
				    +"  ; closing db");
		   this.commandDoneFuture.complete("+++ list objects completed successfuly in consumer");
	}
	
	
}
