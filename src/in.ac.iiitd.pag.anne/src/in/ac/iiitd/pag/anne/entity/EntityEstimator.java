package in.ac.iiitd.pag.anne.entity;

import in.ac.iiitd.pag.util.FileUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class EntityEstimator {
	public static void main(String[] args) {
		Properties props = FileUtil.loadProps();
		String inputFile = props.getProperty("OUTPUT_FILE_MERGED");
		String outputFile = props.getProperty("FINAL_OUTPUT_FILE");
		estimate(inputFile, outputFile);
	}

	private static void estimate(String inputFile, String outputFile) {
		Map<String,Integer> map = new HashMap<String, Integer>();
		FileWriter fw;
		try {
			fw = new FileWriter(outputFile);		
			BufferedWriter bw = new BufferedWriter(fw);	
			BufferedReader reader = new BufferedReader(new FileReader(inputFile), 4 * 1024 * 1024);
			String line = null;			
			int lineCount = 0;
			System.out.println("Reading file...");
			while ((line = reader.readLine()) != null) {
				lineCount++;
				if (lineCount % 2000 == 0) System.out.print(".");
				if (lineCount % 100000 == 0) System.out.println(lineCount);
								
				String[] items = line.split(":");
				if (items.length > 1) {
					String[] terms = items[1].split(",");
					if (terms.length > 0) {
						for(String term: terms) {
							int count = 0;
							if (map.containsKey(term)) {
								count = map.get(term);
							}
							count++;
							map.put(term, count);
						}
					}
				}				
			}
			FileUtil.writeMapToFile(map, outputFile, 10);
			bw.close();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
