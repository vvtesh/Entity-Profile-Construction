package in.ac.iiitd.pag.anne.entity;

import in.ac.iiitd.pag.util.FileUtil;
import in.ac.iiitd.pag.util.StringUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class PostDataMerger {
	public static void main(String[] args) {
		Properties props = FileUtil.loadProps();
		String inputFile = props.getProperty("OUTPUT_FILE");
		String outputFile = props.getProperty("OUTPUT_FILE_MERGED");
		Map<Integer,Post> posts = merge(inputFile);
		saveMergedPosts(posts, outputFile);
	}

	private static void saveMergedPosts(Map<Integer, Post> posts, String outputFile) {
		Map<Integer, String> newPosts = new HashMap<Integer, String>();
		for(int id: posts.keySet()) {
			Post post = posts.get(id);
			Set<String> titleTokens = post.titleTokens;
			titleTokens.removeAll(post.codeTokens);
			newPosts.put(id, StringUtil.getAsCSV(titleTokens));
		}
		
		try {
			FileWriter fw = new FileWriter(outputFile);
			BufferedWriter bw = new BufferedWriter(fw);	
			for(int id: newPosts.keySet()) {
				String tokenCSV = newPosts.get(id);
				bw.write(id + ":" + tokenCSV + "\n");				
			}
			bw.close();
			fw.close();
		} catch (Exception e) {
			
		}
	}

	private static Map<Integer,Post> merge(String inputFile) {
		Map<Integer,Post> map = new HashMap<Integer, Post>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile), 4 * 1024 * 1024);
			String line = null;			
			int lineCount = 0;
			System.out.println("Reading file...");
			while ((line = reader.readLine()) != null) {
				lineCount++;
				if (lineCount % 2000 == 0) System.out.print(".");
				if (lineCount % 100000 == 0) System.out.println(lineCount);
								
				String[] items = line.split(":");
				
				
				int id = Integer.parseInt(items[0]);
				int parentId = Integer.parseInt(items[1]);
				
				String title = "";
				if (items.length > 2) title = items[2];
				String code = "";
				if (items.length > 3) {
					code = items[3];
				}
				
				if (parentId == 0) {
					Post post = new Post();
					post.id = id;
					String[] titleTokens = title.split(",");					
					Set<String> titleTokensSet = new HashSet<String>();
					for(String token: titleTokens) {
						titleTokensSet.add(token);
					}
					post.titleTokens = titleTokensSet;
					
					String[] codeTokens = code.split(",");
					Set<String> codeTokensSet = new HashSet<String>();
					for(String token: codeTokens) {
						codeTokensSet.add(token);
					}
					post.codeTokens = codeTokensSet;
					map.put(id, post);
				} else {
					
					Post post = map.get(parentId);
					if (post == null) post = new Post();
					if (post.titleTokens == null) post.titleTokens = new HashSet<String>();
					if (post.codeTokens == null) post.codeTokens = new HashSet<String>();
					String[] titleTokens = title.split(",");					
					Set<String> titleTokensSet = new HashSet<String>();
					for(String token: titleTokens) {
						titleTokensSet.add(token);
					}
					post.titleTokens.addAll(titleTokensSet);
					
					String[] codeTokens = code.split(",");
					Set<String> codeTokensSet = new HashSet<String>();
					for(String token: codeTokens) {
						codeTokensSet.add(token);
					}
					post.codeTokens.addAll(codeTokensSet);
					
					if (post.codeTokens.size() > 0) {					
						map.put(parentId, post);
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
}
