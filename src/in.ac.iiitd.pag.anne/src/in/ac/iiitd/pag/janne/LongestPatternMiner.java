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

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class LongestPatternMiner {
	public static void main(String[] args) {
		
		
		Properties props = FileUtil.loadProps();
		if (props == null) return;			
		String filePath = props.getProperty("FILE_PATH");
		String entityName = "loop";
		if (args.length == 1) {
			entityName = args[0];
		}
		
		try {
			Map<String, Float> weightedLines = FileUtil.getLastNFloatMapFromFile(entityName + "-WeightedLines.txt", 0);
			Set<Integer> ids = FileUtil.readFromFileAsSet(ConfigUtil.getInputStream(entityName + "-relevant-post-ids.txt")); //allIds.txt
			Map<String, Float> unigramEntityTF = FileUtil.getLastNFloatMapFromFile(entityName + "-NormalizedTF.txt", 0);
			
			Map<String, Integer> patterns = findLongPatterns(filePath, ids, unigramEntityTF, weightedLines, 20, 0);
			FileUtil.writeMapToFile(patterns, entityName + "-Long-patterns.txt", 0); //-in-all-code
			
			Set<Integer> allIds = FileUtil.readFromFileAsSet(ConfigUtil.getInputStream("allIds.txt")); //allIds.txt
			Map<String, Integer> patternsAll = findLongPatterns(filePath, allIds, unigramEntityTF, weightedLines, 50, 5);
			FileUtil.writeMapToFile(patternsAll, entityName + "-Long-patterns-in-all-code.txt", 0); //
			
			
			/*Map<String, Float> patternsMap = FileUtil.getLastNFloatMapFromFile(entityName + "-Long-patterns-in-all-code.txt",100);			
			Map<String, Float> entityPatternFreq = FileUtil.getLastNFloatMapFromFile(entityName + "-Long-patterns.txt", 0);
			
			patternsMap = TFNormalizer.normalizeFloatWeightsSimple(patternsMap, entityPatternFreq, 0);
			patternsMap = TFNormalizer.normalizeWeights(patternsMap, entityPatternFreq, 0);
			Map<String, Integer> patternsInt = convertToInt(patternsMap);
			patternsInt = FileUtil.sortByValues(patternsInt);
			FileUtil.writeMapToFile(patternsInt, "long-loop-patterns-normalized.txt", 0);*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	public static Map<String, Integer> convertToInt(
			Map<String, Float> map) {
		Map<String, Integer> converted = new HashMap<String, Integer>();
		for(String item: map.keySet()) {
			int value = (int) (map.get(item) * 100);
			converted.put(item, value);
		}
		return converted;
	}

	public static Map<String, Integer> findLongPatterns(String filePath, Set<Integer> ids, Map<String, Float> unigramEntityTF, Map<String, Float> entityTF, int speedUp, int collFreqCutOff) throws IOException, FactoryConfigurationError {		
		
		List<String> longestPatterns = getLongestPatterns(ids, filePath, entityTF.keySet(), unigramEntityTF.keySet(), speedUp);
		Map<String, Integer> patternsMap = new HashMap<String, Integer>();
		for(String pattern: longestPatterns) {
			int count = 0;
			if (patternsMap.containsKey(pattern)) {
				count = patternsMap.get(pattern);
			}
			count++;
			patternsMap.put(pattern, count);
		}
		patternsMap = FileUtil.sortByValues(patternsMap);
		//patternsMap = TFNormalizer.normalizeWeightsSimple(patternsMap, entityTF, collFreqCutOff);
		return patternsMap;
	}
	
	public static List<String> getLongestPatterns(Set<Integer> ids, String filePath, Set<String> patterns, Set<String> unigramEntityPatterns, int speedUp) throws IOException, FactoryConfigurationError {
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
			if ((speedUp > 0) && (lineCount % speedUp != 0)) continue; //Speed up.
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
		                			   lineItem = StringUtil.cleanCode(lineItem);
		                			   
		                			   if (!CodeFragmentInspector.isTagWorthyCode(lineItem)) continue;
		                			   
		                			   List<String> tokens = CodeFragmentInspector.tokenizeAsList(lineItem);
		                			   
		                			   String newLineItem = "";
		                			   for(String token: tokens) {
		                				   if (unigramEntityPatterns.contains(token)) {
		                					   newLineItem = newLineItem + token + " ";
		                				   }
		                			   }	
		                			   boolean found = false;
		                			   for(int i=6; i>0; i--) {
		                				   Set<String> ngrams = NGramBuilder.getSequentialNgramsAnyN(newLineItem, i);
		                				   
		                				   for(String ngram: ngrams) {
		                					  /* if (ngram.contains("for")) {
		                						   System.out
														.println(ngram);
		                					   }*/
		                					   if (patterns.contains(ngram.trim())) {
		                						   found = true;
		                						   newLineItem = newLineItem.trim() + "\t:=\t" + ngram;
		                						   tempSet.add(ngram);	  
		                						   break;
		                					   }		                						    
		                				   }
		                				   if (found) break;
		                			   }		                			              				   
		                			   	                			   
		                		   }
		                		   for(String item: tempSet) 
		                			   code.add(item);
		                		   
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
	
	
}
