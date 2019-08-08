package sf.sf.command;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import sf.sf.CliOptions;
import sf.sf.Constants;
import sf.sf.verticle.ListObjectsV2Verticle;

/**
 * Command to run AWS API's list objects v2.
 * 
 * @author ari
 *
 */
public class ListObjectsV2Command  extends ListObjectsCommand{
    private static final Logger logger = LogManager.getLogger(ListObjectsV2Command.class);
	public ListObjectsV2Command(CliOptions cliOptions, Vertx vertx,
			Future<String> commandDoneFuture ) {
		super(cliOptions, vertx, commandDoneFuture);
	}

	public void launchVerticle(Optional<String> token) {
		numRequests++;
		logger.debug("Num requests: " + numRequests );

		JsonObject config = this.makeConfig(token);
		
		DeploymentOptions options = new DeploymentOptions().setInstances(1).setConfig(config);
		ListObjectsV2Verticle listObjectVerticle = new ListObjectsV2Verticle(this.commandDoneFuture);
		vertx.deployVerticle(listObjectVerticle, options, resultHandler());
		
	}
	
	
	private JsonObject makeConfig(Optional<String> token){
		JsonObject config = this.makeBasicHttpClientConfig();
		config.put(Constants.BUCKET_CONFIG_KEY, this.cliOptions.getBucket());
		config.put(Constants.MAX_KEYS_CONFIG_KEY, this.cliOptions.getMaxKeys());
		config.put(Constants.OBJECT_PATTERN_CONFIG_KEY, this.cliOptions.getPattern());
		config.put(Constants.START_AFTER_CONFIG_KEY, this.cliOptions.getStartAfter());
		config.put(Constants.DELIMITER, this.cliOptions.getDelimiter());
		config.put(Constants.PREFIX_CONFIG_KEY, this.cliOptions.getPrefix());
		if (token.isPresent()) {
			config.put(Constants.CONTINUATION_TOKEN_CONFIG_KEY, token.get());
		}
		return config;
	}
	
}
