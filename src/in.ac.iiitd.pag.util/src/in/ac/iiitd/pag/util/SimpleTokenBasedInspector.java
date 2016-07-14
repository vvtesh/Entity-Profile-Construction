package in.ac.iiitd.pag.util;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleTokenBasedInspector {
	public static void main(String[] args) {
		Properties props = FileUtil.loadProps();
		if (props == null) return;
				
		String folderPath = props.getProperty("TEMP_FOLDER_TO_WRITE");
		
		System.out.println("CodeFragmentInspector v1.0");
		System.out.println("====================================");		
		System.out.println("folderPath=" + folderPath);
		System.out.println("====================================");
		
		File[] files = new File("D:\\svn\\dataset\\binary-search\\all").listFiles();
		for(File file: files) {
			//System.out.println(file.getAbsolutePath());
			List<String> codeFragment = FileUtil.readFromFileAsList(file.getAbsolutePath());
			List<String> statements = transform(codeFragment);				
			int complexity = computeComplexity(statements);
			//if (complexity < 10)
				System.out.println( complexity ); //+ "\t" + file.getName()
		}		
		
		/*File file = new File("D:\\svn\\dataset\\test1.java");
		List<String> codeFragment = FileUtil.readFromFileAsList(file.getAbsolutePath());
		List<String> statements = transform(codeFragment);	
		System.out.println(statements);
		int complexity = computeComplexity(statements);
		if (complexity < 10)
			System.out.println(file.getName() + "\t" + complexity ); //+ "\t" + file.getName()
		System.out.println(complexity);*/
	}
	
	private static int computeComplexity(List<String> statements) {
		int complexity = 0;
		int wB = 0;
		int wL = 0;
		int wO = 0;
		int wM = 0;
		for(String statement: statements) {
			if (statement.equalsIgnoreCase("branch")) {
				wB++;
			}
			if (statement.equalsIgnoreCase("method")) {
				wM++;
			}
		}
		boolean isInLoop = false;
		for(String statement: statements) {
			
			if (statement.equalsIgnoreCase("loop")) {
				isInLoop = true;				
				wL++;
			}
			if (statement.equalsIgnoreCase("endloop")) {
				isInLoop = false;				
			}
			if (isInLoop) {
				int x =0;
				try {
					x = Integer.parseInt(statement);
				} catch (Exception e) {}
				wL = wL + x;
			} else {
				if (!statement.equalsIgnoreCase("method")) {
					int x =0;
					try {
						x = Integer.parseInt(statement);
					} catch (Exception e) {}
					wO = wO + x;
				}
			}
		}
		//System.out.println(wL + "" + wB + "" + wO + "" + wM);
		complexity = 1 * wL + wB * statements.size() + wO + (int)Math.round(0.5 * wM);
		//System.out.println(complexity);
		return complexity;
	}

	/**
	 * Matches strings like {@code obj.myMethod(params)} and
	 * {@code if (something)} Remembers what's in front of the parentheses and
	 * what's inside.
	 * <p>
	 * {@code (?U)} lets {@code \\w} also match non-ASCII letters.
	 */
	public static final Pattern PARENTHESES_REGEX = Pattern
	        .compile("(?U)([.\\w]+)\\s*\\((.*)\\)");

	/*
	 * After these Java keywords may come an opening parenthesis.
	 */
	private static List<String> keyWordsBeforeParens = Arrays.asList("while", "for", "if",
	        "try", "catch", "switch");

	private static boolean containsMethodCall(final String s) {
	    final Matcher matcher = PARENTHESES_REGEX.matcher(s);

	    while (matcher.find()) {
	        final String beforeParens = matcher.group(1);
	        final String insideParens = matcher.group(2);
	        if (keyWordsBeforeParens.contains(beforeParens)) {
	            //System.out.println("Keyword: " + beforeParens);
	            return containsMethodCall(insideParens);
	        } else {
	            //System.out.println("Method name: " + beforeParens);
	            return true;
	        }
	    }
	    return false;
	}
	
	public static List<String> transform(List<String> codeFragment) {
		List<String> statements = new ArrayList<String>();
		Stack<String> stack = new Stack<String>();
		for(String statement: codeFragment) {			
			statement = clean(statement);
			if (statement.length() == 0) continue;
			//System.out.println(statement);
			boolean isOther = true;
			if (statement.contains("{")) {
				stack.push("{");
			}
			if (statement.contains("}")) {
				if (!stack.isEmpty()) {
					String op = stack.pop();
					if (op.length()>1) {
						statements.add(op);
					}
				}
			}
			if (statement.contains(" if ") || statement.contains(" if(") ||
					statement.startsWith("if ") || statement.startsWith("if(")
					) {
				statements.add("branch");				
				if (statement.contains("{")) {
					//remove it from stack
					stack.pop();
				}
				stack.push("endbranch");
				isOther = false;
			}
			if (statement.startsWith("switch ") || statement.startsWith("switch(") ||
					statement.contains(" switch ") || statement.contains(" switch(")) {
				statements.add("branch");
				if (statement.contains("{")) {
					//remove it from stack
					if (!stack.isEmpty())
						stack.pop();
				}
				stack.push("endbranch");
				isOther = false;
			}
			if (statement.startsWith("for ")||statement.startsWith("for(") ||
					statement.contains(" for ")||statement.contains(" for(")
					) {
				statements.add("loop");
				stack.push("endloop");
				isOther = false;
			}
			if (statement.startsWith("while ")||statement.startsWith("while(") ||
					statement.contains(" while ")||statement.contains(" while(")
					) {
				statements.add("loop");
				if (statement.contains("{")) {
					//remove it from stack
					if (!stack.isEmpty())
						stack.pop();
				}
				stack.push("endloop");
				isOther = false;
			}
			if (containsMethodCall(statement)) {
				statements.add("method");
				isOther = false;
			}
			if (isOther) {
				List<String> ids = CodeFragmentInspector.getIdentifiers(statement, true);
				if (ids.size() > 0)
					statements.add(ids.size() + ""); 
				else
					statements.add("1");
			}
		}
		
		return statements;
	}

	private static String clean(String statement) {
		statement = statement.toLowerCase().trim();
		//System.out.println(statement);
		statement = statement.replaceAll("[0-9]+[:\\.#]", "");
		//System.out.println(statement);
		while (true) {
			int start = statement.indexOf("\"");
			int end = statement.indexOf("\"",start + 1);
			if (end > start) {
				statement = statement.replace(statement.substring(start, end+1), "|");
			} else {
				break;
			}
			start = statement.indexOf("\'");
			end = statement.indexOf("\'",start + 1);
			if (end > start) {
				statement = statement.replace(statement.substring(start, end+1), "|");
			}
		}
		if (statement.startsWith("//") || statement.startsWith("*") || statement.startsWith("\\*") || (statement.endsWith("*/"))) statement = "";
		return statement;
	}
}
