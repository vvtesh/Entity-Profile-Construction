package in.ac.iiitd.pag.janne;

import in.ac.iiitd.pag.util.ConfigUtil;
import in.ac.iiitd.pag.util.FileUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;

public class EntityTagger {
	public static void main(String[] args) {
		try {
			Properties props = FileUtil.loadProps();
			if (props == null) return;			
			int speedUp = 20;
			String filePath = props.getProperty("FILE_PATH");
			List<String> entities = FileUtil.readFromFileAsList("knownEntities.txt");
			Map<String, Float> collectionTF = FileUtil.getLastNFloatMapFromFile("allCodeTF1.csv", 100);
			collectionTF = normalize(collectionTF);
			FileUtil.writeMapToFileFloat(collectionTF, "normalizedAllCodeTF.csv", 0);
			Map<String, Set<String>> skipEntities = getSkipLists("skipList.txt");
			for(String entityName: entities) {
				System.out.println("Processing " + entityName);
				Set<String> skipEntitySet = skipEntities.get(entityName);
				Map<String,Integer> weightedCode = computeTermWeights(filePath, entityName, collectionTF, skipEntitySet, speedUp);
				FileUtil.writeMapToFile(weightedCode, "ranked-" + entityName + ".txt", 0); 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Map<String, Float> normalize(Map<String, Float> collectionTF) {
		float max = 0f;
		for(String item: collectionTF.keySet()) {
			if (collectionTF.get(item) > max) max = collectionTF.get(item);
		}
		Map<String, Float> normalized = new HashMap<String, Float>();
		for(String item: collectionTF.keySet()) {
			normalized.put(item, collectionTF.get(item) * 1f/max);
		}
		//normalized = FileUtil.sortByFloatValues(normalized);
		return normalized;
	}

	public static Map<String, Set<String>> getSkipLists(String skipFile) {
		List<String> entities = FileUtil.readFromFileAsList(skipFile);		
		Map<String, Set<String>> skipEntities = new HashMap<String, Set<String>>();		
		for(String entity: entities) {
			String[] entityInfo = entity.split(":=");
			if (entityInfo.length < 2) continue;
			String[] skips = entityInfo[1].split(",");
			Set<String> entitySkipSet = new HashSet<String>();
			for(String skip: skips) {
				entitySkipSet.add(skip);
			}
			skipEntities.put(entity, entitySkipSet);
		}
		return skipEntities;
	}

	public static Map<String, Integer> computeTermWeights(String filePath,
			String entityName, Map<String, Float> collectionTF, Set<String> skipEntities, int speedUp) throws FactoryConfigurationError, IOException {
		Set<Integer> ids = LineRanker.extract(filePath, entityName, skipEntities);		
		FileUtil.writeSetToFile(ids, entityName + "-relevant-post-ids.txt");
		List<String> code = LineBasedCodeExtractor.getAllCode(ids, filePath, speedUp); //Collect unique tokens per codeset (not per line).
		FileUtil.writeListToFile(code, entityName + "-UniqueTokensInCodeSet.txt"); 
		Map<String, Integer> tf = TFCalculator.construct(entityName + "-UniqueTokensInCodeSet.txt");
		FileUtil.writeMapToFile(tf, entityName + "tf.txt", 0);
		Set<Integer> idsRead = FileUtil.readFromFileAsSet(ConfigUtil.getInputStream(entityName + "-relevant-post-ids.txt"));		
		Map<String, Float> entityTF = FileUtil.getLastNFloatMapFromFile( entityName + "tf.txt",25);
		entityTF = TFNormalizer.normalizeWeights(collectionTF, entityTF, 0);		
		FileUtil.writeMapToFileFloat(entityTF, entityName + "-entityTFNormalized.txt", 0);		
				
		Map<String,Integer> weightedCode = LineRanker.getAllCode(idsRead, filePath, entityTF); //what if we do not normalize?
		weightedCode = FileUtil.sortByValues(weightedCode);		
		return weightedCode;
	}
}
