package com.cognizant.IBM_Modify_PropFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.urbancode.commons.httpcomponentsutil.HttpClientBuilder;

public class ModifyPropfile {

	public static void main(String[] args) throws IOException, URISyntaxException {
		
		// suppress log4j messages from UCD library
		Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF); 
		String username=args[0];
		String password=args[1];
		String baseUrl=args[2];
		//String fileName=args[3];
		String jsonFilePath=args[3];
		//String prop_key=args[4];
		//String prop_value=args[5];
		//String performFunction =args[6];
		//String componentName=args[7];
		//String versionNo=args[8];
		String basePath=args[4];
		File jsonFile = new File(jsonFilePath);
		String jsonString = FileUtils.readFileToString(jsonFile);
		JSONObject obj = (JSONObject)JSONValue.parse(jsonString);
	    JSONArray propArr= (JSONArray) obj.get("Property_File");
	    for(int i=0;i<propArr.size();i++){
	    	JSONObject obj1=(JSONObject)propArr.get(i);
			String fileName=obj1.get("File").toString()+".properties";
			String[] arr=fileName.split("_");
			String componentName=arr[0];
  		    String performFunction=obj1.get("Opertaion").toString();
  		    String prop_key=obj1.get("Property_Name").toString();
  		    String prop_value=obj1.get("Property_value").toString();
  		    String versionNo=obj1.get("Realease_Version").toString();
  		    String dir=basePath+"\\"+componentName+"\\"+versionNo;
  		    Files.createDirectories(Paths.get(dir));
			File file = new File(basePath+"\\"+componentName+"\\"+versionNo+"\\"+fileName);
  		  if(performFunction.equalsIgnoreCase("modify")){
  			  modifyProp(username,password,baseUrl,fileName,prop_key,prop_value,file,componentName,versionNo);
  			  System.out.println(file+" "+componentName+" "+prop_key+" "+prop_value+" "+versionNo);
  		}
  		else if(performFunction.equalsIgnoreCase("delete")){
  			deleteProp(username,password,baseUrl,fileName,prop_key,prop_value,file,componentName,versionNo);
  			System.out.println(file+" "+componentName+" "+prop_key+" "+prop_value+" "+versionNo);
  		}
  		else if(performFunction.equalsIgnoreCase("add")){
  			addProp(username,password,baseUrl,fileName,prop_key,prop_value,file,componentName,versionNo);
  			System.out.println(file+" "+componentName+" "+prop_key+" "+prop_value+" "+versionNo);
  		}
  		//addPropFiletoVersion(username,password,baseUrl,dir);
	    }
	    
		
		
	}
	
	private static void modifyProp(String username,String password,String baseUrl,String fileName,String prop_key,String prop_value,File file,String componentName,String versionNo){
		HttpClientBuilder clientBuilder = new HttpClientBuilder();
		clientBuilder.setUsername(username);
		clientBuilder.setPassword(password);
		clientBuilder.setTrustAllCerts(true);
		DefaultHttpClient client = clientBuilder.buildClient();
		BufferedWriter bw = null;
		BufferedReader br =null;
		try {
			HttpGet getReq = new HttpGet(new URI(baseUrl+"/cli/version/downloadArtifacts?component="+componentName+"&version="+versionNo+"&location=.&singleFilePath="+fileName));
			HttpResponse response= client.execute(getReq);
			br = new BufferedReader(new InputStreamReader(
	                (response.getEntity().getContent())));
			bw = new BufferedWriter(new FileWriter(file));
			String output;
			while ((output = br.readLine()) != null) {
				if (output.contains(prop_key+"="))
					output = output.replaceAll(prop_key+"=.*", prop_key+"="+prop_value);
		            bw.write(output+"\n");
				
					
			}
			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
	         try {
	             if(br != null)
	                br.close();
	          } catch (IOException e) {
	             //
	          }
	          try {
	             if(bw != null)
	                bw.close();
	          } catch (IOException e) {
	             //
	          }
	       }
		
		
	}
	
	private static void deleteProp(String username,String password,String baseUrl,String fileName,String prop_key,String prop_value,File file,String componentName,String versionNo){
		HttpClientBuilder clientBuilder = new HttpClientBuilder();
		clientBuilder.setUsername(username);
		clientBuilder.setPassword(password);
		clientBuilder.setTrustAllCerts(true);
		DefaultHttpClient client = clientBuilder.buildClient();
		BufferedWriter bw = null;
		BufferedReader br =null;
		try {
			HttpGet getReq = new HttpGet(new URI(baseUrl+"/cli/version/downloadArtifacts?component="+componentName+"&version="+versionNo+"&location=.&singleFilePath="+fileName));
			HttpResponse response= client.execute(getReq);
			br = new BufferedReader(new InputStreamReader(
	                (response.getEntity().getContent())));
			bw = new BufferedWriter(new FileWriter(file));
			String output;
			while ((output = br.readLine()) != null) {
				if (output.contains(prop_key+"="))
					output = output.replaceAll(prop_key+"=.*", "#"+output);
		            bw.write(output+"\n");
			}
			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
	         try {
	             if(br != null)
	                br.close();
	          } catch (IOException e) {
	             //
	          }
	          try {
	             if(bw != null)
	                bw.close();
	          } catch (IOException e) {
	             //
	          }
	       }
		
		
	}
	
	private static void addProp(String username,String password,String baseUrl,String fileName,String prop_key,String prop_value,File file,String componentName,String versionNo){
		HttpClientBuilder clientBuilder = new HttpClientBuilder();
		clientBuilder.setUsername(username);
		clientBuilder.setPassword(password);
		clientBuilder.setTrustAllCerts(true);
		DefaultHttpClient client = clientBuilder.buildClient();
		BufferedWriter bw = null;
		BufferedReader br =null;
		try {
			HttpGet getReq = new HttpGet(new URI(baseUrl+"/cli/version/downloadArtifacts?component="+componentName+"&version="+versionNo+"&location=.&singleFilePath="+fileName));
			HttpResponse response= client.execute(getReq);
			br = new BufferedReader(new InputStreamReader(
	                (response.getEntity().getContent())));
			bw = new BufferedWriter(new FileWriter(file));
			String output;
			while ((output = br.readLine()) != null) {
				if (output.contains(prop_key+"="))
					output = output.replaceAll(prop_key+"=.*", prop_key+"="+prop_value);
				
		            bw.write(output+"\n");
			}
			bw.append(prop_key+"="+prop_value);
			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
	         try {
	             if(br != null)
	                br.close();
	          } catch (IOException e) {
	             //
	          }
	          try {
	             if(bw != null)
	                bw.close();
	          } catch (IOException e) {
	             //
	          }
	       }
		
		
	}
	
	private static void addPropFiletoVersion(String username,String password,String baseUrl,String localFilePath) throws URISyntaxException, ClientProtocolException, IOException {
		HttpClientBuilder clientBuilder = new HttpClientBuilder();
		  clientBuilder.setUsername(username);
		  clientBuilder.setPassword(password);

		  // for SSL enabled servers, accept all certificates
		  clientBuilder.setTrustAllCerts(true); 
		  DefaultHttpClient client = clientBuilder.buildClient();
		  HttpPost postReq = new HttpPost(new URI(baseUrl+"/cli/version/addFiles?"));
		  List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		  urlParameters.add(new BasicNameValuePair("component", "anamika-comp"));
		  urlParameters.add(new BasicNameValuePair("version", "1.0"));
		  urlParameters.add(new BasicNameValuePair("base", localFilePath));
		  postReq.setEntity(new UrlEncodedFormEntity(urlParameters));
		  //component=Component-B&version=3.0&base="+localFilePath
           HttpResponse response = client.execute(postReq);
           
           String output = null;
            if(response.getStatusLine().getStatusCode()!=204){
            	BufferedReader br = new BufferedReader(new InputStreamReader(
                        (response.getEntity().getContent())));
            	 while ((output = br.readLine()) != null) {
            		System.out.println(response.getStatusLine().getReasonPhrase());
            }
             
            }else{
            	System.out.println("The server has fulfilled the request. There is no additional content to send in the response.");
            	
            }
	}
	
	

}
