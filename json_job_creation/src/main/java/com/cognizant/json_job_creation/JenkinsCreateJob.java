package com.cognizant.json_job_creation;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;



public class JenkinsCreateJob {

	//Initialize the variable and logger 
    
    private static org.apache.log4j.Logger logger= Logger.getLogger(JenkinsCreateJob.class);
  

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String selectedRepo= args[0];
		String selectedComp= args[1];
		List<String> complist = new ArrayList<String>(Arrays.asList(selectedComp.split(",")));
		logger.info("Selected repo is: "+selectedRepo);
		logger.info("Selected components are: "+complist);
		List<String> list = new ArrayList<String>();
		//String filePath="D:\\TestSelfService\\repo_json.txt";
		String filePath = getPropData("Json_filePath");
		//System.out.println(filePath);
		File jsonFile = new File(filePath);
		String jsonString = FileUtils.readFileToString(jsonFile);
		
		JSONObject obj = (JSONObject)JSONValue.parse(jsonString);
		JSONObject repoObj= (JSONObject) obj.get("REPO_NAMES");
		logger.info(repoObj);
		JSONArray repoArr=(JSONArray) repoObj.get("svn");
		//System.out.println(repoArr);
		//System.out.println(list);
		for(int i=0;i<repoArr.size();i++){
			JSONObject repository=(JSONObject)repoArr.get(i);
			
			String repo_Name=(String) repository.get("name");
			//System.out.println(repo_Name);
			if(repo_Name.equalsIgnoreCase(selectedRepo))
			{
				list.add(repo_Name);
			}
			
			
		}
		
		/*JSONObject compObj =(JSONObject) obj.get("COMPONENT");
		System.out.println(compObj);
		for(int k=0;k<list.size();k++){
		JSONArray compArr=(JSONArray) compObj.get(list.get(k));
		String repo_Name=list.get(k).toString();
		//System.out.println(compArr);
			//String repo_Name=list.get(k).toString();
		for(int j=0;j<compArr.size();j++){
		  JSONObject jrepo=(JSONObject) compArr.get(j);
		  String component_name= (String)jrepo.get("name");
		  System.out.println(component_name);
			
	    //sendPost(repo_Name,component_name);
		}
		}*/
		
		JSONObject compTypeObj= (JSONObject) obj.get("COMPONENT_TYPE");
		for(int k=0;k<list.size();k++){
			JSONArray compTypeArr=(JSONArray) compTypeObj.get(list.get(k));
			String repo_Name=list.get(k).toString();
			//System.out.println(compTypeArr);
			for(int j=0;j<compTypeArr.size();j++){
				JSONObject jrepo1=(JSONObject)compTypeArr.get(j);
				String component_name= (String)jrepo1.get("name");
				String component_type=(String)jrepo1.get("type");
				//System.out.println(component_type);
				
				//send post request to the component type job
				if(component_type.equalsIgnoreCase("jar"))
				{
					String jobURL=getPropData("Jar_job_url");
					if(complist.contains(component_name))
					{
						sendPost(repo_Name,component_name,component_type,jobURL);
					}
				}
				else if(component_type.equalsIgnoreCase("war"))
				{
					String jobURL=getPropData("War_job_url");
					if(complist.contains(component_name))
					{
						sendPost(repo_Name,component_name,component_type,jobURL);
					}
				}
				else if(component_type.equalsIgnoreCase("ear"))
				{
					String jobURL=getPropData("Ear_job_url");
					if(complist.contains(component_name))
					{
						sendPost(repo_Name,component_name,component_type,jobURL);
					}
				}				
			}
		}
	

	}
	
	public static void sendPost(String repoName,String componentName,String componentType,String jobUrl) throws Exception     
		{
  		
  try {
        String urlParameters = "Repository_Name="+repoName+"&Component_Name="+componentName+"&Component_Type="+componentType;
        logger.info("Build Parameters:"+urlParameters);
        //String jobUrl = getPropData("Jar_job_url");
        //String jobUrl="http://10.219.193.72:8080/job/Jar_Project_Creation/buildWithParameters?";
        URL url = new URL(jobUrl);
        String username = getPropData("Jenkins_username");
        String password = getPropData("Jenkins_password");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //logger.info("Connecting to Cloudset");
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
			logger.info("Response Code :"+responseCode);
			logger.info("Cloudset Job Created Successfully");
		}
		else{
			logger.warn("Cloudset Job Creation Failed,Response Code :"+responseCode);
			
			}
  	}
		
  catch (Exception ex) {
   		 logger.error("Exception occured:", ex);
			}
			
 }
	
	/*
	 Method to get property data from property file
	 */
	private static String getPropData(String text) throws IOException {
		// using properties class to load the properties file
		Properties properties = new Properties();
		properties.load(new FileInputStream("JenkinsProperties.properties"));
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
