package in.ac.iiitd.pag.janne;

import in.ac.iiitd.pag.util.ConfigUtil;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.XMLUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class GrabJavaPosts {
	public static void main(String[] args) {
		Properties props = FileUtil.loadProps();
		if (props == null) return;			
					
		String filePath = props.getProperty("FILE_PATH");
		String outputFile = "JavaPosts.xml";
		
		try {
			grab(filePath, outputFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void grab(String filePath, String outputFile) throws IOException, FactoryConfigurationError {
		FileWriter fw = new FileWriter(outputFile);
		BufferedWriter bw = new BufferedWriter(fw);		
		
		Set<Integer> ids = FileUtil.readFromFileAsSet(ConfigUtil.getInputStream("allIds.txt"));
		BufferedReader reader = new BufferedReader(new FileReader(filePath), 4 * 1024 * 1024);
		String line = null;			
		int lineCount = 0;
		System.out.println("Reading file...");
		Set<String> tempSet = new HashSet<String>();
		while ((line = reader.readLine()) != null) {
			lineCount++;
			if (lineCount % 2000 == 0) System.out.print(".");
			if (lineCount % 100000 == 0) {
				System.out.println(line.substring(0,20) + "..." + line.substring(line.length()-20));				
				System.out.println(lineCount);
			}
			try {
				if (!line.trim().startsWith("<row")) {
					bw.write(line + "\n");
					continue;
				}
				
				if (!line.trim().startsWith("<")) continue;
				if (!line.trim().endsWith(">")) continue;
				
				XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();        
		        XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new StringReader(line));    
		        boolean add = false;
		        int id = 0;
		        int parentId = 0;
		        
		        while(xmlEventReader.hasNext()){
		           XMLEvent xmlEvent = xmlEventReader.nextEvent();
		           if (xmlEvent.isStartElement()){
		               StartElement startElement = xmlEvent.asStartElement();
		               if(startElement.getName().getLocalPart().equalsIgnoreCase("row")){			            	   
		            	   id = XMLUtil.getIntElement(startElement, "Id");		            	   
		            	   parentId = XMLUtil.getIntElement(startElement, "ParentId");
		               }
		            }
		        }
		        
		        if (id !=0)
		        	if (ids.contains(id)) add = true;
         	    if (!add) {
         		   if ((parentId > 0) && (ids.contains(parentId))) {
         			   add = true;
         		   }
         	    }
         	   if (add) {
         		   bw.write(line + "\n");
         	   }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		bw.flush();
		bw.close();
		reader.close();
	}
}
