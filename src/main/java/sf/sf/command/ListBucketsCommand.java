package sf.sf.command;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import sf.sf.CliOptions;
import sf.sf.verticle.ListBucketsVerticle;
/**
 * List S3 buckets belonging to an account. Cannot be used with unauthenticated requests.
 *
 * @author ari
 *
 */
public class ListBucketsCommand extends S3Command{

	public ListBucketsCommand(CliOptions cliOptions, Vertx vertx, Future<String> blockingCommandHandler) {
		super(cliOptions, vertx, blockingCommandHandler);
	}
	
	public void launch(){
		JsonObject config = this.makeBasicHttpClientConfig();
		DeploymentOptions options = new DeploymentOptions().setInstances(1).setConfig(config);

		ListBucketsVerticle verticle = new ListBucketsVerticle(this.commandDoneFuture);
		vertx.deployVerticle(verticle, options, resultHandler());

	}

}
