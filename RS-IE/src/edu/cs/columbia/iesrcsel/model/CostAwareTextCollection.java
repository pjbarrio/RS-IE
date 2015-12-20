package edu.cs.columbia.iesrcsel.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import weka.core.UnsupportedAttributeTypeException;

import edu.cs.columbia.iesrcsel.execution.logger.CostLogger;
import edu.cs.columbia.iesrcsel.execution.logger.impl.DummyCostLogger;
import edu.cs.columbia.iesrcsel.model.collection.DummyTextCollection;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;

public class CostAwareTextCollection extends TextCollection implements CostAware{

	private TextCollection underlyingCollection;
	private Set<Query> issuedQueries;
	private Set<Query> limitedissuedQueries;
	private Set<Integer> retrievedDocuments;
	private Set<String> issuedTerm;
	private Set<Integer> consultedTerm;
	private Set<Integer> consultedFreqMap;
	private Set<Integer> consultedTokenizedTerm;
	private Set<Integer> consultedHGram;
	private Set<Query> limitedmatchedQuery;
	private Set<Query> matchedQuery;
	private CostLogger cl;
	private Map<String, CostAwareTextCollection> individualLoggers;
	
	public CostAwareTextCollection(TextCollection underlyingCollection, CostLogger cl) {
		super(underlyingCollection.getId());
		this.underlyingCollection = underlyingCollection;
		this.cl = cl;
		individualLoggers = new HashMap<String, CostAwareTextCollection>();
		//TODO: I could add like a registry where these elements register and then, I can just consult there.
		initialize();
	}
	
	@Override
	public CostLogger getCostLogger() {
		return cl;
	}
	
	private void initialize() {
		issuedQueries = new HashSet<Query>();
		limitedissuedQueries = new HashSet<Query>();
		retrievedDocuments = new HashSet<Integer>();	
		issuedTerm = new HashSet<String>();
		consultedTokenizedTerm = new HashSet<Integer>();
		consultedTerm = new HashSet<Integer>();
		consultedFreqMap = new HashSet<Integer>();
		consultedHGram = new HashSet<Integer>();
		limitedmatchedQuery = new HashSet<Query>();
		matchedQuery = new HashSet<Query>();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public List<ScoredDocument> search(Query query) {
		issuedQueries.add(query);
		cl.log();
		for (CostAwareTextCollection col : individualLoggers.values()) {
			col.issuedQueries.add(query);
		}
		return updateTextCollection(underlyingCollection.search(query));
	}

	private List<ScoredDocument> updateTextCollection(
			List<ScoredDocument> documents) {
		
		for (ScoredDocument scoredDocument : documents) {
			scoredDocument.setTextCollection(this);
		}
		
		return documents;
	}

	@Override
	public List<ScoredDocument> search(Query query, long maxNumberOfDocuments) {
		limitedissuedQueries.add(query);
		cl.log();
		for (CostAwareTextCollection col : individualLoggers.values()) {
			col.limitedmatchedQuery.add(query);
		}

		return updateTextCollection(underlyingCollection.search(query, maxNumberOfDocuments));
	}

	@Override
	public long getDocumentFrequency(String term) {
		issuedTerm.add(term);
		cl.log();
		for (CostAwareTextCollection col : individualLoggers.values()) {
			col.issuedTerm.add(term);
		}

		return underlyingCollection.getDocumentFrequency(term);
	}

	@Override
	public double matchingItems(Query query) {
		matchedQuery.add(query);
		cl.log();
		for (CostAwareTextCollection col : individualLoggers.values()) {
			col.matchedQuery.add(query);
		}
		return underlyingCollection.matchingItems(query);
	}

	@Override
	public double matchingItems(Query query, Set<Document> documents,
			int docsPerQuery) {
		limitedmatchedQuery.add(query);
		cl.log();
		for (CostAwareTextCollection col : individualLoggers.values()) {
			col.limitedmatchedQuery.add(query);
		}

		return underlyingCollection.matchingItems(query, documents, docsPerQuery);
	}

	@Override
	public long size() {
		return underlyingCollection.size();
	}

	@Override
	public String getPath(Document doc) {
		retrievedDocuments.add(doc.getId());
		cl.log();
		for (CostAwareTextCollection col : individualLoggers.values()) {
			col.retrievedDocuments.add(doc.getId());
		}

		return underlyingCollection.getPath(doc);
	}

	@Override
	public Set<String> getTerms(Document doc) {
		consultedTerm.add(doc.getId());
		cl.log();
		for (CostAwareTextCollection col : individualLoggers.values()) {
			col.consultedTerm.add(doc.getId());
		}

		return underlyingCollection.getTerms(doc);
	}

	@Override
	public Set<String> getHGrams(Document doc, int h) {
		consultedHGram.add(doc.getId());
		cl.log();
		for (CostAwareTextCollection col : individualLoggers.values()) {
			col.consultedHGram.add(doc.getId());
		}

		return underlyingCollection.getHGrams(doc, h);
	}
	
	@Override
	public Set<String> getStopWords() {
		return underlyingCollection.getStopWords();
	}

	@Override
	public boolean isStopWord(String term) {
		return underlyingCollection.isStopWord(term);
	}

	@Override
	public boolean containsTerm(Document document, String term) {
		consultedTerm.add(document.getId());
		cl.log();
		for (CostAwareTextCollection col : individualLoggers.values()) {
			col.consultedTerm.add(document.getId());
		}

		return false; //XXX why false?
	}

	public void clearCost(){
		initialize();
	}
	
	public Map<String,Integer> getCost(){
		Map<String,Integer> ret = new HashMap<String,Integer>();
		
		ret.put("Issued Queries", issuedQueries.size());
		ret.put("Limited Issued Queries", limitedissuedQueries.size());
		ret.put("Retrieved Documents", retrievedDocuments.size());
		ret.put("Issued Term", issuedTerm.size());
		ret.put("Consulted Tokenized Term", consultedTokenizedTerm.size());
		ret.put("Consulted Term", consultedTerm.size());
		ret.put("Consulted Freq Map", consultedFreqMap.size());
		ret.put("Consulted HGram", consultedHGram.size());
		ret.put("Limited Matched Query",limitedmatchedQuery.size());
		ret.put("Matched Query", matchedQuery.size());
		return ret;
	}

	@Override
	public List<String> getTokenizedTerms(Document doc) {
		
		consultedTokenizedTerm.add(doc.getId());
		cl.log();
		for (CostAwareTextCollection col : individualLoggers.values()) {
			col.consultedTokenizedTerm.add(doc.getId());
		}

		return underlyingCollection.getTokenizedTerms(doc);
		
	}

	@Override
	public Map<String, Integer> getTermFreqMap(Document doc) {
	
		consultedFreqMap.add(doc.getId());
		cl.log();
		
		for (CostAwareTextCollection col : individualLoggers.values()) {
			col.consultedFreqMap.add(doc.getId());
		}
		
		return underlyingCollection.getTermFreqMap(doc);
	
	}

	@Override
	public Map<String, String> getParams() {
		
		Map<String, String> ret = new HashMap<String,String>();
		
		ret.put("collection.id", underlyingCollection.getId());
		
		return ret;
	
	}
	
	@Override
	public String getName() {
		return "Collection";
	}
	
	@Override
	public void startIndividualLogging(String id) {
		
		individualLoggers.put(id, new CostAwareTextCollection(new DummyTextCollection(), new DummyCostLogger()));
	
	}

	@Override
	public CostAware stopIndividualLogging(String id) {
		
		return individualLoggers.remove(id);
		
	}

	@Override
	public void addCost(CostAware cost) {
		
		if (cost instanceof CostAwareTextCollection){
			CostAwareTextCollection catc = (CostAwareTextCollection)cost;
			
			issuedQueries.addAll(catc.issuedQueries);
			limitedissuedQueries.addAll(catc.limitedissuedQueries);
			retrievedDocuments.addAll(catc.retrievedDocuments);
			issuedTerm.addAll(catc.issuedTerm);
			consultedTerm.addAll(catc.consultedTerm);
			consultedFreqMap.addAll(catc.consultedFreqMap);
			consultedTokenizedTerm.addAll(catc.consultedTokenizedTerm);
			consultedHGram.addAll(catc.consultedHGram);
			limitedmatchedQuery.addAll(catc.limitedmatchedQuery);
			matchedQuery.addAll(catc.matchedQuery);
			
		} else
			try {
				throw new UnsupportedAttributeTypeException("Need to pass a CostAwareCollection");
			} catch (UnsupportedAttributeTypeException e) {
				e.printStackTrace();
			}
		
	}

	@Override
	public void close() {
		underlyingCollection.close();
	}
	
}
