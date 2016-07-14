package in.ac.iiitd.pag.janne;

import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.StringUtil;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CoocAnalysis {
	public static void main(String[] args) {
		
		String entityName = "loop";
		
		if (args.length == 1) {
			entityName = args[0];
		}
		
		String inputFile = entityName + "-Cooccurrence.txt";
		List<String> cooc = FileUtil.readFromFileAsList(inputFile);
		Set<String> knownPatterns = new HashSet<String>();
		for(String coocItem: cooc) {
			String[] patterns = coocItem.split(",");
			for(int i=0; i<patterns.length; i++) {	
				if (patterns[i].trim().length() == 0) continue;
				knownPatterns.add(patterns[i]);
			}
		}
		
		Map<String, Integer> longPatternIds = new HashMap<String, Integer>();
		String[] longPatternIdsArr = new String[knownPatterns.size()];
		int id = 0;
		for(String pattern: knownPatterns) {
			if (pattern.trim().length() == 0) continue;
			longPatternIds.put(pattern, id);
			longPatternIdsArr[id] = pattern;
			id++;
		}
		id = knownPatterns.size();
				
		int[][] patternsMatrix = new int[id][id];
		
		for(String coocItem: cooc) {
			String[] patterns = coocItem.split(",");
			for(int i=0; i<patterns.length; i++) {	
				if (patterns[i].trim().length() == 0) continue;
				int id1 = longPatternIds.get(patterns[i]);
				for(int j=i+1; j<patterns.length; j++) {
					if (patterns[j].trim().length() == 0) continue;
					int id2 = longPatternIds.get(patterns[j]);
					patternsMatrix[id1][id2] = patternsMatrix[id1][id2] + 1;
				}
			}
		}
		
		String outputFile = entityName + "-coocAnalysis.txt";
		String rows = "";
		String rowsFC = "";
		for(int i=0; i<id; i++) {			
			String row = padRight(longPatternIdsArr[i].replace(" ", ""), 20) + " ";
			String rowFC = "";
			int rowCount = 0;
			for (int j=0; j<id; j++) {
				int temp = patternsMatrix[i][j] + patternsMatrix[j][i];
				//if (temp == 0) temp = 1;
				if (i == j) temp = 0;
				rowCount = rowCount + temp;
				row = row + padRight(temp + "", 5) + " ";
				rowFC = rowFC + temp + ",";
			}
			if (rowCount == 0) {
				System.out.println(longPatternIdsArr[i]);
			}
			row = row.trim() + "\n";
			rows = rows + row;
			if (rowFC.endsWith(",")) rowFC = rowFC.substring(0, rowFC.length()-1);
			rowsFC = rowsFC + rowFC + "\n";
		}
		
		double[][] corrMatrix = new double[id][id];
		for(int i=0; i<id-1; i++) {	
			for (int j=i+1; j<id; j++) {
				
				double cor = PearsonCorrelation.getPearsonCorrelation(patternsMatrix[i], patternsMatrix[j]);
				if (Double.isNaN(cor)) cor = 0;
				corrMatrix[i][j] = cor;
				corrMatrix[j][i] = cor;
			}
		}
		
		try {
			FileUtil.saveFile(outputFile, rows);
			FileUtil.saveFile("cooc.txt", rowsFC);
			FileUtil.saveFile("pearson.txt", getString(corrMatrix, id, longPatternIdsArr));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static String getString(double[][] corrMatrix, int n, String[] longPatternIdsArr) {
		
		String pattern = "##.##";
		DecimalFormat decimalFormat = new DecimalFormat(pattern);
		String rows = padRight(" ", 20);	
		for(int i=0; i<n; i++) {
			rows = rows + padRight(longPatternIdsArr[i].replace(" ", ""), 20);
		}
		rows = rows + "\n";
		for(int i=0; i<n; i++) {	
			String row = padRight(longPatternIdsArr[i].replace(" ", ""), 20);
			for (int j=0; j<n; j++) {
				double temp = corrMatrix[i][j];
				if (i == j) temp = 1;					
				row = row + padRight(decimalFormat.format(temp), 20);
			}			
			row = row.trim() + "\n";
			rows = rows + row;
		}
		return rows;
	}
	
	private static String getCSVString(double[][] corrMatrix, int n, String[] longPatternIdsArr) {
		
		String pattern = "##.##";
		DecimalFormat decimalFormat = new DecimalFormat(pattern);
		String rows = "";
		/*for(int i=0; i<n; i++) {
			rows = rows + longPatternIdsArr[i] + ",";
		}
		rows = rows.substring(0, rows.length() - 1) + "\n";*/
		for(int i=0; i<n; i++) {	
			String row = ""; //longPatternIdsArr[i] + ",";
			for (int j=0; j<n; j++) {
				double temp = corrMatrix[i][j];
				if (i == j) temp = 1;	
				if ((temp > 1) || (temp < -1)||(Double.isNaN(temp))) {
					System.out.println(temp);
					row = "";
					break;
				}								
				row = row + decimalFormat.format(temp)  + ",";
			}			
			if (row.length() > 1) { 
				row = row.substring(0, row.length() - 1)  + "\n";
				rows = rows + row;
			}			
		}
		return rows;
	}

	/*public static String padRight(String s, int n) {
	     //return String.format("%1$-" + n + "s", s);  
		return String.format("%-" + n + "s", s);
	}*/
	
	public static String padRight(String str, int size)
	{
	  StringBuffer padded = new StringBuffer(str);
	  while (padded.length() < size)
	  {
	    padded.append(' ');
	  }
	  return padded.toString();
	}
}
