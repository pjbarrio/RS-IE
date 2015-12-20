package edu.cs.columbia.iesrcsel.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lemurproject.indri.IndexEnvironment;
import lemurproject.indri.ParsedDocument;
import lemurproject.indri.QueryEnvironment;
import lemurproject.indri.ScoredExtentResult;

import edu.cs.columbia.iesrcsel.model.Queryable;
import edu.cs.columbia.iesrcsel.model.handler.InMemoryGlobalLuceneHandler;
import edu.cs.columbia.iesrcsel.model.handler.InMemoryIndriHandler;
import edu.cs.columbia.iesrcsel.model.handler.InMemoryLuceneHandler;

public class SampleCollection implements Queryable {

	private static final String COLLECTION_METADATA = "col";
	private Set<Sample> itsSamples = new HashSet<Sample>();
	private Set<TextCollection> itsCollections = new HashSet<TextCollection>();
	private InMemoryGlobalLuceneHandler inMemoryLuceneHandler;
	
	public SampleCollection() {
		inMemoryLuceneHandler = new InMemoryGlobalLuceneHandler();
		
	}
	
	public void clear() {
		itsSamples.clear();
		itsCollections.clear();
	}
	
	/**
	 * We accept only 1 sample per text collection
	 * @param s
	 * @throws Exception 
	 */
	public void addSampleDocuments(Sample s) {
		// we assume only 1 sample per collection!
		final TextCollection c = s.getCollection();
		if (itsCollections.contains(c)) {
			throw new IllegalArgumentException(this.getClass().getSimpleName()
					+ " already contains sample for collection " + c.getId());
		}
		itsCollections.add(c);
		itsSamples.add(s);
		// Note: what about with duplicate documents?
		// There are no duplicated docs in a sample. Across collections, we just add them.
		// FIXME ok, but in Document, equality is defined based on ID + collection,
		// hence we will NOT be able to find out that document X (which belongs to both A and B)
		// belongs to A, if it was B's copy that got added first
		// i.e. for d the result of search(..), we will have A.contains(d) == false, even if
		// d.id == x.id and A.contains(x) ...
		for (Document document : s) {
			inMemoryLuceneHandler.addDocument(document);
		}
	}

	@Override
	public List<ScoredDocument> search(Query query) {
		return search(query, Integer.MAX_VALUE);
	}
	
	/**
	 * Search for query, but only return documents from sample s
	 * @param query
	 * @param s
	 * @return  Scored documents (from s only)
	 */
	public List<ScoredDocument> search(Query query, Sample s) {
		if (!itsSamples.contains(s)) {
			throw new IllegalArgumentException(this.getClass().getSimpleName()
					+ " does not contain given sample (for collection "
					+ s.getCollection().getId() + ")");
		}
		List<ScoredDocument> allSampleResults = search(query);
		List<ScoredDocument> result = new ArrayList<ScoredDocument>();
		for (ScoredDocument d : allSampleResults) {
			if (d.getCollection().equals(s.getCollection())) {
				result.add(d);
			}
		}
		return result;
	}

	@Override
	public List<ScoredDocument> search(Query query, long maxNumberOfDocuments) {
		return inMemoryLuceneHandler.search(query, maxNumberOfDocuments);
	}

	/**
	 * Search for query, but only return documents from sample s
	 * @param query
	 * @param s
	 * @param maxNumberOfDocuments
	 * @return  Scored documents (from s only)
	 */
	public List<ScoredDocument> search(Query query, Sample s, long maxNumberOfDocuments) {
		if (!itsSamples.contains(s)) {
			throw new IllegalArgumentException(this.getClass().getSimpleName()
					+ " does not contain given sample (for collection "
					+ s.getCollection().getId() + ")");
		}
		// Since we cannot anticipate how many documents a sample s' will contain that
		// are relevant to the query, and how many of those will be more relevant
		// than documents from the given sample s, we cannot a priori bound the 
		// total number of documents to retrieve from the union of all samples and
		// still ensure we have at least the top-maxNumberOfDocuments for sample s.
		final List<ScoredDocument> allSampleResults = search(query);
		List<ScoredDocument> result = new ArrayList<ScoredDocument>();
		for (ScoredDocument d : allSampleResults) {
			if (d.getCollection().equals(s.getCollection())) {
				result.add(d);
				if (result.size() >= maxNumberOfDocuments) {
					break;
				}
			}
		}
		return result;
	}
	
	public boolean contains(Sample s) {
		return itsSamples.contains(s);
	}
	

	@Override
	public long getDocumentFrequency(String term) {
		return inMemoryLuceneHandler.getDocumentFrequency(term);
	}

	@Override
	public double matchingItems(Query query) {
		return inMemoryLuceneHandler.matchingItems(query);
	}

	@Override
	public double matchingItems(Query query, Set<Document> documents,
			int docsPerQuery) {
		return inMemoryLuceneHandler.matchingItems(query, documents, docsPerQuery);
	}
	
}
