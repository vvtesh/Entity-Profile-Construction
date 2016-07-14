package in.ac.iiitd.pag.datasetinspector;

import in.ac.iiitd.pag.entity.SONavigator;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.SOUtil;
import in.ac.iiitd.pag.util.StringUtil;
import in.ac.iiitd.pag.util.XMLUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * For each topic, we gather the list of highest voted discussions.
 * 
 * steps: 
 *   1. id-titles has all ids, parentids and titles. 
 * @author venkateshv
 *
 */
public class IdTitleVoteIsJavaGrabber {
	static String FILE_PATH = "";
	static String EXPERIMENT_PATH = "";
	static String ALGO_NAMES_FILE_PATH = "";
	static String SHORT_POSTS_FILE_PATH = "";
	static String ALGO_POSTID_FILE_PATH = "";
	static String CODE_OUTPUT_PATH = "";
	static String NEW_POSTS_FILE_PATH = "";
	
	static String ID_TITLES_VOTES_FILE_PATH = "";
	static String ID_TITLES_VOTES_JAVA_FILE_PATH = "";

	public static void main(String[] args) {
		
		grab();
	}

	public static void grab() {
		Properties props = FileUtil.loadProps();
		if (props == null) return;
		
		FILE_PATH = props.getProperty("FILE_PATH");
		ID_TITLES_VOTES_FILE_PATH = props.getProperty("ID_TITLES_VOTES_FILE_PATH");
		ID_TITLES_VOTES_JAVA_FILE_PATH = props.getProperty("ID_TITLES_VOTES_JAVA_FILE_PATH");
			
		
		System.out.println("Generating id titles vote map file...");
		generateIdTitles(FILE_PATH, ID_TITLES_VOTES_JAVA_FILE_PATH);
		System.out.println("Generating id titles vote map file... Done.");
		
}


	private static boolean hasJavaTag(String tags) {
		 if (tags!=null) {                		   
 		   String[] tagArray = tags.toLowerCase().split(">");
 		   for(int i=0; i<tagArray.length; i++) {
 			   String temp = tagArray[i].replace("<", "");
 			   if (temp.trim().equalsIgnoreCase("java")) {
 				   return true;
 			   }
 		   }
 	     }
		 return false;
	}

	
	private static void generateIdTitles(String postsFilePath,
			String idTitlesFilePath) {
		try {
			Map<Integer, SONavigator> map = new HashMap<Integer, SONavigator>();
			
			BufferedReader reader = new BufferedReader(new FileReader(postsFilePath), 2 * 1024 * 1024);
			String line = null;			
			int lineCount = 0;
			System.out.println("Reading file...");
			while ((line = reader.readLine()) != null) {
				lineCount++;
				
				if (lineCount % 20000 == 0) System.out.print(".");
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
		                	   String tags = XMLUtil.getStringElement(startElement, "Tags");
		                	   
		                	   int score = XMLUtil.getIntElement(startElement, "Score");
		                	   SONavigator item = new SONavigator();
		                	   item.parentId = parentId;
		                	   item.title = title;
		                	   item.votes = score;
		                	   if (tags != null) {
		                		   if (hasJavaTag(tags)) {
		                			   item.isJava = true;
		                		   }
		                	   }
		                	   if (!item.isJava) continue; 
		                	   map.put(id, item);		                	   
			               }
			           }
			        }
			        xmlEventReader.close();
					
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
				
			}
			FileWriter fw = new FileWriter(idTitlesFilePath);
			BufferedWriter bw = new BufferedWriter(fw);		
			for(int id: map.keySet()) {
				String writerStr = id + "," + map.get(id).votes + "," + map.get(id).parentId + "," + (map.get(id).isJava?1:0);
				String title = map.get(id).title;
				if (title == null) title = "";
				writerStr = writerStr + "," + title;					
				bw.write(writerStr + "\n");
			}
			bw.close();
			fw.close();
			
			reader.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
			
}
