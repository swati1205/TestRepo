package com.cognizant.IBM_Version_Status;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.urbancode.commons.httpcomponentsutil.HttpClientBuilder;


/**
* 
* Java utility to add status to  component versions in UDeploy through Rest API. The code parses from a json file the 
* components and their respective versions where the status needs to be added to.
*
* @author  Anamika Bhowmick,Cognizant GTO SEA Labs
* @version 1.0
*/

@SuppressWarnings("deprecation")
public class UpdateVersionStatus {
	
	
	private static org.apache.log4j.Logger logger= Logger.getLogger(UpdateVersionStatus.class);


    /**
    * 
    * This method adds the status to the component version through UDeploy REST API.
    *
    * @param null
    * @return null
    * @exception IOException is thrown when file is not found in the mentioned path.
    *
    */
	
	public static void main(String[] args) throws IOException {
		String username = getPropData("UDeploy_Username");
        String password = getPropData("Udeploy_Password");
        String baseUrl=getPropData("Udeploy_URL");
        String Versionstatus=getPropData("VersionStatus");
		

		  HttpClientBuilder clientBuilder = new HttpClientBuilder();
		  clientBuilder.setUsername(username);
		  clientBuilder.setPassword(password);

		  // for SSL enabled servers, accept all certificates
		  clientBuilder.setTrustAllCerts(true); 
		  DefaultHttpClient client = clientBuilder.buildClient();
		  
		  String fileName= getPropData("JSON_Filepath");
		  File jsonFile = new File(fileName);
		  String jsonString = FileUtils.readFileToString(jsonFile);
		  JSONObject obj = (JSONObject)JSONValue.parse(jsonString);
			JSONArray compArr= (JSONArray) obj.get("COMPONENTS");
			for(int i=0;i<compArr.size();i++){
				JSONObject obj1=(JSONObject)compArr.get(i);
				String ComponentName=obj1.get("COMPONENT_NAME").toString();
        		String VersionNo=obj1.get("COMPONENT_VERSION").toString(); 
				//System.out.println(ComponentName);
				
				 try {
					  
						HttpGet getReq= new HttpGet(new URI(baseUrl+"/cli/component"));
						HttpResponse response1= client.execute(getReq);
						BufferedReader br1 = new BufferedReader(new InputStreamReader(
			                    (response1.getEntity().getContent())));
						String output1;
			        	 while ((output1 = br1.readLine()) != null) {
			                 JSONArray comp = (JSONArray) JSONValue.parse(output1);
			                 for(int j=0;j<comp.size();j++){
			                	 JSONObject eachCompObj=(JSONObject)comp.get(j);
			                	 String CompName=eachCompObj.get("name").toString();
			                	 
			                	 if(CompName.equalsIgnoreCase(ComponentName)){
			                		logger.info("Component Name: "+ComponentName);
			                		logger.info("Component Version: "+VersionNo);
			                		HttpPut putReq = new HttpPut(new URI(baseUrl+"/cli/version/addStatus?component="+ComponentName+"&version="+VersionNo+"&status="+Versionstatus));
			 			            putReq.addHeader("Accept", "application/json");
			 			            HttpResponse response = client.execute(putReq);
			 			            //logger.info(response.getStatusLine().getStatusCode()+" "+response.getStatusLine().getReasonPhrase());
			 			            
			 			            String output = null;
			 			            if(response.getStatusLine().getStatusCode()!=204){
			 			            	BufferedReader br = new BufferedReader(new InputStreamReader(
			 			                        (response.getEntity().getContent())));
			 			            	 while ((output = br.readLine()) != null) {
			 			            		 logger.info(output);
			 			            }
			 			             
			 			            }else{
			 			            	logger.info("The server has fulfilled the request. There is no additional content to send in the response.");
			 			            	logger.info("The status has been set to "+Versionstatus+" for component "+ComponentName+" for the version number :"+VersionNo);
			 			            }
			 					  }
			                	 }
			                 }
			        	 }catch (URISyntaxException e) {
			        		    logger.error(e);
		 					  }
						  
					    

			}
		 
	}
	

	
	
	
	/**
	    * getPropData method - Load the IBM_details Properties file     
	    * @param  properties FileName and properties data  
	    * @throws IOException 
	    * @throws FileNotFoundException 
	    */ 
	
	private static String getPropData(String text) throws IOException {
		// using properties class to load the properties file
		Properties properties = new Properties();
		properties.load(new FileInputStream("IBM_details.properties"));
		try {
			String propText = null;
			if (properties.getProperty(text) != null) {
				propText = properties.getProperty(text);
				//logger.info( text+" data is "+propText);


			} else {

				logger.debug("No data available in properties file for" + text);
			}
		} catch (Exception ex) {
			logger.error("Exception occured:", ex);
		}
		return properties.getProperty(text);
	}

	}
	
	

