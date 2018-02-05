package com.cognizant.Workbench_Bulk_Job_Creation;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class BulkJobCreation {

	
//Initialize the variable and logger 
    
    private static org.apache.log4j.Logger logger= Logger.getLogger(BulkJobCreation.class);
  
	public static void main(String[] args) throws IOException {
		String sprint=args[0];
		String username=args[1];
		String password=args[2];
		String component_excel_path=args[3];
		String Job_Url=args[4];
		BulkJobCreation.excelParser(sprint,username,password,component_excel_path,Job_Url);

	    }
	    
	 @SuppressWarnings({ "rawtypes", "unchecked" })
		private static void excelParser(String SPRINT_NO,String username,String password,
				   String fileName,String Job_Url) throws IOException{		 
			// Create an ArrayList to store the data read from excel sheet.
		        List sheetData = new ArrayList();

		        FileInputStream fis = null;
		        try {

		    // Create a FileInputStream that will be use to read the excel file.

		            fis = new FileInputStream(fileName);

		    // Create an excel workbook from the file system.

		            XSSFWorkbook workbook = new XSSFWorkbook(fis);

		    // Get the first sheet on the workbook.

		            XSSFSheet sheet = workbook.getSheetAt(0);

		
		// When we have a sheet object in hand we can iterate on
		// each sheet's rows and on each row's cells. 
		
		            Iterator rows = sheet.rowIterator();
		            while (rows.hasNext()) {
		                XSSFRow row = (XSSFRow) rows.next();
		                Iterator cells = row.cellIterator();

		                List data = new ArrayList();
		                while (cells.hasNext()) 
		                		{
		                    		XSSFCell cell = (XSSFCell) cells.next();
		                    		data.add(cell);
		                		}

		                sheetData.add(data);
		            }
		   
		        for (int i = 1; i < sheetData.size(); i++) {
		            List list = (List) sheetData.get(i);
		            XSSFCell componentTemplate= (XSSFCell) list.get(0);
	                XSSFCell componentName = (XSSFCell) list.get(1);
	                XSSFCell gitUrl = (XSSFCell) list.get(2);
	                
	                String COMPONENT_NAME_TEMPLATE=componentTemplate.getRichStringCellValue().getString();
	                String COMPONENT_NAME=componentName.getRichStringCellValue().getString();
	                String GIT_URL=gitUrl.getRichStringCellValue().getString();
	                
	                BulkJobCreation.sendPost(COMPONENT_NAME_TEMPLATE,COMPONENT_NAME,GIT_URL,SPRINT_NO,
	                		username,password,Job_Url);      
		        	}
			   }
		        catch (Exception ex) {
		        	 ex.printStackTrace();
		      	}
			   finally {
		           if (fis != null) {
		               fis.close();
		           }
		   
			   }
		 }

	 public static void sendPost(String COMPONENT_NAME_TEMPLATE, String COMPONENT_NAME, String GIT_URL,
			   String SPRINT_NO,String username,String password,String jobUrl) 
					   throws Exception     
		{
	  		
	  try {
         String urlParameters = "COMPONENT_NAME="+COMPONENT_NAME+"&COMPONENT_NAME_TEMPLATE="+COMPONENT_NAME_TEMPLATE+"&GIT_URL="+GIT_URL+"&SPRINT_NO="+SPRINT_NO;
         System.out.println("Build Parameters:"+urlParameters);
         //String jobUrl=getPropData("Cloudset_Job_url");
         URL url = new URL(jobUrl);
         //String username = getPropData("Cloudset_User");
         //String password = getPropData("Cloudset_Password");
         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
         System.out.println("Connecting to Cloudset");
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
 			System.out.println("Cloudset Job Created Successfully");
 		}
 		else{
 			System.out.println("Cloudset Job Creation Failed,Response Code :"+responseCode);
 			
 			}
	  	}
 		
	  catch (Exception ex) {
		   ex.printStackTrace();
 			}
			
  }
		  

	}


