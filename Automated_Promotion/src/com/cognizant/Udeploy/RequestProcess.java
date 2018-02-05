package com.cognizant.Udeploy;
import java.io.BufferedReader;
import java.nio.file.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;

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


public class RequestProcess {

	public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {

		// suppress log4j messages from UCD library
		 Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

		String username = args[0];
		String password = args[1];
		String baseUrl = args[2];
		String appProcessName=args[3];
		String appName=args[4];
		String fileName= args[5];
		String iterations=args[6];
		String requiredResult = args[7];
		
		/*int iterNo = 0;
		if(args.length==6){
			iterNo= 10;
		}
		else if(args.length==7){*/
		int	iterNo=Integer.parseInt(iterations);
		
		File jsonFile = new File(fileName);
		String jsonString = FileUtils.readFileToString(jsonFile);
		JSONObject obj = (JSONObject)JSONValue.parse(jsonString);
		
		JSONArray compArr=(JSONArray) obj.get("COMPONENTS");
		System.out.println("The application process selected is :"+appProcessName+" for application :"+appName);
		
		for(int i=0;i<compArr.size();i++){
			JSONObject obj1=(JSONObject)compArr.get(i);
			String ComponentName=obj1.get("component").toString();
    		String VersionNo=obj1.get("version").toString();
    		System.out.println("The application process will be triggered for component :"+ComponentName+" for version :"+VersionNo);
			
		}
		String CompJson=compArr.toString();
		//System.out.println(CompJson);
		
		 
	    Timer time = new Timer(); // Instantiate Timer Object
		ScheduledTask st = new ScheduledTask(); // Instantiate SheduledTask class
		time.schedule(st, 0, 1000); // Create Repetitively task for every 1 secs
		
	    JSONArray envArr= (JSONArray) obj.get("ENVIRONEMNT");
	    for(int i=0;i<envArr.size();i++){
	    	String env=envArr.get(i).toString();
	    	//System.out.println(env);
	        String requestID=requestAppProcess(username,password,baseUrl,env,appName,appProcessName,CompJson);
	        System.out.println("Request ID is: "+requestID);
	        long seconds=5000;
	        //String result=getProcessStatus(username,password,baseUrl,requestID);
	        
	        for (int j = 0; j <= iterNo; j++) {
				//System.out.println("Execution in Main Thread...." + j);
				Thread.sleep(seconds);
				String result=getProcessStatus(username,password,baseUrl,requestID);
				//System.out.println(result);//|| result.equalsIgnoreCase("AWAITING APPROVAL")
				if (j == iterNo || result.equalsIgnoreCase(requiredResult) ) { 
					System.out.println("Process result is :"+result);
					System.out.println("Application Process Request Terminates");
					//break;
					System.exit(0);
				}
			}
	        
	    }
	    
	}
	    
	
	
	private static String requestAppProcess(String username,String password,String baseUrl,String env,String appName,String appProcessName,String CompJson) 
			throws ClientProtocolException, IOException {
		HttpClientBuilder clientBuilder = new HttpClientBuilder();
		clientBuilder.setUsername(username);
		clientBuilder.setPassword(password);
		clientBuilder.setTrustAllCerts(true);
		DefaultHttpClient client = clientBuilder.buildClient();
		StringEntity input;
		HttpPut putReq;
		String requestID=null;
		try {
			putReq = new HttpPut(new URI(baseUrl
					+ "/cli/applicationProcessRequest/request"));
			putReq.addHeader("Accept", "application/json");
			String text = "{\"application\": "+appName+",\"applicationProcess\": "+appProcessName+",\"environment\": "+env+",\"onlyChanged\": \"false\",\"versions\": "+CompJson+"}";
			//System.out.println(text);
			input = new StringEntity(text);
			putReq.setEntity(input);

			HttpResponse response = client.execute(putReq);
			
			
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}
			else
			{
		       //System.out.println(response.getStatusLine().getStatusCode()+" :"+response.getStatusLine().getReasonPhrase());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(
					(response.getEntity().getContent())));
			String output;
			while ((output = br.readLine()) != null) {
				//System.out.println(output);
				JSONObject responsejson=(JSONObject)JSONValue.parse(output);
				requestID=responsejson.get("requestId").toString();
				//System.out.println(requestID);	
			}		
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return requestID;

	}
	
	private static String getProcessStatus(String username,String password,String baseUrl,String requestID) 
			throws ClientProtocolException, IOException, URISyntaxException {
		HttpClientBuilder clientBuilder = new HttpClientBuilder();
		clientBuilder.setUsername(username);
		clientBuilder.setPassword(password);
		clientBuilder.setTrustAllCerts(true);
		DefaultHttpClient client = clientBuilder.buildClient();
		
		HttpGet getReq= new HttpGet(new URI(baseUrl+"/cli/applicationProcessRequest/requestStatus?request="+requestID ));
		getReq.addHeader("Accept", "application/json");
		
		HttpResponse response1=client.execute(getReq);
		BufferedReader br1 = new BufferedReader(new InputStreamReader(
				(response1.getEntity().getContent())));
		String output1=null;
		String res=null;
		while ((output1 = br1.readLine()) != null) {
			JSONObject responsejson1=(JSONObject)JSONValue.parse(output1);
			//System.out.println(responsejson1);
			//String statusOfReq=responsejson1.get("status").toString();
			res=responsejson1.get("result").toString();
			
	}
		return res;
	}
	
}
