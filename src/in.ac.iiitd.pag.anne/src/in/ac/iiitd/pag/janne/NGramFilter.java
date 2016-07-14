package in.ac.iiitd.pag.janne;

import in.ac.iiitd.pag.util.CodeFragmentInspector;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.NGramBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NGramFilter {
	public static void main(String[] args) {
		String entityName = "loop";
		
		try {
			findPatterns(entityName, "ranked-" + entityName + ".txt");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void findPatterns(String entityName, String filename) throws IOException {
		//Set<String> patterns = new HashSet<String>();
		Map<String,Integer> codeWithFreq = FileUtil.getMapFromFile(filename);	
		Set<String> code = codeWithFreq.keySet();
		FileUtil.writeListToFile(code, "temp11"+entityName+".txt");
		
		Map<String, Integer> tf = TFCalculator.construct("temp11loop.txt");
		tf = FileUtil.sortByValues(tf);
		FileUtil.writeMapToFile(tf, "temp11tf.txt",0);
		/*tf = getReducedTF(tf);
		Set<String> codeReduced = getReducedCode(tf, "temp11.txt");
		FileUtil.writeListToFile(codeReduced, "temp111.txt");*/
		
		Map<String, Integer> ngramFreq = getNGramFreq(code);
		ngramFreq = FileUtil.sortByValues(ngramFreq);
		FileUtil.writeMapToFile(ngramFreq, "temp111ngrams"+entityName+".txt", 50);
		//return patterns;
	}

	private static Map<String, Integer> getNGramFreq(Set<String> codeReduced) {
		 Map<String, Integer> ngf = new HashMap<String, Integer>();
		 for(String code: codeReduced) {
			 for(int i=1; i<5; i++) {
				 Set<String> ngrams = NGramBuilder.getSequentialNgramsAnyN(code.replace(";", ""), i);
				 for(String ngram: ngrams) {
					 ngram = ngram.trim();
					 if (ngram.length() == 0) continue;
					 int count = 0;
					 if (ngf.containsKey(ngram)) {
						 count = ngf.get(ngram);
					 }
					 count++;
					 ngf.put(ngram, count);
				 }
			 }
		 }
		 return ngf;
	}

	private static Map<String, Integer> getReducedTF(Map<String, Integer> tf) {
		Map<String, Integer> rtf = new HashMap<String, Integer>();
		int size = tf.size();
		int cutoff = size * 2/10;
		String[] tfItems = tf.keySet().toArray(new String[size]);
		for(int i=0; i<size; i++) {
			if (i < cutoff) continue;
			rtf.put(tfItems[i], tf.get(tfItems[i]));
		}
		return rtf;
	}

	private static Set<String> getReducedCode(Map<String, Integer> weights,
			String fileName) {
		   Set<String> newCode = new HashSet<String>();
		   List<String> statements = FileUtil.readFromFileAsList(fileName);
		   
		   
		   float lineWeight = 0;
		   for(String statement: statements) {
			   String tokensOfInterest = "";
			   String[] tokens = statement.split(" ");
			   for(String token: tokens) {
				   if (weights.containsKey(token)) {
					   //lineWeight = lineWeight + weights.get(token);
					   tokensOfInterest = tokensOfInterest + " " + token;
				   }
			   }
			   newCode.add(tokensOfInterest.trim());
		   }		                			   
		   return newCode;
	}
}
