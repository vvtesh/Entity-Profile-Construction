package in.ac.iiitd.pag.janne.tests;

import java.util.List;

import in.ac.iiitd.pag.util.CodeFragmentInspector;
import in.ac.iiitd.pag.util.FileUtil;

public class TestIsJava {
	public static void main(String[] args) {
		List<String> codeLines = FileUtil.readFromFileAsList("snippet.java");
		for(String line: codeLines) {
			if (CodeFragmentInspector.isJavaH(line)) {
				System.out.println(line);
			}
		}
	}
}
