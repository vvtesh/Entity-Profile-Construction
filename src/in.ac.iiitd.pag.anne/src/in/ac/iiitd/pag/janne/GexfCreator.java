package in.ac.iiitd.pag.janne;

import in.ac.iiitd.pag.util.FileUtil;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GexfCreator {
	public static void main(String[] args) {
		String entityName = "call";
		String gexf = buildGEXF(entityName, "ranked-" + entityName + ".txt");
		try {
			FileUtil.saveFile("gexf-" + entityName + ".gexf", gexf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String buildGEXF(String entityName, String fileName) {		
		Map<String,Integer> codeWithFreq = FileUtil.getLastNIntMapFromFile(fileName, 500);	
		codeWithFreq = cleanCode(codeWithFreq);
		Set<String> tokens = getTokensFromMap(codeWithFreq);
		Map<String, Integer> tokenIDMap = getTokenIDMap(tokens);
		String header = "<graph defaultedgetype=\"directed\">\n";
		String nodesSection = getNodesSection(tokenIDMap);
		String edgesSection = getEdgesSection(codeWithFreq, tokenIDMap);
		String footer = "</graph>";
		String gexf = header + nodesSection + edgesSection + footer;
		return gexf;
	}

	private static Map<String, Integer> cleanCode(
			Map<String, Integer> codeWithFreq) {
		Map<String, Integer> cleanCodeWithFreq = new HashMap<String, Integer>();
		for(String code: codeWithFreq.keySet()) {
			String cleanCode = code.replace("&", "&amp;");
			cleanCode = cleanCode.replace("<", "&lt;");
			cleanCode = cleanCode.replace(">", "&gt;");
			cleanCodeWithFreq.put(cleanCode, codeWithFreq.get(code));
		}
		return cleanCodeWithFreq;
	}

	private static String getEdgesSection(Map<String, Integer> codeWithFreq, Map<String, Integer> tokenIDMap) {
		String edges = "\t<edges>\n";
		Map<Integer, Integer> nodeParticipation = new HashMap<Integer, Integer>();
		Map<String, Integer> edgesMap = new HashMap<String, Integer>();
		for(String line: codeWithFreq.keySet()) {			
			String code = line;
			int countLine = codeWithFreq.get(code);
			String[] codeTokens = code.split(" ");
			for(int i=0; i<codeTokens.length-1; i++) {
				int nodeIdFrom = tokenIDMap.get(codeTokens[i]);
				int nodeIdTo = tokenIDMap.get(codeTokens[i+1]);
				int count = 0;
				if (edgesMap.get(nodeIdFrom + "-"  + nodeIdTo) != null)
					count = edgesMap.get(nodeIdFrom + "-"  + nodeIdTo);
				count+=countLine;
				edgesMap.put(nodeIdFrom + "-"  + nodeIdTo, count);
				
				//add source
				int nodePCount = 0;
				if (nodeParticipation.containsKey(nodeIdFrom)) {
					nodePCount = nodeParticipation.get(nodeIdFrom);
				}
				nodePCount++;
				nodeParticipation.put(nodeIdFrom, nodePCount);
				//add target
				nodePCount = 0;
				if (nodeParticipation.containsKey(nodeIdTo)) {
					nodePCount = nodeParticipation.get(nodeIdTo);
				}
				nodePCount++;
				nodeParticipation.put(nodeIdTo, nodePCount);
			}
		}
				
		int cutOff = findCutoff(nodeParticipation);
		System.out.println(cutOff);
		
		int edgeId = 0;
		for(String edge: edgesMap.keySet()) {
			String[] edgeInfo = edge.split("-");
			String from = edgeInfo[0];
			String to = edgeInfo[1];
			
			if ((nodeParticipation.get(Integer.parseInt(from)) < cutOff) || (nodeParticipation.get(Integer.parseInt(to)) < cutOff)) continue;
			int count = edgesMap.get(edge);
			edgeId++;
			String line = MessageFormat.format("\t\t<edge id=\"{0}\" source=\"{1}\" target=\"{2}\" weight=\"{3}\" />\n", edgeId, from, to, count);
			edges = edges + line;
			System.out.println(line);
		}
		edges = edges + "\t</edges>\n";
		return edges;
	}

	private static int findCutoff(Map<Integer, Integer> nodeParticipation) {
		int cutoff = 0;
		nodeParticipation = FileUtil.sortIntsByValues(nodeParticipation);
		int count = nodeParticipation.size();
		int itemIndex = (count * 80)/100;
		cutoff = nodeParticipation.values().toArray(new Integer[count])[itemIndex];
		return cutoff;
	}

	private static String getNodesSection(Map<String, Integer> tokenIDMap) {
		String nodes = "\t<nodes>\n";
		for(String token: tokenIDMap.keySet()) {			
			String nodeLine = MessageFormat.format("\t\t<node id=\"{1}\" label=\"{0}\" />\n", token, tokenIDMap.get(token));
			nodes = nodes + nodeLine;
		}
		nodes = nodes + "\t</nodes>\n";
		return nodes;
	}

	private static Map<String, Integer> getTokenIDMap(Set<String> tokens) {
		Map<String, Integer> tokenIDMap = new HashMap<String,Integer>();
		int i=0;
		for(String token: tokens) {
			i++;
			tokenIDMap.put(token, i);
		}
		return tokenIDMap;
	}

	private static Set<String> getTokensFromMap(
			Map<String, Integer> codeWithFreq) {
		Set<String> tokens = new HashSet<String>();
		for(String line: codeWithFreq.keySet()) {			
			if (line.trim().length() == 0) continue;
			String[] codeTokens = line.trim().split(" ");
			for(String codeToken: codeTokens) {
				tokens.add(codeToken.trim());
			}
		}
		return tokens;
	}
}
