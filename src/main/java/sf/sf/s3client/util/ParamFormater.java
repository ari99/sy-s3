package sf.sf.s3client.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import com.google.common.base.Strings;

/**
 * Used to create a URL parameter string from a list of string name/value pairs.
 * See: http://www.programcreek.com/java-api-examples/index.php?api=org.apache.http.client.utils.URLEncodedUtils
 *
 */
public class ParamFormater {
	List<NameValuePair> list = new ArrayList<NameValuePair>();
	
	public void addParam(String key, String value){
		if(!Strings.isNullOrEmpty(value)){
			list.add(new BasicNameValuePair(key, value));
		}
	}
	
	public void addParam(String key, Integer value){
		if(value != null){
			list.add(new BasicNameValuePair(key, Integer.toString(value)));
		}
	}
	
	public String getUrlParams(){
		return URLEncodedUtils.format(list, "UTF-8");
	}
}
