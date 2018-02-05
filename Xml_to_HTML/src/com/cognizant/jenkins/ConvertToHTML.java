package com.cognizant.jenkins;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class ConvertToHTML {

	public static void main(String[] args) {
		String xmlFile=args[0];
		List<String> compAndVer=new ArrayList<String>();
		List<String> snapshotList=new ArrayList<String>();
		Multimap<String, String> multimap = ArrayListMultimap.create();
		Multimap<String, String> multimap2 = ArrayListMultimap.create();
		Multimap multimap3 = ArrayListMultimap.create();
		StringBuilder buf = new StringBuilder();
		File file = new File("index.html");
		// TODO Auto-generated method stub
		 try {

				File fXmlFile = new File(xmlFile);
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(fXmlFile);

				//optional, but recommended
				//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
				doc.getDocumentElement().normalize();
				

				//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
				NodeList nList1 = doc.getElementsByTagName("snapshotapplication");
				for(int temp1 = 0; temp1 < nList1.getLength(); temp1++){
					Node nNode1 = nList1.item(temp1);
					Element eElement1 = (Element) nNode1;
					//System.out.println("Snapshot : " + eElement1.getElementsByTagName("name").item(0).getTextContent());
					String snapshot_name=eElement1.getElementsByTagName("name").item(0).getTextContent().toString();
					snapshotList.add(snapshot_name);
					String comp=eElement1.getElementsByTagName("component").item(0).getTextContent().toString().replaceAll("\\n",";").replaceAll("\\s","");
					//System.out.println("Comp : " + comp.substring(1, comp.length()-1));
					multimap.put(snapshot_name, comp.substring(1, comp.length()-1));
					String []arr= comp.split(";");
					
					
					//System.out.println("----------------------------");
				}
				buf.append("<html>" +
				           "<body>" +
				           "<table border=1>" +
				           "<tr>" +
				           "<th>Snapshot-Application</th>" +
				           "<th>Component Name</th>" +
				           "<th>Versions</th>" +
				           "</tr>");
				//System.out.println(multimap);
				Map<String, Collection<String>> map = multimap.asMap();
				for (Map.Entry<String,  Collection<String>> entry : map.entrySet()) {
			         String key = entry.getKey();
			         Collection<String> value =  multimap.get(key);
			         System.out.println(key + ":" + value);
			         String[]arr=value.toString().replaceAll("\\[", "").replaceAll("\\]","").split("\\,");
			         buf.append("<tr><td rowspan="+arr.length+">")
	        		 .append(key+"</td><td>" );
			         for(int i=0;i<arr.length;i++){
			        	 //System.out.println("Is: "+arr[i]);
			        	 String []arr2=arr[i].split("\\;");
			        	 for(int j=0;j<arr2.length;){
			        	 //System.out.println("comp: "+arr2[j]);
			        	 //System.out.println("ver: "+arr2[j+1]);
			        		
			        		 buf.append(arr2[j])
			        		 .append("</td><td>")
			        		 .append(arr2[j+1])
			        		 .append("</td></tr><tr><td>");
				         //multimap2.put(arr2[j], arr2[j+1]);
				         //multimap3.put(key,multimap2);
			        	 j=j+2;
			        	 }
			        	 
			         }
			         //System.out.println(multimap2);
			      }
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
