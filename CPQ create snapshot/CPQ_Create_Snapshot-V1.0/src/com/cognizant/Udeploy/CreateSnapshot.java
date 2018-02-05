package com.cognizant.Udeploy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
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

public class CreateSnapshot {

	public static void main(String[] args) throws IOException {
		
		Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		
		String filepath=args[0];
		String username=args[1];
		String password=args[2];
		String baseUrl=args[3];
		String snapshotName=args[4];
		String publishVersion=args[5];
		String appProcessName=args[6];
		String environment=args[7];
		
		Properties prop = new Properties();
		InputStream input = null;
		input = new FileInputStream(filepath);
		if (input == null) {
			System.out.println("Sorry, unable to find property file" + filepath);
			return;
		}

		prop.load(input);
		Set<Object> keys = prop.keySet();
		
		JSONArray compArr=new JSONArray();
		for(Object k:keys){
			String key = (String)k;
			String value=prop.getProperty(key);
			JSONObject jo = new JSONObject();
			String versionNo=publishVersion;
			jo.put(value, versionNo);
			compArr.add(jo);
		}
		JSONObject jo= new JSONObject();
		jo.put("CPQ_TAR",publishVersion );
		compArr.add(jo);
		JSONObject jo1= new JSONObject();
		jo1.put("CPQ_JAR",publishVersion );
		compArr.add(jo1);
		String CompJson=compArr.toString();
		//System.out.println("Json for snapshot is :"+CompJson);
		
		createSnapshotchangedVersions(username,password,baseUrl,CompJson,snapshotName);
		requestAppProcess(username,password,baseUrl,environment,appProcessName,snapshotName);

	}
   
	private static void createSnapshotchangedVersions(String username, String password, String baseUrl, String compJson,
			String snapshotName)throws ClientProtocolException, IOException  {
		HttpClientBuilder clientBuilder = new HttpClientBuilder();
		clientBuilder.setUsername(username);
		clientBuilder.setPassword(password);
		clientBuilder.setTrustAllCerts(true);
		DefaultHttpClient client = clientBuilder.buildClient();
		StringEntity input;
		//String appName="CPQ";
		HttpPut putReq;
		try {
			putReq = new HttpPut(new URI(baseUrl
					+ "/cli/snapshot/createSnapshot"));
			putReq.addHeader("Accept", "application/json");
			String text = "{\"application\": \"CPQ\",\"name\": "+snapshotName+",\"description\": \"Snapshot created for published versions of CPQ\",\"versions\": "+compJson+"}";
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
		       System.out.println(response.getStatusLine().getStatusCode()+" :"+response.getStatusLine().getReasonPhrase()+". Snapshot Created.");
			}
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
	}

	private static void requestAppProcess(String username,String password,String baseUrl,String env,String appProcessName,String snapshotName) 
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
			String text = "{\"application\": "+appName+",\"applicationProcess\": "+appProcessName+",\"environment\": "+env+",\"onlyChanged\": \"false\",\"snapshot\": "+snapshotName+"}";
			System.out.println("Json request for creating snapshot: "+text);
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
