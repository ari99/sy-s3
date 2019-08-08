package sf.sf.s3client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;

import sf.sf.s3client.util.ParamFormater;

/**
 * Creates the vertx http requests and the associated configs for them.
 * 
 * @author ari
 *
 */
public class S3ClientOperations {
	private static final Logger logger = LogManager.getLogger(S3ClientOperations.class);
	
	private S3Client client;
	
	//This talks about using the bucket in the path vs subdomain:
	//http://docs.aws.amazon.com/AmazonS3/latest/dev/VirtualHosting.html#VirtualHostingSpecifyBucket
	private Handler<HttpClientResponse> responseHandler;
	private Handler<Throwable> requestExceptionHandler;
	public S3ClientOperations(
			Handler<HttpClientResponse> responseHandler,
			 Handler<Throwable> requestExceptionHandler
			){

		client = new S3Client();
		this.responseHandler = responseHandler;
		this.requestExceptionHandler = requestExceptionHandler;
	}
	
	
	
	
	/**
	 * Used by GetObjects and GetSingleObject commands.
	 * http://docs.aws.amazon.com/AmazonS3/latest/API/RESTObjectGET.html
	 * 
	 */
	public HttpClientRequest getObjectRequest(GetObjectRequest request){
		
		return this.client.createGetRequest(request.getBucketName(), request.getKey(), null,
				this.responseHandler, this.requestExceptionHandler);
	}
	
	
	/**
	 * Used by ListBuckets command.
	 * 
	 */
	public HttpClientRequest getListBucketsRequest(){
		
		return this.client.createGetRequest(null, null, null,
				this.responseHandler, this.requestExceptionHandler);
						
	}
	
    
	/**
	 * This is used by ListObjectsV1 command.
	 * http://docs.aws.amazon.com/AmazonS3/latest/API/RESTBucketGET.html
	 * 
	 * @return vertx Http Request
	 * 
	 */
    public HttpClientRequest getBucketListObjectsRequestV1(ListObjectsRequest request){
    	
    	ParamFormater f = new ParamFormater();
    	
    	f.addParam("max-keys",request.getMaxKeys());
    	f.addParam("marker", request.getMarker());
    	f.addParam("prefix", request.getPrefix());
    	f.addParam("delimiter", request.getDelimiter());
    	String params = f.getUrlParams();
    
    	return  this.client.createGetRequest(request.getBucketName(), null, params,
    						this.responseHandler, this.requestExceptionHandler);
    }
    
    
    /**
     * Used by ListObjectsV2 command.
     * 
     * http://docs.aws.amazon.com/AmazonS3/latest/API/v2-RESTBucketGET.html
     */
    public HttpClientRequest getBucketListObjectsRequestV2(ListObjectsV2Request request){
    	ParamFormater f = new ParamFormater();
    	
    	f.addParam("max-keys", request.getMaxKeys());
    	f.addParam("list-type", 2);
    	f.addParam("continuation-token", request.getContinuationToken());
    	f.addParam("start-after", request.getStartAfter());
    	f.addParam("prefix", request.getPrefix());
    	f.addParam("delimiter", request.getDelimiter());
    	String params = f.getUrlParams();

    	return  this.client.createGetRequest(request.getBucketName(), null, params,
    			this.responseHandler, this.requestExceptionHandler);
    }
}
