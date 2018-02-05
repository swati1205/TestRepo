package com.cognizant.Udepoy;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.urbancode.commons.httpcomponentsutil.HttpClientBuilder;

public class AddStatus {

	public static void main(String[] args) throws IOException, IllegalStateException, URISyntaxException, JSONException {
		// TODO Auto-generated method stub
		// suppress log4j messages from UCD library
				Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
				
				String username = args[0];
		        String password = args[1];
		        String baseUrl=args[2];
		        String Versionstatus=args[3];
		        String fileName= args[4];
		        
		        String[] result = Versionstatus.split(",");
		        
		        for(int i=0;i<result.length;i++){
		        	String status=result[i].toString();
		        	System.out.println(status);
		        	addStatus(username,password,baseUrl,status,fileName);

		        }

	}
	
	private static void addStatus(String username,String password,String baseUrl,String Versionstatus,String fileName) throws IOException, IllegalStateException, URISyntaxException, JSONException{
		HttpClientBuilder clientBuilder = new HttpClientBuilder();
		  clientBuilder.setUsername(username);
		  clientBuilder.setPassword(password);

		  // for SSL enabled servers, accept all certificates
		  clientBuilder.setTrustAllCerts(true); 
		  DefaultHttpClient client = clientBuilder.buildClient();
		  
		  
		  File jsonFile = new File(fileName);
		  String jsonString = FileUtils.readFileToString(jsonFile);
		  JSONObject obj = (JSONObject)JSONValue.parse(jsonString);
			JSONArray compArr= (JSONArray) obj.get("COMPONENTS");
			for(int i=0;i<compArr.size();i++){
				JSONObject obj1=(JSONObject)compArr.get(i);
				String ComponentName=obj1.get("component").toString();
      		    String VersionNo=getComponentVersionProp(username, password,baseUrl, ComponentName);
				
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
	
	private static String getComponentVersionProp(String username,
			String password, String baseUrl, String componentName)
			throws IllegalStateException, IOException, URISyntaxException,
			JSONException {
		HttpClientBuilder clientBuilder = new HttpClientBuilder();
		clientBuilder.setUsername(username);
		clientBuilder.setPassword(password);

		// for SSL enabled servers, accept all certificates
		clientBuilder.setTrustAllCerts(true);
		DefaultHttpClient client = clientBuilder.buildClient();

		HttpGet getReq = new HttpGet(new URI(baseUrl
				+ "/cli/component/versions?component=" +componentName));
		HttpResponse response1 = client.execute(getReq);
		BufferedReader br1 = new BufferedReader(new InputStreamReader(
				(response1.getEntity().getContent())));
		String output1;
		StringBuffer outputJson = new StringBuffer();
		while ((output1 = br1.readLine()) != null) {
			// System.out.println(output1);
			outputJson.append(output1);
		}
		String json = outputJson.toString();
		JSONArray versionNames = (JSONArray) JSONValue.parse(json);
		ArrayList<String> versionList = new ArrayList<String>();
		// Iterates the JSON Array
		for (int i = 0; i < versionNames.size(); i++) {
			JSONObject objects = (JSONObject) versionNames.get(i);
			// Gets the object named "name" and stores it in a string
			String name = objects.get("name").toString();
			versionList.add(name);
		}
		// System.out.println("version names list:::::"+versionNames);
		String latestVersion = versionList.get(0);
		//System.out.println("Latest version::" + latestVersion);
		return latestVersion;
	}
	

}
