package in.ac.iiitd.pag.anne.brown;

import java.io.File;
import java.io.IOException;

import in.ac.iiitd.pag.util.FileUtil;

public class Preprocessor {
	public static void main(String[] args) {
		try {
			cleanCorpus("c:\\tools\\brown\\brown.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void cleanCorpus(String filePath) throws IOException {
		String brown = FileUtil.readFromFile(filePath);
		brown = brown.replaceAll("[^a-zA-Z0-9\\s]", " ");
		brown = brown.toLowerCase();
		FileUtil.saveFile(new File("c:\\tools\\brown"), "brown", brown, "clean");
	}
}
