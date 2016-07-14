package in.ac.iiitd.pag.util;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class NGramBuilder {
	
	static Set<String> phrases = new HashSet<String>();
	static String[] words = null;
	static int loopCount = 0;
	
	public static void main(String[] args) {
		String test = "stage method flowpane flowpane method scene scene method method method method method method method method eventhandler actionevent actionevent + method method / method method / method method eventhandler actionevent";
		Set<String> ngrams = getSequentialNgramsAnyN(test,7); //getSequentialNgrams(test,2);
		for(String ngram: ngrams) {
			System.out.println(ngram);
		}
	}

	public static Set<String> getSequentialNgramsAnyN(String input, int n) {
		if (n < 1) {
			System.out.println("n cannot be less than 1. Exiting.");
			phrases.clear();
			return phrases;
		}
		words = input.split(" ");
		if (words.length > 30) {
			String[] shortnenedWords = new String[30];
			for(int i=0;i<30;i++) {
				shortnenedWords[30-i-1] = words[words.length - i -1];
			}
			words = shortnenedWords;
			/*System.out.println("method too big. skipping");
			phrases.clear();
			return phrases;*/
		}
		
		phrases.clear();
		addPhrases("", 0, n-1);	
		/*if (phrases.size() > 500) {
			System.out.println(input);
			System.out.println(phrases.size());
			System.out.println(n);
		}*/
		return phrases;
	}
	
	public static Set<String> getSequentialNgrams(String input, int n) {
		if (n < 3) {
			System.out.println("n cannot be less than 3. Exiting.");
			phrases.clear();
			return phrases;
		}
		words = input.split(" ");
		if (words.length > 30) {
			String[] shortnenedWords = new String[30];
			for(int i=0;i<30;i++) {
				shortnenedWords[30-i-1] = words[words.length - i -1];
			}
			words = shortnenedWords;
			/*System.out.println("method too big. skipping");
			phrases.clear();
			return phrases;*/
		}
		
		phrases.clear();
		addPhrases("", 0, n-1);	
		/*if (phrases.size() > 500) {
			System.out.println(input);
			System.out.println(phrases.size());
			System.out.println(n);
		}*/
		return phrases;
	}

	private static synchronized void addPhrases(String phrase, int start, int wordCount) {
		if (wordCount >= 0) {
			for(int i =start; i< words.length - wordCount; i++) {
				loopCount++;
				if (wordCount == Integer.MAX_VALUE) {
					System.out.println("problem.");
				}
				addPhrases((phrase + " " + words[i]).trim(), i+1, wordCount - 1);
			}
		} else {
			phrases.add(phrase.trim());
			loopCount = 0;
		}
		
	}
}
