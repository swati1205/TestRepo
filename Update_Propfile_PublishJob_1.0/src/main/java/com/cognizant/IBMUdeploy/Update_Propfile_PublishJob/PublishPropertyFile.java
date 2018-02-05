package com.cognizant.IBMUdeploy.Update_Propfile_PublishJob;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.TreeSet;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
* 
* Java utility to publish updated property file to IBM UDeploy through REST API in a new version.This java client sends a
* post request to Cloudset job with the build parameters parsed from json file.
*
* @author  Anamika Bhowmick
* @version 1.0
*/ 
public class PublishPropertyFile {


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String username=args[0];
		String password=args[1];
		String basePath=args[2];
		String jsonPath=args[3];
		String jobUrl=args[4];
		
		try {
			PublishPropertyFile.jsonParser(username, password,basePath, jsonPath,jobUrl);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
/**
 * 
 * This method parses json file and makes a call to the sendPost method to start a Cloudset job, which is a parameterized job
 * whose parameters come from the json file.
 * 
 *  @param parameters required for sending post request to Cloudset job
 *  @return null
 *  @throws IOException
 *  
		    
*/
	private static void jsonParser(String username,String password,String basePath,String jsonFilePath,String jobUrl) throws IOException{
		HashSet<String> set = new HashSet<String>();
		File jsonFile = new File(jsonFilePath);
		String jsonString = FileUtils.readFileToString(jsonFile);
		JSONObject obj = (JSONObject)JSONValue.parse(jsonString);
		String versionNo =obj.get("Realease_Version").toString();
	    JSONArray propArr= (JSONArray) obj.get("Property_File");
	    for(int i=0;i<propArr.size();i++){
	    	JSONObject obj1=(JSONObject)propArr.get(i);
			//String componentName=obj1.get("File").toString()+".properties";
			String component=obj1.get("File").toString();
			String[] arr=component.split("\\.");
			//String fileName=arr[arr.length-1]+".properties";
  		   // String versionNo=obj1.get("Realease_Version").toString();
  		    String dir=basePath+"\\"+component+"\\"+versionNo;
  		    set.add(component+"+"+dir+"+"+versionNo);
  		    
	    }
	    for (String s : set) {
		      String[] arr=s.split("\\+");
		      //System.out.println(arr[0]);
		      //System.out.println(arr[1]);
		      //System.out.println("Version: "+arr[2]);
		      PublishPropertyFile.sendPost(arr[0],arr[1],arr[2],jobUrl,username,password);
		  }
	}

/**
* 
* This method sends a post request to Cloudset job, which is a parameterized job.
* 
*  @param parameters required for sending post request to Cloudset job
*  @return null
*  
*  
*	    
*/
	private static void sendPost(String componentName, String dir,
			String versionNo,String jobUrl,String username,String password) {
		// TODO Auto-generated method stub
		try{
		String urlParameters ="COMPONENT_NAME="+componentName+"&BASE_COMPONENT_PATH="+dir+"&VERSION="+versionNo;
		System.out.println(urlParameters);
		URL url = new URL(jobUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        System.out.println("Connecting to Cloudset");
        // add request header
        conn.setConnectTimeout(50000);
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        //Cloudset user authentication
        conn.setRequestProperty(
        		"Authorization",
        		"Basic "
        		+ (new String(Base64.encodeBase64((username+":"+password)
        		.getBytes()))));
    
        // send POST request
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
		
		int responseCode = conn.getResponseCode();
		if (responseCode==201){
			System.out.println("Response Code :"+responseCode);
			System.out.println("Build Triggered");
		}
		else{
			System.out.println("Cloudset Build Trigger Failed,Response Code :"+responseCode);
			
			}
	  	}
		
	  catch (Exception ex) {
		  ex.printStackTrace();
			}
			
	}

}
