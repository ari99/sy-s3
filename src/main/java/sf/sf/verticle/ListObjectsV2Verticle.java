package sf.sf.verticle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.google.common.base.Throwables;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;
import sf.sf.Constants;
import sf.sf.s3client.S3ClientOperations;



/**
 * 
 * Verticle for the ListObjectsV2 command.
 * See http://docs.aws.amazon.com/AmazonS3/latest/API/v2-RESTBucketGET.html
 * 
 * @author ari
 *
 * 
 */
public class ListObjectsV2Verticle extends ListObjectVerticle{
	
	private static final Logger logger = LogManager.getLogger(ListObjectsV2Verticle.class);
	
	public ListObjectsV2Verticle(Future<String> commandDoneFuture) {
		super(commandDoneFuture);
	}
	
	/**
	 * start() is called when the verticle is deployed.
	 */
	@Override
	public void start() {

		Handler<HttpClientResponse> responseHandler = this.makeListResponseHandler();
		ListObjectsV2Request requestModel = this.makeRequestModel();

		Handler<Throwable> requestExceptionHandler = this.makeRequestExceptionHandler(requestModel);
		
		S3ClientOperations ops = new S3ClientOperations(responseHandler, requestExceptionHandler);

		HttpClientRequest getRequest = ops.getBucketListObjectsRequestV2(requestModel);
		getRequest.end();

	}
	
	/**
	 * AWS API ListObjectV2Request object from verticle config.
	 * 
	 * @return the AWS API SDK object.
	 */
	private ListObjectsV2Request makeRequestModel() {
		JsonObject config = this.config();
		ListObjectsV2Request request = new ListObjectsV2Request();
        
		request.setBucketName(config.getString(Constants.BUCKET_CONFIG_KEY));
        request.setContinuationToken(config.getString(Constants.CONTINUATION_TOKEN_CONFIG_KEY));
        request.setPrefix(config.getString(Constants.PREFIX_CONFIG_KEY));
        request.setDelimiter(config.getString(Constants.DELIMITER));
        request.setMaxKeys(config.getInteger(Constants.MAX_KEYS_CONFIG_KEY));
        request.setStartAfter(config.getString(Constants.START_AFTER_CONFIG_KEY));        
        
        return request;
	}

	@Override
	public String getListObjectsEventBus(){
		return Constants.LIST_OBJECTS_V2_BUS;
	}
	
	private Handler<Throwable> makeRequestExceptionHandler(ListObjectsV2Request request){
		Handler<Throwable> exceptionHandler  = (Throwable exception) -> {
			String message =
					"  List objects v2 exception. bucket name: "+request.getBucketName()
					+" , no key "
					+ ", continutation token : "+ request.getContinuationToken()
					+ ", start after: "+ request.getStartAfter()
					+ ", prefix: " + request.getPrefix()
					+ ", delimeter: "+ request.getDelimiter();
			String stackStraceStr = Throwables.getStackTraceAsString(exception);
			logger.error("Error {} {}", message, exception);
			logger.error(stackStraceStr);
			this.commandDoneFuture.fail(message + " " + stackStraceStr);
			
		};
		
		return exceptionHandler;
	}
	
}
