package sf.sf.consumer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.transform.XmlResponsesSaxParser;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import sf.sf.CliOptions;
import sf.sf.command.ListObjectsV2Command;

/**
 * Consumes ListObjectsV2 messages from the vertx event bus.
 * @author user
 *
 */
public class ListObjectsV2ResponseConsumer extends ListObjectsConsumer {

	private static final Logger logger = LogManager.getLogger(ListObjectsV2ResponseConsumer.class);
	
	public ListObjectsV2ResponseConsumer(Vertx vertx,
			CliOptions cliOptions, Future<String> commandDoneFuture) {
		super(vertx, cliOptions, commandDoneFuture);
	}

	/**
	 * Handle the AWS API response.
	 * 
	 * @param listBucketResponseBody
	 */
	@Override
	protected void handleResponseBody(String listBucketResponseBody){
	
	    try {
	    	ListObjectsV2Result listing = this.convertXmlStringToListObjectsV2Result(listBucketResponseBody);
	    	
	    	this.saveResult(listing);
			  
		   if(listing.isTruncated() && this.cliOptions.numRequestsIsLessThanMaxRequestsListObjects()){
			  this.continueTruncatedResponse(listing);
		   }else{
			   this.succeedListObjects();
		   }
		 
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Create another ListObjectsV2Command because the previous request was truncated.
	 * 
	 * @param listing
	 */
	private void continueTruncatedResponse(ListObjectsV2Result listing) {
		 //For v2 continuationToken is the token that was sent for the current request. 
		   // To get where the next one should start, use nextContinuationToken.
		   String token = listing.getNextContinuationToken();
		   ListObjectsV2Command command = new ListObjectsV2Command(
				   cliOptions, vertx, this.commandDoneFuture);
		   logger.debug("launching verticle with continuation token {} ", token);
		   
		   command.launchVerticle(Optional.ofNullable(token));
	}
	
	/**
	 * Save ListObjectsV2Result into the db.
	 * 
	 * @param listing
	 */
	private void saveResult(ListObjectsV2Result listing) {
		  List<S3ObjectSummary> summarys = listing.getObjectSummaries();
    	  
		  this.handleObjectSummarys(summarys);
		  this.handleCommonPrefixes(listing.getCommonPrefixes(), listing.getBucketName());
	}
	
	protected ListObjectsV2Result convertXmlStringToListObjectsV2Result(String xmlString) throws IOException{
		 XmlResponsesSaxParser parser = new XmlResponsesSaxParser();
		 InputStream in = IOUtils.toInputStream(xmlString, "UTF-8");
		 XmlResponsesSaxParser.ListObjectsV2Handler lbhandler = parser.parseListObjectsV2Response(in, true);
		 ListObjectsV2Result listing = lbhandler.getResult();
		 return listing;		    				
	}
}
