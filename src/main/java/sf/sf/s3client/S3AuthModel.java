package sf.sf.s3client;

import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * 
 * Holds vars for S3 authentication and helper methods.
 * 
 * @author ari
 *
 */
public class S3AuthModel {
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

	private String method;
	private String key;
	private String bucket;
	private String xamzdate = currentDateString();
	private String awsSessionToken;

	// These are optional--
	private String contentMd5;
	private String contentType;
		
	public S3AuthModel(String method,
			String bucket,
			String key,
			String awsSessionToken
			) {
		this(method, bucket, key, awsSessionToken, "", "");
	}

	public S3AuthModel(String method,
			String bucket,
			String key,
			String awsSessionToken,
			String contentMd5,
			String contentType) {
		this.method = method;
		this.bucket = bucket;
		this.key = key;
		this.awsSessionToken = awsSessionToken;
		this.contentMd5 = contentMd5;
		this.contentType = contentType;
	}
	public boolean isSessionTokenBlank() {
		return awsSessionToken == null || awsSessionToken.trim().length() == 0;
	}
	public static String currentDateString() {
		return dateFormat.format(new Date());
	}
	public String getMethod() {
		return this.method;
	}
	
	public String getBucket() {
		return this.bucket;
	}
	
	public String getKey() {
		return this.key;
	}
	
	public String getAwsSessionToken() {
		return this.awsSessionToken;
	}
	
	public String getContentMd5() {
		return this.contentMd5;
	}
	
	public String getContentType() {
		return this.contentType;
	}
	
	public String getXamzDate() {
		return this.xamzdate;
	}
}
