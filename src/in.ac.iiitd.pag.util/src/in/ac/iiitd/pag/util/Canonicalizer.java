package in.ac.iiitd.pag.util;

import java.util.List;
import java.util.Properties;

public class Canonicalizer {
		
	public static void main(String[] args) {
		String element = "loop*=";
		Properties props = FileUtil.loadProps();
		if (props == null) return;
		String operatorsFile = props.getProperty("CANONICALIZED_OPERATORS_FILE"); 
		List<String> operators = FileUtil.readFromFileAsList(operatorsFile);
		System.out.println(canonicalize(element, operators));		
	}
	
	public static String canonicalize(String algo, List<String> operators) {
		String returnVal = "";
		String[] elements = algo.split(" ");
		for(String element: elements) {			
			returnVal = returnVal + " " + modifyElement(element, operators);
		}
		return returnVal.trim();
	}

	private static String modifyElement(String element, List<String> operators) {
		
		String modifiedElement = element;
		for(String operator: operators) {
			if (element.contains(operator)) {
				modifiedElement = element.replace(operator, operator.replace("=", ""));				
			}
		}
		return modifiedElement;
	}
}
