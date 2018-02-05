package com.cognizant.Udeploy;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.urbancode.commons.httpcomponentsutil.HttpClientBuilder;

public class RequestAppProcess {

	public static void main(String[] args) throws IOException, IllegalStateException, URISyntaxException, JSONException {
		// TODO Auto-generated method stub
		// suppress log4j messages from UCD library
				 Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

				String username = args[0];
				String password = args[1];
				String baseUrl = args[2];
				String appProcessName=args[3];
				String appName=args[4];
				String fileName= args[5];
				//String jobFileName=args[6];
				File jsonFile = new File(fileName);
				String jsonString = FileUtils.readFileToString(jsonFile);
				JSONObject obj = (JSONObject)JSONValue.parse(jsonString);

				//File jsonFile2= new File(jobFileName);
				//String jsonString2= FileUtils.readFileToString(jsonFile2);
				//JSONObject obj2=(JSONObject) JSONValue.parse(jsonString);
				JSONArray compArr=(JSONArray) obj.get("COMPONENTS");
				JSONArray envArr= (JSONArray) obj.get("ENVIRONMENT");
				
				for(int j=0;j<envArr.size();j++){
					String env=envArr.get(j).toString();
				for(int i=0;i<compArr.size();i++){
					JSONObject obj1=(JSONObject)compArr.get(i);
					String ComponentName=obj1.get("component").toString();
					String version=obj1.get("version").toString();
					String VersionNo=null;
					if(version.equals("Latest Version")){
						VersionNo=getComponentVersionProp(username, password,baseUrl, ComponentName);
					}
					else{
						VersionNo=version;
					}
					//for(int j=0;j<envArr.size();j++){
		    		//String VersionNo=getComponentVersionProp(username, password,baseUrl, ComponentName,env,appName);
		    		obj1.put("version",VersionNo );
		    		System.out.println("The version selected for component "+ComponentName+" is :"+VersionNo);
					}
				String CompJson=compArr.toString();
				//System.out.println("Json is: "+CompJson);
				requestAppProcess(username,password,baseUrl,env,appName,appProcessName,CompJson);
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
				+ "/cli/component/versions?component=" + componentName));
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
		
		String latestVersion = versionList.get(0);
		return latestVersion;
	}
	
	private static void requestAppProcess(String username,String password,String baseUrl,String env,String appName,String appProcessName,String CompJson) 
			throws ClientProtocolException, IOException {
		HttpClientBuilder clientBuilder = new HttpClientBuilder();
		clientBuilder.setUsername(username);
		clientBuilder.setPassword(password);
		clientBuilder.setTrustAllCerts(true);
		DefaultHttpClient client = clientBuilder.buildClient();
		StringEntity input;
		HttpPut putReq;
		try {
			putReq = new HttpPut(new URI(baseUrl
					+ "/cli/applicationProcessRequest/request"));
			putReq.addHeader("Accept", "application/json");
			String text = "{\"application\": "+appName+",\"applicationProcess\": "+appProcessName+",\"environment\": "+env+",\"onlyChanged\": \"false\",\"versions\": "+CompJson+"}";
			System.out.println(text);
			input = new StringEntity(text);
			putReq.setEntity(input);

			HttpResponse response = client.execute(putReq);
			
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}
			else
			{
		       System.out.println(response.getStatusLine().getStatusCode()+" :"+response.getStatusLine().getReasonPhrase());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(
					(response.getEntity().getContent())));
			String output;
			while ((output = br.readLine()) != null) {
				System.out.println(output);
				JSONObject responsejson=(JSONObject)JSONValue.parse(output);
				String requestID=responsejson.get("requestId").toString();
				HttpGet getReq= new HttpGet(new URI(baseUrl+"/cli/applicationProcessRequest/requestStatus?request="+requestID ));
				getReq.addHeader("Accept", "application/json");
				
				HttpResponse response1=client.execute(getReq);
				BufferedReader br1 = new BufferedReader(new InputStreamReader(
						(response1.getEntity().getContent())));
				String output1;
				while ((output1 = br1.readLine()) != null) {
					JSONObject responsejson1=(JSONObject)JSONValue.parse(output1);
					String statusOfReq=responsejson1.get("status").toString();
					String res=responsejson1.get("result").toString();
					//System.out.println("Status of Process Request: "+statusOfReq);
					System.out.println("Result of Process Request: "+res);
				}
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

	}


}
