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

public class CommonCodeExtractor {
	private static final String[] TAGS_TO_FILTER = {"c"};
	public static void main(String[] args) {		
		try {
			/*Properties props = FileUtil.loadProps();
			if (props == null) return;*/
			
			//String filePath = props.getProperty("FILE_PATH");
			//String filePath = "c:\\temp\\workdir\\java.txt";
			String filePath = "Posts.xml";
			Set<Integer> ids = null;
			if (args.length == 0) {
				ids = extract(filePath);
				FileUtil.writeIntSetToFile(ids, "allIds.txt");	
			}
			Set<Integer> ids1 = FileUtil.readFromFileAsSet(ConfigUtil.getInputStream("allIds.txt"));
			List<String> code = getAllCode(ids1, filePath);
			FileUtil.writeListToFile(code, "allCode.txt");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static List<String> getAllCode(Set<Integer> ids, String filePath) throws IOException, FactoryConfigurationError {
		List<String> code = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(filePath), 4 * 1024 * 1024);
		String line = null;			
		int lineCount = 0;
		System.out.println("Reading file...");
		Set<String> tempSet = new HashSet<String>();
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
		                				   if (token.matches("[a-zA-Z0-9]")) continue;
		                				   tempSet.add(token);		                				   
		                			   }	                			   
		                		   }
		                		   String newLineItem = "";
		                		  
	                			   for(String token: tempSet) {
	                				  
	                				   newLineItem = newLineItem + token + " ";
	                			   }
	                			   
	                			   code.add(newLineItem.trim());
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

	

	public static Set<Integer> extract(String fileName) throws IOException {
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
	                	   if (!SOUtil.hasTag(tags, TAGS_TO_FILTER)) continue; 
	                	   
	                	   ids.add(id);
	                	   
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
