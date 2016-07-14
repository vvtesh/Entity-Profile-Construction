package in.ac.iiitd.pag.anne.entity;

import in.ac.iiitd.pag.util.CodeFragmentInspector;
import in.ac.iiitd.pag.util.ConfigUtil;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.SOUtil;
import in.ac.iiitd.pag.util.StringUtil;
import in.ac.iiitd.pag.util.XMLUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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

public class TokenExtractor {
	
	public static void main(String[] args) {
		Properties props = FileUtil.loadProps();
		String stopsFile = props.getProperty("STOP_FILE_PATH");		
		String postsFile = props.getProperty("FILE_PATH");
		String outputFile = props.getProperty("OUTPUT_FILE");
		
		TokenExtractor entityEstimator = new TokenExtractor();
		Map<String,Integer> map = entityEstimator.estimate(stopsFile, postsFile, outputFile);
		/*map = FileUtil.sortByValues(map);
		try {
			FileUtil.writeMapToFile(map, outputFile, 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
	private Map<String,Integer> estimate(String stopsFile, String postsFile, String outputFile) {
		int javaTitlesCount = 0;
		Map<String,Integer> map = new HashMap<String,Integer>();
		try {
			FileWriter fw = new FileWriter(outputFile);
   			BufferedWriter bw = new BufferedWriter(fw, 2 * 1024 * 1024);
   			
			List<String> stopsList = FileUtil.readFromFileAsList(stopsFile);
			Set<String> stops = new HashSet<String>();
			for(String stop: stopsList) {
				stops.add(stop);
			}
		
			BufferedReader reader = new BufferedReader(new FileReader(postsFile), 4 * 1024 * 1024);
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
			            	   
		                	   String title = XMLUtil.getStringElement(startElement, "Title");
		                	   if (title == null) title = "";
		                	   //if (title.trim().length() == 0) continue;
		                	   		                	   
		                	   /*String tags = XMLUtil.getStringElement(startElement, "Tags");
		                	   boolean isJava = SOUtil.hasJavaTag(tags);
		                	   if (!isJava) continue;
		                	   javaTitlesCount++;*/
		                	   
		                	   String body = XMLUtil.getStringElement(startElement, "Body");
		                	   String code = SOUtil.getCode(body);
		                	   code = StringUtil.cleanCode(code);
		                	   
		                	   Set<String> codeTokens = CodeFragmentInspector.tokenize(code);
		                	   
		                	   Set<String> titleTokens = StringUtil.getTokens(title);
		                	   String output = id + ":" + parentId + ":" + StringUtil.getAsCSV(titleTokens).replace(":", "") + ":" + StringUtil.getAsCSV(codeTokens).replace(":", "") + "\n"; 
		                	   bw.write(output);
		                	  // System.out.println(output);
		                	   /*
		                	   		                	   		                	   
		                	   for(String codetoken: codeTokens) {
		                		   titleTokens.remove(codetoken);
		                	   }
		                	   
		                	   for(String stop: stops) {
		                		   titleTokens.remove(stop);
		                	   }
		                	   		    
		                	   for(String token: titleTokens) {
		                		   int count = 0;
		                		   if (map.containsKey(token)) {
		                			   count = map.get(token);
		                		   }
		                		   count++;
		                		   map.put(token, count);
		                	   }*/
		                	   
			               }
			           }
			        }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			bw.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Total titles with Java tag = " + javaTitlesCount);
		return map;
	}

}
