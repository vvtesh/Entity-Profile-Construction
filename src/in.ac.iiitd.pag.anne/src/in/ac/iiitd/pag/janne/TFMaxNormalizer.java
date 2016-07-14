package in.ac.iiitd.pag.janne;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import in.ac.iiitd.pag.util.FileUtil;

public class TFMaxNormalizer {
	public static void main(String[] args) {
		try {
			String fileName = "loop-lines-weighted-ngram.txt";
			String outputFile = "output.csv";
			int cutoff = 0;
			if (args.length  == 3) {
				fileName = args[0];
				outputFile = args[1];
				cutoff = Integer.parseInt(args[2]);
			}
			Map<String, Integer> collection = FileUtil.getMapFromFile(fileName);
			
			Map<String, Integer> weights = normalizeMax(collection);
			FileUtil.writeMapToFile(weights, outputFile, cutoff);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

	public static Map<String, Integer> normalizeMax(
			Map<String, Integer> collection) {
		Map<String, Integer> normalizedTerms = new HashMap<String, Integer>();
		int max = 0;
		for(String item: collection.keySet()) {
			if (collection.get(item) > max) {
				max = collection.get(item);
			}
		}
		for(String item: collection.keySet()) {
			int value = collection.get(item);
			int newValue = value * 1000 / max;
			normalizedTerms.put(item, newValue);
		}
		return normalizedTerms;
	}


}
