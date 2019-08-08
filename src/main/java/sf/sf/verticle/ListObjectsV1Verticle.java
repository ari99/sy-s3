package sf.sf.verticle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.google.common.base.Throwables;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;
import sf.sf.Constants;
import sf.sf.s3client.S3ClientOperations;
/**
 * Verticle for ListObjectV1 command.
 * 
 * @author ari
 *
 */
public class ListObjectsV1Verticle extends ListObjectVerticle {
	public ListObjectsV1Verticle(Future<String> commandDoneFuture) {
		super(commandDoneFuture);
	}

	private static final Logger logger = LogManager.getLogger(ListObjectsV1Verticle.class);

	/**
	 * Called when verticle is deployed
	 */
	@Override
	public void start() {

		Handler<HttpClientResponse> responseHandler = this.makeListResponseHandler();
		ListObjectsRequest requestModel = this.makeRequestModel();

		Handler<Throwable> requestExceptionHandler = this.makeRequestExceptionHandler(requestModel);

		S3ClientOperations ops = new S3ClientOperations( responseHandler, requestExceptionHandler);
		HttpClientRequest getRequest = ops.getBucketListObjectsRequestV1(requestModel);
		// end() sends the request
		getRequest.end();

	}

	private Handler<Throwable> makeRequestExceptionHandler(ListObjectsRequest request){
		Handler<Throwable> exceptionHandler  = (Throwable exception) -> {
			String message =
					" List objects v1 exception. bucket name: "+request.getBucketName()
					+" , no key, "
					+ " , prefix: "+ request.getPrefix()
					+ " , delimiter: " + request.getDelimiter();
			String stackStraceStr = Throwables.getStackTraceAsString(exception);
			logger.error("Error {} {}", message, exception);
			logger.error(stackStraceStr);
			this.commandDoneFuture.fail(message + " " + stackStraceStr);

		};
		
		return exceptionHandler;
	}

	/**
	 * Create AWS API request object from verticle config.
	 * 
	 * @return the AWS API SDK ListObjects request object.
	 */
	private ListObjectsRequest makeRequestModel() {
		JsonObject config = this.config();
		ListObjectsRequest request = new ListObjectsRequest();

		request.setBucketName(config.getString(Constants.BUCKET_CONFIG_KEY));
		request.setMarker(config.getString(Constants.MARKER_CONFIG_KEY));
		request.setPrefix(config.getString(Constants.PREFIX_CONFIG_KEY));
		request.setDelimiter(config.getString(Constants.DELIMITER));

		request.setMaxKeys(config.getInteger(Constants.MAX_KEYS_CONFIG_KEY));

		return request;
	}


	@Override
	public String getListObjectsEventBus() {
		return Constants.LIST_OBJECTS_V1_BUS;
	}

}
