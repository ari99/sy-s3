package sf.sf.command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Throwables;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import sf.sf.CliOptions;
import sf.sf.Constants;

/**
 * Superclass for other command classes.
 * 
 * @author ari
 *
 */
public class S3Command {
    private static final Logger logger = LogManager.getLogger(S3Command.class);
	protected CliOptions cliOptions;
	public static int numRequests = 0; //not accessed from multiple threads
	protected Future<String> commandDoneFuture;
	protected Vertx vertx;
	
	public S3Command(CliOptions cliOptions, Vertx vertx, Future<String> commandDoneFuture){
		this.cliOptions = cliOptions;
		this.vertx = vertx;
		this.commandDoneFuture = commandDoneFuture;
		
	}
	
	
	protected JsonObject makeBasicHttpClientConfig(){
		JsonObject config = new JsonObject();
		config.put(Constants.ACCESS_KEY_CONFIG_KEY, cliOptions.getAccessKey());
		config.put(Constants.SECRET_KEY_CONFIG_KEY, cliOptions.getSecretKey());
		config.put(Constants.ENDPOINT_CONFIG_KEY, cliOptions.getEndpoint());		
		config.put(Constants.PORT_CONFIG_KEY, cliOptions.getPort());
		return config;
	}
	

	
	protected Handler<AsyncResult<String>> resultHandler() {

		return (AsyncResult<String> res) -> {
			if (!res.succeeded()) {
				logger.debug("Verticle deployment failed");
				String s = Throwables.getStackTraceAsString(res.cause());
				logger.debug(s);
			}else{
				//logger.debug("Verticle deployed successfully");
			}
		};
	}
}
