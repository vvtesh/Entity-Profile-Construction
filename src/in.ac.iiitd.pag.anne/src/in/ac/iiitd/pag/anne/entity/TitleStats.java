package in.ac.iiitd.pag.anne.entity;

import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.SOUtil;
import in.ac.iiitd.pag.util.XMLUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;


/**
 * java only tagged items are 23359.
 * cpp has 16527.
 * 
 * @author Venkatesh
 *
 */
public class TitleStats {
	
	static int count = 0;
	static List<String> items = new ArrayList<String>();
	static String lang = "java";
	static int javaTitles = 0;
	static int cppTitles = 0;
	
	public static void main(String[] args) {
		Properties props = FileUtil.loadProps();
		if (props == null) return;
		
		String filePath = props.getProperty("FILE_PATH");
		if (args.length == 1) {
			lang = args[0];									
		}
		findTitlesWithTag(filePath, lang);
		System.out.println(count);		
		try {
			FileUtil.writeListToFile(items, "titles.txt");
			System.out.println("Titles with tag " + lang + " written to titles.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void findTitlesWithTag(String filePath, String tag) {
		try {
			System.out.println("Looking for only " + tag);
			BufferedReader reader = new BufferedReader(new FileReader(filePath), 4 * 1024 * 1024);
			String line = null;			
			System.out.println("Reading file..." + filePath);
			int lineCount = 0;
			while ((line = reader.readLine()) != null) {
				lineCount++;
				if (lineCount % 1000 == 0) System.out.print(".");
				if (lineCount % 100000 == 0) {System.out.println(lineCount);}
				processLine(line, tag);				
			}
			System.out.println("Java titles = " + javaTitles);
			System.out.println("Cpp titles = " + cppTitles);
			System.out.println("Total rows = " + lineCount);
			reader.close();
		} catch(Exception e) {}
	}

	private static void processLine(String line, String tag) throws XMLStreamException {
		if (!line.trim().startsWith("<row")) return;
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
            		   return;
            	   }
            	   String body = XMLUtil.getStringElement(startElement, "Body");
            	   Set<String> codeSet = SOUtil.getCodeSet(body);
            	   if (codeSet.size() == 0) {
            		   return;
            	   }
            	   String tags = XMLUtil.getStringElement(startElement, "Tags");       
            	   if ((tags == null)||(tags.trim().length()==0)) {
            		   return;
            	   } 
            	   if (SOUtil.hasJavaTag(tags)) {
            		   javaTitles++;
            	   }
            	   if (SOUtil.hasCppTag(tags)) {
            		   cppTitles++;
            	   }
            	   String creationDate = XMLUtil.getStringElement(startElement, "CreationDate");
            	   if (SOUtil.hasTagOnly(tags, tag)) {
            		   count++;
            		   items.add(tags + ":" + creationDate + ":" + title);
            	   }
            	}
           }
        }
	}
}
