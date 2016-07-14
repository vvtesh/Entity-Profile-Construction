package in.ac.iiitd.pag.util;


import in.ac.iiitd.pag.entity.SONavigator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SOUtil {
	
	public static Map<Integer,SONavigator> loadIdTitleMap(String idTitleFilePath) {
		System.out.println("Loading idTitles... ");
		Map<Integer,SONavigator> idTitle = new HashMap<Integer,SONavigator>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(idTitleFilePath));
			String line = null;
			//int lineCount = 0;
			while ((line = reader.readLine()) != null) {
				try {
					//lineCount++;
					//if (lineCount > 100) break;
					String[] items = line.split(",");
					if (items.length < 4) continue;
					SONavigator soNavigator = new SONavigator();
					soNavigator.votes =  Integer.parseInt(items[1]);
					soNavigator.parentId = Integer.parseInt(items[2]);
					int javaCode = Integer.parseInt(items[3]);
					if (javaCode == 1)
						soNavigator.isJava = true; 
					if (items.length == 5)
						soNavigator.title = items[4];
					int id = Integer.parseInt(items[0]);
					/*if ((soNavigator.parentId == 0)&&(soNavigator.isJava==true)) {
						System.out.println(soNavigator.title);
					}*/
					idTitle.put(id, soNavigator);
				} catch (Exception e) {
					System.out.println(line);
					e.printStackTrace();
				}
			}
			
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		System.out.println("Done.");
		return idTitle;
	}
	
	public static boolean hasJavaTag(String tags) {
		 if (tags!=null) {                		   
		   String[] tagArray = tags.toLowerCase().split(">");
		   for(int i=0; i<tagArray.length; i++) {
			   String temp = tagArray[i].replace("<", "");
			   if (temp.trim().equalsIgnoreCase("java")) {
				   return true;
			   }
		   }
	     }
		 return false;
	}
	
	public static boolean hasCppTag(String tags) {
		 if (tags!=null) {                		   
		   String[] tagArray = tags.toLowerCase().split(">");
		   for(int i=0; i<tagArray.length; i++) {
			   String temp = tagArray[i].replace("<", "");
			   if (temp.trim().equalsIgnoreCase("c++")) {
				   return true;
			   }
		   }
	     }
		 return false;
	}
	
	public static boolean hasTagOnly(String tags, String tag) {
		 if (tags!=null) {                		   
		   String[] tagArray = tags.toLowerCase().split(">");
		   if (tagArray.length > 1) return false;
		   for(int i=0; i<tagArray.length; i++) {
			   String temp = tagArray[i].replace("<", "");
			   if (temp.trim().equalsIgnoreCase(tag)) {
				   return true;
			   }
		   }
	     }
		 return false;
	}
	
	/**
	 * Stackoverflow posts have tags in Posts.xml as Tags = "&gt;java&lt;".
	 * Given an array of tags, this method checks if these tags are present in the 
	 * input string that are of the above format.
	 * @param tagsInput
	 * @param tagsToFilter
	 * @return
	 */
	public static boolean hasTag(String tagsInput, String[] tagsToFilter) {
		 if (tagsInput!=null) {                		   
		   String[] tagArray = tagsInput.toLowerCase().split(">");
		   for(int i=0; i<tagArray.length; i++) {
			   String temp = tagArray[i].replace("<", "");
			   for (String tag: tagsToFilter) {
				   if (temp.trim().equalsIgnoreCase(tag)) {
					   return true;
				   }
			   }
		   }
	     }
		 return false;
	}
	
	public static Map<Integer,SONavigator> loadIdTitleMapNoIsJava(String idTitleFilePath) {
		System.out.println("Loading idTitles... ");
		Map<Integer,SONavigator> idTitle = new HashMap<Integer,SONavigator>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(idTitleFilePath));
			String line = null;
			//int lineCount = 0;
			while ((line = reader.readLine()) != null) {
				try {
					//lineCount++;
					//if (lineCount > 100) break;
					String[] items = line.split(",");
					if (items.length < 4) continue;
					SONavigator soNavigator = new SONavigator();
					soNavigator.votes =  Integer.parseInt(items[1]);
					soNavigator.parentId = Integer.parseInt(items[2]);
					soNavigator.isJava = true; 
					if (items.length == 4)
						soNavigator.title = items[3];
					int id = Integer.parseInt(items[0]);
					/*if ((soNavigator.parentId == 0)&&(soNavigator.isJava==true)) {
						System.out.println(soNavigator.title);
					}*/
					idTitle.put(id, soNavigator);
				} catch (Exception e) {
					System.out.println(line);
					e.printStackTrace();
				}
			}
			
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		System.out.println("Done.");
		return idTitle;
	}
	
	public static Set<String> getAlgoList(String algoFile) {
		Set<String> algos = new HashSet<String>();
		List<String> algoList = FileUtil.readFromFileAsList(algoFile);
		for(String algoDesc: algoList) {
			String[] algoNames = algoDesc.split(",");
			algos.add(algoNames[0]);
		}
		return algos;		
	}
	
	public static String removeCode(String body) {
		body = body.toLowerCase();
		int start = 0;
		int end = 0;

		while (true) {
		   start = body.indexOf("<code>", end+1);
		   end = body.indexOf("</code>", start+11);
		   if ((start <=0)||(end <=0)) {
			   break;
		   }
		   String code = body.substring(start,end);
		   body = body.replace(code, "");
		}
		
		return body;
	}
	
	public static String getCode(String body) {
		String code = "";
		body = body.toLowerCase();
		int start = 0;
		int end = 0;

		while (true) {
		   start = body.indexOf("<code>", end+1);
		   end = body.indexOf("</code>", start+6);
		   if ((start <=0)||(end <=0)) {
			   break;
		   }
		   String snippet = body.substring(start+6,end);
		   code = code + " \n" + snippet; 
		   body = body.replace(snippet, "");
		}
		
		return code;
	}
	
	public static Set<String> getCodeSet(String body) {
		Set<String> codeSet = new HashSet<String>();
				
		//body = body.toLowerCase();
		int start = 0;
		int end = 0;
		
		while (true) {
		   
		   start = body.indexOf("<code>", end);
		   end = body.indexOf("</code>", start+6);
		   if ((start <=0)||(end <=0)) {
			   break;
		   }
		   String snippet = body.substring(start + "<code>".length(),end);
		   //check if snippet has 3 lines.
		   
		   body = body.replace("<code>"+snippet+"</code>", "");
		   if (countLines(snippet)>=3) {
			   codeSet.add(snippet);
		   }
		   
		}
		
		return codeSet;
	}
	
	private static int countLines(String str){
		   String[] lines = str.split("\r\n|\r|\n");
		   //System.out.println(str);
		   return  lines.length;
		}
	
	
}
