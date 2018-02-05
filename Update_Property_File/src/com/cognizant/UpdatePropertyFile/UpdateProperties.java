package com.cognizant.UpdatePropertyFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.urbancode.commons.httpcomponentsutil.HttpClientBuilder;


/**
* 
* Java utility to modify property file which is downloaded from IBM UDeploy through REST API.This java client makes a 
* webservice call to UDeploy to download artifacts and then updates the file,storing it in local folder.
*
* @author  Anamika Bhowmick
* @version 1.0
*/
@SuppressWarnings("deprecation")
public class UpdateProperties {
/**
  * 
  * The main method parses the json file and makes a call to the respective function to edit/modify/delete the contents
  * of the property file.
  * 
  *  @return null
  *  @throws IOException,URISyntaxException, IllegalStateException, JSONException
  *  
  *		    
*/
	public static void main (String[] args) throws IOException, IllegalStateException, URISyntaxException, JSONException{
		
		// suppress log4j messages from UCD library
				Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
				String username = args[0];
				String password = args[1];
				String baseUrl = args[2];
				String jsonFilePath = args[3];
				String basePath = args[4];
				
				//HashSet<String>set=new HashSet<String>();
				File jsonFile = new File(jsonFilePath);
				String jsonString = FileUtils.readFileToString(jsonFile);
				JSONObject obj = (JSONObject) JSONValue.parse(jsonString);
				String ReleaseVersion =obj.get("Realease_Version").toString();
				//System.out.println(ReleaseVersion);
				JSONArray propArr = (JSONArray) obj.get("Property_File");
				
				for (int i = 0; i < propArr.size(); i++) {
					JSONObject obj1 = (JSONObject) propArr.get(i);
					String componentName = obj1.get("File").toString();
					String[] arr = componentName.split("\\.");
					String fileName = arr[arr.length - 1] + ".properties";
					String performFunction = obj1.get("Opertaion").toString();
					String prop_key = obj1.get("Property_Name").toString();
					String prop_value = obj1.get("Property_value").toString();
					String versionNo = getComponentVersionProp(username, password,
							baseUrl, componentName);
					//String ReleaseVersion = obj1.get("Realease_Version").toString();
					
					String dir = basePath + "\\" + componentName + "\\"+ ReleaseVersion;
					Files.createDirectories(Paths.get(dir));
					File file = new File(basePath + "\\" + componentName + "\\"+ ReleaseVersion + "\\" + fileName);
					UpdateProperties.updateFile(username, password, baseUrl, fileName, prop_key,
							prop_value, file, componentName, versionNo, dir,performFunction);
					File tmpFile = new File(dir+"\\tmpFile.properties");
					FileWriter fw=null;
					Reader fr=null;
					BufferedReader br=null;
					try
					{
					fw = new FileWriter(tmpFile);
					fr = new FileReader(file);
					br = new BufferedReader(fr);
					String output;
					while ((output = br.readLine()) != null) {
						//System.out.println(output);
						if (output.contains("\\:"))
							output = output.replaceAll("\\\\:", ":");
						if(output.contains("\\="))
							output = output.replaceAll("\\\\=", "=");
						if(output.contains("\\#"))
							output = output.replaceAll("\\\\#", "#");
						if(output.contains("\\!"))
							output = output.replaceAll("\\\\!", "!");
						fw.write(output + "\n");
					}
					}finally {
						try {
							if (br != null)
								br.close();
						} catch (IOException e) {
							//
						}
						try {
							if (fw != null)
							{ 
								
								fw.flush();
								fw.close();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
						try{
							if (fr != null)
								fr.close();
						}catch (IOException e) {
							//
						}
					}
					file.delete();
					File newFile = new File(dir+"\\tmpFile.properties");
					newFile.renameTo(file);
					    
					    //tmpFile.renameTo(file);

					//set.add(componentName);
				}
				
				/*for (String s : set) {
					for (int i = 0; i < propArr.size(); i++) {
						
						UpdateProperties.updateFile();
					}
				      
				  }*/
		
	}
	
	private static void updateFile(String username, String password,
			String baseUrl, String fileName, String prop_key,
			String prop_value, File file, String componentName,
			String versionNo, String dir,String operation) throws IOException{
		
		FileInputStream in=null;
		if (file.exists()) {
		in = new FileInputStream(file);
		
		}
		else{
			HttpClientBuilder clientBuilder = new HttpClientBuilder();
			clientBuilder.setUsername(username);
			clientBuilder.setPassword(password);
			clientBuilder.setTrustAllCerts(true);
			DefaultHttpClient client = clientBuilder.buildClient();
			
			try {
				HttpGet getReq = new HttpGet(new URI(baseUrl
						+ "/cli/version/downloadArtifacts?component="
						+ componentName + "&version=" + versionNo
						+ "&location=.&singleFilePath=" + fileName));
				HttpResponse response = client.execute(getReq);
				InputStream is=response.getEntity().getContent();
				FileOutputStream fos = new FileOutputStream(new File(dir+"\\"+fileName));
				int read = 0;
				byte[] buffer = new byte[32768];
				while( (read = is.read(buffer)) > 0) {
				  fos.write(buffer, 0, read);
				}

				fos.close();
				is.close();
				
				in=new FileInputStream(dir+"\\"+fileName);
			}catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		Properties props = new Properties();
		props.load(in);
		in.close();

		FileOutputStream out = new FileOutputStream(file);
		if(operation.equalsIgnoreCase("New Configuration")||operation.equalsIgnoreCase("Update Configuration")){
			props.setProperty(prop_key, prop_value);
		}
		else if(operation.equalsIgnoreCase("Remove Configuration")){
			props.remove(prop_key);
		}
		
		
		props.store(out, null);
		out.close();
	}
	
	private static String getComponentVersionProp(String username,
			String password, String baseUrl, String componentName)
			throws IllegalStateException, IOException, URISyntaxException,
			JSONException {
		HttpClientBuilder clientBuilder = new HttpClientBuilder();
		clientBuilder.setUsername(username);
		clientBuilder.setPassword(password);

		// for SSL enabled servers, accept all certificates
		clientBuilder.setTrustAllCerts(true);
		DefaultHttpClient client = clientBuilder.buildClient();

		HttpGet getReq = new HttpGet(new URI(baseUrl
				+ "/cli/component/versions?component=" + componentName));
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
		JSONArray versionNames = (JSONArray) JSONValue.parse(json);
		ArrayList<String> versionList = new ArrayList<String>();
		// Iterates the JSON Array
		for (int i = 0; i < versionNames.size(); i++) {
			JSONObject objects = (JSONObject) versionNames.get(i);
			// Gets the object named "name" and stores it in a string
			String name = objects.get("name").toString();
			versionList.add(name);
		}
		// System.out.println("version names list:::::"+versionNames);
		String latestVersion = versionList.get(0);
		//System.out.println("Latest version::" + latestVersion);
		return latestVersion;
	}
}
