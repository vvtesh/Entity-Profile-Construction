package in.ac.iiitd.pag.anne.brown;

import in.ac.iiitd.pag.util.FileUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class TagsFromTitleTags {
	public static void main(String[] args) {
		try {
			Set<String> tags = getTags("c:\\temp\\workdir\\alltitles.txt");
			FileUtil.writeListToFile(tags, "c:\\temp\\workdir\\alltags.txt");			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static Set<String> getTags(String fileName) throws IOException {
		Set<String> tags = new HashSet<String>();
		BufferedReader reader = new BufferedReader(new FileReader(fileName), 4 * 1024 * 1024);
		String line = null;			
		int lineCount = 0;
		System.out.println("Reading file...");
		
		while ((line = reader.readLine()) != null) {
			lineCount++;
			if (lineCount % 1000000 == 0) System.out.print(".");
			String[] tagsTitles = line.split(":");
			if (tagsTitles != null) {
				String tagList = tagsTitles[0];
				tagList = tagList.replaceAll("><", " ");
				tagList = tagList.replace("<", "");
				tagList = tagList.replace(">", "");
				String[] allTags = tagList.split(" ");
				for(int i=0; i<allTags.length; i++) {
					tags.add(allTags[i]);
				}
			}
		}
		return tags;
	}
	
}
