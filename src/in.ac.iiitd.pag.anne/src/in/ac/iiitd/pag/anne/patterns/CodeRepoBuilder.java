package in.ac.iiitd.pag.anne.patterns;


import in.ac.iiitd.pag.util.ASTUtil;
import in.ac.iiitd.pag.util.CodeFragmentInspector;
import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.StringUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CodeRepoBuilder {
	static FileWriter fw;
	static BufferedWriter bw;
	
	public static void main(String[] args) {
		String sourceFilePath = "C:\\data\\svn\\iiitdsvn\\entity\\data\\ossprojects\\hadoop-java-nocomments\\hadoop-trunk";
		String methodsFilePath = "C:\\data\\svn\\iiitdsvn\\entity\\data\\ossprojects\\hadoop-methods.txt";
		grab(sourceFilePath, methodsFilePath);
	}
	
	public static void grab(String sourceFilePath, String methodsFilePath) {
		
				
		try {
				        
	        fw = new FileWriter(new File(methodsFilePath));
		    bw = new BufferedWriter(fw);
	        processFiles(sourceFilePath);
	        bw.close();
	        fw.close();
	        
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		
	}
	
	public static void processFiles( String path ) throws IOException {

        File root = new File( path );
        File[] list = root.listFiles();

        if (list == null) return;

        for ( File f : list ) {
            if ( f.isDirectory() ) {
            	processFiles( f.getAbsolutePath() );                
            }
            else {
                processFile(f);                
            }
        }
    }
	public static void processFile(File f) throws IOException {
		if (!f.getName().endsWith(".java")) return;
		String code = FileUtil.readFromFile(f.getAbsolutePath());
		Set<String> methods = grabMethods(code);
		//System.out.println( "File:" + f.getAbsoluteFile() );
		for(String method: methods) {	
			if (StringUtil.countLines(method) > 3) {
				bw.write(method + "\n\n");
			}
		}
	}
	
	private static Set<String> grabMethods(String code) {
		Set<String> methods = new HashSet<String>();
		methods.addAll(ASTUtil.getMethods(code));		
		return methods;
	}
}
