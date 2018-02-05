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

public class CreateSnapshot {

	public static void main(String[] args) throws IOException, URISyntaxException, ParseException {
		
		 Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		
		String filepath=args[0];
		String username=args[1];
		String password=args[2];
		String baseUrl=args[3];
		String snapshotName2=args[4];
		/*String trunkProcessName=args[4];
		String branchProcessName=args[5];
		String env=args[6];
		*/
		
		File compFile = new File(filepath);
		String content =FileUtils.readFileToString(compFile); 
		//System.out.println(content);
		String[] compArray=content.split(",");
		JSONObject obj = new JSONObject();
		obj.put("name", "My_snapshot");
		obj.put("application","CAFE");
		obj.put("description", "Snapshot created for changed versions for CI");
		
		
		JSONArray compArr1=new JSONArray();
		JSONArray compArr2=new JSONArray();
		
		
		for(int i=0;i<compArray.length;i++){
			//System.out.println(compArray[i]);
			
			String versionNo=checkVersions(username,
					password, baseUrl,compArray[i]);
			System.out.println(versionNo);
			
			if(versionNo!=null)
			{
				String[] versiontokens=versionNo.split("\\.");
				String buildNumber=versiontokens[3];
				JSONObject jo = new JSONObject();
				if(Integer.parseInt(buildNumber)<200){
					jo.put(compArray[i], versionNo);
					compArr1.add(jo);
				}
				else if(Integer.parseInt(buildNumber)>=200){
					jo.put(compArray[i], versionNo);
					compArr2.add(jo);
				}
				
				
			}
			   
		}
		//compArr.add(jo);
		//obj.put("versions", compArr); not required for sending compJson array only
		//System.out.println(obj);
		String CompJson1=compArr1.toString();
		//System.out.println(CompJson1);
		String CompJson2=compArr2.toString();
		//System.out.println(CompJson2);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		Date dateTime = new Date();
		String time = sdf.format(dateTime);
		
		//if(!compArr1.isEmpty()){
			//String snapshotName="CAFE_trunkSnapshot_"+time;
			//System.out.println(snapshotName);
			//createSnapshotchangedVersions(username,password,baseUrl,CompJson1,snapshotName);
			//requestAppProcess(username,password,baseUrl,env,trunkProcessName,snapshotName);
		//}
		
		//if(!compArr2.isEmpty()){
			//String snapshotName2="CAFE_branchSnapshot_"+time;
			System.out.println(snapshotName2);
			createSnapshotchangedVersions(username,password,baseUrl,CompJson2,snapshotName2);
			//requestAppProcess(username,password,baseUrl,env,branchProcessName,snapshotName);
		//}
		//createSnapshotchangedVersions(username,password,baseUrl,CompJson,snapshotName);
		
		//requestAppProcess(username,password,baseUrl,env,appProcessName,snapshotName);

	}
	public static String checkVersions(String username,
			String password, String baseUrl,String component) throws IOException, URISyntaxException, ParseException{
		HttpClientBuilder clientBuilder = new HttpClientBuilder();
		clientBuilder.setUsername(username);
		clientBuilder.setPassword(password);

		// for SSL enabled servers, accept all certificates
		clientBuilder.setTrustAllCerts(true);
		DefaultHttpClient client = clientBuilder.buildClient();

		HttpGet getReq = new HttpGet(new URI(baseUrl
				+ "/cli/component/versions?component="+component));
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
		//System.out.println(json);
		JSONArray versionProps = (JSONArray) JSONValue.parse(json);
		System.out.println("Checking for component: "+component);
		String version=null;
		for (int i = 0; i < 5; i++) {
			JSONObject object = (JSONObject) versionProps.get(i);
			//System.out.println(versionProps.get(i));
			//String versionName = object.get("name").toString();
			String timecreated = object.get("created").toString();
			String unixTime=timecreated.substring(0,10);
			long unixSeconds = Long.parseLong(unixTime);
			Date date = new Date(unixSeconds*1000L); // *1000 is to convert seconds to milliseconds
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss"); // the format of your date
			String formattedDate = sdf.format(date);
			//System.out.println(formattedDate);
			
			Date dateTime = new Date();
			String time = sdf.format(dateTime);
			//System.out.println(d2);
			Date d1 = sdf.parse(formattedDate);
			Date d2=  sdf.parse(time);
			long diff = d2.getTime() - d1.getTime();
			long diffHours = diff / (60 * 60 * 1000);
			//System.out.println(diffHours);
			if(diffHours <= 24)
			{
				//System.out.println(formattedDate);
				//System.out.println("OK");
				//System.out.println(object.get("name").toString());
				version=object.get("name").toString();
				break;
			}
			
		}
		//System.out.println(component+"="+version);
		return version;
		
	}
	//https://10.219.193.35:8443/cli/snapshot/createSnapshot" -X PUT -d @D:\\Excel files\\create_snapshot.json
	private static void createSnapshotchangedVersions(String username,String password,String baseUrl,String CompJson,String snapshotName) 
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
					+ "/cli/snapshot/createSnapshot"));
			putReq.addHeader("Accept", "application/json");
			String text = "{\"application\": \"CAFE\",\"name\": "+snapshotName+",\"description\": \"Snapshot created for changed versions for CI\",\"versions\": "+CompJson+"}";
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

	
	

}
