package in.ac.iiitd.pag.janne;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import in.ac.iiitd.pag.util.CodeFragmentInspector;
import in.ac.iiitd.pag.util.ConfigUtil;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.StringUtil;

public class EntityTaggerUI {
	public static void main(String[] args) {
		String code = FileUtil.readFromFile("snippet.java");
		//System.out.println(code);
		Map<String, Map<String, Float>> allWeights = new HashMap<String, Map<String, Float>>();
		List<String> entityNames = FileUtil.readFromFileAsList(ConfigUtil.getInputStream("knownEntities.txt"));
		
		for(String entityName: entityNames) {
			Map<String, Float> weights = FileUtil.getLastNFloatMapFromFile("weights-" + entityName + ".csv", 500);
			allWeights.put(entityName, weights);
		}		
		float cutoffFactor = 0.5f;
		String taggedCode = "";
		try {
			//computePRPerEntity(code, allWeights, entityNames);
			computeAveragePR(code, allWeights, entityNames);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void computeAveragePR(String code,
			Map<String, Map<String, Float>> allWeights, List<String> entityNames)
			throws IOException {
		String taggedCode;
						
		for(int factor =0; factor < 11; factor++) {					
			System.out.print(factor/10f + " ");
			taggedCode = tag(code, allWeights, factor*1.0f/10);			
			FileUtil.saveFile("systemAnnotations.txt", taggedCode);
			List<String> systemAnnotations = FileUtil.readFromFileAsList("systemAnnotations.txt");
			Oracle.computePR(systemAnnotations, entityNames, "snippet - annotated.java");	
		}
		
	}
	
	private static void computePRPerEntity(String code,
			Map<String, Map<String, Float>> allWeights, List<String> entityNames)
			throws IOException {
		String taggedCode;
		for(String entityName: entityNames) {
			List<String> justOneEntity = new ArrayList<String>();
			justOneEntity.add(entityName);
			allWeights.clear();
			Map<String, Float> weights = FileUtil.getLastNFloatMapFromFile("weights-" + entityName + ".csv", 400);
			allWeights.put(entityName, weights);
			System.out.println(entityName);
			for(int factor =0; factor < 11; factor++) {					
				System.out.print(factor/10f + " ");
				taggedCode = tag(code, allWeights, factor*1.0f/10);			
				FileUtil.saveFile("systemAnnotations.txt", taggedCode);
				List<String> systemAnnotations = FileUtil.readFromFileAsList("systemAnnotations.txt");
				Oracle.computePR(systemAnnotations, justOneEntity, "snippet - annotated.java");	
			}
		}
	}

	private static int getCutOff(Map<String, Float> weights, float cutoffFactor) {
		float maxWt = 0;
		float minWt = 0;
		for(String entity: weights.keySet()) {
			if (weights.get(entity) > maxWt) maxWt = weights.get(entity);
			if (weights.get(entity) < minWt) minWt = weights.get(entity);
		}		
		return (int) ((maxWt - minWt)/2 * cutoffFactor);
	}

	public static String tag(String code,Map<String, Map<String, Float>>  allWeights, float cutoffFactor) throws IOException {
		String taggedCode = "";
		String[] lines = code.split("\r\n|\r|\n");
		String[] tagsForLines = new String[lines.length];
		Map<String,Set<String>> tagAssociations = new HashMap<String, Set<String>>();
		float maxLineWeight = 0;
		int index = 0;
		for(String lineItem: lines) {
			   //tagAssociations = new HashMap<String, List<String>>();
			   String preservedLine = lineItem;
			   lineItem = lineItem.toLowerCase();
			   lineItem = lineItem.trim();
			   lineItem = StringUtil.cleanCode(lineItem);
			   //System.out.println(lineItem);
			   if (inValid(lineItem)) {
				   taggedCode = taggedCode + lineItem + "\n";
				   continue;
			   }
			   
			   for(String entityName: allWeights.keySet()) {
				   Map<String, Float> weights = allWeights.get(entityName);		
				   int cutoffWeight = getCutOff(weights, cutoffFactor);
				   List<String> tokens = CodeFragmentInspector.tokenizeAsList(lineItem);
				   
				   String newLine = "";
				   float lineWeight = 0;
				   int docLen = 0;
				   for(String token: tokens) {
					   if (token.contains(".")) {
						   String[] subTokens = token.split("\\.");
						   for(String subToken: subTokens) {
							   newLine = newLine + subToken + " ";
							   if (weights.containsKey(subToken)) {
								   docLen++;
								   newLine = newLine   + " "; //+  +  "(" + weights.get(subToken) + ")"
								   lineWeight = lineWeight + weights.get(subToken);							   
							   }
						   }
					   } else {
						   newLine = newLine + token + " ";
						   if (weights.containsKey(token)) {
							   newLine = newLine  + " "; //+ "(" + weights.get(token) + ")"
							   lineWeight = lineWeight + weights.get(token);
							   docLen++;
						   }
					   }
				   }		       
				   if (lineWeight/docLen > cutoffWeight) {					   
					   Set<String> entityNamesAssociated = null;
					   if (tagAssociations.containsKey(preservedLine)) {
						   entityNamesAssociated = tagAssociations.get(preservedLine);
					   } else {
						   entityNamesAssociated = new HashSet<String>();						   
					   }
					   entityNamesAssociated.add(entityName);
					   tagAssociations.put(preservedLine, entityNamesAssociated);					   
				   } 
			   }
		   }                	
		
		taggedCode = getCode(code, tagAssociations);
		//if (tagAssociations.keySet().size() == 0) taggedCode = "";
		return taggedCode;
	}

	private static String getCode(String code, Map<String, Set<String>> tagAssociations) {
		String taggedCode = "";
		String[] lines = code.split("\r\n|\r|\n");
		for (String line: lines) {			
			String tags = "";
			if ((line.trim().length() > 0)&&(tagAssociations.get(line) != null)&&(tagAssociations.get(line).size() > 0)) {
				tags = StringUtil.getAsCSV(tagAssociations.get(line)).replace(",", " ");
				taggedCode = taggedCode + line +  " \t:= " + tags + "\n"; //"(" + wt + ")" +
			} else {
				taggedCode = taggedCode + line + "\n";
			}				
		}
		return taggedCode;
	}

	private static boolean inValid(String lineItem) {
		boolean invalid = false;
		if (lineItem.startsWith("//")) invalid = true;
		   if (lineItem.length() <=2 ) invalid = true;
		   if (lineItem.startsWith("import ")) invalid = true;
		   if (lineItem.startsWith("public")) invalid = true;
		   if (lineItem.startsWith("private")) invalid = true;
		   if (lineItem.startsWith("protected")) invalid = true;
		   if (lineItem.startsWith("class")) invalid = true;
		return invalid;
	}
}
