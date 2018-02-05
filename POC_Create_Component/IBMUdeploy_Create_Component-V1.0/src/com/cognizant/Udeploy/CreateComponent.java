package com.cognizant.Udeploy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import com.urbancode.commons.httpcomponentsutil.HttpClientBuilder;

public class CreateComponent {

	public static void main(String[] args) throws ClientProtocolException, IOException {
		Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		String username=args[0];
		String password=args[1];
		String baseUrl=args[2];
		String compName=args[3];
		String tagName=args[4];
		
		createNewComponent(username,password,baseUrl,compName);
		addTagtoComponent(username,password,baseUrl,compName,tagName);

	}

	private static void createNewComponent(String username,String password, String baseUrl,String compName) throws ClientProtocolException, IOException {
		HttpClientBuilder clientBuilder = new HttpClientBuilder();
		clientBuilder.setUsername(username);
		clientBuilder.setPassword(password);
		clientBuilder.setTrustAllCerts(true);
		DefaultHttpClient client = clientBuilder.buildClient();
		StringEntity input;
		HttpPut putReq;
		
		try {
			putReq = new HttpPut(new URI(baseUrl
					+ "/cli/component/create"));
			putReq.addHeader("Accept", "application/json");
			String text = "{\"name\":"+compName+",\"description\":\"Component Created via Cloudset utility\",\"templateName\":\"CAFE\",\"templateVersion\":\"Always Use Latest\",\"template/MyProperty\":\"MyValue\","
					+ "\"componentType\":\"STANDARD\", \"sourceConfigPlugin\":\"\",\"importAutomatically\":\"false\", \"useVfs\":\"true\",\"defaultVersionType\":\"FULL\", \"importAgentType\":\"inherit\",\"inheritSystemCleanup\":\"true\","
					+ "\"runVersionCreationProcess\":\"false\",\"properties\":{},\"teamMappings\": [{\"teamId\": \"f90bea90-a5cc-4703-bedb-b66192a28963\"}]}";
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
		      // System.out.println(response.getStatusLine().getStatusCode()+" :"+response.getStatusLine().getReasonPhrase());
		       System.out.println("Component "+compName+" has been created.");
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(
					(response.getEntity().getContent())));
			String output;
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void addTagtoComponent(String username, String password, String baseUrl, String compName,
			String tagName) throws IllegalStateException, IOException {
		HttpClientBuilder clientBuilder = new HttpClientBuilder();
		clientBuilder.setUsername(username);
		clientBuilder.setPassword(password);
		clientBuilder.setTrustAllCerts(true);
		DefaultHttpClient client = clientBuilder.buildClient();
		StringEntity input;
		HttpPut putReq;
		try {
			putReq = new HttpPut(new URI(baseUrl
					+ "/cli/component/tag?component="+compName+"&tag="+tagName));

			HttpResponse response = client.execute(putReq);
			
			if(response.getStatusLine().getStatusCode()==204){
				System.out.println("Tag "+tagName+ " has been added to component "+compName+".");
				
			}else{
				System.out.println(response.getStatusLine().getStatusCode()+" :"+response.getStatusLine().getReasonPhrase());

			}
		    		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		
		
	}

}
