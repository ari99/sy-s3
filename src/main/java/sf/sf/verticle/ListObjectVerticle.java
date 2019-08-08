package sf.sf.verticle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClientResponse;

/**
 * Abstract superclass for the ListObjectVerticle classes.
 * 
 * @author ari
 *
 */
public abstract class ListObjectVerticle extends AbstractVerticle {
	private static final Logger logger = LogManager.getLogger(ListObjectVerticle.class);

	protected Future<String> commandDoneFuture;

	public ListObjectVerticle( Future<String> commandDoneFuture){
		this.commandDoneFuture = commandDoneFuture;
	}
	
	
	/** 
	 * Create handler for the Http response.
	 * 
	 * List response handler doesn't need to have a future know when the command
	 *  is complete, because one is used in the event bus consumer
	 */
	protected Handler<HttpClientResponse> makeListResponseHandler() {

		Handler<HttpClientResponse> handler = (HttpClientResponse response) -> {
			
			// The body handler is called once when all the body has been
			// received:
			response.bodyHandler((Buffer totalBuffer) -> {
				
				this.handleResponse(totalBuffer);

			});

		};

		return handler;
	}
	
	/**
	 * Send the response to the event bus to be handled by the consumers listening on it
	 * {@link sf.sf.bin.RequesterSaver#setupEventBus}
	 * 
	 * @param totalBuffer the content of the API response.
	 */
	public void handleResponse(Buffer totalBuffer) {
		String response = totalBuffer.toString();
		EventBus eventBus = vertx.eventBus();
		
		//Publish the response string to the event bus
		eventBus.publish(this.getListObjectsEventBus(), response); 
	}
	
	
	public abstract String getListObjectsEventBus();
}
