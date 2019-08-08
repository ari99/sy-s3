package sf.sf.verticle;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.s3.model.transform.XmlResponsesSaxParser;
import com.amazonaws.services.s3.model.transform.XmlResponsesSaxParser.ListAllMyBucketsHandler;
import com.google.common.base.Throwables;
import com.amazonaws.services.s3.model.Bucket;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import sf.sf.s3client.S3ClientOperations;

/**
 * Verticle for the ListBuckets command.
 * 
 * @author ari
 *
 */
public class ListBucketsVerticle extends AbstractVerticle{
	private static final Logger logger = LogManager.getLogger(ListBucketsVerticle.class);
	private Future<String> commandDoneFuture;
	
	public ListBucketsVerticle( Future<String> commandDoneFuture){
		this.commandDoneFuture = commandDoneFuture;
	}
	
	/**
	 * start is called when the verticle is deployed.
	 */
	@Override
	public void start() {
		
		Handler<HttpClientResponse> responseHandler = this.makeResponseHandler();
		Handler<Throwable> requestExceptionHandler = this.makeRequestExceptionHandler();
		S3ClientOperations ops = new S3ClientOperations(responseHandler, requestExceptionHandler );
		HttpClientRequest getRequest = ops.getListBucketsRequest();
		getRequest.end();
		
	}
	
	/**
	 * Creates a vertx Handler for the AWS API response.
	 * 
	 * @return The response handler.
	 */
	public Handler<HttpClientResponse> makeResponseHandler(){
		Handler<HttpClientResponse> handler = (HttpClientResponse response) -> {
			int statusCode = response.statusCode();
			this.debugReponse(statusCode, response.headers(), response.statusMessage());
			
			
			// The body handler is called once when all the body has been
			// received:
			response.bodyHandler((Buffer totalBuffer) -> {
				// Now all the body has been read
				logger.debug("Total response body length is " +
								totalBuffer.length());
				
				logger.debug(totalBuffer.toString());

				this.handleResponse(totalBuffer, statusCode);

			});

		};

		return handler;
		
	}
	
	private void debugReponse(int statusCode, MultiMap headers, String statusMessage) {
		logger.debug("Status code "+ statusCode);
		logger.debug("status message " + statusMessage);
		logger.debug("headers "+ headers);
		
		// print headers
		for(Map.Entry<String, String> entry : headers.entries()) {
			logger.debug("key " + entry.getKey());
			logger.debug("value " + entry.getValue());
		}
	}
	
	/**
	 *  Handle AWS API response content.
	 *  
	 * @param totalBuffer Response body.
	 */
	public void handleResponse(Buffer totalBuffer, int statusCode) {
		String xmlString = totalBuffer.toString();
		if(statusCode != 200){
			logger.debug("Error returned from s3 for listbuckets request:");
			logger.debug(xmlString);
		}else{
			List<Bucket> buckets = this.makeBucketList(xmlString);
			for(Bucket bucket : buckets){
				//can debug bucket owner too
				logger.debug("Name: "+bucket.getName()+ " , Creation Date: "+bucket.getCreationDate());
			}
		}
		
		this.commandDoneFuture.complete("complete list buckets");
	}
	
	/**
	 * Convert xml string to List of Bucket objects.
	 * 
	 */
	public List<Bucket> makeBucketList(String xmlString){
		List<Bucket> buckets = new ArrayList<Bucket>();
		logger.debug("Full list buckets response: ");
		logger.debug(xmlString);
		
		XmlResponsesSaxParser parser = new XmlResponsesSaxParser();
		try {
			InputStream in = IOUtils.toInputStream(xmlString, "UTF-8");
			ListAllMyBucketsHandler lbhandler = parser.parseListMyBucketsResponse(in);
			buckets = lbhandler.getBuckets();
		} catch (IOException e) {
			logger.error("Error parsing xml ", e);
		}
		return buckets;
	}
	
	private Handler<Throwable> makeRequestExceptionHandler(){
		Handler<Throwable> exceptionHandler  = (Throwable exception) -> {
			String message = "List buckets request ";
			String stackTraceStr = Throwables.getStackTraceAsString(exception);
			logger.error("Error {} {}", message, exception);
			logger.error(stackTraceStr);
			this.commandDoneFuture.fail(message + " " + stackTraceStr);
			
		};
		return exceptionHandler;
	}
	


	
	
	
	
}
