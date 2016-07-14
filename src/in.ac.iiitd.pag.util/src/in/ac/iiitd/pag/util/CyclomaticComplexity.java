package in.ac.iiitd.pag.util;

import java.io.IOException;
import java.util.List;

public class CyclomaticComplexity {
	public static void main(String[] args) {
		String method = FileUtil.readFromFile("c:\\temp\\method1.txt");
		System.out.println(method);		
		int ccn = getCCN(method);
		System.out.println(ccn);
	}
	public static int getCCN(String method) {
		int ccn = 1;
		try {
			List<String> tokens = CodeFragmentInspector.tokenizeAsList(method);
			for(String token: tokens) {
				token = token.toLowerCase();
				switch(token) {
					case "return": ccn++; break;
					case "if": ccn++; break;
					case "else": ccn++; break;
					case "case": ccn++; break;
					case "default": ccn++; break;
					case "for": ccn++; break;
					case "while": ccn++; break;
					case "do": ccn++; break;
					case "break": ccn++; break;
					case "continue": ccn++; break;
					case "&&": ccn++; break;
					case "||": ccn++; break;
					case "?": ccn++; break;
					case ":": ccn++; break;
					case "catch": ccn++; break;
					case "finally": ccn++; break;
					case "throw": ccn++; break;
					case "throws": ccn++; break;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ccn;
	}
}
