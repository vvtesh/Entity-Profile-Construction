package in.ac.iiitd.pag.util;


import java.util.StringTokenizer;

public class LanguageUtil {

	public static int getJavaMethodCount(String codeSnippet) {
		int methodCount = 0;
		//Let's not count it as a method if tokens <= 6. We want sufficiently long ones.
		StringTokenizer st = new StringTokenizer(codeSnippet);
		if (st.countTokens() <= 6) return 0;
		
		int start = codeSnippet.indexOf("public", 0);
		if (codeSnippet.contains("public")) methodCount = 1;		
		boolean done = false;
		while (!done) {
			   start = codeSnippet.indexOf("public", start+6);			   
			   if ((start <=0)) {
				   done = true;
			   } else {
				   methodCount++;
			   }
		}
		start = codeSnippet.indexOf("private", 0);
		if (codeSnippet.contains("private")) methodCount++;		
		done = false;
		while (!done) {
			   start = codeSnippet.indexOf("private", start+6);			   
			   if ((start <=0)) {
				   done = true;
			   } else {
				   methodCount++;
			   }
		}
		if (methodCount == 1) {
			int astHint = ASTUtil.getMethods(codeSnippet).size();
			if (astHint > 1) methodCount = astHint;
		}
		return methodCount;
	}
}
