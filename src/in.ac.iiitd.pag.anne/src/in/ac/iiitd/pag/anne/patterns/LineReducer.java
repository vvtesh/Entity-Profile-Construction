package in.ac.iiitd.pag.anne.patterns;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import in.ac.iiitd.pag.util.FileUtil;

public class LineReducer {
	public static void main(String[] args) {
		String inputTokensFile = "C:\\data\\svn\\iiitdsvn\\entity\\data\\ossprojects\\hadoop-line-tokens.txt";
		String inputTFFile = "C:\\data\\svn\\iiitdsvn\\entity\\data\\ossprojects\\hadoop-line-tokens-tf.txt";
		String outputFile = "C:\\data\\svn\\iiitdsvn\\entity\\data\\ossprojects\\hadoop-line-reduced.txt";
		
		reduceLines(inputTokensFile, inputTFFile, outputFile);
	}

	public static void reduceLines(String inputTokensFile, String inputTFFile,
			String outputFile) {
		try {
			FileWriter fw = new FileWriter(new File(outputFile));
			BufferedWriter bw = new BufferedWriter(fw);
		
			Set<String> keyTokens = FileUtil.getMapFromFile(inputTFFile).keySet();
			List<String> lines = FileUtil.readFromFileAsList(inputTokensFile);
			for(String line: lines) {
				String tokens[] = line.trim().split(" ");
				String newLine = "";
				for(String token: tokens) {
					if (keyTokens.contains(token)) {
						newLine = newLine + token + " ";
					}
				}
				newLine = newLine.trim() + "\n";
				bw.write(newLine);
			}
			
			bw.close();
			fw.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
