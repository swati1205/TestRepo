package com.cognizant.ParseExcelCAFE;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.cognizant.ParseExcelCAFE.ParseExcel;

public class ParseExcel {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		ParseExcel.excelParser();
	

	}
	 private static void excelParser() throws IOException{
		 String fileName="D:\\CAFE_Components_SelfService.xlsx";
		 
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
	        		            System.out.println(list);
	        		                XSSFCell componentName= (XSSFCell) list.get(0);
	        		                XSSFCell componentType = (XSSFCell) list.get(1);
	        		                XSSFCell solutionPath = (XSSFCell) list.get(2);
	        		                XSSFCell projectPath = (XSSFCell) list.get(3);
	        		                XSSFCell prepareTarget = (XSSFCell) list.get(4);
	        		                XSSFCell buildTarget = (XSSFCell) list.get(5);
	        		                XSSFCell unitPath = (XSSFCell) list.get(6);
	        		                XSSFCell skipTest = (XSSFCell) list.get(7);
	        		                
	        		                String COMPONENT_NAME=componentName.getRichStringCellValue().getString();
	        		                String COMPONENT_TYPE=componentType.getRichStringCellValue().getString();
	        		                String BUILD_SLN_PATH=solutionPath.getRichStringCellValue().getString();
	        		                String PREPARE_PROJ_PATH=projectPath.getRichStringCellValue().getString();
	        		                String PREPARE_TARGET=prepareTarget.getRichStringCellValue().getString();
	        		                String BUILD_TARGET=buildTarget.getRichStringCellValue().getString();
	        		                String UNIT_TEST_PATH=unitPath.getRichStringCellValue().getString();
	        		                String SKIP_TEST=skipTest.getRichStringCellValue().getString();
	        		                
	        		                System.out.println("COMPONENT_NAME="+COMPONENT_NAME);
	        		                ParseExcel.displayParameters(COMPONENT_NAME,COMPONENT_TYPE,BUILD_SLN_PATH,PREPARE_PROJ_PATH,
	        		                		PREPARE_TARGET,BUILD_TARGET,UNIT_TEST_PATH,SKIP_TEST);
	        		        	}
	        			   }
	        		        catch (Exception ex) {
	        		        	 //logger.error("Exception occured:", ex);
	        		      	}
	        			   finally {
	        		           if (fis != null) {
	        		               fis.close();
	        		           }
	        		   
	        			   }
	        		 }
	 
	 public static void displayParameters(String cOMPONENT_NAME, String cOMPONENT_TYPE, String bUILD_SLN_PATH, String pREPARE_PROJ_PATH, String pREPARE_TARGET, String bUILD_TARGET, String uNIT_TEST_PATH, String sKIP_TEST){
		 System.out.println("COMPONENT_NAME="+cOMPONENT_TYPE);
	 }
	        		   
	        		   
	 }


