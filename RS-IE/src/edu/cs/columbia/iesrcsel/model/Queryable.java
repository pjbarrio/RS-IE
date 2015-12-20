package edu.cs.columbia.iesrcsel.model;

import java.util.List;
import java.util.Set;

import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;

public interface Queryable {

	public List<ScoredDocument> search(Query query);
	
	public List<ScoredDocument> search(Query query, long maxNumberOfDocuments);
	
	public long getDocumentFrequency(String term);
	
	public double matchingItems(Query query);
	
	public double matchingItems(Query query, Set<Document> documents, int docsPerQuery);
	
}
