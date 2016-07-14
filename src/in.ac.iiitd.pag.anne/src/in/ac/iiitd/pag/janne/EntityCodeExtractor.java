package in.ac.iiitd.pag.janne;

import in.ac.iiitd.pag.util.ConfigUtil;
import in.ac.iiitd.pag.util.FileUtil;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class EntityCodeExtractor {
	public static void main(String[] args) {		
		try {
			Properties props = FileUtil.loadProps();
			if (props == null) return;
			int speedUp = 50;
			String filePath = props.getProperty("FILE_PATH");
			String entityName = "loop";
			
			if (args.length == 2) {
				entityName = args[0];
				speedUp = Integer.parseInt(args[1]);							
			}
			Map<String, Set<String>> skipEntities = EntityTagger.getSkipLists("skipList.txt");
			Set<String> skipEntitySet = skipEntities.get(entityName);
			Set<Integer> ids = null;
			if (args.length == 3) {
				String idsFile = args[2];	
				ids = FileUtil.readFromFileAsSet(ConfigUtil.getInputStream(idsFile));
			} else {
				ids = LineRanker.extract(filePath, entityName, skipEntitySet);
				FileUtil.writeSetToFile(ids, entityName + "-relevant-post-ids.txt");
			}		
			
			List<String> code = LineBasedCodeExtractor.getAllCode(ids, filePath, speedUp); //Collect unique tokens per codeset (not per line).
			FileUtil.writeListToFile(code, entityName + "-UniqueTokensInCodeSet.txt"); 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
/*
	private static List<String> getAllCode(Set<Integer> ids, String filePath) throws IOException, FactoryConfigurationError {
		List<String> code = new ArrayList<String>();
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
		                			   
		                			   lineItem = StringUtil.getAsStringFromList(CodeFragmentInspector.tokenizeAsList(lineItem));
		                			   code.add(lineItem);		                			   
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

	private static Set<Integer> extract(String fileName, String word) throws IOException {
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
	*/
}
