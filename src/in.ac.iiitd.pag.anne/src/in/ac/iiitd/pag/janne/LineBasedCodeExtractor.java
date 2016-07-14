package in.ac.iiitd.pag.janne;

import in.ac.iiitd.pag.util.ASTUtil;
import in.ac.iiitd.pag.util.CodeFragmentInspector;
import in.ac.iiitd.pag.util.ConfigUtil;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.SOUtil;
import in.ac.iiitd.pag.util.StringUtil;
import in.ac.iiitd.pag.util.XMLUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class LineBasedCodeExtractor {
	public static void main(String[] args) {		
		try {
			Properties props = FileUtil.loadProps();
			if (props == null) return;
			
			String filePath = props.getProperty("FILE_PATH");
			String entityName = "remove";
			List<String> code = getUniqueTokensPerCodeFragment(filePath, entityName, 0);
			FileUtil.writeListToFile(code, "code-remove.txt"); 
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}

	public static List<String> getUniqueTokensPerCodeFragment(String filePath, String entityName, int speedUp) throws FactoryConfigurationError, IOException {
		Set<Integer> ids = extract(filePath, entityName);
		/*FileUtil.writeIntSetToFile(ids, "ids-remove.txt");	
		Set<Integer> ids = FileUtil.readFromFileAsSet(ConfigUtil.getInputStream("ids-remove.txt"));  */	
		List<String> code = getAllCode(ids, filePath, speedUp);
		return code;
	}

	public static List<String> getAllCode(Set<Integer> ids, String filePath, int speedUp) throws IOException, FactoryConfigurationError {
		List<String> code = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(filePath), 4 * 1024 * 1024);
		String line = null;			
		int lineCount = 0;
		System.out.println("Reading file...");
		int emptyPosts = 0;
		int codeAdded = 0;
		while ((line = reader.readLine()) != null) {
			lineCount++;
			if (lineCount % 2000 == 0) System.out.print(".");
			if (lineCount % 100000 == 0) {System.out.println(lineCount);}
			if ((speedUp > 0) && (lineCount % speedUp != 0)) continue;
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
		                	   if (codeSet.size() == 0) {
		                		   emptyPosts++;
		                	   }
		                	   for(String codeFound: codeSet) {
		                		   String[] lines = codeFound.split("\r\n|\r|\n");
		                		   tempSet.clear();
		                		   for(String lineItem: lines) {
		                			   
		                			   lineItem = lineItem.toLowerCase();
		                			   lineItem = lineItem.trim();
		                			   
		                			   if (!CodeFragmentInspector.isJavaH(lineItem)) continue;
		                			   
		                			   lineItem = StringUtil.cleanCode(lineItem);
		                			   
		                			   if (lineItem.startsWith("//")) continue;
		                			   if (lineItem.length() <=2 ) continue;
		                			   if (lineItem.startsWith("import ")) continue;
		                			   if (lineItem.startsWith("public")) continue;
		                			   if (lineItem.startsWith("private")) continue;
		                			   if (lineItem.startsWith("protected")) continue;
		                			   if (lineItem.startsWith("class")) continue;
		                			   List<String> tokens = CodeFragmentInspector.tokenizeAsList(lineItem);
		                			   
		                			   for(String token: tokens) {
		                				   tempSet.add(token);
		                			   }		                			   	                			   
		                		   }
		                		   String newLineItem = "";
	                			   for(String token: tempSet) {
	                				   newLineItem = newLineItem + token + " ";
	                			   }
	                			   
	                			   code.add(newLineItem.trim());	
	                			   codeAdded++;
		                	   }
		                	   
		            	   }
		               }
		           }
		        }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println(emptyPosts + " empty posts found.");
		System.out.println(codeAdded + " snippets added.");
		return code;
	}

	public static Set<Integer> extract(String fileName, String word) throws IOException {
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
	                	   if (!SOUtil.hasJavaTag(tags)) continue; 
	                	   if (title.contains(word)) {
	                		   ids.add(id);
	                	   }
		               }
		           }
		        }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ids;
	}
}
