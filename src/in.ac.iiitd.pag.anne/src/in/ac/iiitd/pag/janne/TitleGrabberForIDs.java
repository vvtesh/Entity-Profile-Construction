package in.ac.iiitd.pag.janne;

import in.ac.iiitd.pag.util.ConfigUtil;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.XMLUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class TitleGrabberForIDs {
	public static void main(String[] args) {
		try {
			Properties props = FileUtil.loadProps();
			if (props == null) return;
			
			String entityName = "loop";
			String filePath = props.getProperty("FILE_PATH");
			String inputFile = entityName + "-relevant-post-ids.txt";
			String outputFile = entityName + "-Titles.csv";
			if (args.length == 2) {
				inputFile = args[0];	
				outputFile = args[1];
			}
			Set<Integer> ids = FileUtil.readFromFileAsSet(ConfigUtil.getInputStream(inputFile)); 
			Map<String, String> titles = grabTitles(filePath, ids, outputFile);
			FileUtil.writeStrMapToFile(titles, outputFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static Map<String, String> grabTitles(String filePath, Set<Integer> ids,
			String outputFile) {
		
		String line = null;			
		int lineCount = 0;
		Map<String, String> idTitle = new HashMap<String, String>();
		System.out.println("Reading file...");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath), 4 * 1024 * 1024);
			while ((line = reader.readLine()) != null) {
				lineCount++;
				if (lineCount % 2000 == 0) System.out.print(".");
				if (lineCount % 100000 == 0) {System.out.println(lineCount);}
				try {
					if (!line.trim().startsWith("<row")) continue;
					
					XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();        
			        XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new StringReader(line));        
			        while(xmlEventReader.hasNext()){
			           XMLEvent xmlEvent = xmlEventReader.nextEvent();
			           if (xmlEvent.isStartElement()){
			               StartElement startElement = xmlEvent.asStartElement();
			               if(startElement.getName().getLocalPart().equalsIgnoreCase("row")){			            	   
			            	   int id = XMLUtil.getIntElement(startElement, "Id");		            	   
			            	   int parentId = XMLUtil.getIntElement(startElement, "ParentId");
			            	   boolean add = false;
			            	   if (ids.contains(id)) add = true;
			            	   if (!add) {
			            		   if ((parentId > 0) && (ids.contains(parentId))) {
			            			   add = true;
			            		   }
			            	   }
			            	   if (add) {
			                	   
			                	   String title = XMLUtil.getStringElement(startElement, "Title");
			                	   idTitle.put(id+"", title);
			            	   }
			               }
			           }
			        }
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return idTitle;
	}
}
