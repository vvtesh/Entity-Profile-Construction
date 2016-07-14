package in.ac.iiitd.pag.janne;

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
import java.util.ArrayList;
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

public class CooccurrenceMatrix {
	public static void main(String[] args) {
		try {
			Properties props = FileUtil.loadProps();
			if (props == null) return;						
			String filePath = props.getProperty("FILE_PATH");
			
			String entityName = "loop";
			
			if (args.length == 1) {
				entityName = args[0];
			}
			
			String idsFile = entityName + "-relevant-post-ids.txt";
			String longPatternsFile = entityName + "-Long-Normalized.txt";
			String outputFile = entityName + "-Cooccurrence.txt";
			
			Set<Integer> idsRead = FileUtil.readFromFileAsSet(ConfigUtil.getInputStream(idsFile));
			
			Map<String, Integer> patterns = FileUtil.getMapFromFile(entityName + "-NormalizedTF.txt");
			Map<String, Integer> longPatterns = FileUtil.getMapFromFile(longPatternsFile);
			
			List<String> patternSet = constructMatrix(patterns, longPatterns, filePath, idsRead, outputFile);
			FileUtil.writeListToFile(patternSet, outputFile);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private static List<String> constructMatrix(Map<String, Integer> patterns,
			Map<String, Integer> longPatterns, String filePath, Set<Integer> ids, String outputFile) {
		List<String> patternSet = new ArrayList<String>();
		Set<String> tempPatterns = new HashSet<String>();
		String line = null;			
		int lineCount = 0;
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
			                	   
			                	   String body = XMLUtil.getStringElement(startElement, "Body");
			                	   Set<String> codeSet = SOUtil.getCodeSet(body);
			                	   for(String codeFound: codeSet) {
			                		   tempPatterns.clear();
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
			                			   
			                			   Set<String> patternsFound = processLineItem(lineItem, patterns, longPatterns);	                						    
			                			   tempPatterns.addAll(patternsFound);         			
			                		   }
			                		   
			                		   patternSet.add(StringUtil.getAsCSV(tempPatterns));
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
		return patternSet;
				
	}


	private static Set<String> processLineItem(String lineItem, Map<String, Integer> patterns, Map<String, Integer> longPatterns) throws IOException {
		Set<String> patternsSet = new HashSet<String>();
		List<String> tokens = CodeFragmentInspector.tokenizeAsList(lineItem);
		   String newLineItem = "";
		   for(String token: tokens) {
			   if (patterns.containsKey(token)) {
				   newLineItem = newLineItem + token + " ";
			   }
		   }	
		   if (newLineItem.length() > 0) { 
			   
			   for(int i=6; i>2; i--) {
				   Set<String> ngrams = NGramBuilder.getSequentialNgramsAnyN(newLineItem, i);
				   
				   for(String ngram: ngrams) {
					   if (longPatterns.containsKey(" " + ngram)) {
						   patternsSet.add(ngram);
					   }
				   }
			   }
			   
		   }
		   return patternsSet;
	}
}
