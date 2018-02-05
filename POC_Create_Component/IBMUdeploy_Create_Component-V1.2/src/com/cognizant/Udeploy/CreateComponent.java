package com.cognizant.Udeploy;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import com.urbancode.commons.httpcomponentsutil.HttpClientBuilder;

public class CreateComponent {
	// Initialize the variable and logger

		private static org.apache.log4j.Logger logger = Logger.getLogger(CreateComponent.class);

	public static void main(String[] args) throws ClientProtocolException, IOException {
		Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		String username=args[0];
		String password=args[1];
		String baseUrl=args[2];
		String compName=args[3];
		String tagName=args[4];
		String appName=args[5];
		String propFile=args[6];
		String componentProps=args[7];
		//String envProps=args[7];
		
		JSONObject obj = new JSONObject();
		
		//System.out.println("Component properties are :"+obj);
		
		createNewComponent(username,password,baseUrl,compName,appName,propFile);
		addTagtoComponent(username,password,baseUrl,compName,tagName);
		
		if(args.length==8 && (!args[7].isEmpty())){
		String[] array=componentProps.split("\\,");
		for(int i=0;i<array.length;i++){			
			if(array[i].contains("=")||array[i].contains(":")){
				String[] array2=array[i].split("\\:|\\=");
				//obj.put(array2[0], array2[1]);
				//System.out.println(array2[0]+" : "+array2[1]);
				setComponentProperty(username,password,baseUrl,compName,array2[0], array2[1]);
			}
			else{
				setComponentProperty(username,password,baseUrl,compName,array[i], "");
			}
		}
		}
		
		/*if(args.length==8 && (!args[7].isEmpty())){
	    String envProps=args[7]	;
		String[] array_env=envProps.split("\\,");
		for(int i=0;i<array_env.length;i++){
			
			if(array_env[i].contains("=")||array_env[i].contains(":")){
				String[] array2=array_env[i].split("\\:|\\=");
				setComponentEnvironmentProperty(username,password,baseUrl,compName,array2[0], array2[1]);
			}
			else{
				setComponentEnvironmentProperty(username,password,baseUrl,compName,array_env[i], "");
			}
		}
		}*/
		//\"template/MyProperty\":\"MyValue\"

	}

	private static void createNewComponent(String username,String password, String baseUrl,String compName, String appName,String filePath) throws ClientProtocolException, IOException {
		HttpClientBuilder clientBuilder = new HttpClientBuilder();
		clientBuilder.setUsername(username);
		clientBuilder.setPassword(password);
		clientBuilder.setTrustAllCerts(true);
		DefaultHttpClient client = clientBuilder.buildClient();
		StringEntity input;
		HttpPut putReq;
		HttpPut putReq2;
		String templateName=getPropData(appName+"_TemplateName",filePath);
		//System.out.println(templateName);
		String teamId=getPropData(appName+"_TeamId",filePath);
		//System.out.println(teamId);		
		
		try {
			putReq = new HttpPut(new URI(baseUrl
					+ "/cli/component/create"));
			putReq.addHeader("Accept", "application/json");
			String text = "{\"name\":"+compName+",\"description\":\"Component Created via Cloudset utility\",\"templateName\":"+templateName+",\"templateVersion\":\"Always Use Latest\","
					+ "\"componentType\":\"STANDARD\", \"sourceConfigPlugin\":\"\",\"importAutomatically\":\"false\", \"useVfs\":\"true\",\"defaultVersionType\":\"FULL\", \"importAgentType\":\"inherit\",\"inheritSystemCleanup\":\"true\","
					+ "\"runVersionCreationProcess\":\"false\",\"properties\":{},\"teamMappings\": [{\"teamId\": "+teamId+"}]}";
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
		       System.out.println("Component \""+compName+"\" has been created.");
			}
			
			BufferedReader br = new BufferedReader(new InputStreamReader(
					(response.getEntity().getContent())));
			String output;
			
			//add the component to the application 
			putReq2 = new HttpPut(new URI(baseUrl
					+ "/cli/application/addComponentToApp?component="+compName+"&application="+appName));
			putReq2.addHeader("Accept", "application/json");

			HttpResponse response2 = client.execute(putReq2);
			
			if (response2.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response2.getStatusLine().getStatusCode());
			}
			else
			{
		      // System.out.println(response.getStatusLine().getStatusCode()+" :"+response.getStatusLine().getReasonPhrase());
		       System.out.println("Component \""+compName+"\" has been added to application \""+appName+"\".");
			}
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
			if (response.getStatusLine().getStatusCode() != 204) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}
			else
			{
		      // System.out.println(response.getStatusLine().getStatusCode()+" :"+response.getStatusLine().getReasonPhrase());
				System.out.println("Tag \""+tagName+ "\" has been added to component \""+compName+"\".");
			}
			
		    		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		
		
	}


	private static void setComponentProperty(String username, String password, String baseUrl, String compName,
			String name, String value) throws ClientProtocolException, IOException {
		HttpClientBuilder clientBuilder = new HttpClientBuilder();
		clientBuilder.setUsername(username);
		clientBuilder.setPassword(password);
		clientBuilder.setTrustAllCerts(true);
		DefaultHttpClient client = clientBuilder.buildClient();
		StringEntity input;
		HttpPut putReq;
		try {
			putReq = new HttpPut(new URI(baseUrl
					+ "/cli/component/propValue?component="+compName+"&name="+name+"&value="+value));

			HttpResponse response = client.execute(putReq);
			
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}
			else
			{
		      // System.out.println(response.getStatusLine().getStatusCode()+" :"+response.getStatusLine().getReasonPhrase());
				System.out.println("The component property \""+name+ "\" has been added to component \""+compName+"\".");
			}
		    		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void setComponentEnvironmentProperty(String username, String password, String baseUrl, String compName,

			String name, String value) throws ClientProtocolException, IOException {
		HttpClientBuilder clientBuilder = new HttpClientBuilder();
		clientBuilder.setUsername(username);
		clientBuilder.setPassword(password);
		clientBuilder.setTrustAllCerts(true);
		DefaultHttpClient client = clientBuilder.buildClient();
		StringEntity input;
		HttpPut putReq;
		try {
			putReq = new HttpPut(new URI(baseUrl
					+ "/cli/component/addEnvProp?name="+name+"&component="+compName+"&default="+value));

			HttpResponse response = client.execute(putReq);
			
			if (response.getStatusLine().getStatusCode() != 204) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}
			else
			{
		      // System.out.println(response.getStatusLine().getStatusCode()+" :"+response.getStatusLine().getReasonPhrase());
				System.out.println("The environment property \""+name+ "\" has been added to component \""+compName+"\".");
			}
		    		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
	}
	
	public static String getPropData(String text,String filePath) throws FileNotFoundException, IOException {
		// using properties class to load the properties file
		Properties properties = new Properties();
		properties.load(new FileInputStream(filePath));
		try {
			String propText = null;
			if (properties.getProperty(text) != null) {
				propText = properties.getProperty(text);

			} else {

				logger.debug("No data available in properties file for" + text);
			}
		} catch (Exception ex) {
			logger.error("Exception occured:", ex);
		}
		return properties.getProperty(text);
	}


}
