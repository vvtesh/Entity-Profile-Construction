package in.ac.iiitd.pag.janne.experiment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class LineCounter {
	public static void main(String[] args) {
		String folderPath = "C:\\data\\svn\\iiitdsvn\\entity\\eval\\PV0\\2-enum-untagged-73files\\";
		File folder = new File(folderPath);
		File[] files = folder.listFiles();
		int lineCount = 0;
		for(File file: files) {
			
				try {
					lineCount += getLineCount(file);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
		}
		System.out.println(lineCount/files.length);
	}
	
	public static int getLineCount(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		int lines = 0;
		while (reader.readLine() != null) lines++;
		reader.close();
		return lines;
	}
}
