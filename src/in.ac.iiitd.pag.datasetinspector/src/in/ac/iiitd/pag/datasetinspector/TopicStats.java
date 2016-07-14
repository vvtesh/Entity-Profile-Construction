package in.ac.iiitd.pag.datasetinspector;

import in.ac.iiitd.pag.entity.SONavigator;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.SOUtil;
import in.ac.iiitd.pag.util.XMLUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class TopicStats {
	public static void main(String[] args) {
		Properties props = FileUtil.loadProps();
		if (props == null) return;
		String ID_TITLES_VOTES_JAVA_FILE_PATH = props.getProperty("ID_TITLES_VOTES_JAVA_FILE_PATH");
		String SINGLE_METHOD_JAVA_POSTS_FILE_PATH = props.getProperty("SINGLE_METHOD_JAVA_POSTS_FILE_PATH");
		String TEMP1_OUTPUT = props.getProperty("TEMP1_OUTPUT");
		String TEMP2_OUTPUT = props.getProperty("TEMP2_OUTPUT");
		String topic = "factorial";
		String findIn = "title"; //use "title" or "fullpost"
		if (args.length > 1) {
			topic = args[0];
			findIn = args[1];
		}
		Map<Integer, SONavigator> info = SOUtil.loadIdTitleMap(ID_TITLES_VOTES_JAVA_FILE_PATH);
		processFile(SINGLE_METHOD_JAVA_POSTS_FILE_PATH, ID_TITLES_VOTES_JAVA_FILE_PATH, topic, TEMP1_OUTPUT, TEMP2_OUTPUT, info, findIn);
		//System.out.println(isSequenceFound("convert int to string", "convert int to string"));
	}

	private static void processFile(String sINGLE_METHOD_JAVA_POSTS_FILE_PATH,
			String ID_TITLES_VOTES_JAVA_FILE_PATH, String topic, String tEMP1_OUTPUT, String tEMP2_OUTPUT,Map<Integer, SONavigator> info, String findIn) {
		try {
			FileWriter fw1 = new FileWriter(tEMP1_OUTPUT);
   			BufferedWriter bw1 = new BufferedWriter(fw1, 2 * 1024 * 1024);	
   			FileWriter fw2 = new FileWriter(tEMP2_OUTPUT);
   			BufferedWriter bw2 = new BufferedWriter(fw2, 2 * 1024 * 1024);
   			
   			   			
   			String codeSnippets = "";
   			
   			
			BufferedReader reader = new BufferedReader(new FileReader(sINGLE_METHOD_JAVA_POSTS_FILE_PATH), 2 * 1024 * 1024);
			String line = null;			
			int lineCount = 0;
			int count=0;
			System.out.println("Reading file..." + sINGLE_METHOD_JAVA_POSTS_FILE_PATH);
			while ((line = reader.readLine()) != null) {
				lineCount++;
				//if (lineCount < 140000) continue;
				if (lineCount % 100 == 0) System.out.print(".");
				if (lineCount % 5000 == 0) {System.out.println(lineCount);}
				try {
					if (!line.trim().startsWith("<row")) continue;
					
					XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();        
			        XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new StringReader(line));        
			        while(xmlEventReader.hasNext()){
			           XMLEvent xmlEvent = xmlEventReader.nextEvent();
			           if (xmlEvent.isStartElement()){
			               StartElement startElement = xmlEvent.asStartElement();
			               if(startElement.getName().getLocalPart().equalsIgnoreCase("row")){
			            	   Attribute idAttr = startElement.getAttributeByName(new QName("Id"));		                	   		                	   
			            	   int id = Integer.parseInt(idAttr.getValue());	                	   
		                	   String bodyOrig = XMLUtil.getStringElement(startElement, "Body");
		                	   String body = bodyOrig.toLowerCase();
		                	   
		                	   SONavigator infoItem = info.get(id);
		                	   if (infoItem != null) {
		                		   String title = info.get(id).title.toLowerCase();
		                		   String code = SOUtil.getCode(body);
			                	   if (findIn.equalsIgnoreCase("title")) {
			                		   if (isSequenceFound(topic, title)) {
			                			   bw1.write(id + " " + title + "\n");
			                			   codeSnippets = codeSnippets + "**********" + id + "**********\n" + title + "\n" + code;
			                			
			                		   }
			                	   }
			                	   if (findIn.equalsIgnoreCase("fullpost")) {
			                		   if (isSequenceFound(topic, title) || isSequenceFound(topic, body)) {
			                			   codeSnippets = codeSnippets + "**********" + id + "**********\n" + title + "\n" + code;
			                			   bw1.write(id + " " + title + "\n");
			                			   count++;
			                		   }
			                	   }
		                	   } else {
		                		   System.out.println("Warning: No info on entity found for " + id);
		                	   }
		                	   
		                	   
			               }
			           }
			        }
			        xmlEventReader.close();			        
				} catch (Exception e) {}
			}
			
			bw2.write(codeSnippets);
			bw1.close();
			bw2.close();
		} catch (Exception e) {}
	
		
	}
	
	private static boolean isSequenceFound(String phrase1, String phrase2) {
		boolean isFound = false;
		String[] tokens = phrase1.split(" ");
		int matchCount = 0;
		int pointer = 0;
		for(String token: tokens) {			
			int i = phrase2.indexOf(token,pointer);
			if (i < 0) break;
			pointer = i;
			matchCount++;
		}
		if (matchCount == tokens.length) isFound = true;
		return isFound;
	}
	
}
