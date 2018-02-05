package com.cognizant.jenkins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class CreateHTMLReport {

	public static void main(String[] args) {
		String fileName=args[0];
		List<String> snapshotList=new ArrayList<String>();
		Map<String, String> map = new HashMap<String,String>();
		StringBuilder buf = new StringBuilder();
		File file = new File("index.html");
		// TODO Auto-generated method stub
		 try {
			    File jsonFile = new File(fileName);
				String jsonString = FileUtils.readFileToString(jsonFile);
				JSONObject obj = (JSONObject)JSONValue.parse(jsonString);
				
				JSONArray itemsArr=(JSONArray) obj.get("items");
				for(int i=0;i<itemsArr.size();i++){
					JSONObject obj1=(JSONObject)itemsArr.get(i);
					String ItemID=obj1.get("id").toString();
					
					String InQueueSince=obj1.get("inQueueSince").toString();
					String unixTime=InQueueSince.substring(0,10);
					long unixSeconds = Long.parseLong(unixTime);
					Date date = new Date(unixSeconds*1000L); // *1000 is to convert seconds to milliseconds
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss"); // the format of your date
					String formattedDate = sdf.format(date);
					Date d1 = sdf.parse(formattedDate);
					
					JSONObject obj2=(JSONObject)obj1.get("task");
					String jobName=obj2.get("name").toString();
					String jobUrl=obj2.get("url").toString();
					
					String reason=obj1.get("why").toString();
					map.put(ItemID, formattedDate+","+jobName+","+jobUrl+","+reason);
				}
				//System.out.println(map);
				
				buf.append("<html>" +
				           "<body>" +
				           "<table border=1>" +
				           "<tr>" +
				           "<th>Queue ID</th>" +
				           "<th>Start Time</th>" +
				           "<th>Job Name</th>" +
				           "<th>Job URL</th>" +
				           "<th>Reason</th>" +
				           "</tr>");
				
				for (Map.Entry<String, String> entry : map.entrySet()) {
			         String key = entry.getKey();
			         String value = map.get(key);
			         //Collection<String> value =  multimap.get(key);
			         //System.out.println(key + ":" + value);
			         String[]arr=value.toString().split("\\,");
			         //.append("<tr><td rowspan=1")
			         buf.append("<tr><td>"+key+"</td>" );
			         for(int i=0;i<2;i++){
			        	 	        		
			        		 buf.append("<td>"+arr[i]+"</td>");
			        		 //.append("</td></tr><tr><th>");
				         
			        	 }
			         buf.append("<td><a href="+arr[2]+">"+arr[2]+"</a></td>");
			         buf.append("<td>"+arr[3]+"</td>");
			         buf.append("</tr>");
			        	 
			         }
			         //System.out.println(multimap2);
			      
				buf.append("</table>" +
				           "</body>" +
				           "</html>");
				String html = buf.toString();
				//System.out.println("Html is: "+html);
				//System.out.println(multimap3);
				
			    OutputStream outputStream = new FileOutputStream(file.getAbsoluteFile());
			    Writer writer=new OutputStreamWriter(outputStream);
			    writer.write(html);
			    writer.close();

				
			    } catch (Exception e) {
				e.printStackTrace();
			    }
		 

	}


}
