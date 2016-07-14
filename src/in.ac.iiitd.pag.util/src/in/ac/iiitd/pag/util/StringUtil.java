package in.ac.iiitd.pag.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StringUtil {

	/**
	 * Convert a string array to a string with csv of its content.
	 * 
	 * @param words
	 * @return
	 */
	public static String covert(String[] words) {
		String output = "";
		for(String word:words) output = output + word + ",";
		output = output.substring(0,output.length()-1);
		return output;
	}
	
	public static int countLines(String str){
		   String[] lines = str.split("\r\n|\r|\n");
		   return  lines.length;
	}
	
	public static String getAsCSV(Set<String> tokens) {
		String result = "";
		for(String token: tokens) {
			token = token.trim();
			if (token.length() > 0)
				result = result + token + ",";
		}
		if (result.length()  > 0) result = result.substring(0, result.length() -1);
		return result;
	}
	
	public static String getAsCSV(List<String> tokens) {
		String result = "";
		for(String token: tokens) {
			token = token.trim();
			if (token.length() > 0)
				result = result + token + ",";
		}
		if (result.length()  > 0) result = result.substring(0, result.length() -1);
		return result;
	}
	
	public static String getAsString(String[] tokens) {
		String output = "";
		for (String token: tokens) {
			output = output +  token + " "; 
		}
		return output;
	}
	
	public static String getAsStringFromList(List<String> tokens) {
		String output = "";
		for (String token: tokens) {
			output = output +  token + " "; 
		}
		return output;
	}
	
	public static Set<String> getTokens(String data) {
		Set<String> set = new HashSet<String>();
		String[] items = data.split(" ");
		for(String item: items) {
			item = item.toLowerCase();
			item = item.replace(",","");
			if (item.matches("[a-z0-9]+") && item.length()>3) {
				if (!set.contains(item))
					set.add(item);				
			}
		}
		return set;
				
	}
	
	public static Map<String,Integer> getTokenFreq(String data) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		String[] items = data.split(" ");
		for(String item: items) {
			item = item.toLowerCase();
			item = item.replace(",","");
			if (item.matches("[a-z0-9]+") && item.length()>3) {
				int count = 0;
				if (map.containsKey(item)) {
					count = map.get(item);
				}
				count++;
				map.put(item, count);
			}
		}
		return map;
				
	}
	
	public static String cleanCode(String code) {
		String codeFragment = code;
		codeFragment = codeFragment.replace("code", "");
		codeFragment = codeFragment.replace("&gt;", ">");
		codeFragment = codeFragment.replace("&lt;", "<");
		codeFragment = codeFragment.replace("&amp;", "&");
		if (codeFragment.contains("import ")) {
			int start = 0;
			int end = 0;
			while (true) {
				start = codeFragment.indexOf("import ", end);
				end = codeFragment.indexOf(";", start);
				int newline = codeFragment.indexOf("\n", start);
				if ((newline > 0)&&(newline < end)) end = newline;
				if ((start == -1) || (end == -1)) break;				
				codeFragment = codeFragment.replace(codeFragment.substring(start, end+1), "");				
			}
		}
		return codeFragment;
	}
	
	public static List<Integer> covertToIntList(List<String> lst) {
		List<Integer> result = new ArrayList<Integer>();
		System.out.println(lst.get(lst.size()-1) + "***");
		for(int i=0;i<lst.size();i++) {			
				result.add(Integer.parseInt(lst.get(i)));
		}	
		return result;
	}
	
	public static String processWord(String x) {
		x = x.replaceAll("[@&,.;:!?(){}\\[\\]/=#<>%]", " ");
		x = x.replaceAll("\r", " ");
		x = x.replaceAll("\n", " ");
		x = x.replaceAll("\"", " ");
		x = x.replaceAll("\'", " ");
		x = x.replaceAll("_", " ");
		x = x.replaceAll("-", "");
	    return x;
	}
	
	public static boolean isOnlyAlphabets(String x) {
		boolean isAlpha = false;
		if (x.length() < 4) return false;
		if (x.matches("[a-zA-Z]+")) isAlpha = true;		
		return isAlpha;
	}

	public static Set<String> getWindow(String text, String term, int windowSize) {
		
		text = processWord(text).toLowerCase();
		term = term.toLowerCase();		
		String compressedTerm = "";
		if (term.contains(" ")) {			
			compressedTerm = processWord(term).toLowerCase();
			compressedTerm = term.replaceAll(" ", "");
			text = text.replaceAll(term, compressedTerm);
			term = compressedTerm;
		}
		
		Set<String> window = new HashSet<String>();
		Set<String> terms = getTokens(text);
		int start = 0;
		int end = 0;
		if (terms.contains(term)) {
			while(true) {
				//get all windows
				start = text.indexOf(term, end);
				end = term.length() + start;
				
				if ((start <0)||(end<0)) {
					break;
				}
				
				int itemCount = 0;
				for(int i=start-1; i>0;i--) {
					if (text.charAt(i) == ' ') {
						String itemFound = text.substring(i, start);
						if (itemFound.trim().length() > 2) {
							window.add(itemFound);
							itemCount++;
							if (itemCount == 3) break;
							start = i;
						} else {
							start = i;
						}
					}
				}				
			}
			
			start = 0;
			end = 0;
			while(true) {
				int itemCount = 0;
				start = text.indexOf(term, end);
				end = term.length() + start;
				
				if ((start <0)||(end<0)) {
					break;
				}
				
				for(int i=end+1; i<text.length();i++) {
					if (text.charAt(i) == ' ') {
						String itemFound = text.substring(end, i);
						if (itemFound.trim().length() > 2) {
							window.add(itemFound);
							itemCount++;
							if (itemCount == 3) break;
							end = i;
						} else {
							end = i;
						}
					}
				}
			}
		}
		return window;
	}
	
	public static String sort(String s) {			
		String[] s1= s.split("");
		Arrays.sort(s1);
		String sorted = "";
		for (int i = 0; i < s1.length; i++)
		{
		  sorted += s1[i];
		}
		return sorted;
	}
}
