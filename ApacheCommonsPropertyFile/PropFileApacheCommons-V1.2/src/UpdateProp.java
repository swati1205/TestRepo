import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;
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


public class UpdateProp {

	public static void main(String[] args) throws IOException, IllegalStateException, URISyntaxException, JSONException, ConfigurationException {
		// TODO Auto-generated method stub
		//suppress log4j messages from UCD library
		Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		String username = args[0];
		String password = args[1];
		String baseUrl = args[2];
		String jsonFilePath = args[3];
		String basePath = args[4];
		
		jsonParser(username,password,baseUrl,jsonFilePath,basePath);
		/*try {
			PropertiesConfiguration  configuration = new PropertiesConfiguration("D://Excel files//property1//Component-A_prop1.properties");
			PropertiesConfigurationLayout pcl= new PropertiesConfigurationLayout((PropertiesConfiguration) configuration);
			configuration.addProperty("anamika", "1234");
			configuration.setProperty("Udeploy_URL", "http://10.124.131.83:8080/");
			configuration.clearProperty("JSON_Filepath");
			pcl.setComment("anamika", "Just added comment");
			//configuration.getLayout().setComment("anamika", "This is my comment.");
			configuration.save( new FileWriter( "D://Excel files//property1//new_file.properties"));
			
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}*/
}

	private static void jsonParser(String username, String password,
			String baseUrl, String jsonFilePath, String basePath) throws IOException, IllegalStateException, URISyntaxException, JSONException, ConfigurationException {
		// TODO Auto-generated method stub
		File jsonFile = new File(jsonFilePath);
		String jsonString = FileUtils.readFileToString(jsonFile);
		JSONObject obj = (JSONObject) JSONValue.parse(jsonString);
		String ReleaseVersion =obj.get("Release_Version").toString();
		JSONArray envArr= (JSONArray) obj.get("Environment");
	    String environment= envArr.get(0).toString();

		//create a property file to store latest version numbers of components
		Path path =Paths.get(basePath+"\\component_versions.properties");
		if(Files.notExists(path)){
			File f=new File(basePath+"\\component_versions.properties");
			f.createNewFile();
		}
		File propsfile= new File(basePath+"\\component_versions.properties");
		Properties props =new Properties();
		FileInputStream in=new FileInputStream(propsfile);
		props.load(in);
		in.close();
		FileOutputStream out = new FileOutputStream(propsfile);
		//props.store(out, null);
		
		JSONArray propArr = (JSONArray) obj.get("Property_File");
		for (int i = 0; i < propArr.size(); i++) {
			JSONObject obj1 = (JSONObject) propArr.get(i);
			String componentName = obj1.get("File").toString();
			String[] arr = componentName.split("\\.");
			String fileName = arr[arr.length - 1] + ".properties";
			String performFunction = obj1.get("Operation").toString();
			String prop_key = obj1.get("Property_Name").toString();
			String prop_value = obj1.get("Property_value").toString();
			String versionNo = getComponentVersionProp(username, password,
					baseUrl, componentName,environment);
			props.setProperty(componentName, versionNo);
			//String ReleaseVersion = obj1.get("Realease_Version").toString();
			
			String dir = basePath + "\\" + componentName + "\\"+ ReleaseVersion;
			Files.createDirectories(Paths.get(dir));
			File file = new File(basePath + "\\" + componentName + "\\"+ ReleaseVersion + "\\" + fileName);
			UpdateProp.updateFile(username, password, baseUrl, fileName, prop_key,
					prop_value, file, componentName, versionNo, dir,performFunction,ReleaseVersion);
	}
		props.store(out, null);
	}
	
	private static void updateFile(String username, String password,
			String baseUrl, String fileName, String prop_key,
			String prop_value, File file, String componentName,
			String versionNo, String dir,String operation,String ReleaseVersion) throws IOException, ConfigurationException{
		
		FileInputStream in=null;
		PropertiesConfiguration  configuration=null;
		if (file.exists()) {
		configuration = new PropertiesConfiguration(file);
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
				configuration = new PropertiesConfiguration(dir+"\\"+fileName);
			}catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}

		in.close();
		
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		//System.out.println(dateFormat.format(cal.getTime())); //11/04/2016 16:00:22

		
		FileOutputStream out = new FileOutputStream(file);
		if(operation.equalsIgnoreCase("New Configuration")){
			configuration.addProperty(prop_key, prop_value);
			configuration.getLayout().setComment(prop_key, "This property is added by Cloudset job on "+dateFormat.format(cal.getTime())+" for the Release Version "+ReleaseVersion+".");
		}
		else if(operation.equalsIgnoreCase("Update Configuration")){
			configuration.setProperty(prop_key, prop_value);
			configuration.getLayout().setComment(prop_key, "This property is modified by Cloudset job on "+dateFormat.format(cal.getTime())+" for the Release Version "+ReleaseVersion+".");
		}
		else if(operation.equalsIgnoreCase("Remove Configuration")){
			configuration.clearProperty(prop_key);
		}

		
		configuration.save( new FileWriter(file));
		out.close();
	}
	
	private static String getComponentVersionProp(String username,
			String password, String baseUrl, String componentName,String environment)
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
		//String environment= "LKG";
		String json = outputJson.toString();
		JSONArray versionNames = (JSONArray) JSONValue.parse(json);
		ArrayList<String> versionList = new ArrayList<String>();
		// Iterates the JSON Array
		for (int i = 0; i < versionNames.size(); i++) {
			JSONObject objects = (JSONObject) versionNames.get(i);
			// Gets the object named "name" and stores it in a string
			String name = objects.get("name").toString();
			if(name.startsWith(environment))
			{
			versionList.add(name);
			}
			else{
				versionList.add(name);	
			}
		}
		
		String latestVersion = versionList.get(0);
		//System.out.println("Latest version is: "+latestVersion);
		return latestVersion;
	}
}