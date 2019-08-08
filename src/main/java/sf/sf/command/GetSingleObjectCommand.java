package sf.sf.command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Throwables;

import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import sf.sf.CliOptions;
import sf.sf.Constants;
import sf.sf.verticle.getobject.GetObjectFutureWrapper;
import sf.sf.verticle.getobject.GetObjectVerticle;

/**
 * Download a single object from S3.
 * 
 * http://docs.aws.amazon.com/AmazonS3/latest/API/RESTObjectGET.html
 * 
 * @author ari
 *
 */

public class GetSingleObjectCommand extends S3Command {
	private static final Logger logger = LogManager.getLogger(GetSingleObjectCommand.class);
	//The handler for the main logic to know its ok to move on to the next command in a chain of commands
	public GetSingleObjectCommand(CliOptions cliOptions, Vertx vertx,
			 Future<String> commandDoneFuture) {
		super(cliOptions, vertx,commandDoneFuture);
	}

	/**
	 * Set up verticle config and launch it.
	 * 
	 */
	public void launch(){
		JsonObject config = this.makeBasicHttpClientConfig();
		config.put(Constants.BUCKET_CONFIG_KEY, cliOptions.getBucket());
		config.put(Constants.OBJECT_KEY_CONFIG_KEY, cliOptions.getObjectKey());
		// vertx verticle options
		DeploymentOptions options = new DeploymentOptions().setInstances(1).setConfig(config);
		
		// future to track completion
		Future<Void> getObjectFuture = this.createGetObjectFuture();
		
		GetObjectFutureWrapper futureWrapper = new GetObjectFutureWrapper(getObjectFuture);
		GetObjectVerticle getObjectVerticle = new GetObjectVerticle(futureWrapper);
		vertx.deployVerticle(getObjectVerticle, options, resultHandler());
		
	}
	
	/**
	 * Create future to track the result of the get_object call.
	 * 
	 * @return The future created.
	 */
	private Future<Void> createGetObjectFuture(){
		
		Future<Void> getObjectFuture = Future.future();
		
		getObjectFuture.setHandler((AsyncResult<Void> result) -> {
			if(!result.succeeded()){
				Throwable cause = result.cause();
				String s = Throwables.getStackTraceAsString(cause);
				logger.error("Error from single object command  {} ", cause);
				logger.error(s);
				this.commandDoneFuture.fail("Error from single object command "+ s);
			}else{
				logger.debug("single object command complete");
				this.commandDoneFuture.complete(" single object command complete");
			}
			
		});
		
		return getObjectFuture;
		
	}
	
	
	
}
