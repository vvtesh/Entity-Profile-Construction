package in.ac.iiitd.pag.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class ConfigUtil {

	static boolean isWebContainer = false;
	static List<String> stops = null;
	
	public static void loadStops() {
		stops = FileUtil.readFromFileAsList("c:\\temp\\javastops.txt");
	}
	
	public static List<String> getStops() {
		if (stops == null) {
			loadStops();
		}
		return stops;
	}
	
	public static InputStream getInputStream(String filename) {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return inputStream;
	}
}
