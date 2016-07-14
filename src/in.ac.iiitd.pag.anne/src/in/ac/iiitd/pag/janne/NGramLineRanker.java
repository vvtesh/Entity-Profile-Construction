package in.ac.iiitd.pag.janne;

import in.ac.iiitd.pag.util.CodeFragmentInspector;
import in.ac.iiitd.pag.util.ConfigUtil;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.NGramBuilder;
import in.ac.iiitd.pag.util.SOUtil;
import in.ac.iiitd.pag.util.StringUtil;
import in.ac.iiitd.pag.util.XMLUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class NGramLineRanker {
	public static void main(String[] args) {		
		try {
			Properties props = FileUtil.loadProps();
			if (props == null) return;
			
			String filePath = props.getProperty("FILE_PATH");
			String entityName = "loop";
			int k1 = 20;
			int k2 = 1;
			if (args.length > 0) {
				entityName = args[0];
				k1 = Integer.parseInt(args[1]);
				k2 = Integer.parseInt(args[2]);
			}
			String weightedLinesFile = entityName + "-lines-weighted.txt";
			String unigramPatternTFFile = entityName + "-NormalizedTF.txt";
			String ngramPatternTFFile = entityName + "-Long-Normalized.txt";
			String outputFile = entityName + "-lines-weighted-ngram.txt";

			Map<String,Integer> ngramEntityTF = FileUtil.getMapFromFile(ngramPatternTFFile);
			
			Map<String,Integer> unigramEntityTF = FileUtil.getMapFromFile(unigramPatternTFFile);
			Map<String,Integer> code = getWeightedCode(weightedLinesFile, unigramEntityTF, ngramPatternTFFile, k1, k2);
			code = FileUtil.sortByValues(code);
			FileUtil.writeMapToFile(code, outputFile, 0); 
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static Map<String, Integer> getWeightedCode(
			String weightedLinesFile, Map<String, Integer> unigramEntityTF, String ngramPatternTFFile, int k1, int k2) throws IOException {
		Map<String,Integer> code = new HashMap<String, Integer>();
		Map<String,Integer> weightedLines = FileUtil.getMapFromFile(weightedLinesFile);
		Map<String,Integer> ngramPatterns = FileUtil.getLastNIntMapFromFile(ngramPatternTFFile, 200);
		for(String line: weightedLines.keySet()) {
			int finalScore = score(line, unigramEntityTF, ngramPatterns, k1, k2);
			if (finalScore > 0)
				code.put(line, finalScore);
		}
		return code;
	}

	public static int score(String line, Map<String, Integer> unigramEntityTF, Map<String, Integer> ngramPatterns, int k1, int k2) throws IOException {
		//List<String> tokens = CodeFragmentInspector.tokenizeAsList(line);
		int uniScore = 0;
		int matchedTokens = 0;
		int finalScore = 0;
		//System.out.println(line);
		String lineItem = line.toLowerCase();
	    lineItem = lineItem.trim();
	    lineItem = StringUtil.cleanCode(lineItem);
	   
	    if (!CodeFragmentInspector.isTagWorthyCode(lineItem)) return 0;
	   
	   List<String> tokens = CodeFragmentInspector.tokenizeAsList(lineItem);
	   
	   String newLineItem = "";
	   
	   
		for(String token: tokens) {
			if (unigramEntityTF.containsKey(token)) {
				if (unigramEntityTF.get(token) > 0) {
					 newLineItem = newLineItem + token + " ";
					uniScore = uniScore + unigramEntityTF.get(token);
					matchedTokens++;
				}
			}
		}
		newLineItem = newLineItem.trim();
		//System.out.println(newLineItem);
		//System.out.println(ngramPatterns.keySet());
		if (matchedTokens > 0) {
			int score = 0;
			int ngramMatchCount = 0;
			
		
			for(int i=6; i>0; i--) {
				Set<String> ngrams = NGramBuilder.getSequentialNgramsAnyN(newLineItem, i);
				for(String ngram: ngrams) {
					if (ngramPatterns.containsKey(ngram)) {
						
						score = score + ngramPatterns.get(ngram); ///(7-i)
						ngramMatchCount++;
					}
				}				
			}
			//System.out.println(ngramMatchCount);
			if (ngramMatchCount > 0)
				score = score / ngramMatchCount;
			
			finalScore = (int) ( k1 * score) + k2 * uniScore/matchedTokens;
			
			finalScore = (finalScore / (k1 + k2));
			
			//System.out.println(uniScore + " " + ngramMatchCount + " " + finalScore);
			if ((uniScore == 0)||(ngramMatchCount == 0)) finalScore = 0;
		}
		//System.out.println(finalScore);
		return finalScore;
	}

}
