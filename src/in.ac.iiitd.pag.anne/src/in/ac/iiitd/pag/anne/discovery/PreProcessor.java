package in.ac.iiitd.pag.anne.discovery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.StringUtil;

/**
 * Discovered entities are of the form entity:frequency.
 * Let's clean up and get an entity list such that:
 *    1. no freq info exists.
 *    2. retain only entities which exist more than n times.
 *    3. remove entities with numbers, symbols (any non-alphabet chars). 
 * @author Venkatesh
 *
 */
public class PreProcessor {
	public static void main(String[] args) {
		String filePath = "C:\\data\\svn\\iiitdsvn\\entity\\data\\DiscoveredEntities.txt";
		String outputFilePath = "C:\\data\\svn\\iiitdsvn\\entity\\data\\ProcessedDiscoveredEntities.txt";
		int SKIP_BELOW  = 500;
		List<String> filteredEntitiesList = new ArrayList<String>();
		
		List<String> items = FileUtil.readFromFileAsList(filePath);
		
		for(String item: items) {
			String[] elements = item.split(":");
			if (elements.length != 2) continue;
			int count = Integer.parseInt(elements[1]);
			String entity = elements[0];
			if (count < SKIP_BELOW) continue;
			if (StringUtil.isOnlyAlphabets(entity))
				filteredEntitiesList.add(entity);
		}
		
		try {
			FileUtil.writeListToFile(filteredEntitiesList, outputFilePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
