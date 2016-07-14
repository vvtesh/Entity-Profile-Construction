package in.ac.iiitd.pag.janne.tests;

import in.ac.iiitd.pag.util.CodeFragmentInspector;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.StringUtil;

import java.util.List;

public class TestTokenization {
	public static void main(String[] args) {
		try {
			 String code = FileUtil.readFromFile("snippet.java");
			 String[] lines = code.split("\r\n|\r|\n");
			 for(String lineItem: lines) {
				 List<String> tokens = CodeFragmentInspector.tokenizeAsList(lineItem);
				 System.out.println(lineItem);
			 }
		} catch (Exception e) {
			
		}
		 
	}
}
