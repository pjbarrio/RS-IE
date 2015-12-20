package edu.cs.columbia.iesrcsel.model.collection;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;

public class DummyTextCollection extends TextCollection {

	public DummyTextCollection() {
		super("dummy");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public List<ScoredDocument> search(Query query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ScoredDocument> search(Query query, long maxNumberOfDocuments) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getDocumentFrequency(String term) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double matchingItems(Query query) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double matchingItems(Query query, Set<Document> documents,
			int docsPerQuery) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getPath(Document doc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getTerms(Document doc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getStopWords() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isStopWord(String term) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsTerm(Document document, String term) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<String> getHGrams(Document doc, int h) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getTokenizedTerms(Document document) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Integer> getTermFreqMap(Document document) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

}
