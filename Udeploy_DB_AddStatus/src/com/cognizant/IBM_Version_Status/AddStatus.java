package com.cognizant.IBM_Version_Status;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

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

@SuppressWarnings("deprecation")
public class AddStatus {

	//private static org.apache.log4j.Logger logger= Logger.getLogger(UpdateVersionStatus.class);


	
	public static void main(String[] args) throws IOException {
		
		// suppress log4j messages from UCD library
		Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		
		String username = args[0];
        String password = args[1];
        String baseUrl=args[2];
        String Versionstatus=args[3];
        String CompName= args[4];
        String version=args[5];
        
        String[] result = Versionstatus.split(",");
        
        for(int i=0;i<result.length;i++){
        	String status=result[i].toString();
        	System.out.println(status);
        	addStatus(username,password,baseUrl,status,CompName,version);

        }
      
		  
	}
	

/**
    * 
    * This method adds the status to the component version through UDeploy REST API.
    *
    * @param null
    * @return null
    * @exception IOException is thrown when file is not found in the mentioned path.
    *
 */
	
	private static void addStatus(String username,String password,String baseUrl,String Versionstatus,String ComponentName,String VersionNo) throws IOException{
		HttpClientBuilder clientBuilder = new HttpClientBuilder();
		  clientBuilder.setUsername(username);
		  clientBuilder.setPassword(password);

		  // for SSL enabled servers, accept all certificates
		  clientBuilder.setTrustAllCerts(true); 
		  DefaultHttpClient client = clientBuilder.buildClient();
		  
		  
		 /* File jsonFile = new File(fileName);
		  String jsonString = FileUtils.readFileToString(jsonFile);
		  JSONObject obj = (JSONObject)JSONValue.parse(jsonString);
			JSONArray compArr= (JSONArray) obj.get("COMPONENTS");
			for(int i=0;i<compArr.size();i++){
				JSONObject obj1=(JSONObject)compArr.get(i);
				String ComponentName=obj1.get("component").toString();
      		String VersionNo=obj1.get("version").toString();*/ 
				
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
			                		System.out.println("Component Name: "+ComponentName);
			                		System.out.println("Component Version: "+VersionNo);
			                		HttpPut putReq = new HttpPut(new URI(baseUrl+"/cli/version/addStatus?component="+ComponentName+"&version="+VersionNo+"&status="+Versionstatus));
			 			            putReq.addHeader("Accept", "application/json");
			 			            HttpResponse response = client.execute(putReq);
			 			            //logger.info(response.getStatusLine().getStatusCode()+" "+response.getStatusLine().getReasonPhrase());
			 			            
			 			            String output = null;
			 			            if(response.getStatusLine().getStatusCode()!=204){
			 			            	BufferedReader br = new BufferedReader(new InputStreamReader(
			 			                        (response.getEntity().getContent())));
			 			            	 while ((output = br.readLine()) != null) {
			 			            		System.out.println(output);
			 			            }
			 			             
			 			            }else{
			 			            	System.out.println("The server has fulfilled the request. There is no additional content to send in the response.");
			 			            	System.out.println("The status has been set to "+Versionstatus+" for component "+ComponentName+" for the version number :"+VersionNo);
			 			            }
			 					  }
			                	 }
			                 }
			        	 }catch (URISyntaxException e) {
			        		 System.out.println(e);
		 					  }
						  
					    

			}
		 
	}
