package in.ac.iiitd.pag.entity;

public class StructuralElement {
	public String elementType = "";
	public int complexity = 0;
	
	public StructuralElement(String type, int complexity) {
		this.elementType = type;
		this.complexity = complexity;
	}
}
