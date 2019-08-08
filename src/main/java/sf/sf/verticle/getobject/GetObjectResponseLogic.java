package sf.sf.verticle.getobject;

import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.Pump;
import sf.sf.Constants;
/**
 * This class contains the logic that handles the response for an AWS API GetObject request.
 * It is used in {@link sf.sf.verticle.getobject.GetObjectVerticle}.
 * 
 * @author ari
 *
 */
public class GetObjectResponseLogic {
	private static final Logger logger = LogManager.getLogger(GetObjectResponseLogic.class);
	private JsonObject config ;
	private Vertx vertx;
	private GetObjectFutureWrapper completeFuture;

	public GetObjectResponseLogic(JsonObject config, Vertx vertx,
			GetObjectFutureWrapper completeFuture){
		this.config = config;
		this.vertx = vertx;
		this.completeFuture = completeFuture;
	}
	
	/**
	 * Make a string from the headers that were contained in the response.
	 * 
	 * @return the string concatenation result.
	 */
	private String makeHeaderDebugStr(MultiMap headers){
		StringBuilder str = new StringBuilder();
		for(Entry<String, String> entry : headers.entries()){
			str.append(" , " + entry.getKey() + " : "+entry.getValue());
		}
		return str.toString();
	}

	/**
	 * This is the main entrypoint into the the response logic class. Used by the 
	 * GetObjectVerticle start method.
	 *  
	 * @return the handler for the GetObject response.
	 */
	public Handler<HttpClientResponse> makeResponseHandler() {
		Handler<HttpClientResponse> handler = (HttpClientResponse response) -> {

			logger.debug("Headers for "+this.getPath() +
					" " + this.makeHeaderDebugStr(response.headers()));

			int statusCode =  response.statusCode();
			if(statusCode == 200){
				this.downloadFile(response);
			}else{ //aws s3 returned key missing or other error
				logger.error("NON 200 repsonse status code for "+this.getPath());
				this.debugErrorXml(response);
				this.completeFuture.failVerticle(
						" Error xml was returned from s3 for getobject request for path: " + this.getPath(),
						this.getPath(),
						new Throwable());
			}

		};

		return handler;

	}

	private void debugErrorXml(HttpClientResponse response){
		response.bodyHandler((Buffer totalBuffer) -> {

			String xmlString = totalBuffer.toString();

			logger.debug("XML Error returned from s3 for getObject request:");
			logger.debug(xmlString);

		});
	}

	private void downloadFile(HttpClientResponse response){
		response.pause();  //You have to pause before setting up the files. This is essential.
		
		logger.debug("After pause for "+this.getPath());
		this.setExceptionHandler(response);

		FileSystem fileSystem = vertx.fileSystem();

		String path = this.getPath();

		// http://vertx.io/docs/apidocs/io/vertx/core/file/FileSystem.html#writeFile-java.lang.String-io.vertx.core.buffer.Buffer-io.vertx.core.Handler-

		int indexOfLastSeperator = path.lastIndexOf('/');

		if (indexOfLastSeperator > 0) { 
			// in this case the file is just a dirctory or more than one directory 
			// level deep
			this.createBottomLevelFile(fileSystem, path, indexOfLastSeperator, response);
		} else {
			//if the file is in the top level dir make the file without making dirs 
			this.pumpFile(fileSystem, path, response);
		}
	}

	private void setExceptionHandler(HttpClientResponse response) {
		response.exceptionHandler(exception -> {  
			this.completeFuture.failVerticle(
					"Failed reading response, received repsonse exception for "+this.getPath() ,
					this.getPath(), exception);
		});
	}

	/**
	 * Creates a file or directory that were not in the top level of the path we are requesting
	 * 
	 * @param fileSystem
	 * @param path
	 * @param indexOfLastSeperator
	 * @param response
	 */
	private void createBottomLevelFile(FileSystem fileSystem,
			String path, int indexOfLastSeperator, HttpClientResponse response) {
		
		fileSystem.mkdirs("downloaded/" + path.substring(0, indexOfLastSeperator), //Make the path to the file
				(AsyncResult<Void> result) -> {
					if (result.succeeded()) {
						if(indexOfLastSeperator < path.length()-1){ //there is a file, not just a directory	
							this.pumpFile(fileSystem, path, response);
						}else{ //It is just a path
							this.directoryCompleted(response, path);
						}
					} else {
						this.completeFuture.failVerticle(" Failed making directories for path: " + path,
								this.getPath(), result.cause());
					}
				});
	}

	/**
	 * Finished making a directory. 
	 * In this case the object we are getting is just a directory.
	 * 
	 * @param response
	 * @param path
	 */
	private void directoryCompleted(HttpClientResponse response, String path) {
		response.endHandler((Void event) ->{
			this.completeFuture.succeedVerticle(
					this.config.getString(Constants.OBJECT_DB_STRING_CONFIG_KEY));
		});
		response.resume();

		logger.debug("done making dir " +path);
	}

	/**
	 * Uses vertx AsyncFile and Pump to download a file.
	 * 
	 * @param fileSystem
	 * @param path
	 * @param response
	 */
	private void pumpFile(FileSystem fileSystem, String path, HttpClientResponse response) {
		
		OpenOptions options = new OpenOptions();
		options.setTruncateExisting(true);
		
		// download to the "downloaded/" directory by default
		fileSystem.open("downloaded/" + path, options, result -> {
			if (result.succeeded()) {
				AsyncFile file = result.result();

				response.endHandler(this.makeHttpResponseEndHandler(file, path));

				logger.debug("Starting pump for for "+this.getPath());

				Pump.pump(response, file).start();

				//response was paused before setting up files
				response.resume();

			} else {
				this.completeFuture.failVerticle(
						" Failed opening asyncFile: " + path, path, result.cause());
			}

		});
	}

	private String getPath(){
		// Use config passed to vertx verticle config method
		String bucket = this.config.getString(Constants.BUCKET_CONFIG_KEY);
		String key="";
		if(this.config.containsKey(Constants.OBJECT_KEY_CONFIG_KEY)
				&& this.config.getString(Constants.OBJECT_KEY_CONFIG_KEY) != null){

			key=this.config.getString(Constants.OBJECT_KEY_CONFIG_KEY);
		}
		String path = bucket + "/"+ key;
		return path;
	}
	
	/**
	 * Make end handler for GetObject request.
	 * 
	 * @param file
	 * @param filePath
	 * @return
	 */
	private Handler<Void> makeHttpResponseEndHandler(AsyncFile file, String filePath){
		Handler<Void> responseEndHandler = (Void event) ->{
			file.flush().close(
					(AsyncResult<Void> asyncResult) -> {
						if(asyncResult.succeeded()){
							logger.debug("calling complete future complete() for"
									+ " getobject because file is flushed and closed " + filePath);
							this.completeFuture.succeedVerticle(
									this.config.getString(Constants.OBJECT_DB_STRING_CONFIG_KEY));
						}else{
							this.completeFuture.failVerticle(
									" Failed flushing and closing asyncFile: " + filePath, filePath, 
									asyncResult.cause());
						}
					});	
		};

		return responseEndHandler;

	}


}
