package in.ac.iiitd.pag.anne.entity;

import in.ac.iiitd.pag.util.CodeFragmentInspector;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.SOUtil;
import in.ac.iiitd.pag.util.StringUtil;
import in.ac.iiitd.pag.util.XMLUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Sample Stack Overflow randomly. Ensure only Java/C++ tagged files are present.
 * Takes language as a parameter. 
 * @author Venkatesh
 *
 */
public class RandomSampler {
	
	private static final String LANGUAGE = "Java";
	private static String inputPath = "";
	private static final String outputPath = "RandomSamplePosts-" + LANGUAGE + ".xml";
	private static int THRESHOLD = 100;
	private static boolean done = false;
	private static String  output = "";
	private static int counter = 0;
	
	public static void main(String[] args) {
		Properties props = FileUtil.loadProps();
		inputPath = props.getProperty("FILE_PATH");
		
		if (args.length == 1) {
			THRESHOLD = Integer.parseInt(args[0]);									
		} else {
			System.out.println("No threshold mentioned. Taking it as " + THRESHOLD + ".");
		}
		
		sample();
		try {
			store();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * store the results.
	 * variable output should contain the string
	 * to be saved. 
	 * output may not have opening and closing tags
	 * to make it well formed xml. Hence, add them
	 * here.
	 * @throws IOException 
	 */
	private static void store() throws IOException {
		output = "<posts>\n" + output + "</posts>"; 
		FileWriter fw = new FileWriter(outputPath);
		BufferedWriter bw = new BufferedWriter(fw);	
		bw.write(output);
		bw.close();
		fw.close();
	}

	//Sample and store. Boiler plate code.
	//Actual work is done in processRow method.
	private static void sample() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputPath), 4 * 1024 * 1024);
			String line = null;			
			int lineCount = 0;
			System.out.println("Reading file...");
			while ((line = reader.readLine()) != null) {
				lineCount++;
				//if (lineCount % 2000 == 0) System.out.print(".");
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
			            	   if (!done) processRow(startElement, line);
			               }
			           }
			        }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	private static List<String> getCodeFromLine(StartElement startElement) throws IOException {
		List<String> code = new ArrayList<String>();
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
		return code;
	}

	/**
	 * Sample and Store here. 
	 * Each line of inputPath is used for processing.
	 * To control sample size, use a flag - done.
	 * @param startElement 
	 * @param line
	 * @throws IOException 
	 */
	private static void processRow(StartElement startElement, String line) throws IOException {
		//Randomize.
		Random random = new Random();
		if (random.nextFloat() < 0.5) return;
		
	   String title = XMLUtil.getStringElement(startElement, "Title");
 	   if (title == null) title = "";
 	   String tags = XMLUtil.getStringElement(startElement, "Tags");
 	   boolean isJava = SOUtil.hasJavaTag(tags);
 	   List<String> code = getCodeFromLine(startElement);
 	   if (isJava && code.size() > 0 && title.length() > 3) {
 		   //Let us print the title. 
 		   System.out.println(title);
 		   output = output + "\n" + line;
 		   counter++;
 		   if (counter == THRESHOLD) done = true;
 	   }
	}
	
}
