package com.cognizant.jenkins.BackupJenkinsWorkspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.nio.file.*;
import java.nio.file.attribute.*;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;



public class BackupLatest {
	

	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		File[] files = new File (getPropData("Backup_Folder_Path")).listFiles();
	    showFiles(files);
	}
	public static void showFiles(File[] files) throws FileNotFoundException, IOException {
		 List<Date> datelist = new ArrayList<Date>();
		 TreeSet<Date> set = new TreeSet<Date>();
		 TreeSet<Date> set1= new TreeSet<Date>();
	    for (File file : files) {
	        
	            //System.out.println("Zip Directory: " + file.getName());//will only list folder level zip files
	             Path path = Paths.get(getPropData("Backup_Folder_Path")+"\\"+file.getName());
	             BasicFileAttributes attr;
	             attr = Files.readAttributes(path, BasicFileAttributes.class);
	             Date creationDate =new Date(attr.creationTime().toMillis());
	             //System.out.println("Creation Date of "+path+" is: "+creationDate);
	             set.add(creationDate);
	             datelist.add(creationDate);
	             Collections.sort(datelist,Collections.reverseOrder());
	             System.out.println("DL: "+datelist);
	            
	             //System.out.println("Last access date: " + attr.lastAccessTime());
	             //System.out.println("Last modified date: " + new Date (attr.lastModifiedTime().toMillis()));
	    }
	 
	             System.out.println(datelist.get(0));
	             set1.add(datelist.get(0));
	             System.out.println(datelist.get(1));
	             set1.add(datelist.get(1));
	             System.out.println(datelist.get(2));
	             set1.add(datelist.get(2));
	             
	             System.out.println("Whole set: "+set);
	             System.out.println("Latest three: "+set1);
	             
	             set.removeAll(set1);
	             System.out.println("To be deleted: "+set);
	             
	             for(File file:files){
	     			File destinationDir = new File("D:\\destination");
	            	 Path path = Paths.get(getPropData("Backup_Folder_Path")+"\\"+file.getName());
	            	 BasicFileAttributes attr;
	            	 attr = Files.readAttributes(path, BasicFileAttributes.class);
	            	 Date creationDate =new Date(attr.creationTime().toMillis());
	            	//to delete files older than 3 days
	            	 /*
	  	           Date dateBeforeThreeDays = new DateTime().minusDays(3).toDate();
	  	           if(creationDate.compareTo(dateBeforeThreeDays)>0)
	  	           {
	  	        	 System.out.println(file.delete());
	  	           }
	  	           */
	  	           //to delete all except latest three
	            	 if(set.contains(creationDate))
	            	 {
	            		// System.out.println(file.delete());
	            		 FileUtils.deleteDirectory(file);
	            	 }
	            	 if(set1.contains(creationDate))
	     			{
	     				FileUtils.copyDirectoryToDirectory(file,destinationDir);
	     			}
	             }
	             //Files.move(Paths.get("/foo.txt"), Paths.get("bar.txt"));
	             
	    
	           
	         
	}
	 public static String getPropData(String text) throws FileNotFoundException, IOException { 
	    	//using properties class to load the properties file 
	    	Properties properties = new Properties();
	    	properties.load(new FileInputStream("FolderProp.properties"));
	    	try {
	    		String propText=null;
	    		if(properties.getProperty(text)!=null) {
	    			propText=properties.getProperty(text);
	    		//	logger.info( text+" data is "+propText); 
	    			//System.out.println(text+" data is "+propText);
	    		 } 
	    		else {
	    			
	    			//logger.error("No data available in properties file for"+text);
	    			}    		
	    		}
	    	catch (Exception ex) {
	    		 //logger.error("Exception occured:", ex);
	    			}
 	return properties.getProperty(text);
	    }

}
