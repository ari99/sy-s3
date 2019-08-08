package sf.sf.s3client;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * 
 * Auth class originally based on https://github.com/spartango/SuperS3t. Heavily refactored.
 * Performs HMAC encryption.
 *
 */
public class S3Auth {
	private static final Logger logger = LogManager.getLogger(S3Auth.class);
	
	// Used for authentication(which may be optional depending on the bucket)
	private String awsAccessKey;
	private String awsSecretKey;

	private boolean useAuth;
	private S3AuthMessage authMessage;
	
	public S3Auth(
			S3AuthMessage authMessage,
			String awsAccessKey,
			String awsSecretKey,
			boolean useAuth)
	{

		this.awsAccessKey = awsAccessKey;
		this.awsSecretKey = awsSecretKey;
		this.useAuth = useAuth;
		this.authMessage = authMessage;
	}
	

	public String getAuthorization(){

		String signature;
		try {
			
			signature = b64SignHmacSha1(awsSecretKey, this.authMessage.makeMessageToSign());
		
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			signature = "ERRORSIGNATURE";
			logger.error("Failed to sign S3 request due to {} " , e);
		}

		String authorization = "AWS" + " " + this.awsAccessKey + ":" + signature;

		return authorization;
	}

	private static String b64SignHmacSha1(String awsSecretKey, String canonicalString) 
			throws NoSuchAlgorithmException,InvalidKeyException {
		SecretKeySpec signingKey = new SecretKeySpec(awsSecretKey.getBytes(),
				"HmacSHA1");
		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(signingKey);
		return new String(Base64.getEncoder().encode(mac.doFinal(canonicalString.getBytes())));
	}


	public boolean isAuthenticated() {
		return awsAccessKey != null && awsSecretKey != null && this.useAuth;
	}

	public void setAwsAccessKey(String awsAccessKey) {
		this.awsAccessKey = awsAccessKey;
	}

	public void setAwsSecretKey(String awsSecretKey) {
		this.awsSecretKey = awsSecretKey;
	}

	public String getAwsSecretKey(){
		return this.awsSecretKey;
	}
	
	public String getAwsAccessKey(){
		return this.awsAccessKey;
	}

}
