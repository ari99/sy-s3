package sf.sf.verticle.getobject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Throwables;

import io.vertx.core.Future;
import sf.sf.storage.DbLogic;
import sf.sf.storage.SetLogic;

/**
 * Objects of this class are used to track when GetObject verticles are complete. Normally
 * there is a commandDoneFuture, but because multiple GetObject verticles are created for a GetObjects command
 * therefore this is how we track each verticle.
 * 
 * @author ari
 *
 */
public class GetObjectFutureWrapper {
	private static final Logger logger = LogManager.getLogger(GetObjectFutureWrapper.class);
	private Future<Void> completeFuture;
	private static final String objectsToDlDebugName = "objectsToDl";
	
	public GetObjectFutureWrapper(Future<Void> completeFuture){
		this.completeFuture = completeFuture;
	}
	
	public boolean isComplete(){
		return this.completeFuture.isComplete();
	}
	
	public boolean failed(){
		return this.completeFuture.failed();
	}
	
	public boolean succeeded(){
		return this.completeFuture.succeeded();
	}
	
	public void succeedVerticle(String dbObjectString){	
		if(dbObjectString!=null){ //will be null for getsingleobject command
			SetLogic.addIntoObjectsSet(DbLogic.getDledObjects(), dbObjectString);
			SetLogic.removeFromObjectsSet(objectsToDlDebugName, DbLogic.getObjectsToDl(), dbObjectString);
		}
		
		this.completeFuture.complete();
	}
	
	public void failVerticle(String message, String path){
		this.failVerticle(message, path, new Throwable());
	}
	
	public Future<Void> getFuture(){
		return this.completeFuture;
	}
	
	public void failVerticle(String message, String path, Throwable cause) {
		logger.error("------------START VERTICLE FAILURE :"+path+"--------------");
		String stackTraceStr = Throwables.getStackTraceAsString(cause);
		
		//All the verticles completed by the complete-all-timeout will throw another exception
		//about error reading response , connection closed.
		if(this.completeFuture.isComplete()){
			logger.debug(path + " ;Calling fail on future that is already complete because of "+message);
			logger.error(path + " ;Fail future call on ALREADY complete future: {} {}", message, cause);
			logger.error(path + " ;Fail future call on ALREADY complete future stack trace: "+stackTraceStr);
		}else{
			logger.error(path + " ;Fail future call on NOT complete future: {} {}", message, cause);
			logger.error(path + " ;Fail future call on NOT complete future stack trace: "+stackTraceStr);
			this.completeFuture.fail(path + " , "+message + " " + stackTraceStr);
		}
		logger.error("------------END VERTICLE FAILURE: "+path+"--------------");

	}
}
