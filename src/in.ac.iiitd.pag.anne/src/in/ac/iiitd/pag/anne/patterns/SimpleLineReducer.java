package in.ac.iiitd.pag.anne.patterns;

import in.ac.iiitd.pag.util.FileUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleLineReducer {
	public static void main(String[] args) {
		try {
			String inputFile = "C:\\data\\svn\\iiitdsvn\\entity\\data\\ossprojects\\hadoop-line-reduced.txt";
			String outputFile = "C:\\data\\svn\\iiitdsvn\\entity\\data\\ossprojects\\hadoop-line-reduced-freq.txt";
			
			reduce(inputFile, outputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void reduce(String inputFile, String outputFile) throws IOException {
		List<String> lines = FileUtil.readFromFileAsList(inputFile);
		Map<String,Integer> map = new HashMap<String,Integer>();
		for(String line: lines) {
			if (line.split(" ").length > 15) continue;
			if (map.containsKey(line)) {
				map.put(line, map.get(line)+1);
			} else {
				map.put(line, 1);
			}
		}
		FileUtil.writeMapToFile(map, outputFile, 0);
	}
}
