package edu.cs.columbia.iesrcsel.model.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lemurproject.indri.ParsedDocument;

import edu.cs.columbia.iesrcsel.model.Queryable;

public abstract class TextCollection implements Queryable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	
	public TextCollection(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof TextCollection){
			TextCollection tc = (TextCollection) obj;
			return tc.id.equals(id);
		}
		return false;
		
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	public abstract long size();

	public abstract String getPath(Document doc);
	
	/**
	 * All terms, not necessarily in the order they appear in the document. Terms appear only once.
	 * @param doc
	 * @return
	 */
	public abstract Set<String> getTerms(Document doc);
	
	/**
	 * 
	 * @return  Words that will be ignored as query terms
	 */
	public abstract Set<String> getStopWords();
	
	/**
	 * Should treat term as case-insensitive
	 * @param term
	 * @return
	 */
	public abstract boolean isStopWord(String term);

	public abstract boolean containsTerm(Document document, String term);
	
	public abstract Set<String> getHGrams(Document doc, int h);

	public abstract List<String> getTokenizedTerms(Document document);

	public abstract Map<String, Integer> getTermFreqMap(Document document);

	public abstract void close();

}
