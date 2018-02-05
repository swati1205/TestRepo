package com.cognizant.Udeploy;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

public class TriggerProcess {

	public static void main(String[] args) throws IOException, URISyntaxException, ParseException {
		
		 Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		
		String username=args[0];
		String password=args[1];
		String baseUrl=args[2];
		//String trunkProcessName=args[3];
		String branchProcessName=args[3];
		String env=args[4];
		String snapshotName2=args[5];
		
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		Date dateTime = new Date();
		String time = sdf.format(dateTime);
		
		
			//String snapshotName="CAFE_trunkSnapshot_"+time;
			//System.out.println(snapshotName);
			//requestAppProcess(username,password,baseUrl,env,trunkProcessName,snapshotName);
		

			//String snapshotName2="CAFE_branchSnapshot_"+time;
			System.out.println(snapshotName2);
			requestAppProcess(username,password,baseUrl,env,branchProcessName,snapshotName2);
		

	}
	
	//https://10.219.193.35:8443/cli/snapshot/createSnapshot" -X PUT -d @D:\\Excel files\\create_snapshot.json

	private static void requestAppProcess(String username,String password,String baseUrl,String env,String appProcessName,String snapshotName) 
			throws ClientProtocolException, IOException {
		HttpClientBuilder clientBuilder = new HttpClientBuilder();
		clientBuilder.setUsername(username);
		clientBuilder.setPassword(password);
		clientBuilder.setTrustAllCerts(true);
		DefaultHttpClient client = clientBuilder.buildClient();
		StringEntity input;
		String appName="CAFE";
		HttpPut putReq;
		try {
			putReq = new HttpPut(new URI(baseUrl
					+ "/cli/applicationProcessRequest/request"));
			putReq.addHeader("Accept", "application/json");
			String text = "{\"application\": "+appName+",\"applicationProcess\": "+appProcessName+",\"environment\": "+env+",\"onlyChanged\": \"false\",\"snapshot\": "+snapshotName+"}";
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
		       System.out.println(response.getStatusLine().getStatusCode()+" :"+response.getStatusLine().getReasonPhrase()+". Process triggered.");
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

	
	

}
