package com.cognizant.IBMUdeploy.CPQ_Publish_Artifacts;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

public class PublishArtifacts {

	public static void main(String[] args) throws IOException {
		String username=args[0];
		String password=args[1];
		String basePath=args[2];//the workspace path to copy the checked out files
		String localPath=args[3];//the local directory to create individual folders
		String jobUrl=args[4];//downstream job
		String publish_version=args[5];//version to which files will be published
		
		PublishArtifacts.propParser(username, password,basePath, localPath,jobUrl,publish_version);


	}
	
	private static void propParser(String username,String password,String basePath,String localFilePath,String jobUrl,String publish_version) throws IOException {
		FileInputStream in=null;
		File file=new File(basePath+"//publish_mapping.properties");
		in = new FileInputStream(file);
		Properties props = new Properties();
		props.load(in);
		in.close();
		//String sprint=null;
		for(String key : props.stringPropertyNames()) {
			  String value = props.getProperty(key);
			  //System.out.println(key + " = " + value);
			  String dir =localFilePath+"//"+key;
			  Files.createDirectories(Paths.get(dir));
			  File src=null;		  
				  src= new File (basePath+"//external_deployments//"+key);
				  File dest=new File(dir+"//"+key);
				  Files.copy(src.toPath(),dest.toPath() ,StandardCopyOption.REPLACE_EXISTING );
			  sendPost(username,password,jobUrl,dir,value,publish_version);
			}

		
	}
	
	private static void sendPost(String username,String password,String jobUrl,String dir,String value,String publish_version){
		try{
			String urlParameters ="COMPONENT_NAME="+value+"&BASE_COMPONENT_PATH="+dir+"&VERSION_NO="+publish_version;
			System.out.println(urlParameters);
			URL url = new URL(jobUrl);
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        System.out.println("Connected to Cloudset");
	        // add request header
	        conn.setConnectTimeout(50000);
	        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
	        conn.setDoOutput(true);
	        conn.setDoInput(true);
	        conn.setRequestMethod("POST");
	        //Cloudset user authentication
	        conn.setRequestProperty(
	        		"Authorization",
	        		"Basic "
	        		+ (new String(Base64.encodeBase64((username+":"+password)
	        		.getBytes()))));
	    
	        // send POST request
	        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			
			int responseCode = conn.getResponseCode();
			if (responseCode==201){
				System.out.println("Response Code :"+responseCode);
				System.out.println("Build Triggered");
			}
			else{
				System.out.println("Cloudset Build Trigger Failed,Response Code :"+responseCode);
				
				}
		  	}
			
		  catch (Exception ex) {
			  ex.printStackTrace();
				}
				
	}


}
