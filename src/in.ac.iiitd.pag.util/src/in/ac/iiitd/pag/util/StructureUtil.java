package in.ac.iiitd.pag.util;

import in.ac.iiitd.pag.util.ASTUtil;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.internal.compiler.ast.OperatorExpression;

public class StructureUtil {
	
	static String[] uniops = {"++","--"};
	
	public static List<String> getAlgo(String codeFragment) {
		final ArrayList<String> elements = new ArrayList<String>();
		final ArrayList<Integer> positions = new ArrayList<Integer>();
		elements.clear();
		elements.add("<algo>");
		ASTNode node = ASTUtil.getASTNode(codeFragment);
		final String methodName = getMethodName(codeFragment);
		node.accept(new ASTVisitor() {
			boolean skip = false;
			@Override
			public void preVisit(ASTNode node) {
				//System.out.println(node.toString() + node.getNodeType());
				if (node.getNodeType() == 37) {
					//System.out.println(node.toString());
					if (node.toString().contains(uniops[0])) {
						elements.add("+");
					}
					if (node.toString().contains(uniops[1])) {
						elements.add("-");
					}
				}
				if (node instanceof SimpleType) {	
					//System.out.println(node.toString());
					String typeName = ((SimpleType) node).getName().getFullyQualifiedName();
					if (!typeName.contains("Exception") && !typeName.contains("Scanner"))
						elements.add(typeName);
				}
				
				if (node instanceof MethodInvocation) {
					String cmethodName = ((MethodInvocation) node).getName().toString();
					if (cmethodName.equalsIgnoreCase("println") || cmethodName.equalsIgnoreCase("log")) {
						skip = true;
					}
					//System.out.println("Inside " + ((MethodInvocation) node).getName() + " invoked.");
				}
				if (node instanceof IfStatement) {
					if (!skip) elements.add("<branch>");
				}
				if ((node instanceof ForStatement)||(node instanceof WhileStatement)) {
					if (!skip) elements.add("<loop>");
				}
				
				if (node instanceof Assignment) {
					//System.out.println(node.toString());
					String op =((Assignment) node).getOperator().toString();
					if (!op.equalsIgnoreCase("=")) {
						elements.add(op);
					}
					/*List<String> operators = getOperator(node.toString(), operatorsFile);
					for(String op: operators) {
						if (!skip) elements.add(op); }*/
				}
			}			
			
			@Override
			public boolean visit(Assignment node) {
				//InfixExpression infixExpression=new InfixExpression(node.getAST().);
				
				return super.visit(node);
			}
			
			@Override
			public boolean visit(InfixExpression node) {
				//System.out.println(node.getStartPosition() + node.toString());
				/*if (positions.contains(node.getStartPosition())) {
					
					return true;
				}
				List<String> operators = getOperator(node.toString(), operatorsFile);
				for(String op: operators)
					if (!skip) elements.add(op);
				positions.add(node.getStartPosition());*/
				return true;
			}
			
			@Override
			public void endVisit(InfixExpression node) {
				elements.add(node.getOperator().toString());
				//System.out.println(node.toString());
			}
			
			
			@Override
			public void endVisit(IfStatement node) {
				if (!skip) elements.add("</branch>");
			}
			
			@Override
			public void endVisit(ForStatement node) {
				if (!skip) elements.add("</loop>");
			}
			
			@Override
			public void endVisit(WhileStatement node) {
				if (!skip) elements.add("</loop>");
			}
			
			public void endVisit(MethodInvocation node) {
				String calledMethodName = ((MethodInvocation) node).getName().toString();
				if (calledMethodName.equalsIgnoreCase("Scanner") || calledMethodName.equalsIgnoreCase("print") || calledMethodName.equalsIgnoreCase("println") || calledMethodName.equalsIgnoreCase("log")) {					
					skip = false;					
				} else {
					if (methodName == null) return;
					if (calledMethodName.equalsIgnoreCase(methodName)) {
						elements.add("recursion");
					} else {
						elements.add("method");
					}
				}
				//System.out.println(((MethodInvocation) node).getName() + " over.");
			};
			
		});	
		
		
		//flattenAlgo(elements);
	    elements.add("</algo>");
	    return elements;
	}
	
	public static List<String> getOperator(String str, String operatorsFile) {
		List<String> list = new ArrayList<String>();
		
		List<String> operators = FileUtil.readFromFileAsList(operatorsFile );
		for(int i=0; i<operators.size();i++) {
			if (str.contains(operators.get(i))) {
				list.add(operators.get(i));
				str = str.replace(operators.get(i), "");
			}
		}
		
		return list;
	}
	
	public static List<String> flattenAlgo(List<String> algo) {
		List<String> newElements = new ArrayList<String>();
		String prefix = "";
		for(int i=0; i<algo.size();i++) {
			String node = algo.get(i);
			switch (node) {
			case "<branch>":
				prefix = prefix + "branch";
				break;
			case "<loop>":
				prefix = prefix + "loop";
				break;
			case "</loop>":
				prefix = prefix.substring(0, prefix.length() - 4);
				break;
			case "</branch>":
				prefix = prefix.substring(0, prefix.length() - 6);
				break;
			case "<algo>":
				//do nothing
				break;
			case "</algo>":
				//do nothing
				break;
			default:
				//newElements.add(URLEncoder.encode(prefix + node));
				newElements.add(prefix + node);
			}
		}

		return newElements;
	}
	public static String getMethodName(String codeFragment) {
		final List<String> methodName = new ArrayList<String>();
		ASTNode node = ASTUtil.getASTNode(codeFragment);
		node.accept(new ASTVisitor() {			
			public void endVisit(MethodDeclaration node) {
				methodName.add(node.getName().getFullyQualifiedName());				
			}

		});			
	    if (methodName.size() > 0)
	    	return methodName.get(0);
	    else
	    	return null;
	}
}
