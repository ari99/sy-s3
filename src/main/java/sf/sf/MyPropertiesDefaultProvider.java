package sf.sf;
import com.beust.jcommander.IDefaultProvider; 

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger; 
 
/**
 * A default provider for jCommander that reads its default values from a property file. 
 *  
 *  https://www.mkyong.com/java/java-properties-file-examples/
 */ 
public class MyPropertiesDefaultProvider implements IDefaultProvider { 
private static final Logger logger = LogManager.getLogger(MyPropertiesDefaultProvider.class);
  public static final String DEFAULT_FILE_NAME = "jc.properties"; 
  private Properties m_properties; 
 
  public MyPropertiesDefaultProvider() { 
    init(DEFAULT_FILE_NAME); 
  } 
 
  public MyPropertiesDefaultProvider(String fileName) { 
    init(fileName); 
  } 
 
  private void init(String fileName) { 
    try (InputStream input = new FileInputStream(fileName)){ 
      m_properties = new Properties(); 
      m_properties.load(input);  
    } catch (IOException e) {
    	logger.error("Error loading properties file " , e);
	} 
  } 
   
  public String getDefaultValueFor(String optionName) { 
    int index = 0; 
    while (index < optionName.length() && ! Character.isLetterOrDigit(optionName.charAt(index))) { 
      index++; 
    } 
    String key = optionName.substring(index); 
    return m_properties.getProperty(key); 
  } 
 
}
