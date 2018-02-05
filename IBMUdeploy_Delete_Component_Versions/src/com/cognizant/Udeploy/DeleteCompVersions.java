package com.cognizant.Udeploy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

public class DeleteCompVersions {

	public static void main(String[] args) throws ClientProtocolException, URISyntaxException, IOException {
		Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		String compName=args[0];
		String username=args[1];
		String password=args[2];
		String baseUrl=args[3];
		// TODO Auto-generated method stub
		getComponentVersions(username, password, baseUrl, compName);

	}
	private static void getComponentVersions(String username, String password, String baseUrl, String compName
			) throws URISyntaxException, ClientProtocolException, IOException {
		
		HttpClientBuilder clientBuilder=new HttpClientBuilder();
		clientBuilder.setUsername(username);
	    clientBuilder.setPassword(password);
	    
	    clientBuilder.setTrustAllCerts(true);
	    
	    DefaultHttpClient client = clientBuilder.buildClient();
	    
	    HttpGet getReq=new HttpGet(new URI(baseUrl+"/cli/component/versions?component="+compName));
	    HttpResponse response=client.execute(getReq);
	    BufferedReader br=new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	    String output;
	    StringBuffer outputJson=new StringBuffer();
	    
	    List<String> lines=new ArrayList<String>();
	    while((output=br.readLine()) != null){
	    	outputJson.append(output);
	    }
	    String json =outputJson.toString();
	    
		JSONArray array= (JSONArray) JSONValue.parse(json);
		
		for(int i=0;i<array.size();i++){
			JSONObject object= (JSONObject)array.get(i);
			String name=(String) object.get("name");
			//System.out.println(name);
			if(name.startsWith("17.3")||name.startsWith("17.2")||name.startsWith("17.1")||name.startsWith("16.")){
				System.out.println(name);
				deleteVersions(username,password,baseUrl,compName,name);
			}
		}
		
	}
	private static void deleteVersions(String username, String password, String baseUrl,String compName, String name) throws ClientProtocolException, IOException {
		// TODO Auto-generated method stub
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
					+ "/cli/version/deleteVersion?component="+compName+"&version="+name));
			putReq.addHeader("Accept", "application/json");
			HttpResponse response = client.execute(putReq);
			
			if (response.getStatusLine().getStatusCode() != 204) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}
			else
			{
		       System.out.println("Deleted.");
			}
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
	}

}
