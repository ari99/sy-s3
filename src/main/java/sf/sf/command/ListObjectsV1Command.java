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
import sf.sf.verticle.ListObjectsV1Verticle;
/**
 * Command for AWS API list objects v1. Deploys verticles sequentially.
 * 
 * @author ari
 *
 */
public class ListObjectsV1Command extends ListObjectsCommand{
    private static final Logger logger = LogManager.getLogger(ListObjectsV1Command.class);

	public ListObjectsV1Command(CliOptions cliOptions, Vertx vertx,
			Future<String> commandDoneFuture) {
		super(cliOptions, vertx, commandDoneFuture);
	}
	
	public void launchVerticle(Optional<String> nextMarker) {
		numRequests++;
		logger.debug("Num requests: " + numRequests );
		
		// http://www.programcreek.com/java-api-examples/index.php?api=io.vertx.core.json.JsonObject
		JsonObject config = this.makeConfig(nextMarker);
		
		DeploymentOptions options = new DeploymentOptions().setInstances(1).setConfig(config);
		ListObjectsV1Verticle listObjectVerticle = new ListObjectsV1Verticle(this.commandDoneFuture);
		vertx.deployVerticle(listObjectVerticle, options, resultHandler());
		
	}
	
	
	private JsonObject makeConfig(Optional<String> nextMarker){
		JsonObject config = this.makeBasicHttpClientConfig();
		config.put(Constants.BUCKET_CONFIG_KEY, this.cliOptions.getBucket());
		config.put(Constants.MAX_KEYS_CONFIG_KEY, this.cliOptions.getMaxKeys());
		// http://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/util/AntPathMatcher.html
		// http://vertx.io/docs/apidocs/io/vertx/core/eventbus/DeliveryOptions.html
		// http://vertx.io/docs/apidocs/io/vertx/core/eventbus/EventBus.html
		config.put(Constants.OBJECT_PATTERN_CONFIG_KEY, this.cliOptions.getPattern());
		config.put(Constants.PREFIX_CONFIG_KEY, this.cliOptions.getPrefix());
		config.put(Constants.DELIMITER, this.cliOptions.getDelimiter());
		
		//next marker signifys where in the list of objects in the bucket to start getting the next objects
		if (nextMarker.isPresent()) {
			config.put(Constants.MARKER_CONFIG_KEY, nextMarker.get());
		}
		return config;
	}
	
	
	
}
