package in.ac.iiitd.pag.janne;

import in.ac.iiitd.pag.util.CodeFragmentInspector;
import in.ac.iiitd.pag.util.ConfigUtil;
import in.ac.iiitd.pag.util.FileUtil;
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

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class LineRanker {
	public static void main(String[] args) {		
		try {
			Properties props = FileUtil.loadProps();
			if (props == null) return;
			
			String filePath = props.getProperty("FILE_PATH");
			/*
			Set<String> skipEntities = new HashSet<String>();
			skipEntities.add("arraylist");
			skipEntities.add("list");
			Set<Integer> ids = extract(filePath, "remove", skipEntities);
			FileUtil.writeIntSetToFile(ids, "ids-remove.txt"); */		
			Set<Integer> ids = FileUtil.readFromFileAsSet(ConfigUtil.getInputStream("ids-remove.txt"));
			Map<String,Float> weights = FileUtil.getFloatMapFromFile("normalizedTF.csv");
			Map<String,Integer> code = getAllCode(ids, filePath, weights);
			code = FileUtil.sortByValues(code);
			FileUtil.writeMapToFile(code, "code-remove-ranked.txt", 0); 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Map<String,Integer> getAllCode(Set<Integer> ids, String filePath, Map<String, Float> weights) throws IOException, FactoryConfigurationError {
		Map<String,Integer> code = new HashMap<String, Integer>();
		BufferedReader reader = new BufferedReader(new FileReader(filePath), 4 * 1024 * 1024);
		String line = null;			
		int lineCount = 0;
		System.out.println("Reading file...");
		while ((line = reader.readLine()) != null) {
			lineCount++;
			if (lineCount % 2000 == 0) System.out.print(".");
			if (lineCount % 100000 == 0) {System.out.println(lineCount);}
			try {
				if (!line.trim().startsWith("<row")) continue;
				
				XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();        
		        XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new StringReader(line));    
		        Set<String> tempSet = new HashSet<String>();
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
		                		   String[] lines = codeFound.split("\r\n|\r|\n");
		                		   tempSet.clear();
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
		                			   
		                			   String tokensOfInterest = "";
		                			   float lineWeight = 0;
		                			   int tokensInLine = 0;
		                			   for(String token: tokens) {
		                				   if (weights.containsKey(token)) {
		                					   tokensInLine++;
		                					   lineWeight = lineWeight + weights.get(token);
		                					   tokensOfInterest = tokensOfInterest + " " + token;
		                				   }
		                			   }
		                			   tokensOfInterest = tokensOfInterest.trim();
		                			   if (tokensInLine > 0) {
			                			   int countToI = 0;
			                			   if (code.containsKey(tokensOfInterest)) {
			                				   countToI = countToI + code.get(tokensOfInterest);
			                			   }
			                			   countToI = countToI + (int) lineWeight / tokensInLine;		                			   
		                				   code.put(tokensOfInterest.trim(), countToI );
		                			   }
		                		   }                	
		                	   }
		                	   
		            	   }
		               }
		           }
		        }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		reader.close();
		return code;
	}
	
	public static Map<String,Integer> getAllCodeSetLevel(Set<Integer> ids, String filePath, Map<String, Float> weights) throws IOException, FactoryConfigurationError {
		Map<String,Integer> code = new HashMap<String, Integer>();
		BufferedReader reader = new BufferedReader(new FileReader(filePath), 4 * 1024 * 1024);
		String line = null;			
		int lineCount = 0;
		System.out.println("Reading file...");
		while ((line = reader.readLine()) != null) {
			lineCount++;
			if (lineCount % 2000 == 0) System.out.print(".");
			if (lineCount % 100000 == 0) {System.out.println(lineCount);}
			try {
				if (!line.trim().startsWith("<row")) continue;
				
				XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();        
		        XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new StringReader(line));    
		        Set<String> tempSet = new HashSet<String>();
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
		                		   String[] lines = codeFound.split("\r\n|\r|\n");
		                		   tempSet.clear();
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
		                			   
		                			   String tokensOfInterest = "";
		                			   float lineWeight = 0;
		                			   int tokensInLine = 0;
		                			   for(String token: tokens) {
		                				   if (weights.containsKey(token)) {
		                					   tokensInLine++;
		                					   lineWeight = lineWeight + weights.get(token);
		                					   tokensOfInterest = tokensOfInterest + " " + token;
		                				   }
		                			   }
		                			   if (tokensInLine > 0)
		                				   code.put(tokensOfInterest.trim(), (int) lineWeight / tokensInLine );
		                		   }                	
		                	   }
		                	   
		            	   }
		               }
		           }
		        }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return code;
	}

	public static Set<Integer> extract(String fileName, String word, Set<String> skipEntities) throws IOException {
		Set<Integer> ids = new HashSet<Integer>();
		BufferedReader reader = new BufferedReader(new FileReader(fileName), 4 * 1024 * 1024);
		String line = null;			
		int lineCount = 0;
		System.out.println("Reading file...");
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
		            	   	            	   
	                	   String title = XMLUtil.getStringElement(startElement, "Title");
	                	   if (title == null) {
	                		   title = "";
	                		   continue;
	                	   }
	                	   String tags = XMLUtil.getStringElement(startElement, "Tags");       
	                	   if ((tags == null)||(tags.trim().length()==0)) {
	                		   return null;
	                	   } 
	                	   //if (!SOUtil.hasJavaTag(tags)) continue; //Uncomment if you are not using Reduced Posts.xml where language filtering is already done.
	                	   boolean toAdd = false;
	                	   boolean hasToken = hasToken(word, title);
	                	   if (hasToken) {	
	                		   toAdd = true;
	                		   if (skipEntities != null) {
		                		   for(String stop: skipEntities) {
		                			   if (hasToken(stop, title)) {
		                				   toAdd = false;
		                				   break;
		                			   }
		                		   }
	                		   }
	                	   }
	                	   if (toAdd) {
	                		   ids.add(id);
	                	   }
		               }
		           }
		        }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		reader.close();
		return ids;
	}

	private static boolean hasToken(String word, String title) {
		boolean hasToken = false;
		Set<String> tokens = StringUtil.getTokens(title.toLowerCase()); 
		if (tokens.contains(word)) {
			hasToken = true;
		}
		return hasToken;
	}
}
