package in.ac.iiitd.pag.datasetinspector;

import in.ac.iiitd.pag.util.FileUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * Creates a smaller Posts.xml file with only posts that are tagged as Java.
 * Run id titles creater (JavaTaggedPostIdsCollector.java) before running this.
 * @author Venkatesh
 *
 */
public class PostsReducer {
	public static void main(String[] args) {
		Properties props = FileUtil.loadProps();
		if (props == null) return;
		String FILE_PATH = props.getProperty("FILE_PATH");
		String JAVA_POSTS_FILE_PATH = props.getProperty("JAVA_POSTS_FILE_PATH");
		String ID_TITLES_VOTES_JAVA_FILE_PATH = props.getProperty("ID_TITLES_VOTES_JAVA_FILE_PATH");
		HashSet<Integer> ids = loadIds(ID_TITLES_VOTES_JAVA_FILE_PATH);
		reducePosts(FILE_PATH, JAVA_POSTS_FILE_PATH, ids);
	}

	private static void reducePosts(String posts,
			String javaPosts,HashSet<Integer> ids) {
		try {
		    FileWriter fw = new FileWriter(javaPosts);
   			BufferedWriter bw = new BufferedWriter(fw, 2 * 1024 * 1024);		
   			bw.write("<posts>\n");
   			bw.flush();

			BufferedReader reader = new BufferedReader(new FileReader(posts), 2 * 1024 * 1024);
			String line = null;			
			int lineCount = 0;
			int finalAdditionsCount = 0;
			System.out.println("Reading file...");
			while ((line = reader.readLine()) != null) {
				lineCount++;
				if (lineCount % 20000 == 0) System.out.print(".");
				if (lineCount % 1000000 == 0) System.out.println(lineCount);
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
			            	   
			            	   if (ids.contains(id)) {
			            		   bw.write(line + "\n");			            		   
			            		   ids.remove(id);
			            		   finalAdditionsCount++;
			            	   }    
			               }
			           }
			        }
			        xmlEventReader.close();
					
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
		reader.close();
		bw.write("</posts>");
		bw.close();
		System.out.println(finalAdditionsCount + " items added.");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		
	}

	private static HashSet<Integer> loadIds(String idTitleVotes) {
		HashSet<Integer> postIds = new HashSet<Integer>();
		List<String> posts = FileUtil.readFromFileAsList(idTitleVotes);
		for(String post: posts) {
			String[] items = post.split(",");
			try {
				int postId = Integer.parseInt(items[0]);
				postIds.add(postId);
			} catch (Exception e) {System.out.println(e.getMessage());}			
		}
		return postIds;
	}
}
