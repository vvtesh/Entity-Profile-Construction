package in.ac.iiitd.pag.anne.patterns;

import in.ac.iiitd.pag.util.CodeFragmentInspector;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.NGramBuilder;
import in.ac.iiitd.pag.util.StringUtil;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class PatternExamplePrinter {
	public static void main(String[] args) {
		try {
			print(1, "for");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void print(int n, String pattern) throws IOException {
		int count = 100;
		String filePath = "C:\\data\\svn\\iiitdsvn\\entity\\data\\ossprojects\\hadoop-methods.txt";
		List<String> lines = FileUtil.readFromFileAsList(filePath);
		for(String line: lines) {
			if (count == 0) break;
			String newLine = line.toLowerCase().trim();
			newLine = StringUtil.cleanCode(newLine);
			
			   if (newLine.startsWith("//")) continue;			   
			   if (newLine.startsWith("import ")) continue;
			   if (newLine.startsWith("@")) continue;
			   if (newLine.startsWith("public")) continue;
			   if (newLine.startsWith("private")) continue;
			   if (newLine.startsWith("protected")) continue;
			   if (newLine.startsWith("class")) continue;
			   
			if (newLine.length() < 4) continue;
			List<String> tokens = CodeFragmentInspector.tokenizeAsList(newLine);
			if (tokens.size() < 3) continue; 
			String processedLine = StringUtil.getAsStringFromList(tokens);
			if (tokens.size() > 15) continue;
			Set<String> ngrams = NGramBuilder.getSequentialNgramsAnyN(processedLine, n);
			if (newLine.contains("for (")) continue;
			if (newLine.startsWith("* ")) continue;
			for(String ngram: ngrams) {				
				if (pattern.equalsIgnoreCase(ngram)) {
					System.out.println(line);					
					count--;
					break;
				}
			}
		}
	}
}
