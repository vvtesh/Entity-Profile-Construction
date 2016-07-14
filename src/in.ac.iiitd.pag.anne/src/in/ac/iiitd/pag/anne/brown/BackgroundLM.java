package in.ac.iiitd.pag.anne.brown;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import in.ac.iiitd.pag.util.FileUtil;

public class BackgroundLM {
	public static void main(String[] args) {
		try {
			construct("C:\\tools\\brown\\javaTitles.txt", "C:\\tools\\brown\\javaTitlesLM.csv");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void construct(String inputFile, String outputFile) throws IOException {
		HashMap<String, Float> counts = new HashMap<String,Float>();
		BufferedReader reader = new BufferedReader(new FileReader(inputFile), 4 * 1024 * 1024);
		String line = null;			
		int lineCount = 0;
		System.out.println("Reading file...");
		while ((line = reader.readLine()) != null) {
			lineCount++;
			if (lineCount % 100000 == 0) System.out.print(".");
		
			String[] words = line.split(" ");
			int docLen = words.length;
			for(int i=0; i<docLen; i++) {
				String word = words[i].replaceAll("\\r|\\n", "").toLowerCase();
				word = word.replaceAll("[^a-zA-Z0-9]", "");
				if (word.trim().length() <= 1) {
					continue;
				}			
				float newCount = 0;
				if (counts.containsKey(word)) {
					newCount = counts.get(word);
				}
				newCount++;
				counts.put(word, newCount);
			}
		}
		reader.close();
		
		int docLen = 0;
		for(String word: counts.keySet()) {
			if (counts.get(word) < 10) continue;
			docLen = docLen + ((int) (counts.get(word) * 1));
		}
		
		float total = 0;
		System.out.println(docLen);
		FileWriter fw = new FileWriter(outputFile);		
		BufferedWriter bw = new BufferedWriter(fw);	
		for(String key: counts.keySet()) {
			if (counts.get(key) < 10) continue;
			float normalizedCount = counts.get(key) * 1.0f / docLen;
			if (normalizedCount > 1) {
				System.out.println(counts.get(key) + " " + docLen);
			}
			bw.write(key + "," + normalizedCount + "\n");
			//counts.put(key, normalizedCount);
			total = total + normalizedCount;
		}
		System.out.println(total);
		bw.close();
		fw.close();
	}
	
	public static Float precision(int decimalPlace, Float d) {

	    BigDecimal bd = new BigDecimal(Float.toString(d));
	    bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
	    return bd.floatValue();
	  }
}
