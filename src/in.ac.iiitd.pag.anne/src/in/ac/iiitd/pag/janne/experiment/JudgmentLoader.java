package in.ac.iiitd.pag.janne.experiment;

import in.ac.iiitd.pag.util.FileUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * We have 18 judgment files. We load all these files line by line. For each line,
 * we collect all the manually annotated tags and count the occurence of each tag 
 * per line. If at least half the annotators have marked a tag, we retain the tag.
 * We write a new file x-annotated.txt with all retained tags.
 * 
 * @author Venkatesh
 *
 */
public class JudgmentLoader {
	public static void main(String[] args) {
		String baseFolder = "C:\\data\\svn\\iiitdsvn\\entity\\data\\experiment\\expcode\\";
		Set<String> paths = FileUtil.getAllFilePaths(baseFolder, "txt");
		
		Map<String, List<String>> lineToTagsMap = new HashMap<String, List<String>>();
		
		for(String path: paths) {
			String code = FileUtil.readFromFile(path);
			String[] lines = code.split("\r\n|\r|\n");
			for(String line: lines) {
				line = line.trim();
				if (line.length() == 0) continue;
				List<String> tags = new ArrayList<String>();
												
				if (line.contains("//")) {					
					String[] parts = line.split("//");					
					if (parts.length == 2) {
						if (lineToTagsMap.containsKey(parts[0].trim())) {
							tags =  lineToTagsMap.get(parts[0].trim());
						}
						String[] tokens = parts[1].split(",");
						if (tokens.length == 0) continue;
						for(String token: tokens) {
							if (token.trim().length() == 0) continue;
							tags.add(token.trim());							
						}
						lineToTagsMap.put(parts[0].trim(), tags);
					}
				}
			}
		}
		
		baseFolder = "C:\\data\\svn\\iiitdsvn\\entity\\data\\experiment\\unannotated\\";
		try {
			saveAnnotatedCode(baseFolder + "Activiti-Set1.txt", baseFolder + "Activiti-Set1-annotated.txt", lineToTagsMap);
			saveAnnotatedCode(baseFolder + "Activiti-Set2.txt", baseFolder + "Activiti-Set2-annotated.txt", lineToTagsMap);
			saveAnnotatedCode(baseFolder + "Hadoop-Set1.txt", baseFolder + "Hadoop-Set1-annotated.txt", lineToTagsMap);
			saveAnnotatedCode(baseFolder + "Hadoop-Set2.txt", baseFolder + "Hadoop-Set2-annotated.txt", lineToTagsMap);
			saveAnnotatedCode(baseFolder + "Guava-Set1.txt", baseFolder + "Guava-Set1-annotated.txt", lineToTagsMap);
			saveAnnotatedCode(baseFolder + "Guava-Set2.txt", baseFolder + "Guava-Set2-annotated.txt", lineToTagsMap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void saveAnnotatedCode(String path, String outputPath, Map<String, List<String>> lineToTagsMap) throws IOException {
		String code = FileUtil.readFromFile(path);
		String[] lines = code.split("\r\n|\r|\n");
		FileWriter fw = new FileWriter(outputPath);
		BufferedWriter bw = new BufferedWriter(fw);		
		for(String line: lines) {			
			if (lineToTagsMap.containsKey(line.trim())) {
				String tags = getTagsFrom(lineToTagsMap.get(line.trim()));	
				if (tags.length() > 0)
					line = line + "\t\t//" + tags;				
			}
			bw.write(line + "\n");
		}
		bw.close();
		fw.close();
	}

	private static String getTagsFrom(List<String> tagsList) {
		Map<String, Integer> tags = new HashMap<String,Integer>();
		int count = 0;
		for(String tag: tagsList) {
			if (tags.containsKey(tag)) {
				count = tags.get(tag);
				tags.put(tag, count+1);
			} else {
				tags.put(tag, 1);
			}
		}
		
		String output = "";
		for(String tag: tags.keySet()) {
			//output = output + tag + "(" + tags.get(tag) + "),";
			if (tags.get(tag) > 2) {
				output = output + tag + ",";
			}
		}
		if (output.endsWith(","))
			output = output.substring(0, output.length()-1);
		return output;
	}
}
