package com.cognizant.IBM.RequestProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.log4j.Logger;


import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.urbancode.commons.httpcomponentsutil.HttpClientBuilder;

@SuppressWarnings("deprecation")
public class RequestAppProcess {

	public static void main(String[] args) throws IOException, URISyntaxException {

		// suppress log4j messages from UCD library
		 Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

		String username = args[0];
		String password = args[1];
		String baseUrl = args[2];
		//String tagName=args[3];
		String appProcessName=args[3];
		String appName=args[4];
		String fileName= args[5];
		File jsonFile = new File(fileName);
		String jsonString = FileUtils.readFileToString(jsonFile);
		JSONObject obj = (JSONObject)JSONValue.parse(jsonString);
		
		JSONArray compArr=(JSONArray) obj.get("COMPONENTS");
		for(int i=0;i<compArr.size();i++){
			JSONObject obj1=(JSONObject)compArr.get(i);
			String ComponentName=obj1.get("component").toString();
    		String VersionNo=obj1.get("version").toString();
    		//System.out.println(ComponentName);
			//addTagtocomp(username,password, baseUrl, ComponentName,tagName) ;
			
		}
		String CompJson=compArr.toString();
		System.out.println(CompJson);
		
	    JSONArray envArr= (JSONArray) obj.get("ENVIRONEMNT");
	    for(int i=0;i<envArr.size();i++){
	    	String env=envArr.get(i).toString();
	    	//System.out.println(env);
	        requestAppProcess(username,password,baseUrl,env,appName,appProcessName,CompJson);
	    }
	    
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
					System.out.println("Status of Process Request: "+statusOfReq);
					System.out.println("Result of Process Request: "+res);
				}

				
			}
			
			
		
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

	}
	
	/*private static void addTagtocomp(String username,String password,String baseUrl,String componentName,String tagName) 
			throws URISyntaxException, ClientProtocolException, IOException{
		HttpClientBuilder clientBuilder = new HttpClientBuilder();
		clientBuilder.setUsername(username);
		clientBuilder.setPassword(password);
		clientBuilder.setTrustAllCerts(true);
		DefaultHttpClient client = clientBuilder.buildClient();
		
		HttpPut putReq = new HttpPut(new URI(baseUrl
				+ "/cli/component/tag?component="+componentName+"&tag="+tagName));
		HttpResponse response = client.execute(putReq);
		
	       System.out.println(response.getStatusLine().getStatusCode()+" :"+response.getStatusLine().getReasonPhrase());
		
		
		
	}*/

	
}
