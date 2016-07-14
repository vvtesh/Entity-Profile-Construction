package in.ac.iiitd.pag.datasetinspector;

import java.io.File;
import java.io.IOException;

public class FolderHierarchyPrinter {
	
	static int count = 0; 
	
	public static void main(String[] args) {
		try {
			processFiles("C:\\data\\svn\\iiitdsvn\\research\\algogrep\\full-paper\\book-examples\\dataset");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void processFiles( String path ) throws IOException {

        File root = new File( path );
        File[] list = root.listFiles();

        if (list == null) return;

        for ( File f : list ) {
            if ( f.isDirectory() ) {
            	processFiles( f.getAbsolutePath() );    
            	System.out.println(++count + ". " + f.getAbsolutePath());
            }
            else {
                processFile(f);                
            }
        }
    }

	private static void processFile(File f) {
		//if (f.getName().endsWith("lst") || f.getName().endsWith("java"))
		if (f.isDirectory())
			System.out.println(++count + ". " + f.getAbsolutePath());
	}
}
