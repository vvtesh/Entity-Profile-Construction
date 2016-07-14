package in.ac.iiitd.pag.anne.patterns;

import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PatternGrabber {
	public static void main(String[] args) {
		try {
			grab();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void grab() throws IOException {
		String baseFolder = "C:\\data\\svn\\iiitdsvn\\entity\\data\\ossprojects\\";
		Map<String, Integer> patternCounts = new HashMap<String, Integer>();
		Map<String, Integer> onegrams = FileUtil.getLastNIntMapFromFile(baseFolder + "hadoop-1grams-final.txt", 10);
		
		Map<Integer,Map<String,Integer>> ngrams = new HashMap<Integer,Map<String,Integer>>();
		for(int i = 2; i<=7; i++) {
			Map<String, Integer> patternCount = FileUtil.getLastNIntMapFromFile(baseFolder + "hadoop-"+i+"grams-final.txt", 50);
			ngrams.put(i, patternCount);
		}
		
		Map<String, List<Integer>> topPatterns = new HashMap<String, List<Integer>>();
		String output = "";
		for(String pattern: onegrams.keySet()) {
			int patternVal = onegrams.get(pattern);
			if (patternVal < 4000) continue;
			patternVal = patternVal / 2;
			
			List<Integer> patternsWithKey = new ArrayList<Integer>();
			for(int i=2; i<=7; i++) {
				Map<String, Integer> igrams = ngrams.get(i);
				for(String key: igrams.keySet()) {
					if (igrams.get(key) < patternVal) continue;
					String[] parts = key.split(" ");
					for(String part: parts) {						
						if (part.equalsIgnoreCase(pattern)) {
							patternsWithKey.add(igrams.get(key));							
						}
					}
				}				
			}
			Collections.sort(patternsWithKey);
			Collections.reverse(patternsWithKey);
			topPatterns.put(pattern, patternsWithKey);
		}
		
		for(String pattern: topPatterns.keySet()) {
			output = output + pattern + ",";
			List<Integer> patternsWithKey = topPatterns.get(pattern);
			for(Integer i: patternsWithKey) {
				output = output + i + ",";
			}
			output = output + "\n";
		}
		
		FileUtil.saveFile(baseFolder + "hadoop-all-patterns.txt", output);
	}
}
