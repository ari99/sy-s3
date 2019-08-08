package sf.sf.s3client;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.http.HttpClientRequest;
/**
 * 
 * Adds an auth headers to a vertx HttpClientRequest.
 *
 */
public class RequestAuth {

	private static final Logger logger = LogManager.getLogger(RequestAuth.class);
    private S3Auth s3Auth;
    private S3AuthModel authModel;
    public RequestAuth(S3Auth s3Auth, S3AuthModel authModel){
    	this.s3Auth = s3Auth;
    	this.authModel = authModel;
    }
    
    public HttpClientRequest initAuthenticationHeader(HttpClientRequest request) {
        if (this.s3Auth.isAuthenticated()) {
            // Calculate the signature
            // http://docs.amazonwebservices.com/AmazonS3/latest/dev/RESTAuthentication.html#ConstructingTheAuthenticationHeader

            // Date should look like Thu, 17 Nov 2005 18:49:58 GMT, and must be
            // within 15 min of S3 server time. contentMd5 and type are optional

            String xamzdate = this.authModel.getXamzDate();
            request.headers().add("X-Amz-Date", xamzdate);
         
            if (!this.authModel.isSessionTokenBlank()) {
                request.headers().add("X-Amz-Security-Token", this.authModel.getAwsSessionToken());
            }
            
            String authorization = this.s3Auth.getAuthorization();

           request.headers().add("Authorization", authorization);
        }else{
        	logger.debug("Not adding auth headers");
        }
        
        return request;
    }
    
    
}
