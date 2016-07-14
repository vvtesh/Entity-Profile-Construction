package in.ac.iiitd.pag.util;


import in.ac.iiitd.pag.entity.StructuralElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.internal.localstore.Bucket.Visitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.internal.compiler.ast.CaseStatement;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;

/**
 * From any given fragment, full class, a method or statements, 
 * grab all the variable names, expressions and such things of interest.
 * 
 * @author Venkatesh
 *
 */
public class ASTUtil {

	private static List<String> names = new ArrayList<String>();
	private static List<String> keyExpressions = new ArrayList<String>();
	private static List<String> keyStructures = new ArrayList<String>();
	private static List<String> variablesInExpression = new ArrayList<String>();
	private static List<String> stops = null;
	
	static {
		stops = FileUtil.readFromFileAsList("c:\\temp\\javastops.txt");
	}
	
	public static void main(String[] args) {
		String method = FileUtil.readFromFile("c:\\temp\\method3.txt");
		String[] vocab = getVariableNames(method);
		System.out.println(StringUtil.getAsString(vocab));
	}
	
	public static List<String> getNames() {
		return names;
	}

	public static void setNames(List<String> names) {
		ASTUtil.names = names;
	}

	public static List<String> getKeyExpressions() {
		return keyExpressions;
	}

	public static void setKeyExpressions(List<String> keyExpressions) {
		ASTUtil.keyExpressions = keyExpressions;
	}

	public static List<String> getKeyStructures() {
		return keyStructures;
	}

	public static void setKeyStructures(List<String> keyStructures) {
		ASTUtil.keyStructures = keyStructures;
	}

	public static Set<String> getDistinctVariableNames(String codeFragment) {
		String[] variables = getVariableNames(codeFragment);
		Set<String> distinctItems = new HashSet<String>();
		for(String var: variables) {
			distinctItems.add(var.toLowerCase());
		}
		return distinctItems;
	}
	/**
	 * Collect variable names.
	 * 
	 * @param codeFragment
	 * @return
	 */
	public static String[] getVariableNames(String codeFragment) {

		ASTNode node = getASTNode(codeFragment);

		names.clear();
		node.accept(new ASTVisitor() {
			
						
			public boolean visit(SimpleName node) {
								
				String name = node.getFullyQualifiedName().toLowerCase();
				if (stops.contains(name.toLowerCase())) return true;
				
				if (!names.contains(name) && name.length() > 1)
					names.add(node.getFullyQualifiedName().toLowerCase());
				return true;
			}

		});

		String[] allNames = new String[names.size()];
		return names.toArray(allNames);

	}
	
	/**
	 * get important expressions defined in the given fragment.
	 * 
	 * @param codeFragment
	 * @return
	 */
	public static String[] getKeyExpressions(String codeFragment) {

		ASTNode node = getASTNode(codeFragment);
		keyStructures.clear();
		keyExpressions.clear();
		node.accept(new ASTVisitor() {

			public void preVisit(ASTNode node) {
				
				if ((node instanceof ForStatement)||(node instanceof WhileStatement)||(node instanceof DoStatement)) {
					
					keyStructures.add("loop");
				}
				
				if (node instanceof ReturnStatement) {
					
					keyStructures.add("return");
				}
				
				if ((node instanceof SwitchStatement)||(node instanceof IfStatement)) {
					
					keyStructures.add("branch");
				}
				
				if ((node instanceof Expression) || (node instanceof TypeDeclarationStatement) || (node instanceof VariableDeclarationStatement)) {
					String name = node.toString();
					if (!keyExpressions.contains(name) && !names.contains(name) && name.length() > 1)
						keyExpressions.add(name);						
				}
				
				
			}		
			

		});

		String[] allNames = new String[names.size()];
		return keyExpressions.toArray(allNames);

	}
	
		
	public static List<String> getVariables(String expression) {

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(expression.toCharArray());
		parser.setResolveBindings(false);
		
		parser.setKind(ASTParser.K_EXPRESSION);
		ASTNode node = parser.createAST(null);
		
		variablesInExpression.clear();
		
		node.accept(new ASTVisitor() {
			@Override
			public boolean visit(SimpleName node) {
				System.out.println(node.getFullyQualifiedName() + " " +  node.getNodeType());
				variablesInExpression.add(node.getFullyQualifiedName());
				return super.visit(node);
			}
		});
		
		return variablesInExpression;
		
	}
	
	public static List<String> getVariablesFromStatement(String expression) {

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(expression.toCharArray());
		parser.setResolveBindings(false);
		
		parser.setKind(ASTParser.K_STATEMENTS);
		ASTNode node = parser.createAST(null);
		
		variablesInExpression.clear();
		
		node.accept(new ASTVisitor() {
			@Override
			public boolean visit(SimpleName node) {
				
				variablesInExpression.add(node.getFullyQualifiedName());
				return super.visit(node);
			}
		});
		
		return variablesInExpression;
		
	}
	
	public static List<String> getMethodsFromExp(String expression, final List<String> methodName) {

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(expression.toCharArray());
		parser.setResolveBindings(false);
		
		parser.setKind(ASTParser.K_EXPRESSION);
		ASTNode node = parser.createAST(null);
		
		variablesInExpression.clear();
		
		node.accept(new ASTVisitor() {
			public void preVisit(ASTNode node) {
				
				if (node instanceof MethodInvocation) {
					String name = ((MethodInvocation) node).getName().toString();
					if (name.contains("println")) return;
					if (name.contains("scanner")) return;
					if (name.contains("System")) return;
					if (methodName.contains(name)) {						
						variablesInExpression.add("method-recursion");
					} else {
						//System.out.println(name);
						variablesInExpression.add("method");
					}
				}	
			}			
			
		});
		
		return variablesInExpression;
		
	}
	
	public static List<String> getMethodsFromStatementExp(String expression, final List<String> methodName) {

		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(expression.toCharArray());
		parser.setResolveBindings(false);
		
		parser.setKind(ASTParser.K_STATEMENTS);
		ASTNode node = parser.createAST(null);
		
		variablesInExpression.clear();
		
		node.accept(new ASTVisitor() {
			public void preVisit(ASTNode node) {
				
				if (node instanceof MethodInvocation) {
					String name = ((MethodInvocation) node).getName().toString();
					if (name.contains("println")) return;
					if (name.contains("scanner")) return;
					if (name.contains("System")) return;
					if (methodName.contains(name)) {
						variablesInExpression.add("method-recursion");
					} else {
						//System.out.println(name);
						variablesInExpression.add("method");
					}
				}	
			}			
			
		});
		
		return variablesInExpression;		
	}
	
	
	public static ASTNode getASTNode(String codeFragment) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(codeFragment.toCharArray());
		parser.setResolveBindings(false);
		ASTNode node = null;

		parser.setKind(ASTParser.K_STATEMENTS);
		try {
			node = (Block) parser.createAST(null);
		} catch (Exception e) {
			return null;
		}
		if (node.toString().trim().equalsIgnoreCase("{\n}")) {
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setSource(codeFragment.toCharArray());
			parser.setResolveBindings(false);
			node = (CompilationUnit) parser.createAST(null);
			if (node.toString().trim().equalsIgnoreCase("{\n}")
					|| node.toString().trim().equalsIgnoreCase("")) {
				codeFragment = "public class A { \n" + codeFragment + "\n }";
				
				parser.setKind(ASTParser.K_COMPILATION_UNIT);
				parser.setSource(codeFragment.toCharArray());
				parser.setSource(codeFragment.toCharArray());
				parser.setResolveBindings(false);
				node = (CompilationUnit) parser.createAST(null);

			}
		}
		return node;
	}

	public static List<String> getMethods(String codeFragment) {
		final List<String> methods = new ArrayList<String>();
		ASTNode node = getASTNode(codeFragment);
		node.accept(new ASTVisitor() {
			@Override
			public void preVisit(ASTNode node) {
				if (node instanceof MethodDeclaration) {
					methods.add(node.toString());
				}
			}
		});
		return methods;
	}
	
	/**
	 * Assuming there is only one method, compute structural complexity.
	 * 
	 * @param codeFragment
	 * @return
	 */
	public static int getStructuralComplexity(String codeFragment) {
		codeFragment = codeFragment.toLowerCase();
		codeFragment = codeFragment.replaceAll("system\\.out\\.println", "println");
		codeFragment = codeFragment.replaceAll("\\(", " \\( ");
		codeFragment = codeFragment.replaceAll("\\)", " \\) ");
		//codeFragment = codeFragment.replaceAll("([a-z]+)\\.([a-z]+)", "$2");
		//System.out.println(codeFragment);
		//System.out.println(" hellow.world.com(".replaceAll("\\s(.*\\.)(.*)(\\s|\\()", " $2$3"));
		
		final List<StructuralElement> elements = new ArrayList<StructuralElement>();
		final List<String> methodName =  new ArrayList<String>();
		ASTNode node = getASTNode(codeFragment);
		
		node.accept(new ASTVisitor() {
			
			private void handleExpression(ASTNode node) {				
				List<String> vars = getVariables(node.toString());
				if (vars.size() == 0) {
					vars = getVariablesFromStatement(node.toString());
					vars.remove("scanner");
					vars.remove("system");
					vars.remove("println");
				}
				if (vars.size() > 0) {
					
					//System.out.println(vars);					
					elements.add(new StructuralElement("expression ", vars.size()));
					/*for(String var: vars) {
						elements.add(new StructuralElement(var,1));
					}*/
				}
				List<String> vars2 = new ArrayList<String>();

				List<String> vars1 = new ArrayList<String>();
				vars1 = getMethodsFromExp(node.toString(), methodName);
				if (vars1.size() == 0) {
					vars1 = getMethodsFromStatementExp(node.toString(), methodName);
					vars1.remove("scanner");
					vars1.remove("system");
					vars1.remove("println");
				}
				for(String var: vars1) {
					if (!vars.contains(var)) vars2.add(var);
				}
				for(String var: vars2) {
					if (vars1.size() > 0) {
						elements.add(new StructuralElement("method ", vars1.size()));
					}
				}
					
			}
			
			public void preVisit(ASTNode node) {
				if ((node instanceof ForStatement)||(node instanceof WhileStatement)||(node instanceof DoStatement)) {					
					elements.add(new StructuralElement("loop", 1));
				}
				
				if ((node instanceof IfStatement)) {					
					elements.add(new StructuralElement("branch", 1));
					Statement elseStatement= ((IfStatement) node).getElseStatement();
					if (elseStatement != null && !(elseStatement instanceof IfStatement)) {
						elements.add(new StructuralElement("else", 1));
					}
				}
				
				if (node instanceof MethodDeclaration) {
					methodName.add(((MethodDeclaration)node).getName().toString());
				}
				
				if ((node instanceof VariableDeclarationStatement)||(node instanceof ExpressionStatement)) {
					handleExpression(node);
				}
				
				//System.out.println(node.getNodeType() + node.toString());
				
				if (node instanceof MethodInvocation) {
					String name = ((MethodInvocation) node).getName().toString();
					if (methodName.contains(name)) {
						elements.add(new StructuralElement("method-recursion", 1));
					} else {
						if (!name.equals("println")) {
							//System.out.println(name);
							elements.add(new StructuralElement("method", 1));
						}
					}
				}				
				
				if (node.getNodeType() == ASTNode.SWITCH_STATEMENT) {
					elements.add(new StructuralElement("branch", 1));	
					List<Statement> stmts = ((SwitchStatement)node).statements();
					for(Statement stmt: stmts) {
						if (stmt.getNodeType() == ASTNode.SWITCH_CASE) {
							elements.add(new StructuralElement("case",1));
						} else {							
							handleExpression(stmt);
						}
					}
				}				
				
			}		
			
			@Override
			public void endVisit(IfStatement node) {
				elements.add(new StructuralElement("endbranch", 1));				
			}
			
			@Override
			public void endVisit(SwitchStatement node) {
				elements.add(new StructuralElement("endbranch", 1));
			}
			
			@Override
			public void endVisit(ForStatement node) {
				elements.add(new StructuralElement("endloop", 1));
			}
			
			public void endVisit(WhileStatement node) {
				elements.add(new StructuralElement("endloop", 1));
			};
			
			public void endVisit(MethodDeclaration node) {
				methodName.clear();				
			};
			
		});

		int totalComplexity = 0;
		int x = 1;
		boolean isRecursive = false; 
		List<Integer> complexityGrowth = new ArrayList<Integer>();
		complexityGrowth.add(totalComplexity);
		for(StructuralElement element: elements) {
			//System.out.println(element.elementType + element.complexity);
			if (element.elementType.equals("loop")) {
				x = 7;
			} 
			if (element.elementType.equals("endloop")) {
				x = 1;
			}
			if (element.elementType.equals("method-recursion")) 
				isRecursive = true;
			if (element.elementType.equals("endbranch")) continue;
			if (element.elementType.equals("loop")) continue;
			if (element.elementType.equals("endloop")) continue;
			totalComplexity = totalComplexity + element.complexity * x;
			complexityGrowth.add(totalComplexity);
		}
		if (isRecursive) {
			totalComplexity = totalComplexity * 5;
			complexityGrowth.add(totalComplexity);
		}		
		//System.out.println(complexityGrowth);
		return totalComplexity;

	}
}
