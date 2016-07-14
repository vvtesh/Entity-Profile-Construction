package in.ac.iiitd.pag.anne.patterns;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import in.ac.iiitd.pag.util.CodeFragmentInspector;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.StringUtil;

public class LineBasedTokenizer {
	static FileWriter fw;
	static BufferedWriter bw;
	public static void main(String[] args) {
		String tokenizedOutputFile = "C:\\data\\svn\\iiitdsvn\\entity\\data\\ossprojects\\hadoop-line-tokens.txt";
		String methodsFilePath = "C:\\data\\svn\\iiitdsvn\\entity\\data\\ossprojects\\hadoop-methods.txt";
		tokenize(tokenizedOutputFile, methodsFilePath);
	}

	public static void tokenize(String tokenizedOutputFile, String methodsFilePath) {
		try {
			fw = new FileWriter(new File(tokenizedOutputFile));
		    bw = new BufferedWriter(fw);
		    prepareTokenizedFile(methodsFilePath);
	        bw.close();
	        fw.close();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void prepareTokenizedFile(String methodsFilePath) throws IOException {
		
		List<String> lines = FileUtil.readFromFileAsList(methodsFilePath);
		for(String line: lines) {
			String newLine = line.toLowerCase().trim();
			newLine = StringUtil.cleanCode(newLine);
			newLine = newLine.replace(",", " ");
			   if (newLine.startsWith("//")) continue;			   
			   if (newLine.startsWith("import ")) continue;
			   if (newLine.startsWith("@")) continue;
			   if (newLine.startsWith("public")) continue;
			   if (newLine.startsWith("private")) continue;
			   if (newLine.startsWith("protected")) continue;
			   if (newLine.startsWith("class")) continue;
			   if (newLine.startsWith("*")) continue;
			   
			if (newLine.length() < 4) continue;
			List<String> tokens = CodeFragmentInspector.tokenizeAsList(newLine);
			if (tokens.size() < 3) continue; 
			if (tokens.size() > 15) continue;
			bw.write(StringUtil.getAsStringFromList(tokens) + "\n");
		}
	}
}
