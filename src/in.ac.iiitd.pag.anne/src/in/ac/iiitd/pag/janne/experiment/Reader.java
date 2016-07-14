package in.ac.iiitd.pag.janne.experiment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import in.ac.iiitd.pag.util.FileUtil;

public class Reader {
	public static void main(String[] args) {
		printStats();
	}

	private static void printStats() {
		
		String baseFolder = "C:\\data\\svn\\iiitdsvn\\entity\\data\\experiment\\expcode\\";
		Set<String> paths = FileUtil.getAllFilePaths(baseFolder, "txt");
		
		for(String path: paths) {
			if (!path.contains("Guava-Set2")) continue;
			Map<String, Set<String>> lineToTagsMap = new HashMap<String, Set<String>>();
			int lineCount = 0;
			int linesWithAtleastOneTag = 0;
			int tagCount = 0;
			String code = FileUtil.readFromFile(path);
			String[] lines = code.split("\r\n|\r|\n");
			for(String line: lines) {
				if (line.trim().length() == 0) continue;
				Set<String> tags = new HashSet<String>();
				lineCount++;
				if (line.contains("//")) {					
					String[] parts = line.split("//");
					if (parts.length == 2) {
						
						String[] tokens = parts[1].split(",");
						if (tokens.length == 0) continue;
						for(String token: tokens) {
							if (token.trim().length() == 0) continue;
							tags.add(token.trim());							
						}
						lineToTagsMap.put(line.trim(), tags);
						tagCount = tagCount + tags.size();
					}
				}
				//if (tagCount > 0) linesWithAtleastOneTag++;
			}
			System.out.println(path.replace(baseFolder, "") + "," + lineCount + "," + lineToTagsMap.size() + "," + tagCount);
			
		}
	}
}
