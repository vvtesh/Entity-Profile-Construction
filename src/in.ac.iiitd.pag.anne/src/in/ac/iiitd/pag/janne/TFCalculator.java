package in.ac.iiitd.pag.janne;

import in.ac.iiitd.pag.util.FileUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TFCalculator {
	public static void main(String[] args) {
		try {
			String inputFile = "C:\\data\\svn\\iiitdsvn\\entity\\data\\ossprojects\\hadoop-line-tokens.txt"; 
			String outputFile = "C:\\data\\svn\\iiitdsvn\\entity\\data\\ossprojects\\hadoop-line-tokens-tf.txt";
			int cutoff = 200;
			if (args.length == 3) {
				inputFile = args[0];
				outputFile = args[1];
				cutoff = Integer.parseInt(args[2]);
			}
			Map<String, Integer> tf = construct(inputFile);
			FileUtil.writeMapToFile(tf, outputFile, cutoff);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Map<String, Integer> construct(String inputFile) throws IOException {
		Map<String, Integer> counts = new HashMap<String,Integer>();
		BufferedReader reader = new BufferedReader(new FileReader(inputFile), 4 * 1024 * 1024);
		String line = null;
		Set<String> tempSet = new HashSet<String>();
		int lineCount = 0;
		System.out.println("Reading file...");
		while ((line = reader.readLine()) != null) {
			try {
				lineCount++;
				if (lineCount % 100000 == 0) System.out.print(".");
			    line = line.replace(".", " ");
			    line = line.toLowerCase();
				String[] words = line.split(" ");
				int docLen = words.length;
				tempSet.clear();
				for(int i=0; i<docLen; i++) {
					try {
						String word = words[i].trim();
						word = word.replace(",", "");
						//word = word.replaceAll("[^a-zA-Z0-9]", "");
						if (word.length() == 0) {
							continue;
						}			
						if (word.matches("[a-zA-Z0-9]")) continue;
						tempSet.add(word);
						
					} catch (Exception e) {
						System.out.println("Error in line " + e.getMessage());
					}
				}
				for(String word: tempSet) {					
					int newCount = 0;
					
					if (counts.containsKey(word)) {
						newCount = counts.get(word);
					}
					newCount++;
					
					counts.put(word, newCount);
				}
			} catch (Exception e) {
				System.out.println("Error " + e.getMessage());
			}
		}
		reader.close();
		return counts;
		
	}
	
	public static Float precision(int decimalPlace, Float d) {

	    BigDecimal bd = new BigDecimal(Float.toString(d));
	    bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
	    return bd.floatValue();
	  }
}
