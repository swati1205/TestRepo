package com.cognizant.sonarqube.project.metricparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
* 
* Java utility to list the metric values of a project analysed in Sonarqube in a property file
* named "projectKey_sonarqubemetrics.properties",stored in a metric key=value format.
*
* @author  Cognizant GTO SEA Labs
* @version 1.0
*/

public class SonarQubeProjectMetricsParser {
	
	//Initialize the variable and logger 
	
	private static org.apache.log4j.Logger logger = Logger.getLogger(SonarQubeProjectMetricsParser.class);
	
/**
* getPropData method - Load the FolderStructure Properties file     
* 
* @param  properties FileName and properties data  
* @throws IOException 
* @throws FileNotFoundException 
*/ 
	
	 private static String getPropData(String text) throws IOException {
			// using properties class to load the properties file
			Properties properties = new Properties();
			properties.load(new FileInputStream("sonarqubedetails.properties"));
			try {
				String propText = null;
				if (properties.getProperty(text) != null) {
					propText = properties.getProperty(text);
					logger.info(text+" data is "+propText);


				} else {

					logger.debug("No data available in properties file for" + text);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				logger.error("Exception occured:", ex);
			}
			return properties.getProperty(text);
		}
	 
/**
* main method - takes two string arguments from user-project key and comma-separated list of metric keys and stores the values
* in a property file. 
* @throws JSONException 
* @throws FileNotFoundException 
* @throws MalformedURLException
* @throws IOException
*/ 

	public static void main(String[] args) throws JSONException {
		
		//create InputStream to load file
		
		FileInputStream in;
		
		try {
			
			//check if arguments are given otherwise throw exception
			if (args.length != 2) {
				logger.info("Argument syntax is <Project Key> <Metrics> ");
				throw new IllegalArgumentException("Expects 2 arguments");
			} 
			
			//first argument is project key
			String resource_key=args[0];
			logger.info(resource_key);
			
			//second argument is comma-separated list of metric keys
			String metrics=args[1];
			logger.info(metrics);
			
			//check for sonarqubemetrics.properties file,if it doesn't exist then create it
			File file=new File(resource_key+"_sonarqubemetrics.properties");
            if(!file.exists()){
         	   file.createNewFile();
            }
			in = new FileInputStream(resource_key+"_sonarqubemetrics.properties");
			Properties props = new Properties();
			props.load(in);
			in.close();
			
			
			//create an InputStream for jsonstring response from url
			InputStream input;
			
			//fetch query url from properties file
			String resourcesUrl = getPropData("Sonar_Rurl");
			
			//store the input string in jsonArray
			input = new URL(resourcesUrl+resource_key+"&metrics="+metrics+"&format=json").openStream();
			JSONArray jsonArray= new JSONArray(IOUtils.toString(input));
			logger.info(jsonArray);
			
			//parse the json array using for loop
			for(int i=0;i<jsonArray.length();i++){
				JSONObject obj=jsonArray.getJSONObject(i);
				//logger.info(obj);
				JSONArray metricArray=(JSONArray) obj.get("msr");
				String resourceName=obj.get("lname").toString();
				String resourceKey=obj.get("key").toString();
				logger.info(metricArray);
				//fetch the metric array from jsonArray and loop through it to get the required values
				for(int j=0;j<metricArray.length();j++){
					JSONObject metric=(JSONObject) metricArray.get(j);
					logger.info(metric);
					
					//create an output stream to write to the property file
					FileOutputStream out = new FileOutputStream(resource_key+"_sonarqubemetrics.properties");
					String key=(String) metric.get("key");
					String value=(String) metric.get("frmt_val");
					props.setProperty(key, value);
					props.store(out,resourceName+"_"+resourceKey);
					out.close();
				}
				
			}


		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (MalformedURLException e2) {
			e2.printStackTrace();
		} catch (IOException e3) {
			e3.printStackTrace();
		}
		
	}
	

}
