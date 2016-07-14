package in.ac.iiitd.pag.anne.entity;

import in.ac.iiitd.pag.util.FileUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;

public class TryWordnet {
	public static void main(String[] args) {
		Properties props = FileUtil.loadProps();
		String dictFIle = props.getProperty("DICT_FILES");
		
		URL url = null;
		IDictionary dict = null;
		try {
			url = new URL("file", null , dictFIle) ;
			dict = new Dictionary(url) ;
			dict.open() ;
			String item = "duplicate";
			WordnetStemmer stemmer = new WordnetStemmer(dict);
			
			IIndexWord idxWord = dict.getIndexWord(item, POS.NOUN);
			if (idxWord == null) {
				idxWord = dict.getIndexWord(item, POS.ADJECTIVE);
			}
			if (idxWord == null) {
				idxWord = dict.getIndexWord(item, POS.ADVERB);
			}
			if (idxWord == null) {
				idxWord = dict.getIndexWord(item, POS.VERB);
			}
			if (idxWord != null) {
				List<String> stems = stemmer.findStems(item, idxWord.getPOS());
				for(String stem: stems) {
					System.out.println(stem);
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
