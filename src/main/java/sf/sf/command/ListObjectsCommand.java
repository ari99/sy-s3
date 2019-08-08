package sf.sf.command;

import java.util.Optional;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import sf.sf.CliOptions;

/**
 * Abstract superclass for ListObjects commands.
 * 
 * @author ari
 *
 */
public abstract class ListObjectsCommand extends S3Command {
	
	public ListObjectsCommand(CliOptions cliOptions, Vertx vertx,Future<String> commandDoneFuture) {
		super(cliOptions, vertx, commandDoneFuture);
	}

	protected abstract void launchVerticle(Optional<String> next);
	
	
}
