package in.ac.iiitd.pag.anne.patterns;

import in.ac.iiitd.pag.util.FileUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SignificantPatternsGrabber {
	public static void main(String[] args) {
		try {
			String baseFolder = "C:\\data\\svn\\iiitdsvn\\entity\\data\\ossprojects\\";
			String project = "hadoop";
			String outputFilePath = baseFolder + "significantPatterns.txt";
			grab(baseFolder, project, outputFilePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void grab(String baseFolder, String project, String outputFilePath) throws IOException {
		
		Set<String> significantPatterns = new HashSet<String>();
		String onegramFilePath = baseFolder + project + "-1grams-final.txt";
		
		Map<String, Integer> onegrams = FileUtil.getLastNIntMapFromFile(onegramFilePath, 25);
		Map<Integer,Map<String,Integer>> ngrams = new HashMap<Integer,Map<String,Integer>>();
		for(int i = 2; i<=7; i++) {
			String ngramFilePath = baseFolder + project + "-"+i+"grams-final.txt";
			Map<String, Integer> patternCount = FileUtil.getLastNIntMapFromFile(ngramFilePath, 100);
			ngrams.put(i, patternCount);
		}		
		
		
		for(String pattern: onegrams.keySet()) {
			List<String> patterns = new ArrayList<String>();
			int patternVal = onegrams.get(pattern) / 2;
			for(int i=7; i>1; i--) {
				Map<String, Integer> igrams = ngrams.get(i);
				for(String key: igrams.keySet()) {
					if (igrams.get(key) < patternVal) continue;
					if (hasOnegram(key, pattern)) {
						//if (!subPatternOf(key, patterns)) {
							patterns.add(key);
						//}
					}
				}
			}
			print(patterns, pattern);
			significantPatterns.addAll(patterns);
			if (patterns.size() == 0) significantPatterns.add(pattern);
		}
		
		FileUtil.writeListToFile(significantPatterns, outputFilePath);
	}

	private static void print(List<String> patterns, String token) {
		System.out.println("\n" + token);
		System.out.println("==============");
		for(String pattern: patterns) {
			System.out.println(pattern);
		}
		if (patterns.size() == 0) System.out.println(token);
	}

	private static boolean subPatternOf(String key, List<String> patterns) {
		boolean returnValue = false;
		String[] keyParts = key.split(" ");
		for(String pattern: patterns) {
			String[] patternParts = pattern.split(" ");
			int matchCount= 0;
			for(int i=0; i<keyParts.length; i++) {
				for (int j=i; j<patternParts.length; j++ ) {
					if (keyParts[i].equalsIgnoreCase(patternParts[j])) {
						matchCount++;
					}
				}
			}
			if (matchCount == keyParts.length) {
				returnValue = true;
			}
		}
		return returnValue;
	}

	private static boolean hasOnegram(String key, String pattern) {
		boolean returnValue = false;
		String[] parts = key.split(" ");
		for(String part: parts) {						
			if (part.equalsIgnoreCase(pattern)) {
				returnValue = true;
				break;
			}
		}
		return returnValue;
	}
}
