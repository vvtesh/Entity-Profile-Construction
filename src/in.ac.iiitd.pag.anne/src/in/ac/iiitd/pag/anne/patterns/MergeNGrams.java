package in.ac.iiitd.pag.anne.patterns;

import in.ac.iiitd.pag.util.FileUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MergeNGrams {
	public static void main(String[] args) {
		try {
			int n = 5;
			String inputFile = "C:\\data\\svn\\iiitdsvn\\entity\\data\\ossprojects\\hadoop-"+n+"grams.txt";
			String outputFile = "C:\\data\\svn\\iiitdsvn\\entity\\data\\ossprojects\\hadoop-"+n+"grams-final.txt";
			
			merge(n, inputFile, outputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void merge(int n, String inputFile, String outputFile) throws IOException {
		Map<String,Integer> mapOut = new HashMap<String, Integer>();
		List<String> lines = FileUtil.readFromFileAsList(inputFile);
		for(String line: lines) {
			String[] parts = line.split(",");
			if (parts.length > 2) continue;
			String newLine = parts[0].trim();
			int count = Integer.parseInt(parts[1]);
			
			if (mapOut.containsKey(newLine)) {
				mapOut.put(newLine, mapOut.get(newLine) + count);
			} else {
				mapOut.put(newLine, count);
			}
			
		}
		FileUtil.writeMapToFile(mapOut, outputFile, 0);
	}
}
