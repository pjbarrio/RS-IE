package edu.cs.columbia.iesrcsel.model.collection;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lemurproject.indri.DocumentVector;
import lemurproject.indri.ParsedDocument;
import lemurproject.indri.QueryEnvironment;
import lemurproject.indri.ScoredExtentResult;
import lemurproject.indri.ParsedDocument.TermExtent;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;

import org.apache.commons.io.FileUtils;

import com.google.gdata.util.NotImplementedException;

import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.utils.SearchableUtils;

public class IndriCollection extends TextCollection {

	protected String index;
	protected QueryEnvironment queryEnvironment;
	protected Set<String> stopWords = new HashSet<String>();
	
	public final static String STOPWORDSFILE = "data/stopWords.txt";
	
	public IndriCollection(String id, String index) {
		super(id);
		this.index = index;
		try {
			List<String> stopWordsFromFile = FileUtils.readLines(new File(STOPWORDSFILE));
			for (String w : stopWordsFromFile) {
				stopWords.add(w.toLowerCase());
			}
		} catch (IOException e) {
			// no stopwords read...
			e.printStackTrace();
		}
	}

	@Override
	public List<ScoredDocument> search(Query query) {
		return search(query,Integer.MAX_VALUE);
	}

	private QueryEnvironment getQueryEnvironment() {
		try {
			if (queryEnvironment == null){
				queryEnvironment = new QueryEnvironment();
				String[] stopWordsArray = stopWords.toArray(new String[0]);
				queryEnvironment.setStopwords(stopWordsArray);
				queryEnvironment.addIndex(index);
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return queryEnvironment;
	}

	@Override
	public List<ScoredDocument> search(Query query, long maxNumberOfDocuments) {
		try{
			ScoredExtentResult[] indriQueryResult = getQueryEnvironment().runQuery(query.toString(), (int)maxNumberOfDocuments);
			List<ScoredDocument> result = new ArrayList<ScoredDocument>(indriQueryResult.length);
	        for (ScoredExtentResult d : indriQueryResult) {
				result.add(new ScoredDocument(this, d.document, d.score));
			}
	        return result;
		} catch (Exception e) {
			System.err.println(query.toString());
			e.printStackTrace();
			return new ArrayList<ScoredDocument>();
		}
//		return null;
	}

	@Override
	public long getDocumentFrequency(String term) {
		try {
			return getQueryEnvironment().documentCount(term);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public double matchingItems(Query query) {
		try {
			return getQueryEnvironment().documentCount(query.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public String getPath(Document doc) {
		try {
			return getQueryEnvironment().documentMetadata(new int[]{doc.getId()}, "doc")[0];
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Set<String> getTerms(Document doc) {
		ParsedDocument indriDoc = getParsedDocument(doc);
		Set<String> termList = new HashSet<String>();
		
		try {
			if (indriDoc.terms.length == 0) {
				final TermExtent[] docPositions = indriDoc.positions;
				final DocumentVector aux = getQueryEnvironment().documentVectors(new int[]{doc.getId()})[0];
				//int size = getQueryEnvironment().documentLength(doc.getId()); can't use documentLenght because is the actual lenght of the document, not the indexed size
				
				final int[] docVectorPositions = aux.positions;
				assert docPositions.length == docVectorPositions.length;
				for (int i = 0; i < docVectorPositions.length; ++i) {
					if (docVectorPositions[i] > 0) {
						TermExtent t = docPositions[i];
						String candidateTerm = indriDoc.content.substring(t.begin, t.end);
						termList.add(candidateTerm.toLowerCase());
					}
				}
				indriDoc.terms = SearchableUtils.filterSearchableTerms(termList.toArray(new String[0]), 0);
			}
			return new HashSet<String>(Arrays.asList(indriDoc.terms));			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null; // something went wrong...
	}
	
	/**
	 * 
	 * @param doc
	 * @return The unique stemmed terms occurring in the given doc
	 */
	public String[] getTermsAsIndexed(Document doc){
		DocumentVector aux;
		try {
			aux = getQueryEnvironment().documentVectors(new int[]{doc.getId()})[0];
			return Arrays.copyOfRange(aux.stems, 1, aux.stems.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null; //something went wrong
	}

	@Override
	public long size() {
		try {
			return getQueryEnvironment().documentCount();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public ParsedDocument getParsedDocument(Document doc) {
		try {
			return getQueryEnvironment().documents(new int[]{doc.getId()})[0];
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}

	@Override
	public Set<String> getStopWords() {
		return stopWords;
	}

	@Override
	public boolean isStopWord(String term) {
		return stopWords.contains(term.toLowerCase());
	}

	public boolean containsTermAsIndexed(Document doc, String term) {
		DocumentVector aux;
		try {
			aux = getQueryEnvironment().documentVectors(new int[]{doc.getId()})[0];
			for (int i =0; i < aux.stems.length; ++i) {
				if (aux.stems[i].equals(term)) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false; //something went wrong
	}
	
	@Override
	public boolean containsTerm(Document document, String term) {
		
		try {
			
			Set<String> terms = getTerms(document);
			
			return terms.contains(term);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}

	@Override
	public double matchingItems(Query query, Set<Document> documents, int docsPerQuery) {
		try {
			List<ScoredDocument> res = search(query);
			
			res.retainAll(documents);
			
			return res.size();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public Set<String> getHGrams(Document doc, int h) {
		
		try {
		
			throw new NotImplementedException("Need to implement Hgrams for Indri Collection");
		
		} catch (NotImplementedException e) {
		
			e.printStackTrace();
		
		}
		
		return null;
	
	}

	@Override
	public List<String> getTokenizedTerms(Document doc) {
	
		ParsedDocument indriDoc = getParsedDocument(doc);
		List<String> termList = new ArrayList<String>();
		
		try {
			final TermExtent[] docPositions = indriDoc.positions;
			final DocumentVector aux = getQueryEnvironment().documentVectors(new int[]{doc.getId()})[0];
			
			final int[] docVectorPositions = aux.positions;
			assert docPositions.length == docVectorPositions.length;
			for (int i = 0; i < docVectorPositions.length; ++i) {
				if (docVectorPositions[i] > 0) {
					TermExtent t = docPositions[i];
					String candidateTerm = indriDoc.content.substring(t.begin, t.end);
					termList.add(candidateTerm.toLowerCase());
				}
			}
			
			return Arrays.asList(SearchableUtils.filterSearchableTerms(termList.toArray(new String[0]), 0));
						
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null; // something went wrong...		
	
	}

	@Override
	public Map<String, Integer> getTermFreqMap(Document doc) {
		
		List<String> terms = getTokenizedTerms(doc);
		
		Map<String,Integer> map = new HashMap<String, Integer>();
		
		for (int i = 0; i < terms.size(); i++) {
			
			Integer freq = map.get(terms.get(i));
			
			if (freq == null){
				freq = 0;
			}
			
			map.put(terms.get(i), freq + 1);
		
		}
		
		return map;
		
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
	
}
