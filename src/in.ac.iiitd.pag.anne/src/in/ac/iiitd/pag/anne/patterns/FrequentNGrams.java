package in.ac.iiitd.pag.anne.patterns;

import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.NGramBuilder;
import in.ac.iiitd.pag.util.StringUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FrequentNGrams {
	public static void main(String[] args) {
		try {
			
			int n = 5;
			String inputFile = "C:\\data\\svn\\iiitdsvn\\entity\\data\\ossprojects\\hadoop-line-reduced-freq.txt";
			String outputFile = "C:\\data\\svn\\iiitdsvn\\entity\\data\\ossprojects\\hadoop-"+n+"grams.txt";
			if (args.length == 3) {
				n = Integer.parseInt(args[0]);
				inputFile = args[1];
				outputFile = args[2];
			}
			mine(n, inputFile, outputFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void mine(int n, String inputFile, String outputFile) throws IOException {
		int count = 0;
		
		Map<String, Integer> ngramFreq = new HashMap<String, Integer>();
		
		
		FileWriter fw = new FileWriter(new File(outputFile));
		BufferedWriter bw = new BufferedWriter(fw);
		List<String> lines = FileUtil.readFromFileAsList(inputFile);
		for(String newLine: lines) {
			count++;
			String[] parts = newLine.split(",");
			String line = parts[0];
			int value = Integer.parseInt(parts[1]);
			Set<String> ngrams = NGramBuilder.getSequentialNgramsAnyN(line, n);
			for(String ngram: ngrams) {
				if (ngramFreq.containsKey(ngram)) {
					ngramFreq.put(ngram, ngramFreq.get(ngram) + value);
				} else {
					ngramFreq.put(ngram, value);
				}				
			}
			if (count > 1000) {
				System.out.print(".");
				count = 0;
				for(String key: ngramFreq.keySet()) {
					if (ngramFreq.get(key) > 10) {
						bw.write(key + "," + ngramFreq.get(key) + "\n");
					}
				}
				ngramFreq.clear();
				bw.flush();
			}
		}
		
		
		bw.close();
		fw.close();
	}
}
