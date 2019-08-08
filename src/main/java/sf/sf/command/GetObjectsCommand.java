package sf.sf.command;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.AntPathMatcher;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import sf.sf.CliOptions;
import sf.sf.Constants;
import sf.sf.storage.DbLogic;
import sf.sf.storage.SetLogic;
import sf.sf.verticle.getobject.GetObjectFutureWrapper;
import sf.sf.verticle.getobject.GetObjectVerticle;
/**
 * 
 * Download objects in "to download" set in db.
 * 
 * @author ari
 *
 */
public class GetObjectsCommand extends S3Command {
	private static final Logger logger = LogManager.getLogger(GetObjectsCommand.class);

	public GetObjectsCommand(CliOptions cliOptions, 
							Vertx vertx, 
							Future<String> commandDoneFuture) {
		super(cliOptions, vertx, commandDoneFuture);
	}

	public void launch() {
		try {
			this.downloadListedObjects();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 *  Download objects listed in db.
	 *  
	 * @throws IOException
	 */
	public void downloadListedObjects() throws IOException {
		logger.debug("setting up verticles");

		NavigableSet<String> objects = SetLogic.getObjectsFromStart(
										DbLogic.getObjectsToDl(),
										this.cliOptions.getReadDbStart());

		HashMap<String, GetObjectFutureWrapper> getObjectFutures = 
				this.createVerticles(objects);

		long periodicTimerId = this.checkFuturesPeriodically(getObjectFutures);
		long completeAllTimerId = this.completeAllFuturesTimeout(getObjectFutures);

		List<Future> futureList = getObjectFutures.values().stream().map(wrapper -> wrapper.getFuture())
										.collect(Collectors.toList());
		
		this.setGetObjectFuturesCompleteHandler(futureList, periodicTimerId, completeAllTimerId);
		

	}
	
	/**
	 * 
	 * Logic to handle command completion or failure of get_object verticles.
	 * 
	 * http://vertx.io/docs/vertx-core/java/#_async_coordination
	 * https://github.com/eclipse/vert.x/blob/master/src/main/java/io/vertx/core/CompositeFuture.java
	 * 
	 * 
	 * @param futureList
	 * @param periodicTimerId
	 * @param completeAllTimerId
	 */
	private void setGetObjectFuturesCompleteHandler(List<Future> futureList, 
			long periodicTimerId, long completeAllTimerId ) {
		// join waits for all to be complete and fails if one fails
		CompositeFuture.join(futureList).setHandler(ar -> {
			if (ar.succeeded()) {
				// All succeeded
				logger.debug("Composite Future all objects succeeded. ");
				this.commandDoneFuture.complete();
				
			} else {
				logger.debug("Composite future at least one object failed. ");
				// All completed and at least one failed
				this.commandDoneFuture.fail("At least one command failed in getObjects");
			}
			vertx.cancelTimer(periodicTimerId);
			vertx.cancelTimer(completeAllTimerId);
		});
	}
	
	/**
	 * Creates get_object verticles.
	 * 
	 * @param objects List of objects to download.
	 * @param includePattern Pattern of objects to include in download.
	 * @return HashMap of futures to get notified of the completion of the verticles.
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private HashMap<String, GetObjectFutureWrapper> createVerticles(
			NavigableSet<String> objects) 
					throws JsonParseException, JsonMappingException, IOException{
		
		ObjectMapper mapper = new ObjectMapper();
		AntPathMatcher matcher = new AntPathMatcher();
		TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {};

		HashMap<String, GetObjectFutureWrapper> getObjectFutures = new HashMap<>();
		
		for (String object : objects) {

			HashMap<String, String> objectMap = mapper.readValue(object, typeRef);

			String path = objectMap.get(Constants.PATH_CONFIG_KEY);
			String pattern = this.cliOptions.getPattern();
			
			if ((!Strings.isNullOrEmpty(pattern)
					|| matcher.match(pattern, path))
					&& this.cliOptions.numRequestsIsLessThanMaxRequestsGetObjects()) {
				
				logger.debug("Deploying verticle to download: {}", path);

				GetObjectFutureWrapper getObjectCompleteFuture = this.deployVerticle(objectMap, object);
				getObjectFutures.put(path, getObjectCompleteFuture);
				
				numRequests++;
				this.sleepIfNeeded(numRequests);
			}

		}
		return getObjectFutures;
	}
	
	/**
	 * Deploy a single verticle.
	 * 
	 * @param objectMap Map specifying object to download.
	 * @param object String of object as it was saved in db.
	 * @param path S3 path for object.
	 * @return Future used to get notified of the completion of the verticle.
	 */
	private GetObjectFutureWrapper deployVerticle(
			HashMap<String, String> objectMap, String object) {
		
		DeploymentOptions options = this.makeGetObjectVerticleDeploymentOption(
				objectMap.get(Constants.BUCKET_CONFIG_KEY),
				objectMap.get(Constants.OBJECT_KEY_CONFIG_KEY),
				object);

		Future<Void> getObjectFuture = Future.future(); 
		GetObjectFutureWrapper getObjectCompleteFuture  = new GetObjectFutureWrapper(getObjectFuture);
		
		GetObjectVerticle verticle = new GetObjectVerticle(getObjectCompleteFuture);
		
		vertx.deployVerticle(verticle, options, resultHandler());
		
		return getObjectCompleteFuture;
		
	}
	
	protected DeploymentOptions makeGetObjectVerticleDeploymentOption(String bucket, 
			String objectKey, String objectDbString){

		JsonObject config = this.makeBasicHttpClientConfig();
		config.put(Constants.BUCKET_CONFIG_KEY, bucket);
		config.put(Constants.OBJECT_KEY_CONFIG_KEY, objectKey);
		config.put(Constants.OBJECT_DB_STRING_CONFIG_KEY, objectDbString);
		DeploymentOptions options = new DeploymentOptions().setInstances(1).setConfig(config);
		
		return options;
	}
	/**
	 * Force complete of all futures because of timeout.
	 * 
	 * @param futures
	 * @return vertx timer id.
	 */
	private long completeAllFuturesTimeout(HashMap<String, GetObjectFutureWrapper> futures){

		long timerId = vertx.setTimer(240000, 
				(Long id) ->{
					logger.debug("Completing all incomplete futures for getobjects start============");

					for(Entry<String, GetObjectFutureWrapper> entry : futures.entrySet()){
						
						GetObjectFutureWrapper getObjectFuture = entry.getValue();
						logger.debug("Completing all - Entry key: "+entry.getKey()+" Complete: " 
									+ getObjectFuture.isComplete()+" succeded: "+getObjectFuture.succeeded());
						
						if(!getObjectFuture.isComplete()){
							logger.debug("Completing all - This one is not complete: "+entry.getKey());
							// complete any incomplete future by calling failVerticle
							getObjectFuture.failVerticle("Get object future timeout because of the "+
											" 'complete all timeout' in getobjects command , path: "
											 + entry.getKey(), entry.getKey());	
						}
						
					}
					logger.debug("=============Completing all incomplete futures for getobjects end============");

				}
		
		);

		return timerId;
	}
	
	/**
	 * Periodically print the status of the futures.
	 * @param futures
	 * @return vertx timer id.
	 */
	private long checkFuturesPeriodically(HashMap<String, GetObjectFutureWrapper> futures){
		long timerId = vertx.setPeriodic(30000, (Long id) ->{
			logger.debug("Debugging get object futures start============");
			for(Entry<String, GetObjectFutureWrapper> entry : futures.entrySet()){
				logger.debug("Entry key: " + entry.getKey() + " Complete: " 
							+ entry.getValue().isComplete() + " succeded: "+ entry.getValue().succeeded());
				if(! entry.getValue().isComplete()){
					logger.debug("This one is not complete: " + entry.getKey());
				}
			}
			logger.debug("Debugging get object futures end============");

		});
		return timerId;
	}
	
	/**
	 * Put the thread to sleep if the numVerticles is divisible by its configured amount.
	 * 
	 * @param numVerticles current number of verticles launched for the command.
	 */
	private void sleepIfNeeded(int numVerticles) {
		if (this.cliOptions.getNumVerticlesPerSleep() != null && this.cliOptions.getNumVerticlesPerSleep() != 0
				&& this.cliOptions.getSleepTime() != null && this.cliOptions.getSleepTime() != 0) {
			if (numVerticles % this.cliOptions.getNumVerticlesPerSleep() == 0) {
				logger.debug("===================================");
				logger.debug("================STARTING Sleeping the thread , numVerts: "
						+ numVerticles + " , numVertsPerSleep: " + this.cliOptions.getNumVerticlesPerSleep()
						+ "===================");
				logger.debug("===================================");

				try {
					Thread.sleep(this.cliOptions.getSleepTime());
				} catch (InterruptedException e) {
					logger.debug("sleeping interupted");
					e.printStackTrace();
				}
				logger.debug("===================================");
				logger.debug("================ENDING Sleeping the thread , numVerts: "
						+ numVerticles + " , numVertsPerSleep: " + this.cliOptions.getNumVerticlesPerSleep()
						+ "===================");
				logger.debug("===================================");

			}
		}
	}

}
