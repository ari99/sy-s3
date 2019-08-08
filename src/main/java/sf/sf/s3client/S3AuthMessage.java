package sf.sf.s3client;

import java.util.StringJoiner;

import com.google.common.base.Strings;

/**
 * Created the message which will be Hmac'd for aws authentication.
 * 
 */
public class S3AuthMessage {
	private S3AuthModel authModel;
	
	public S3AuthMessage(S3AuthModel authModel) {
		this.authModel = authModel;
		
	}
	/**
	 * Created the message which will be Hmac'd for aws authentication.
	 * 
	 * @return message which will be Hmac'd
	 */
	public String makeMessageToSign(){
		String canonicalizedAmzHeaders = this.makeAmzHeaderMessage();
		String canonicalizedResource = this.makeResourceMessage();

		String toSign = this.authModel.getMethod()
				+ "\n"
				+ this.authModel.getContentMd5()
				+ "\n"
				+ this.authModel.getContentType()
				+ "\n\n" // Skipping the date, we'll use the x-amz
				// date instead
				+ canonicalizedAmzHeaders
				+ canonicalizedResource;

		return toSign;
	}
	
	private String makeResourceMessage() {
		String canonicalizedResource = "/";
		if(!Strings.isNullOrEmpty(this.authModel.getBucket())){
			canonicalizedResource += this.authModel.getBucket()+"/";
		}
		if(!Strings.isNullOrEmpty(this.authModel.getKey())){
			canonicalizedResource =canonicalizedResource +   this.authModel.getKey();
		}
		return canonicalizedResource;
	}

	private String makeAmzHeaderMessage() {
		final StringJoiner canonicalizedAmzHeadersBuilder = new StringJoiner("\n", "", "\n");
		canonicalizedAmzHeadersBuilder.add("x-amz-date:" + this.authModel.getXamzDate());
		if (!this.authModel.isSessionTokenBlank()) {
			canonicalizedAmzHeadersBuilder.add("x-amz-security-token:" + this.authModel.getAwsSessionToken());
		}

		String canonicalizedAmzHeaders = canonicalizedAmzHeadersBuilder.toString();

		return canonicalizedAmzHeaders;
	}
	


}
