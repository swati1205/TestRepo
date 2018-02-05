package com.cognizant.Udeploy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.urbancode.commons.httpcomponentsutil.HttpClientBuilder;

public class RunAppProcess {

	public static void main(String[] args) throws ClientProtocolException, IOException {
		Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		
		String username=args[0];
		String password=args[1];
		String baseUrl=args[2];
		String appProcessName=args[3];
		
		String snapshotName=getLatestCISnapshot(username,password,baseUrl);
		System.out.println("Name of latest CI snapshot: "+snapshotName);
		
		requestAppProcess(username,password,baseUrl,appProcessName,snapshotName);

	}

	private static String getLatestCISnapshot(String username, String password, String baseUrl) throws ClientProtocolException, IOException {
		HttpClientBuilder clientBuilder = new HttpClientBuilder();
		clientBuilder.setUsername(username);
		clientBuilder.setPassword(password);
		clientBuilder.setTrustAllCerts(true);
		String CIsnapshot=null;
		DefaultHttpClient client = clientBuilder.buildClient();
		
		try {
			HttpGet getReq= new HttpGet(new URI(baseUrl+"/cli/application/snapshotsInApplication?application=CPQ"));
			getReq.addHeader("Accept", "application/json");
			
			HttpResponse response1=client.execute(getReq);
			BufferedReader br1 = new BufferedReader(new InputStreamReader(
					(response1.getEntity().getContent())));
			String output1;
			while ((output1 = br1.readLine()) != null) {
				JSONArray responsejson1=(JSONArray)JSONValue.parse(output1);
				for(int i=0;i<responsejson1.size();i++){
				JSONObject jsonObj=(JSONObject) responsejson1.get(i);
				String snapshotName=jsonObj.get("name").toString();
				//System.out.println("Name of snapshot: "+snapshotName);
				if(snapshotName.startsWith("ci_")){
					CIsnapshot=snapshotName;
					break;
				}
				}
			}
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return CIsnapshot;
		
	}
	
	private static void requestAppProcess(String username,String password,String baseUrl,String appProcessName,String snapshotName) 
			throws ClientProtocolException, IOException {
		HttpClientBuilder clientBuilder = new HttpClientBuilder();
		clientBuilder.setUsername(username);
		clientBuilder.setPassword(password);
		clientBuilder.setTrustAllCerts(true);
		DefaultHttpClient client = clientBuilder.buildClient();
		StringEntity input;
		String appName="CPQ";
		HttpPut putReq;
		try {
			putReq = new HttpPut(new URI(baseUrl
					+ "/cli/applicationProcessRequest/request"));
			putReq.addHeader("Accept", "application/json");
			String text = "{\"application\": "+appName+",\"applicationProcess\": "+appProcessName+",\"environment\": \"CI\",\"onlyChanged\": \"false\",\"snapshot\": "+snapshotName+"}";
			System.out.println("Json for requesting application process: "+text);
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
