package com.cognizant.sonarqube.project.bulkdelete;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * 
 * Java utility to delete bulk jobs in SonarQube.This java client send a POST
 * request to SonarQube platform running in server and deletes all the jobs
 * based on the given time period
 *
 * @author Cognizant GTO SEA Labs
 * @version 1.0
 */

public class SonarQubeProjectBulkDelete {

	// Initialize the variable and logger

	private static org.apache.log4j.Logger logger = Logger.getLogger(SonarQubeProjectBulkDelete.class);

	// Get the project id's in a list

	public static List getIdOfProjects() throws MalformedURLException, IOException, JSONException, ParseException {

		String period = getPropData("timeperiod");
		int p = Integer.parseInt(period);

		// get the date based on the time period given in the property file

		Date date = new DateTime().minusMonths(p).toDate();

		String ResourcesUrl = getPropData("Sonar_Rurl");

		List<String> list = new ArrayList<String>();
		InputStream input = new URL(ResourcesUrl).openStream();
		JSONArray jsonArray = new JSONArray(IOUtils.toString(input));

		// get the resources

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject resource = (JSONObject) jsonArray.get(i);
			String lastAnalysis = (String) resource.get("date");
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
			Date resultDate = df.parse(lastAnalysis);
			logger.info("Last analysis date of resource: " + resultDate);
			if (resultDate.compareTo(date) == -1) {
				Integer projectId = (Integer) resource.get("id");
				list.add(projectId.toString());
			}
		}
		return list;
	}

	// HTTP POST request

	private void sendPost(String id) throws Exception {

		// Reading values from properties file

		String url = getPropData("Sonar_Durl");
		String login = getPropData("Sonar_Login");
		String password = getPropData("Sonar_password");

		URL obj = new URL(url);

		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// add request header

		con.setRequestMethod("POST");
		con.setRequestProperty("Authorization",
				"Basic " + (new String(Base64.encodeBase64((login + ":" + password).getBytes()))));

		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		String urlParameters = "id=" + id;
		logger.info("urlParameters: " + urlParameters);

		// Send post request

		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		logger.info("\nSending 'POST' request to URL : " + url);
		logger.info("Post parameters : " + urlParameters);
		logger.info("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		logger.info(response.toString());
	}

	/**
	 * getPropData method - Load the FolderStructure Properties file
	 * 
	 * @param properties
	 *            FileName and properties data
	 * @throws IOException
	 * @throws FileNotFoundException
	 */

	public static String getPropData(String text) throws FileNotFoundException, IOException {
		// using properties class to load the properties file
		Properties properties = new Properties();
		properties.load(new FileInputStream("Sonarbulkretire.properties"));
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

	public static void main(String[] args) throws Exception {

		SonarQubeProjectBulkDelete http = new SonarQubeProjectBulkDelete();
		List list = getIdOfProjects();
		if (list.size() != 0) {
			for (int i = 0; i < list.size(); i++) {
				String id = list.get(i).toString();
				logger.info("Sending Http POST request");
				http.sendPost(id);
			}
		} else {
			logger.info("No project exists for the given time period");
		}
	}

}
