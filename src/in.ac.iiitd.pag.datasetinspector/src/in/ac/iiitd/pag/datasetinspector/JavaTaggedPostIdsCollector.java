package in.ac.iiitd.pag.datasetinspector;

import in.ac.iiitd.pag.entity.SONavigator;
import in.ac.iiitd.pag.util.FileUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class JavaTaggedPostIdsCollector {
	public static void main(String[] args) {
		
		List<String> javaPosts = new ArrayList<String>();
		
		Properties props = FileUtil.loadProps();
		if (props == null) return;
		int count = 0;
		String input = props.getProperty("ID_TITLES_VOTES_FILE_PATH");
		String output = props.getProperty("ID_TITLES_VOTES_JAVA_FILE_PATH");
				
		Map<Integer,SONavigator> postsMap = new HashMap<Integer,SONavigator>();
		
		List<String> posts = FileUtil.readFromFileAsList(input);
		for(String post: posts) {
			String[] items = post.split(",");
			if (items.length > 3) {
				try {
					int isJava = Integer.parseInt(items[3]);
					int id = Integer.parseInt(items[0]);
					int votes = Integer.parseInt(items[1]);
					int parentId = Integer.parseInt(items[2]);
					
					String title = "";
					for (int i=4; i<items.length;i++) {
						title = title + items[i] + " ";
					}
					title = title.trim();
					
					SONavigator postItem = new SONavigator();
					postItem.parentId = parentId;
					postItem.isJava = (isJava == 1);
					postItem.votes = votes;
					postItem.title = title;
					postsMap.put(id, postItem);						
					
				} catch (Exception e) {
					//ignore bad rows
				}
			}
		}
		
		for(int postId: postsMap.keySet()) {
			SONavigator item = postsMap.get(postId);
			boolean isJava = item.isJava;			
			int votes = item.votes;
			String title = item.title;
			int parentId = item.parentId;
			
			if ((item.parentId>0) && (postsMap.containsKey(item.parentId))) {
				SONavigator parent = postsMap.get(item.parentId);
				title = parent.title;
				isJava = parent.isJava;
			}
			
			if ((item.parentId>0) && (!postsMap.containsKey(item.parentId))) {
				System.out.println("Warning: Parentid not defined for " + postId + " - " + parentId);
			}
			
			if (isJava) {
				count++;
				String newPost = postId + "," + votes + "," + parentId + ",1," + title;
				javaPosts.add(newPost);
			}
			
		}
		
		try {
			System.out.println(count + " Java posts found.");
			FileUtil.writeListToFile(javaPosts, output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
