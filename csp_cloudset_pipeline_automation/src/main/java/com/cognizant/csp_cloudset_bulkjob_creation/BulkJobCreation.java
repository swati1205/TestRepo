package com.cognizant.csp_cloudset_bulkjob_creation;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;




/**
* 
* Java utility to create bulk jobs in Cloudset.This java client makes a webservice call to CLoudset platform running in 
* server and creates bulk job in Cloudset
*
* @author  Cognizant GTO SEA Labs
* @version 1.2
**/ 

public class BulkJobCreation {
	
    //Initialize the variable and logger 
    
    private static org.apache.log4j.Logger logger= Logger.getLogger(BulkJobCreation.class);

	public static void main(String[] args) throws IOException {
		//take two arguments and pass to excel parser as sprint_no,username and password
				//String sprint=args[0];
				String username= args[0];
				String password= args[1];
				String Excel_fileName=args[2];
				String CloudsetJobUrl=args[3];
				BulkJobCreation.excelParser(username,password,Excel_fileName,CloudsetJobUrl);

	}
	

    /**
    * 
    * This method is needed to extract values from Excel Sheet and pass it as parameter to Cloudset Jobs .
    *
    * @param action must be an operation that has registered itself with the object
    * @return null
    * @exception IOException is thrown when file is not found in the mentioned path.
    *
    */
	
private static void excelParser(String username,String password,String fileName,String CloudsetJobUrl) throws IOException{
		   
		
		// Excel Sheet Data are passed as Parameters to Cloudset job  
		   
		   //String fileName=getPropData("Resource_Excel_Path");
	 
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
	        for (int i = 1; i < sheetData.size()-1; i++) {
	            List list = (List) sheetData.get(i);
	            List list2=(List) sheetData.get(i+1);
	            XSSFCell componentName= (XSSFCell) list.get(0);
	            XSSFCell componentName2= (XSSFCell) list2.get(0);
	            XSSFCell componentType = (XSSFCell) list.get(1);
	            XSSFCell currentSprintNo = (XSSFCell) list.get(2);
	            XSSFCell currentSprintNo2 = (XSSFCell) list2.get(2);
	            XSSFCell branchSprintNo = (XSSFCell) list.get(3);
	            XSSFCell branchSprintNo2 = (XSSFCell) list2.get(3);
                XSSFCell svnPath = (XSSFCell) list.get(4); 
                XSSFCell skipTest = (XSSFCell) list.get(5);
                
                String COMPONENT_NAME=componentName.getRichStringCellValue().getString();
                String COMPONENT_TYPE=componentType.getRichStringCellValue().getString();
                String CUR_SPRINT_NO=currentSprintNo.getRichStringCellValue().getString();
                String BRANCH_SPRINT_NO=branchSprintNo.getRichStringCellValue().getString();
                String SVN_PATH=svnPath.getRichStringCellValue().getString();
                Boolean SKIP_TEST=skipTest.getBooleanCellValue();
               // System.out.println(componentName2.getRichStringCellValue().getString());
                if(!componentName.getRichStringCellValue().getString().toString().equals("N/A")){
                	 String Downstream_Job=currentSprintNo2.getRichStringCellValue().getString()+"_branch_CSP_"+componentName2.getRichStringCellValue().getString()+"_"+branchSprintNo2.getRichStringCellValue().getString();
                	//String Downstream_Job="N/A";
                	//System.out.println(Downstream_Job);

                    BulkJobCreation.sendPost(COMPONENT_NAME,COMPONENT_TYPE,CUR_SPRINT_NO,BRANCH_SPRINT_NO,SVN_PATH,SKIP_TEST,
                    username,password,CloudsetJobUrl,Downstream_Job);
               }
                else{
                	//String Downstream_Job=currentSprintNo2.getRichStringCellValue().getString()+"_"+componentName2.getRichStringCellValue().getString()+"_"+branchSprintNo2.getRichStringCellValue().getString();
                	//String Downstream_Job="N/A";
                	//System.out.println(Downstream_Job);
                }
               
               
                
	        	}
		   }
	        catch (Exception ex) {
	        	 logger.error("Exception occured:", ex);
	      	}
		   finally {
	           if (fis != null) {
	               fis.close();
	           }
	   
		   }
	 }


/**
 * sendPost method - Method to trigger Cloudset Jobs.    
 * @param  Cloudset job Parameters 
 * @throws Exception 
 * 
 */ 

public static void sendPost(String COMPONENT_NAME, String COMPONENT_TYPE,String CUR_SPRINT_NO,String BRANCH_SPRINT_NO,
		   String SVN_PATH,Boolean SKIP_TEST,String username,String password,String jobUrl,String DOWNSTREAM_JOB) 
				   throws Exception     
	{
		
try {
    String urlParameters = "COMPONENT_NAME="+COMPONENT_NAME+"&CUR_SPRINT_NO="+CUR_SPRINT_NO+"&BRANCH_SPRINT_NO="+BRANCH_SPRINT_NO+
    		"&SVN_PATH="+SVN_PATH+"&COMPONENT_TYPE="+COMPONENT_TYPE+
    		"&Skip-Tests="+SKIP_TEST+"&DOWNSTREAM_JOB="+DOWNSTREAM_JOB;
    logger.info("Build Parameters:"+urlParameters);
   // String jobUrl=getPropData("Cloudset_Job_url");
    URL url = new URL(jobUrl);
    //String username = getPropData("Cloudset_User");
    //String password = getPropData("Cloudset_Password");
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    logger.info("Connecting to Cloudset");
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
		logger.info("Response Code :"+responseCode);
		logger.info("Cloudset Job Created Successfully");
	}
	else{
		logger.warn("Cloudset Job Creation Failed,Response Code :"+responseCode);
		
		}
	}
	
catch (Exception ex) {
		 logger.error("Exception occured:", ex);
		}
		
}

/**
 * getPropData method - Load the FolderStructure Properties file     
 * @param  properties FileName and properties data  
 * @throws IOException 
 * @throws FileNotFoundException 
 */    
 
 public static String getPropData(String text) throws FileNotFoundException, IOException { 
 	//using properties class to load the properties file 
 	Properties properties = new Properties();
 	properties.load(new FileInputStream("CloudsetXLS.properties"));
 	try {
 		String propText=null;
 		if(properties.getProperty(text)!=null) {
 			propText=properties.getProperty(text);
 		//	logger.info( text+" data is "+propText); 
 		 } 
 		else {
 			
 			logger.error("No data available in properties file for"+text);
 			}    		
 		}
 	catch (Exception ex) {
 		 logger.error("Exception occured:", ex);
 			}
	return properties.getProperty(text);
 }
 

}
