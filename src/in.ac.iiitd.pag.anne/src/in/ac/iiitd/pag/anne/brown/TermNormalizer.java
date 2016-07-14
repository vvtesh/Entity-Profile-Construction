package in.ac.iiitd.pag.anne.brown;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import in.ac.iiitd.pag.util.FileUtil;

public class TermNormalizer {
	public static void main(String[] args) {
		try {
			Map<String, Float> brown = getMapFromFile("C:\\tools\\brown\\cleanbrownlm.csv");
			Map<String, Float> computer = getMapFromFile("C:\\tools\\brown\\cleansolm.csv");
			Map<String, Float> java = getMapFromFile("C:\\tools\\brown\\cleanjavalm.csv");
			
			Map<String, Float> computerWords = new HashMap<String, Float>();
			for(String word: computer.keySet()) {
				if (brown.containsKey(word)) {
					float baseProb = brown.get(word);				
					float prob = computer.get(word) / baseProb;
					computerWords.put(word, prob);
				} else {
					computerWords.put(word, computer.get(word));
				}
			}
			
			writeMap(computerWords, "C:\\tools\\brown\\sowords.csv");
			
			Map<String, Float> javaWords = new HashMap<String, Float>();
			for(String word: java.keySet()) {
				if (computer.containsKey(word)) {
					float baseProb = computer.get(word);				
					float prob = computer.get(word) / baseProb;
					javaWords.put(word, prob);
				} else {
					javaWords.put(word, computer.get(word));
				}
			}
			
			writeMap(javaWords, "C:\\tools\\brown\\javawords.csv");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void writeMap(Map<String, Float> computerWords, String fileName)
			throws IOException {
		FileWriter fw = new FileWriter(fileName);		
		BufferedWriter bw = new BufferedWriter(fw);	
		int i=0;
		for(String key: computerWords.keySet()) {
			if (computerWords.get(key) == null) {
				System.out.println(i++ + ". " + key);
				continue;
			}
			bw.write(key + "," + ((int) (computerWords.get(key) * 1000)) + "\n"); 
		}
		bw.close();
		fw.close();
	}
	
	public static Map<String, Float> getMapFromFile(String fileName) {
		Map<String, Float> tokens = new HashMap<String, Float>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line = null;
			
			while ((line = reader.readLine()) != null) {
				String[] vals = line.split(",");
				try {
					String term = vals[0];
					float count = Float.parseFloat(vals[1]);
					tokens.put(term, count);
				} catch (Exception e) {}
			}
			reader.close();
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} 

		return tokens;
		
	}
}
