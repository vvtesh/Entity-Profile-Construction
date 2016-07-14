package in.ac.iiitd.pag.anne.patterns;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.ac.iiitd.pag.util.FileUtil;

public class PatternStats {
	public static void main(String[] args) {
		try {
			getStats();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void getStats() throws IOException {
		String baseFolder = "C:\\data\\svn\\iiitdsvn\\entity\\data\\ossprojects1\\";
		
		List<String> p3 = FileUtil.readFromFileAsList(baseFolder + "guava-significantPatterns.txt");
		List<String> p2 = FileUtil.readFromFileAsList(baseFolder + "activiti-significantPatterns.txt");
		List<String> p1 = FileUtil.readFromFileAsList(baseFolder + "hadoop-significantPatterns.txt");
		
		Map<String,Integer> patternsMap = new HashMap<String,Integer>();
		
		for(String item: p1) {
			patternsMap.put(item, 1);
		}
		
		for(String item: p2) {
			addToMap(patternsMap, item);
			/*if (patternsMap.containsKey(item))
				patternsMap.put(item, patternsMap.get(item) + 1);
			else
				patternsMap.put(item, 1);*/
		}
		for(String item: p3) {
			addToMap(patternsMap, item);
		}
		FileUtil.writeMapToFile(patternsMap, baseFolder + "combined-significantPatterns.txt", 0);
	}
	
	private static void addToMap(Map<String,Integer> pMap, String item) {
		if (pMap.containsKey(item))
			pMap.put(item, pMap.get(item) + 1);
		else
			pMap.put(item, 1);
	}
}
