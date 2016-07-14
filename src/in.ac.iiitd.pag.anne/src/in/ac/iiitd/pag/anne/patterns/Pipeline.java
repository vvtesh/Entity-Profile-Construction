package in.ac.iiitd.pag.anne.patterns;

import in.ac.iiitd.pag.janne.TFCalculator;
import in.ac.iiitd.pag.util.FileUtil;

import java.io.IOException;
import java.util.Map;

public class Pipeline {
	public static void main(String[] args) {
		process();
	}

	private static void process() {
		String baseFolder = "C:\\data\\svn\\iiitdsvn\\entity\\data\\ossprojects1\\";
		String project = "guava";
		
		String sourceFilePath = baseFolder + project + "-code\\";
		String methodsFilePath = baseFolder + project + "-methods.txt";
		String tokenizedOutputFile = baseFolder + project + "-line-tokens.txt";
		String tfOutputFile = baseFolder + project + "-line-tokens-tf.txt";
		String lineReducedFile = baseFolder + project + "-line-reduced.txt";
		String lineReducedMapFile = baseFolder + project + "-line-reduced-freq.txt";
		
		String outputFilePath = baseFolder + project + "-significantPatterns.txt";
		
		try {
			CodeRepoBuilder.grab(sourceFilePath, methodsFilePath);
			LineBasedTokenizer.tokenize(tokenizedOutputFile, methodsFilePath);		
			Map<String, Integer> tf = TFCalculator.construct(tokenizedOutputFile);
			FileUtil.writeMapToFile(tf, tfOutputFile, 400);
			LineReducer.reduceLines(tokenizedOutputFile, tfOutputFile, lineReducedFile);
			SimpleLineReducer.reduce(lineReducedFile, lineReducedMapFile);
			for(int i=1; i<=7; i++) {
				String ngramFile = baseFolder + project + "-"+i+"grams.txt";				
				FrequentNGrams.mine(i, lineReducedMapFile, ngramFile);
				String ngramOutputFile = baseFolder + project + "-"+i+"grams-final.txt";
				MergeNGrams.merge(i, ngramFile, ngramOutputFile);
			}
			SignificantPatternsGrabber.grab(baseFolder, project, outputFilePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
