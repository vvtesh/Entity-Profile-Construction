package in.ac.iiitd.pag.anne.entity;

import in.ac.iiitd.pag.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import snowballstemmer.EnglishStemmer;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;

public class DictionaryWordsExtractor {
	public static void main(String[] args) {
		Properties props = FileUtil.loadProps();
		String inputFile = props.getProperty("FINAL_OUTPUT_FILE");
		String dictFIle = props.getProperty("DICT_FILES");
		String outputFile = props.getProperty("DICT_OUTPUT_FILE");
		
		URL url = null;
		IDictionary dict = null;
		try {
			url = new URL("file", null , dictFIle) ;
			dict = new Dictionary(url) ;
			dict.open() ;
			estimate(inputFile, outputFile, dict);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

	private static void estimate(String inputFile, String outputFile, IDictionary dict) throws IOException {
		List<String> dictWords = new ArrayList<String>();
		Set<String> stemsColl = new HashSet<String>();
		
		List<String> input = FileUtil.readFromFileAsList(inputFile);
		EnglishStemmer stemmer = new EnglishStemmer();
		for(String item: input) {
			String term = item.split(",")[0];
			int count = Integer.parseInt(item.split(",")[1]);
			if (count < 10) continue;
			IIndexWord idxWord = dict.getIndexWord(term, POS.NOUN);
			if (idxWord == null) {
				idxWord = dict.getIndexWord(term, POS.ADJECTIVE);
			}
			if (idxWord == null) {
				idxWord = dict.getIndexWord(term, POS.ADVERB);
			}
			if (idxWord == null) {
				idxWord = dict.getIndexWord(term, POS.VERB);
			}
			if (idxWord != null) {
				dictWords.add(term);				
			}
		}
		
		Map<String,String> afterStemming = new HashMap<String,String>();
		for(String item: dictWords) {
			stemmer.setCurrent(item);
			if(stemmer.stem())
			{
				String stemValue = stemmer.getCurrent();
				String words = "";
				if (afterStemming.containsKey(stemValue)) {
					words = afterStemming.get(stemValue) + ",";
				}
				words = words + item;
				afterStemming.put(stemValue, words);				
			}
		}
		
		FileUtil.writeStrMapToFile(afterStemming, outputFile);
	}
}
