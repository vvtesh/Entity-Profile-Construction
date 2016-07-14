package in.ac.iiitd.pag.anne.discovery;

import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.SOUtil;
import in.ac.iiitd.pag.util.XMLUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class TitleStats {
	static String[] TAGS_TO_FILTER = {"java"};
	
	public static void main(String[] args) {
		String language = args[0];
		if (args.length > 0)
			TAGS_TO_FILTER = new String[] {language};
		System.out.println("Grabbing title stats for language " + language);
		Properties props = FileUtil.loadProps();
		if (props == null) return;
		String filePath = props.getProperty("FILE_PATH");
		String lengthDistributionFile = "lengths.csv";
		try {
			List<Integer> lengths = extract(filePath);
			FileUtil.writeIntListToFile(lengths, lengthDistributionFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static List<Integer> extract(String fileName) throws IOException {
		List<Integer> lengths = new ArrayList<Integer>();
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
		            	   
	                	   String title = XMLUtil.getStringElement(startElement, "Title");
	                	   if (title == null) {
	                		   title = "";
	                		   continue;
	                	   }
	                	   String tags = XMLUtil.getStringElement(startElement, "Tags");       
	                	   if ((tags == null)||(tags.trim().length()==0)) {
	                		   continue;
	                	   } 
	                	   if (!SOUtil.hasTag(tags, TAGS_TO_FILTER)) continue; //Uncomment if you are not using Reduced Posts.xml where language filtering is already done.
	                	   lengths.add(title.split(" ").length);
		               }
		           }
		        }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		reader.close();
		return lengths;
	}
}
