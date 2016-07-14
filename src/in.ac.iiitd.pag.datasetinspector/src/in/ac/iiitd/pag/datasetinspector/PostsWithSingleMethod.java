package in.ac.iiitd.pag.datasetinspector;

import in.ac.iiitd.pag.util.LanguageUtil;
import in.ac.iiitd.pag.util.SOUtil;
import in.ac.iiitd.pag.util.XMLUtil;
import in.ac.iiitd.pag.util.FileUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class PostsWithSingleMethod {
	public static void main(String[] args) {
		Properties props = FileUtil.loadProps();
		if (props == null) return;
		
		String JAVA_POSTS_FILE_PATH = props.getProperty("JAVA_POSTS_FILE_PATH");
		String SINGLE_METHOD_JAVA_POSTS_FILE_PATH = props.getProperty("SINGLE_METHOD_JAVA_POSTS_FILE_PATH");
		
		processFile(JAVA_POSTS_FILE_PATH, SINGLE_METHOD_JAVA_POSTS_FILE_PATH);
	}

	private static void processFile(String jAVA_POSTS_FILE_PATH,
			String sINGLE_METHOD_JAVA_POSTS_FILE_PATH) {
		HashSet<Integer> ids = new HashSet<Integer>();
		try {
			/*FileWriter fwCode = new FileWriter("C:\\temp\\code1.txt");
   			BufferedWriter bwCode = new BufferedWriter(fwCode, 2 * 1024 * 1024);*/		
   			
			FileWriter fw = new FileWriter(sINGLE_METHOD_JAVA_POSTS_FILE_PATH);
   			BufferedWriter bw = new BufferedWriter(fw, 2 * 1024 * 1024);		
   			bw.write("<posts>\n");
   			bw.flush();
   			
			BufferedReader reader = new BufferedReader(new FileReader(jAVA_POSTS_FILE_PATH), 2 * 1024 * 1024);
			String line = null;			
			int lineCount = 0;
			int count=0;
			System.out.println("Reading file..." + jAVA_POSTS_FILE_PATH);
			while ((line = reader.readLine()) != null) {
				lineCount++;
				//if (lineCount < 140000) continue;
				if (lineCount % 100 == 0) System.out.print(".");
				if (lineCount % 5000 == 0) {System.out.println(lineCount);}
				try {
					if (!line.trim().startsWith("<row")) continue;
					
					XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();        
			        XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new StringReader(line));        
			        while(xmlEventReader.hasNext()){
			           XMLEvent xmlEvent = xmlEventReader.nextEvent();
			           if (xmlEvent.isStartElement()){
			               StartElement startElement = xmlEvent.asStartElement();
			               if(startElement.getName().getLocalPart().equalsIgnoreCase("row")){
			            	   Attribute idAttr = startElement.getAttributeByName(new QName("Id"));		                	   		                	   
			            	   int id = Integer.parseInt(idAttr.getValue());	                	   
		                	   String body = XMLUtil.getStringElement(startElement, "Body");
		                	   
		                	  
		                	   Set<String> codeSet = SOUtil.getCodeSet(body);
		                	   Set<String> codeSetWithOneMethod = new HashSet<String>();
		                	   String methodDef = "";
		                	   int methodCount = 0;
		                	   for(String code: codeSet) {
		                		   int methodsInThisCode = LanguageUtil.getJavaMethodCount(code);
		                		   methodCount = methodCount + methodsInThisCode;
		                		   if (methodCount > 1) break;
		                		   if (methodsInThisCode==1) {		                			   
		                			   methodDef = code;
		                			   codeSetWithOneMethod.add(code);
		                		   }
		                	   }
		                	   if (methodCount != 1) continue;
		                	  // bwCode.write("*************" + count + "*************\n" + codeSetWithOneMethod.iterator().next() + "\n\n" );
		                	   	
		                	   //To fill
		                	   ids.add(id);
		                	   bw.write(line + "\n");
		                	   count++;
			               }
			           }
			        }
			        xmlEventReader.close();			        
				} catch (Exception e) {}
			}
			bw.write("</posts>");
			bw.close();
			/*bwCode.flush();
			bwCode.close();*/
			System.out.println(count + " posts written.");
		} catch (Exception e) {}
			        
		
	}
}
