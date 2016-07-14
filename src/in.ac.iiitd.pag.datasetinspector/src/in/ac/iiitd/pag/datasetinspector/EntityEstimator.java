package in.ac.iiitd.pag.datasetinspector;

import in.ac.iiitd.pag.util.CodeFragmentInspector;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.SOUtil;
import in.ac.iiitd.pag.util.StringUtil;
import in.ac.iiitd.pag.util.XMLUtil;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;

public class EntityEstimator {
	
	public static IDictionary dict = null;
	public static WordnetStemmer stemmer = null;

	/** How many entities exist in SO?
	 *  1. Grab titles
	 *  2. Tokenize them
	 *  3. Grab associated code and remove words used in the code after comments removal. 
	 *  4. Remove stopwords.
	 *  5. Compute frequency.
	 * @param args
	 */
	public static void main(String[] args) {
		Properties props = FileUtil.loadProps();
		if (props == null) return;			
		
		String dictFIle = props.getProperty("DICT_FILES");		
		URL url = null;
					
		try {
			url = new URL("file", null , dictFIle) ;
			dict = new Dictionary(url) ;
			dict.open() ;
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		stemmer = new WordnetStemmer(dict);
		
		String filePath = props.getProperty("FILE_PATH");
		
		try {
			HashMap<String, Integer> entities = extractEntities(filePath);
			entities = removeStops(entities);
			FileUtil.writeMapToFile(entities, "EntityEstimates.csv",10);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static HashMap<String, Integer> removeStops(
			HashMap<String, Integer> entities) {
		List<String> stops = FileUtil.readFromFileAsList("stops.txt");
		HashMap<String, Integer> entitiesUpdated = new HashMap<String, Integer>();
		for(String item: entities.keySet()) {
			if (!stops.contains(item)) {
				entitiesUpdated.put(item, entities.get(item));
			}
		}
		return entitiesUpdated;
	}

	private static HashMap<String, Integer> extractEntities(String filePath) throws IOException {
		
		HashMap<String, Integer> entities = new HashMap<String, Integer>();
		
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
		            	   	            	   
	                	   String title = XMLUtil.getStringElement(startElement, "Title");
	                	   if (title == null) {
	                		   title = "";
	                		   continue;
	                	   }
	                	   
	                	   // Get tokens in code fragment
	                	   String body = XMLUtil.getStringElement(startElement, "Body");
	                	   Set<String> codeSet = SOUtil.getCodeSet(body.toLowerCase());
	                	   tempSet.clear();
	                	   for(String codeFound: codeSet) {
	                		   String[] lines = codeFound.split("\r\n|\r|\n");
	                		   
	                		   for(String lineItem: lines) {
	                			   
	                			  
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
	                			   
	                			   for(String token: tokens) {
	                				   tempSet.add(token.toLowerCase());
	                			   }		                			   	                			   
	                		   }
		                 }
	                	 
	                	   if (codeSet.size() > 0) {
		                	   //Remove code tokens from title tokens.
	                		   Set<String> titleTokens = StringUtil.getTokens(title.toLowerCase());
	                		   Set<String> finalTokens = new HashSet<String>();
	                		   for(String token: titleTokens) {
	                			   if (!tempSet.contains(token)) {
	                				   finalTokens.add(token);
	                			   }
	                		   }
	                		   
	                		   for(String token: finalTokens) {
	                			   if (isInDictionary(token)) {
		                			   if (entities.containsKey(token)) {
		                				   int count = entities.get(token);
		                				   entities.put(token, ++count);
		                			   } else {
		                				   entities.put(token, 1);
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
		return entities;
	}

	public static boolean isInDictionary(String item) {
		boolean isInDict = false;
		
		IIndexWord idxWord = dict.getIndexWord(item, POS.NOUN);
		if (idxWord == null) {
			idxWord = dict.getIndexWord(item, POS.ADJECTIVE);
		}
		if (idxWord == null) {
			idxWord = dict.getIndexWord(item, POS.ADVERB);
		}
		if (idxWord == null) {
			idxWord = dict.getIndexWord(item, POS.VERB);
		}
		if (idxWord != null) {
			List<String> stems = stemmer.findStems(item, idxWord.getPOS());
			for(String stem: stems) {
				System.out.println(stem);
				isInDict = true;
			}
		}
		return isInDict;
	}
 }
