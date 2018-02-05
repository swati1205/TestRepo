import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.simple.*;


public class assignQualityGate {
	
	private static org.apache.log4j.Logger logger = Logger.getLogger(assignQualityGate.class);
	
	
	public static void main(String[] args) throws MalformedURLException,FileNotFoundException {
		// TODO Auto-generated method stub
		String projectKey =args[0];
		String qualityGate = args[1];
			try{
				assignQualityGate http = new assignQualityGate();
				String projectID = getIdofProject(projectKey);
				String gateID = getIDofGate(qualityGate);
				http.sendPost(projectID,gateID);
			} catch(FileNotFoundException e){
				System.out.println("Please enter a valid project key");
			} catch(IOException e1){
				System.out.println("Please enter a valid Quality Gate name");
			}
	}
	
	//method to get the ID of project by providing the key
	private static String getIdofProject(String projectKey) throws MalformedURLException, IOException,FileNotFoundException{
		String resourcesUrl = getPropData("res_Url");
		InputStream input = new URL(resourcesUrl+projectKey+"&format=json").openStream();
		JSONArray jsonArray = new JSONArray();
			JSONObject resource = (JSONObject) jsonArray.get(0);
			String projectID = resource.get("id").toString();
		return projectID;
	}
	//method to get the ID of the Quality Gate by providing the name
	private static String getIDofGate(String qualityGate) throws MalformedURLException, IOException{
		String gateID = null;
		String gateUrl = getPropData("gate_Url");
		InputStream input = new URL(gateUrl).openStream();
		JSONObject qualityGates=new JSONObject ();
		JSONArray jarray = (JSONArray) qualityGates.get("qualitygates");
		for(int i=0;i<jarray.size();i++){
			JSONObject eachGate = (JSONObject) jarray.get(i);
			if(eachGate.get("name").equals(qualityGate)){
				gateID = eachGate.get("id").toString();
			}
		}
		return gateID;
	}
	
	//method to assign the quality gate
	private void sendPost(String projectID, String gateID) throws IOException {
		String login = getPropData("Sonar_Login");
		String password = getPropData("Sonar_password");
		String assignUrl = getPropData("post_Url");
		// TODO Auto-generated method stub
		URL obj = new URL(assignUrl);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// add request header

		con.setRequestMethod("POST");
		con.setRequestProperty("Authorization",
				"Basic " + (new String(Base64.encodeBase64((login + ":" + password).getBytes()))));
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		
		String urlParameters = "gateId=" + gateID+"&projectId="+projectID;

		// Send post request

		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		logger.info("\nSending 'POST' request to URL : " + assignUrl);
		logger.info("Post parameters : " + urlParameters);
		logger.info("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		logger.info(response.toString());
	}
	
	//method to retrieve data from property file
	private static String getPropData(String text) throws IOException {
		// using properties class to load the properties file
		Properties properties = new Properties();
		properties.load(new FileInputStream("sonarqubeDetails.properties"));
		try {
			String propText = null;
			if (properties.getProperty(text) != null) {
				propText = properties.getProperty(text);
				logger.info( text+" data is "+propText);


			} else {

				logger.debug("No data available in properties file for" + text);
			}
		} catch (Exception ex) {
			logger.error("Exception occured:", ex);
		}
		return properties.getProperty(text);
	}
	

}
