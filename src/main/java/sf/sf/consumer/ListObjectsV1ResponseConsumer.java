package sf.sf.consumer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.transform.XmlResponsesSaxParser;
import com.amazonaws.services.s3.model.transform.XmlResponsesSaxParser.ListBucketHandler;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import sf.sf.command.ListObjectsV1Command;
import sf.sf.CliOptions;

/**
 * Consumes ListObjectsV1 messages from the event bus.
 * 
 * @author ari
 *
 */
public class ListObjectsV1ResponseConsumer extends ListObjectsConsumer  {
	private static final Logger logger = LogManager.getLogger(ListObjectsV1ResponseConsumer.class);


	public ListObjectsV1ResponseConsumer(Vertx vertx, 
			CliOptions cliOptions, Future<String> commandDoneFuture) {
		super(vertx, cliOptions, commandDoneFuture);
	}


	protected ObjectListing convertXmlStringToObjectListing(String xmlString) throws IOException{
		XmlResponsesSaxParser parser = new XmlResponsesSaxParser();
		InputStream in = IOUtils.toInputStream(xmlString, "UTF-8");
		
		// Apparently the AWS sdk doesn't have a parseListObjectsV1 method. Instead you get the objects
		// through parseListBuckets.
		ListBucketHandler lbhandler = parser.parseListBucketObjectsResponse(in, true);

		ObjectListing listing = lbhandler.getObjectListing();

		return listing;		    		

	}

		
	/**
	 * Handles the AWS API response body. Saves the result in the db.
	 * 
	 * @param responseBody
	 */
	@Override
	protected void handleResponseBody(String responseBody){
		try {
			ObjectListing listing = this.convertXmlStringToObjectListing(responseBody);
			
			this.saveObjectListing(listing);

			if(listing.isTruncated() && this.cliOptions.numRequestsIsLessThanMaxRequestsListObjects()){
				this.continueTruncatedResponse(listing);
			}else{
				this.succeedListObjects();
			}

		} catch (IOException e) {
			logger.error("Error in list objects response consumer" , e);
		}
	}
	
	/**
	 * 
	 * Save the summarys and prefixes from {@link com.amazonaws.services.s3.model.ObjectListing}
	 * 
	 * @param listing {@link com.amazonaws.services.s3.model.ObjectListing} formed from aws response.
	 */
	private void saveObjectListing(ObjectListing listing) {
		List<S3ObjectSummary> summarys = listing.getObjectSummaries();
		List<String> commonPrefixes = listing.getCommonPrefixes();

		//can a common prefix not be a next marker/continuation token?
		logger.debug("Object listing v1 summaries size: "+summarys.size()); 
		logger.debug("Object listing v1 prefixes size: "+commonPrefixes.size()); 

		this.handleObjectSummarys(summarys);		   
		this.handleCommonPrefixes(commonPrefixes, listing.getBucketName());
	}
	
	/**
	 * Launch ListObjectsV1Command verticle again with a nextMarker signifying where
	 * to continue truncated request.
	 * 
	 * @param listing {@link com.amazonaws.services.s3.model.ObjectListing formed from aws response.}
	 */
	private void continueTruncatedResponse(ObjectListing listing ) {
		String nextMarker = listing.getNextMarker();			   
		ListObjectsV1Command command = 
				new ListObjectsV1Command(this.cliOptions, vertx, this.commandDoneFuture);

		logger.debug("launching verticle with nextmarker {} ", nextMarker);
		command.launchVerticle(Optional.of(nextMarker));
	}
}
