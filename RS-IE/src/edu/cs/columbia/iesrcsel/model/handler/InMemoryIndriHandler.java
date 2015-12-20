package edu.cs.columbia.iesrcsel.model.handler;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import opennlp.tools.util.Span;

import org.apache.commons.io.FileUtils;

import lemurproject.indri.IndexEnvironment;
import lemurproject.indri.ParsedDocument;
import lemurproject.indri.ParsedDocument.TermExtent;
import lemurproject.indri.QueryEnvironment;
import lemurproject.indri.ScoredExtentResult;

import edu.cs.columbia.iesrcsel.model.Queryable;
import edu.cs.columbia.iesrcsel.model.collection.IndriCollection;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.utils.SearchableUtils;
import edu.cs.columbia.iesrcsel.utils.indexing.nytlabs.corpus.NYTCorpusDocument;

// FIXME many methods are the same as in IndriCollection --> make common (abstract?) superclass?
public class InMemoryIndriHandler implements Queryable, Serializable{

	private Map<Integer, Document> documentMap;
	private transient IndexEnvironment env;
	private String id;
	private transient QueryEnvironment queryEnvironment;
	
	private final static String TMPINDEXLOCATION = "data/tmp/";
	private static final String STOPWORDSFILE = "data/stopWords.txt";
	
	private String[] stopWords;
	private boolean updatedIndex;
	
	public InMemoryIndriHandler(String indexName){
		System.err.println("constructor");
		this.id = indexName;
		try {
			stopWords = FileUtils.readLines(new File(STOPWORDSFILE)).toArray(new String[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public InMemoryIndriHandler(){
		this(TMPINDEXLOCATION + UUID.randomUUID().toString() +".idx");
		System.err.println("constructor_empty");
	}
	
	@Override
	public List<ScoredDocument> search(Query query) {
		System.err.println("search");
		return search(query, Long.MAX_VALUE);
	}

	@Override
	public List<ScoredDocument> search(Query query, long maxNumberOfDocuments) {
		System.err.println("search by max");
		assert maxNumberOfDocuments >= 1 : "Number of documents to retrieve should be at least 1";
		try{
			final int numberOfDocuments = (maxNumberOfDocuments == Long.MAX_VALUE) ? Integer.MAX_VALUE : (int) maxNumberOfDocuments;
			ScoredExtentResult[] indriQueryResult = getQueryEnvironment().runQuery(query.toString(), numberOfDocuments);
			List<ScoredDocument> resultList = new ArrayList<ScoredDocument>(indriQueryResult.length);
	        for (ScoredExtentResult d : indriQueryResult) {
				resultList.add(new ScoredDocument(getDocumentMap().get(d.document),d.score));
			}
	        return resultList;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public long getDocumentFrequency(String term) {
		System.err.println("document Frequency");
		try {
			return getQueryEnvironment().documentCount(term);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	private QueryEnvironment getQueryEnvironment() {
		try {
			if (queryEnvironment == null || updatedIndex){
				closeIndex(); //Once it queries it for the first time, it does not write any more.
				queryEnvironment = new QueryEnvironment();
				queryEnvironment.setStopwords(stopWords);
		        queryEnvironment.addIndex(id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return queryEnvironment;
		
	}

	private void closeIndex() {
		System.err.println("close index");
		try {
			env.close();
			updatedIndex = false;
			env = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private IndexEnvironment getIndexEnvironment(){
		try{
			if (env == null){
				env = new IndexEnvironment();
				env.setStoreDocs(false);
				env.setStopwords(stopWords);
				env.setStemmer("porter");
				env.setIndexedFields(new String[]{"doc"});
				env.setMetadataIndexedFields(new String[]{"doc"}, new String[0]);
				if (new File(id).exists()){
					env.open(id);
				}else {
					env.create(id);					
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return env;
	}
	
	@Override
	public double matchingItems(Query query) {
		System.err.println("matching items");

		try {
			return getQueryEnvironment().expressionCount(query.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public void addDocument(Document document, TextCollection c) {
		
		System.err.println("add documents");
		
		assert document.getCollection().equals(c);
		
		updatedIndex = true;
		
		ParsedDocument documentToIndex = null;
		
		if (c instanceof IndriCollection){
			
			IndriCollection ic = (IndriCollection) c;
				
			documentToIndex = ic.getParsedDocument(document);
			documentToIndex.terms = document.getTokenizedTerms().toArray(new String[0]);
						
		}else{
			
			documentToIndex  = new ParsedDocument();

			documentToIndex .content = "";

			Map<String,String> map = new HashMap<String, String>();

			map.put("doc", Integer.toString(document.getId()));

			documentToIndex .metadata = map;

			documentToIndex .text = "";

			documentToIndex .terms = c.getTokenizedTerms(document).toArray(new String[0]);

			//doc.positions = termsE;

		}
		
		try {
			int id = getIndexEnvironment().addParsedDocument(documentToIndex);
			getDocumentMap().put(id, document);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

//	private String[] generateTerms(String content, TermExtent[] positions) {
//		String[] terms = new String[positions.length];
//		int i = 0;
//		for (TermExtent t : positions) {
//			terms[i++] = content.substring(t.begin, t.end);
//		}
//		return terms;
//	}

	private Map<Integer, Document> getDocumentMap() {
		if (documentMap == null) {
			documentMap = new HashMap<Integer,Document>();
		}
		return documentMap;
	}

	public double getScore(Query query, Document document, TextCollection collection){
		
		System.err.println("get score");
		
		assert collection.getPath(document) == null : "Document should not be in the index already";
		IndriCollection ic = (IndriCollection) collection;
		try {
			// Add "document" to index, such that we can get score
			updatedIndex = true;
			ParsedDocument documentToIndex = ic.getParsedDocument(document);
			final int id = getIndexEnvironment().addParsedDocument(documentToIndex);
			// Query the updated index
			ScoredExtentResult[] indriQueryResult = getQueryEnvironment().runQuery(query.toString(),Integer.MAX_VALUE);
			// Save score
			Double score = Double.NaN;
			for (ScoredExtentResult d : indriQueryResult) {
				if (d.document == id) {
					score = d.score;
				}
			}
			// Remove "document" from index
			getIndexEnvironment().deleteDocument(id);
			return score;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		// TODO why not just leave Exception uncaught?
		return Double.NaN;
	}

	@Override
	public double matchingItems(Query query, Set<Document> documents,
			int docsPerQuery) {
		System.err.println("matching items docsperquery");

		// TODO Auto-generated method stub
		return 0;
	}
	
}
