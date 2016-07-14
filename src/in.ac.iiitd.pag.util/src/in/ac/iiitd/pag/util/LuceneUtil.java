package in.ac.iiitd.pag.util;


import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class LuceneUtil {
	public static void checkIndex(String indexPath) {
		try {

			Version v = Version.LUCENE_48;
			Analyzer analyzer = new WhitespaceAnalyzer(v);
			Directory fsDir = FSDirectory.open(new File(indexPath));

			IndexReader reader = IndexReader.open(fsDir);
			System.out.println("The index " + indexPath + " has "
					+ reader.maxDoc() + " documents.");
			reader.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static Map<String, Set<String>> getTopicVocabularyMap(String indexPath) {
		Map<String, Set<String>> topics = new HashMap<String, Set<String>>();
		
		try {
			
			Version v = Version.LUCENE_48;
			Analyzer analyzer = new WhitespaceAnalyzer(v);
			Directory fsDir = FSDirectory.open(new File(indexPath));

			IndexReader reader = IndexReader.open(fsDir);
			
			for (int i = 0; i < reader.maxDoc(); i++) {
				Document d = reader.document(i);
				String topic = d.get("topic").toLowerCase();
				String method = d.get("code").toLowerCase();
				String[] variables = ASTUtil.getVariableNames(method);
				Set<String> vocab = null;
				if (topics.containsKey(topic)) {
					vocab = topics.get(topic);
				} else {
					vocab = new HashSet<String>();
				}
				for(String variable: variables) {
					vocab.add(variable);
				}
				topics.put(topic, vocab);
			}
			

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return topics;
	}

	public static Map<String, Integer> printAll(String indexPath, String field) {
		Map<String, Integer> methodNames = new HashMap<String, Integer>();
		try {
			
			Version v = Version.LUCENE_48;
			Analyzer analyzer = new WhitespaceAnalyzer(v);
			Directory fsDir = FSDirectory.open(new File(indexPath));

			IndexReader reader = IndexReader.open(fsDir);
			
			for (int i = 0; i < reader.maxDoc(); i++) {
				Document d = reader.document(i);
				String splitMethodName = d.get(field).toLowerCase();
				
				splitMethodName = splitMethodName.replaceAll("_", " ");
				if (methodNames.containsKey(splitMethodName)) {
					methodNames.put(splitMethodName,
							methodNames.get(splitMethodName) + 1);
				} else {
					methodNames.put(splitMethodName, 1);
				}
			}

			methodNames = FileUtil.sortByValues(methodNames);

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return methodNames;
	}

	public static Map<String, Integer> getAllCodeTokens(String indexPath) {
		Map<String, Integer> tokens = new HashMap<String, Integer>();
		try {
			Version v = Version.LUCENE_48;
			Analyzer analyzer = new StandardAnalyzer(v);
			Directory fsDir = FSDirectory.open(new File(indexPath));

			IndexReader reader = IndexReader.open(fsDir);

			for (int i = 0; i < reader.maxDoc(); i++) {
				Document d = reader.document(i);
				String code = d.get("code");
				if (code == null) continue;
				code = d.get("code").toLowerCase();
				Set<String> tokensFound = cleanCode(code);
				
				for(String token: tokensFound) {
					int count = 1;
					if (tokens.containsKey(token)) {
						count = tokens.get(token) + 1;
						if (count > 99999)
							count = 99999;
					}
					tokens.put(token, count);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return tokens;
	}

	private static Set<String> cleanCode(String code) throws IOException {
		Set<String> tokensFound = new HashSet<String>();
		
		StreamTokenizer st = new StreamTokenizer(new StringReader(code));
		
		st.parseNumbers();
		st.wordChars('_', '_');
		st.eolIsSignificant(true);
		st.ordinaryChars(0, ' ');
		st.slashSlashComments(true);
		st.slashStarComments(true);

		int token = st.nextToken();
		while (token != StreamTokenizer.TT_EOF) {
			String tokenFound = "";			
			switch (token) {
			case StreamTokenizer.TT_NUMBER:
				tokenFound = st.nval + "";
				break;
			case StreamTokenizer.TT_WORD:
				tokenFound = st.sval;
				break;
			case '"':
				//tokenFound = st.sval;
				break;
			case '\'':
				//tokenFound = st.sval;
				break;
			case StreamTokenizer.TT_EOL:
				break;
			case StreamTokenizer.TT_EOF:
				break;
			default:
				char character = (char) st.ttype;
				tokenFound = character + "";
				break;
			}
			if (tokenFound!= null)  {
				tokenFound = tokenFound.replaceAll(",", " ");
				tokenFound = tokenFound.replaceAll("\n", " ");		
				tokenFound = tokenFound.replaceAll("\'", " ");
				tokenFound = tokenFound.replaceAll("\"", " ");
				tokenFound = tokenFound.trim();
				if (tokenFound.trim().length() > 0) {
					tokensFound.add(tokenFound);
				}
			}
			token = st.nextToken();
		}

		
		return tokensFound;
	}
	
	public static List<Query> getComplexQuery(String queryString, String searchIn, boolean applyFilter)  {
		String[] algoItems = queryString.split(" ");
		List<Query> queries = new ArrayList<Query>();
		int count = 1;	
		if (applyFilter) {
			int start = Math.min(7, algoItems.length - 1);
			for(int i=start; i>=3; i--) {
				//if (applyFilter && (i < (algoItems.length * 0.9))) break;
				count++;
				//if (count > 4) break;
				BooleanQuery query = new BooleanQuery();
				query.setMinimumNumberShouldMatch(1);		
				Set<String> phrases = NGramBuilder.getSequentialNgrams(queryString, i);
				//System.out.println(phrases.size());
				
				for(String phrase: phrases) {
					Query spanNearQuery = getSpanNearQuery(phrase, searchIn);
					query.add(spanNearQuery, Occur.SHOULD);	
					if (query.clauses().size() > 1023) break;
				}
				queries.add(query);
			}		
			
		} else {
			BooleanQuery query = new BooleanQuery();
			query.setMinimumNumberShouldMatch(1);		
			Set<String> phrases = NGramBuilder.getSequentialNgrams(queryString, 3);
			for(String phrase: phrases) {
				Query spanNearQuery = getSpanNearQuery(phrase, searchIn);
				query.add(spanNearQuery, Occur.SHOULD);
			}
			queries.add(query);
		}
		return queries;
	}
	
	private static Query getSpanNearQuery(String queryString, String searchIn)  {
		
		String[] terms = queryString.trim().split(" ");
		SpanQuery[] spanQueries = new SpanQuery[terms.length];
		for(int i=0; i<terms.length; i++) {
			spanQueries[i] = new SpanTermQuery(new Term(searchIn, terms[i]));
		}
		
		SpanQuery query = new SpanNearQuery(spanQueries,
				  100,
				  true);		
		
		return query;
	}
	
	public static List<MatchResult> getAllVariants(String indexPath, String topicInput) {
		Map<String, Integer> methodNames = new HashMap<String, Integer>();
		List<MatchResult> mrList = new ArrayList<MatchResult>();
		try {
			
			Version v = Version.LUCENE_48;
			Analyzer analyzer = new WhitespaceAnalyzer(v);
			Directory fsDir = FSDirectory.open(new File(indexPath));

			IndexReader reader = IndexReader.open(fsDir);
			
			for (int i = 0; i < reader.maxDoc(); i++) {
				Document d = reader.document(i);
				String topic = d.get("topic").toLowerCase();
				if (!topic.equalsIgnoreCase(topicInput)) continue;
				String code = d.get("code");
				String postId = d.get("id");
				String title = d.get("title");
				
				MatchResult mr = new MatchResult();
				mr.snippet = code;
				mr.postId = postId;
				mr.title = title;
				mrList.add(mr);
			}


		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return mrList;
	}
}
