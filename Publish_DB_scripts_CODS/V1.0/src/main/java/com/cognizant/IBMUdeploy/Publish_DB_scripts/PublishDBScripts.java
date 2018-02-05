package com.cognizant.IBMUdeploy.Publish_DB_scripts;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;

public class PublishDBScripts {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String username=args[0];
		String password=args[1];
		String basePath=args[2];//the workspace path to copy the checked out files
		String localPath=args[3];//the local directory to create individual folders
		String jobUrl=args[4];//downstream job
		
		PublishDBScripts.propParser(username, password,basePath, localPath,jobUrl);


	}
	
	private static void propParser(String username,String password,String basePath,String localFilePath,String jobUrl) throws IOException {
		FileInputStream in=null;
		File file=new File(localFilePath+"\\publish_db.properties");
		in = new FileInputStream(file);
		Properties props = new Properties();
		props.load(in);
		in.close();
		
		for(String key : props.stringPropertyNames()) {
			  String value = props.getProperty(key);
			  //System.out.println(key + " = " + value);
			  String dir =localFilePath+"\\"+key;
			  Files.createDirectories(Paths.get(dir));
			  File src=new File(basePath+"\\"+key+".sql");
			  File dest=new File(dir+"\\"+key+".sql");
			  Files.copy(src.toPath(),dest.toPath() ,StandardCopyOption.REPLACE_EXISTING );
			  sendPost(username,password,jobUrl,dir,value);
			}

		
	}
	
	private static void sendPost(String username,String password,String jobUrl,String dir,String value){
		try{
			String urlParameters ="COMPONENT_NAME="+value+"&BASE_COMPONENT_PATH="+dir;
					//+"&VERSION="+versionNo;
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
