package in.ac.iiitd.pag.janne.tests;

import in.ac.iiitd.pag.util.CodeFragmentInspector;
import in.ac.iiitd.pag.util.ConfigUtil;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.NGramBuilder;
import in.ac.iiitd.pag.util.SOUtil;
import in.ac.iiitd.pag.util.StringUtil;
import in.ac.iiitd.pag.util.XMLUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class TestPattern {
	public static void main(String[] args) {
		try {
			Properties props = FileUtil.loadProps();
			if (props == null) return;
			
			String entityName = "loop";
			String pattern = "< string > = ( )";
			if (args.length == 2) {
				entityName = args[0];
				pattern = args[1];
			}
			String filePath = props.getProperty("FILE_PATH");
			String inputFile = entityName + "-relevant-post-ids.txt";
			String outputFile = entityName + "-PatternTitles.csv";
			
			Set<Integer> ids = FileUtil.readFromFileAsSet(ConfigUtil.getInputStream(inputFile));
			Map<String, Float> unigramEntityTF = FileUtil.getLastNFloatMapFromFile(entityName + "-NormalizedTF.txt", 0);
			Set<String> titles = grabTitles(filePath, ids, unigramEntityTF.keySet(), pattern);
			FileUtil.writeListToFile(titles, outputFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static Set<String> grabTitles(String filePath, Set<Integer> ids, Set<String> unigramEntityPatterns,
			String pattern) {
		Set<String> tempSet = new HashSet<String>();
		int patternSize = pattern.trim().split(" ").length;
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
			                	   
			                	   String body = XMLUtil.getStringElement(startElement, "Body");
			                	   Set<String> codeSet = SOUtil.getCodeSet(body);
			                	   for(String codeFound: codeSet) {
			                		   String[] lines = codeFound.split("\r\n|\r|\n");
			                		   
			                		   for(String lineItem: lines) {
			                			   
			                			   lineItem = lineItem.toLowerCase();
			                			   lineItem = lineItem.trim();
			                			   lineItem = StringUtil.cleanCode(lineItem);
			                			   
			                			   if (lineItem.startsWith("//")) continue;
			                			   if (lineItem.length() <=2 ) continue;
			                			   if (lineItem.startsWith("import ")) continue;
			                			   if (lineItem.startsWith("public")) continue;
			                			   if (lineItem.startsWith("private")) continue;
			                			   if (lineItem.startsWith("protected")) continue;
			                			   if (lineItem.startsWith("class")) continue;
			                			   
			                			   List<String> tokens = CodeFragmentInspector.tokenizeAsList(lineItem);
			                			   String newLineItem = "";
			                			   for(String token: tokens) {
			                				   if (unigramEntityPatterns.contains(token)) {
			                					   newLineItem = newLineItem + token + " ";
			                				   }
			                			   }	
			                			   boolean found = false;
			                			   
		                				   Set<String> ngrams = NGramBuilder.getSequentialNgramsAnyN(newLineItem, patternSize);
			                				   
			                				   
	                					   if (ngrams.contains(pattern)) {
	                						   System.out.println(title  + "\n" + lineItem);
	                						   tempSet.add(title + "\n" + lineItem);	  
	                					   }		                						    
			                				         			   
			                		   }
			                		   
			                	   }
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
		
		return tempSet;
	}
}
