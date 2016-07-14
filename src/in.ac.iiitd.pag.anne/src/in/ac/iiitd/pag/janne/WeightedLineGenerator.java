package in.ac.iiitd.pag.janne;

import in.ac.iiitd.pag.util.ConfigUtil;
import in.ac.iiitd.pag.util.FileUtil;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class WeightedLineGenerator {
	public static void main(String[] args) {
		try {
			Properties props = FileUtil.loadProps();
			if (props == null) return;						
			String filePath = props.getProperty("FILE_PATH");
			
			//String entityName = "loop";
			int lastN = 0;
			if (args.length == 1) {
				//entityName = args[0];
				lastN = Integer.parseInt(args[0]);
			}
			List<String> entities = FileUtil.readFromFileAsList("knownEntities.txt");
			
			for(String entityName: entities) {
				String entityTFFile = entityName + "-NormalizedTF.txt";
				String outputFile = entityName + "-WeightedLines.txt";
				String idsFile = entityName + "-relevant-post-ids.txt";
				
				Set<Integer> idsRead = FileUtil.readFromFileAsSet(ConfigUtil.getInputStream(idsFile));
				Map<String, Float> entityTF = FileUtil.getFloatMapFromFile(entityTFFile);
				Map<String,Integer> weightedCode = LineRanker.getAllCode(idsRead, filePath, entityTF); //what if we do not normalize?
				weightedCode = FileUtil.sortByValues(weightedCode);	
				FileUtil.writeMapToFile(weightedCode, outputFile, 0);
				weightedCode = FileUtil.getLastNIntMapFromFile(outputFile, lastN);
				FileUtil.writeMapToFile(weightedCode, outputFile, 0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
