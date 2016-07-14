package in.ac.iiitd.pag.anne.brown;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import in.ac.iiitd.pag.util.FileUtil;

public class LMNormalizer {
	public static void main(String[] args) {
		try {
			Map<String, Float> collection = getMapFromFile("allCodeTF.csv");
			Map<String, Float> removeWord = getMapFromFile("code-removeTF.csv");
			
			float maxCTF = 0;
			for(String word: collection.keySet()) {
				if (collection.get(word) > maxCTF) 
					maxCTF = collection.get(word);
			}
						
			Map<String, Float> javaWords = new HashMap<String, Float>();
			for(String word: removeWord.keySet()) {
				if (collection.containsKey(word)) {				
					if (collection.get(word) < 150) continue;
					float prob = removeWord.get(word) * (maxCTF / collection.get(word));
					//System.out.println(word + "," + prob);
					//javaWords.put(word + "(" + collection.get(word) + " : " + removeWord.get(word) + ")", prob);
					javaWords.put(word, prob);
				} 
			}
			
			javaWords = FileUtil.sortByFloatValues(javaWords);
			
			writeMap(javaWords, "normalizedTF.csv");
			
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
			bw.write(key + "," + computerWords.get(key) + "\n"); 
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
