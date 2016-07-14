package in.ac.iiitd.pag.anne.entity;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * Sample class to create Abstract Intermediate Representation.
 * @author Venkatesh
 *
 */
public class AIR {
	public static void main(String[] args) {
		String codeFragment = "int[] numbers = {10,20,30};";
		System.out.println(codeFragment);
		List<String> keyStructures = translateToAIR(codeFragment);
		String ssv = getAsSSV(keyStructures);
		System.out.println(ssv);
	}

	/**
	 * We use EclipseJDT to parse the input. It creates a AST and allows us to traverse.
	 * @param codeFragment
	 * @return
	 */
	private static List<String> translateToAIR(String codeFragment) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(codeFragment.toCharArray());
		parser.setResolveBindings(false);
		ASTNode node = null;
		final List<String> keyStructures = new ArrayList<String>();
		parser.setKind(ASTParser.K_STATEMENTS);
		try {
			node = (Block) parser.createAST(null);
			node.accept(new ASTVisitor() {
				@Override
				public boolean visit(VariableDeclarationStatement node) {
					keyStructures.add("type");
					return super.visit(node);
				}
				
				@Override
				public boolean visit(ArrayType node) {					
					keyStructures.add("[]");
					return super.visit(node);
				}
				
				@Override
				public void postVisit(ASTNode node) {
					if ((node instanceof VariableDeclarationStatement)) {						
						keyStructures.add("var");
					}
				}
				
				@Override
				public boolean visit(Assignment node) {
					System.out.println(node.toString());
					keyStructures.add(node.getOperator().toString());
					return super.visit(node);
				}

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
				}		
			});
		} catch (Exception e) {
			return null;
		}
		return keyStructures;
	}
	
	/**
	 * Helper function to get a space separated string from a list of tokens.
	 * @param tokens
	 * @return
	 */
	private static String getAsSSV(List<String> tokens) {
		String result = "";
		for(String token: tokens) {
			token = token.trim();
			if (token.length() > 0)
				result = result + token + " ";
		}
		if (result.length()  > 0) result = result.substring(0, result.length() -1);
		return result;
	}
}
