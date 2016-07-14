package in.ac.iiitd.pag.datasetinspector;

import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.SOUtil;
import in.ac.iiitd.pag.util.XMLUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class TitleInspector {
	
	static HashMap<String, Integer> entityFreq = new HashMap<String, Integer>();
	
	public static void main(String[] args) {
		Properties props = FileUtil.loadProps();
		if (props == null) return;
		
		String filePath = props.getProperty("FILE_PATH");
		List<String> entities = FileUtil.readFromFileAsList("knownEntities.txt");
		
		findTitlesWithKeyword(filePath, entities);
		System.out.println("");
		for(String entity: entityFreq.keySet()) {
			System.out.println(entity + "  " + entityFreq.get(entity));
		}
	}

	private static void findTitlesWithKeyword(String filePath, List<String> entities) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath), 4 * 1024 * 1024);
			String line = null;			
			System.out.println("Reading file..." + filePath);
			int lineCount = 0;
			while ((line = reader.readLine()) != null) {
				lineCount++;
				if (lineCount % 1000 == 0) System.out.print(".");
				if (lineCount % 100000 == 0) {System.out.println(lineCount);}
				processLine(line, entities);				
			}
			reader.close();
		} catch(Exception e) {}
	}

	private static void processLine(String line, List<String> entities) throws XMLStreamException {
		if (!line.trim().startsWith("<row")) return;
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();        
        XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new StringReader(line));        
        while(xmlEventReader.hasNext()){
           XMLEvent xmlEvent = xmlEventReader.nextEvent();
           if (xmlEvent.isStartElement()){
               StartElement startElement = xmlEvent.asStartElement();
               if(startElement.getName().getLocalPart().equalsIgnoreCase("row")){
            	   String title = XMLUtil.getStringElement(startElement, "Title");
            	   if (title == null) {
            		   title = "";
            		   return;
            	   }
            	   String body = XMLUtil.getStringElement(startElement, "Body");
            	   Set<String> codeSet = SOUtil.getCodeSet(body);
            	   if (codeSet.size() == 0) {
            		   return;
            	   }
            	   for(String entity: entities) {
	            	   if (title.toLowerCase().contains(entity)) {
		            	   addToEntityFrequency(entity);		            	   
	            	   }
            	   }
               }
           }
        }
	}

	private static void addToEntityFrequency(String entity) {
		int freq=1;
		if (entityFreq.containsKey(entity)) {
			freq = entityFreq.get(entity) + 1;
		}
		entityFreq.put(entity, freq);
	}
}
