package in.ac.iiitd.pag.anne.brown;

import in.ac.iiitd.pag.util.FileUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TitleReducer {
	public static void main(String[] args) {
		try {
			getImperativeTitles("c:\\temp\\workdir\\alltitles.txt", "c:\\temp\\workdir\\javaTitles.txt");						
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void getImperativeTitles(String fileName, String outputFile) throws IOException {
		//Set<String> knownTags = new HashSet<String>(Arrays.asList("c", "c#","java","perl","python","ruby","php"));
		Set<String> knownTags = new HashSet<String>(Arrays.asList("java"));
		List<String> titles = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(fileName), 4 * 1024 * 1024);
		FileWriter fw = new FileWriter(outputFile);
		BufferedWriter bw = new BufferedWriter(fw, 2 * 1024 * 1024);
		String line = null;			
		int lineCount = 0;
		System.out.println("Reading file...");
		Set<String> tags = new HashSet<String>();
		while ((line = reader.readLine()) != null) {
			lineCount++;
			if (lineCount % 1000000 == 0) System.out.print(".");
			String[] tagsTitles = line.split(":");
			if (tagsTitles != null) {
				if (tagsTitles.length == 2) {
					tags.clear();
					String tagList = tagsTitles[0];
					String title = tagsTitles[1];
					tagList = tagList.replaceAll("><", " ");
					tagList = tagList.replace("<", "");
					tagList = tagList.replace(">", "").toLowerCase();
					String[] allTags = tagList.split(" ");
					for(int i=0; i<allTags.length; i++) {
						tags.add(allTags[i]);
					}
					for(String knownTag: knownTags) {
						if (tags.contains(knownTag)) {
							bw.write(title + "\n");
							break;
						}
					}
				}
			}			
		}
		bw.close();
		fw.close();
	}
}
