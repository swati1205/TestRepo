package com.cognizant.IBMUDeploy.Century_Publish_Artifacts;

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
import java.util.regex.*;

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
		String ftp_path=args[6];
		
		PublishArtifacts.propParser(username, password,basePath, localPath,jobUrl,publish_version,ftp_path);


	}
	
	private static void propParser(String username,String password,String basePath,String localFilePath,String jobUrl,String publish_version,String ftp_path) throws IOException {
		FileInputStream in=null;
		File file=new File(basePath+"\\publish_mapping.properties");
		in = new FileInputStream(file);
		Properties props = new Properties();
		props.load(in);
		in.close();
		String sprint=null;
		for(String key : props.stringPropertyNames()) {
			  String value = props.getProperty(key);
			  //System.out.println(key + " = " + value);
			  String dir =localFilePath+"\\"+key;
			  Files.createDirectories(Paths.get(dir));
			  File src=null;
			  if(key.startsWith("DB")){
				  src=new File(basePath+"//"+ftp_path+"//DB"); 
				  File dest=new File(dir);
				  FileUtils.copyDirectory(src, dest);
			  }
			  else if(key.startsWith("hotdeploy")){
				  src=new File(basePath+"//"+ftp_path+"//Binaries//hotdeploy"); 
				  File dest=new File(dir);
				  FileUtils.copyDirectory(src, dest);
			  }
			  else if(key.startsWith("CM_PricePlanCache")){
				  src=new File(basePath+"//"+ftp_path+"//Binaries//CM_PricePlanCache"); 
				  File dest=new File(dir);
				  FileUtils.copyDirectory(src, dest);
			  }
			  else
			  {
				  src= new File (basePath+"//"+ftp_path+"//Binaries//"+key);
				  File dest=new File(dir+"\\"+key);
				  Files.copy(src.toPath(),dest.toPath() ,StandardCopyOption.REPLACE_EXISTING );
			  }
			  
					  //new File(basePath+"\\"+key+".war");
			  
			 
			  Matcher m = Pattern.compile("[0-9]{4}").matcher(key);
			 if(m.find())
					  {
				 		sprint=m.group();
					  }
			  sendPost(username,password,jobUrl,dir,value,sprint,key,publish_version);
			}

		
	}
	
	private static void sendPost(String username,String password,String jobUrl,String dir,String value,String sprint,String key,String publish_version){
		try{
			String urlParameters ="COMPONENT_NAME="+value+"&BASE_COMPONENT_PATH="+dir+"&MANIFEST_VERSION="+sprint+"&DEPLOY_FILENAME="+key+"&VERSION_NO="+publish_version;
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
