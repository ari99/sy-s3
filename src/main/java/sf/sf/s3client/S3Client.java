package sf.sf.s3client;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import sf.sf.CliOptions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Strings;

/**
 * Create AWS API Http GET requests.
 * 
 * @author ari
 *
 */
public class S3Client {
	private static final Logger logger = LogManager.getLogger(S3Client.class);
	public static final String DEFAULT_ENDPOINT = "0.0.0.0";//"s3-us-west-1.amazonaws.com";
	public static final int DEFAULT_PORT = 8000;

	private static String awsAccessKey;
	private static String awsSecretKey;
	private static String awsSessionToken;
	private static String endpoint; 
	private static Integer port;
	// DO NOT SHARE HTTPCLIENT BTW VERTICLES, YOU WILL GET THIS WARNING
	// WARNING: Reusing a connection with a different context: 
	//    an HttpClient is probably shared between different Verticles
	// Related git issue:
	// https://github.com/eclipse/vert.x/issues/1248
	private HttpClient client;
	private static Vertx vertx;
	private static Boolean useAuth;
	private static Boolean useSsl;
	
	public static void setup(Vertx vertx, CliOptions cliOptions){
		S3Client.vertx = vertx;
		S3Client.useAuth = cliOptions.getUseAuth();
		S3Client.useSsl = cliOptions.getUseSsl();
		S3Client.awsAccessKey = cliOptions.getAccessKey(); 
		S3Client.awsSecretKey = cliOptions.getSecretKey(); 
		String endpoint = cliOptions.getEndpoint();
		Integer port = cliOptions.getPort(); 

		S3Client.awsSessionToken = null; // session token is used for the x-amz-security-token header. 

		if(port == null){
			port = DEFAULT_PORT;
		}
		if(endpoint == null){
			endpoint = DEFAULT_ENDPOINT;
		}

		S3Client.port = port;
		S3Client.endpoint = endpoint;
	}

	public S3Client(){
		//Must create new http client per verticle
		logger.debug("Creating new http client with endpoint "+ endpoint + " and port " + port);

		HttpClientOptions options = new HttpClientOptions().
				setDefaultHost(endpoint).setDefaultPort(port)
				//default is true: .setKeepAlive(true)
				.setLogActivity(false);
		
		if (S3Client.useSsl == true) {
			options.setSsl(true).setTrustAll(true);
		}
		
		this.client = vertx.createHttpClient(options); 
	}
	
	/**
	 * 
	 * Creates vertx HTTP Get request
	 * 
	 */
	protected HttpClientRequest createGetRequest(String bucket,
			String key, String params,
			Handler<HttpClientResponse> responseHandler,
			Handler<Throwable> exceptionHandler) {


		String path = this.buildPath(bucket, key, params);

		logger.debug("HTTP GET request for list or object : "  + path);

		HttpClientRequest httpRequest = client.get(path, responseHandler);
		//httpRequest.setTimeout(60000);//this didnt fix the hanging connections problem
		httpRequest.exceptionHandler(exceptionHandler);
		
		return this.addAuth("GET",
				bucket, key,
				httpRequest);
	}
	
	/**
	 * 
	 * Create path to resource.
	 * 
	 */
	private String buildPath(String bucket, String key, String params){
		StringBuffer buff = new StringBuffer();
		if(!Strings.isNullOrEmpty(bucket)){
			buff.append("/");
			buff.append(bucket);
			buff.append("/");
		}

		if(!Strings.isNullOrEmpty(key)){
			buff.append(key);  
		}
		if(!Strings.isNullOrEmpty(params)){
			buff.append("?");

			buff.append(params);  
		}
		
		return buff.toString();

	}
	
	/**
	 *  Add authentication header to HttpClientRequest.
	 *  
	 * @return HttpClientRequest with auth header added.
	 */
	public HttpClientRequest addAuth(
			String method,
			String bucket, String key,
			HttpClientRequest request){
		
		S3AuthModel authModel = new S3AuthModel(method, bucket, key, awsSessionToken);
		S3AuthMessage authMessage = new S3AuthMessage(authModel);
		S3Auth s3Auth = new S3Auth(authMessage, S3Client.awsAccessKey, S3Client.awsSecretKey, S3Client.useAuth);
		RequestAuth requestAuth = new RequestAuth(s3Auth, authModel);
		return requestAuth.initAuthenticationHeader(request);

	}


	public void close() {
		this.client.close();
	}

}
