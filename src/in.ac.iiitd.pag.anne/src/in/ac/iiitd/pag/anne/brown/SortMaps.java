package in.ac.iiitd.pag.anne.brown;

import in.ac.iiitd.pag.util.FileUtil;

import java.io.IOException;
import java.util.Map;

public class SortMaps {
	public static void main(String[] args) {
		Map<String,Float> map = FileUtil.getFloatMapFromFile("C:\\tools\\brown\\javaTitlesLM.csv");
		map = FileUtil.sortByFloatValues(map);
		try {
			FileUtil.writeMapToFileFloat(map, "C:\\tools\\brown\\javaTitlesLMSorted.csv", 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
