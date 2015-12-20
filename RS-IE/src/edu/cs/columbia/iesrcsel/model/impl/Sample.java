package edu.cs.columbia.iesrcsel.model.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.cs.columbia.iesrcsel.model.Queryable;
import edu.cs.columbia.iesrcsel.model.handler.InMemoryIndriHandler;
import edu.cs.columbia.iesrcsel.model.handler.InMemoryLuceneHandler;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGenerator;

public class Sample implements Queryable, Iterable<Document> {

	private TextCollection collection;
	
	private Set<Document> documents;
	
	private List<Document> orderedDocuments;

	private SampleGenerator sampleGenerator;

	private InMemoryLuceneHandler inmemoryIndriHandler;

	public Sample(TextCollection collection, SampleGenerator sampleGenerator) {
		this.collection = collection;
		this.documents = new HashSet<Document>();
		this.orderedDocuments = new ArrayList<Document>();
		this.sampleGenerator = sampleGenerator;
		this.inmemoryIndriHandler = new InMemoryLuceneHandler(collection);
	}

	public TextCollection getCollection() {
		return collection;
	}

	public void addDocument(Document document) {
		if (!document.getCollection().equals(collection)) {
			throw new IllegalArgumentException("Can only add documents from the same collection");
		}
		if (!documents.contains(document)) {
			documents.add(document);
			orderedDocuments.add(document);
			inmemoryIndriHandler.addDocument(document);
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Sample){
			Sample s = (Sample) obj;
			if (s.sampleGenerator.equals(sampleGenerator) && s.collection.equals(collection)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return 31* (31 + sampleGenerator.hashCode()) + collection.hashCode();
	}

	@Override
	public List<ScoredDocument> search(Query query) {
		return inmemoryIndriHandler.search(query);
	}

	@Override
	public List<ScoredDocument> search(Query query, long maxNumberOfDocuments) {
		return inmemoryIndriHandler.search(query, maxNumberOfDocuments);
	}

	@Override
	public long getDocumentFrequency(String term) {
		return inmemoryIndriHandler.getDocumentFrequency(term);
	}

	public int size() {
		return documents.size();
	}
	
	public boolean isEmpty() {
		return documents.isEmpty();
	}

	@Override
	public Iterator<Document> iterator() {
		return documents.iterator();
	}
	
	public Set<Document> getDocuments() {
		return documents;
	}

	@Override
	public double matchingItems(Query query) {
		return inmemoryIndriHandler.matchingItems(query);
	}

	@Override
	public double matchingItems(Query query, Set<Document> documents,
			int docsPerQuery) {
		return inmemoryIndriHandler.matchingItems(query, documents, docsPerQuery);
	}
	
	public List<Document> getDocumentsAsAdded(){
		return orderedDocuments;
	}

	public void setCollection(TextCollection collection) {
		if (this.collection.equals(collection))
			this.collection = collection;
	}
	
}

