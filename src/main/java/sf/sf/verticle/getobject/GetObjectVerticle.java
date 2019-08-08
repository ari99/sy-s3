package sf.sf.verticle.getobject;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.s3.model.GetObjectRequest;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;
import sf.sf.Constants;
import sf.sf.s3client.S3ClientOperations;

//	https://groups.google.com/forum/#!topic/vertx/N_wSoQlvMMs%5B1-25%5D
//    http://www.programcreek.com/java-api-examples/index.php?source_dir=platform-master/vertx/src/main/java/org/cradle/platform/vertx/httpgateway/client/FileRequestHandler.java
//    https://gist.github.com/alexlehm/b5726ded353eaafd14f4
//    https://github.com/vert-x3/vertx-examples/blob/2b6f74fd7af731f15834c8cbc7da532437d9c685/core-examples/src/main/java/io/vertx/example/core/http/upload/Server.java
/**
 * Verticle used to download an object from S3.
 * 
 * @author ari
 *
 */
public class GetObjectVerticle extends AbstractVerticle {
	private static final Logger logger = LogManager.getLogger(GetObjectVerticle.class);
	private GetObjectFutureWrapper getObjectCompleteFuture;

	public GetObjectVerticle(GetObjectFutureWrapper getObjectCompleteFuture) {
		 this.getObjectCompleteFuture  = getObjectCompleteFuture; //new GetObjectCompleteFuture(completeFuture);
	}
	
	public void start() {

		// http://www.programcreek.com/java-api-examples/index.php?api=io.vertx.core.json.JsonObject

		GetObjectResponseLogic responseLogic = new GetObjectResponseLogic(
												this.config(), this.vertx, this.getObjectCompleteFuture);
		Handler<HttpClientResponse> responseHandler = responseLogic.makeResponseHandler();
		GetObjectRequest request = this.makeRequestModel();
		Handler<Throwable> requestExceptionHandler = this.makeRequestExceptionHandler(request);
		
		S3ClientOperations ops = new S3ClientOperations( responseHandler, requestExceptionHandler);
		HttpClientRequest getRequest = ops.getObjectRequest(request);

		logger.debug("calling end on get object request");
		getRequest.end();

	}

	private  Handler<Throwable> makeRequestExceptionHandler(GetObjectRequest request){
		Handler<Throwable> exceptionHandler  = (Throwable e) -> {
			String message = " Get object http request exception. bucket name: "+request.getBucketName()
			+" key: "+ request.getKey()+ " "+e.getMessage();
			this.getObjectCompleteFuture.failVerticle(message,request.getBucketName()+request.getKey(), e);
			
			
		};
		return exceptionHandler;
	}

	private GetObjectRequest makeRequestModel() {
		JsonObject config = this.config();
		GetObjectRequest request = new GetObjectRequest(config.getString(Constants.BUCKET_CONFIG_KEY),
				config.getString(Constants.OBJECT_KEY_CONFIG_KEY));
		return request;
	}



	
}
