package com.cognizant.IBMUdeploy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import com.urbancode.commons.httpcomponentsutil.HttpClientBuilder;

public class GetVersionProperty {

	public static void main(String[] args) throws IllegalStateException, IOException, URISyntaxException {
		// TODO Auto-generated method stub
		// suppress log4j messages from UCD library
		Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		String username = args[0];
        String password = args[1];
        String baseUrl=args[2];
        String ComponentName=args[3];
        String VersionNo= args[4];
        
        getComponentVersionProp(username,password,baseUrl,ComponentName,VersionNo);

	}

	private static void getComponentVersionProp(String username,
			String password, String baseUrl, String componentName,
			String versionNo) throws IllegalStateException, IOException, URISyntaxException {
		HttpClientBuilder clientBuilder = new HttpClientBuilder();
		  clientBuilder.setUsername(username);
		  clientBuilder.setPassword(password);

		  // for SSL enabled servers, accept all certificates
		  clientBuilder.setTrustAllCerts(true); 
		  DefaultHttpClient client = clientBuilder.buildClient();
		  
		  HttpGet getReq= new HttpGet(new URI(baseUrl+"/cli/version/versionProperties?component="+componentName+"&version="+versionNo));
			HttpResponse response1= client.execute(getReq);
			BufferedReader br1 = new BufferedReader(new InputStreamReader(
                  (response1.getEntity().getContent())));
			String output1;
      	 while ((output1 = br1.readLine()) != null) {
              System.out.println(output1);
      	 }
		
	}

}
