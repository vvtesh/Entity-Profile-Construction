package in.ac.iiitd.pag.anne.entity;

import in.ac.iiitd.pag.util.FileUtil;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class RandomPicks {
	public static void main(String[] args) {
		Properties props = FileUtil.loadProps();
		String inputFile = props.getProperty("DICT_OUTPUT_FILE");
		String outputFolder = "c:\\temp\\workdir";
		try {
			pickRandom(inputFile, outputFolder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void pickRandom(String inputFile, String outputFolder) throws IOException {
		List<String> lines = FileUtil.readFromFileAsList(inputFile);		
		for (int j=0; j<10; j++)  {
			Set<String> output = new HashSet<String>();
			for(int i=0;i<30;i++) {
				int rnd = (int) Math.floor(Math.random() * lines.size());
				boolean added = false;
				while(!added) {
					added = output.add(lines.get(rnd));		
					if (!added) {
						rnd = (int) Math.floor(Math.random() * lines.size());
					}
				}
			}
			FileUtil.writeListToFile(output, outputFolder + "\\rnd" + j + ".csv" );
		}
	}
}
