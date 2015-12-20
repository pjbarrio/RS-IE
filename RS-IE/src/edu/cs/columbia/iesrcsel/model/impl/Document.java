package edu.cs.columbia.iesrcsel.model.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cs.columbia.iesrcsel.utils.SearchableUtils;

import lemurproject.indri.ParsedDocument;
import lemurproject.indri.ParsedDocument.TermExtent;

public class Document implements Serializable{

	protected TextCollection itsCollection; // FIXME a document can be part of multiple collections at the same time? NO
	protected long itsID;
//	private Map<Integer, Set<String>> itsHGrams; 
	
	/**
	 * 
	 * @param collection
	 * @param documentId  Unique identifier of the document (should be unique
	 *   regardless of collection it belongs to; that will allow 'duplicate'
	 *   documents across multiple collection, ie. the same document can then
	 *   belong simultaneously to multiple collections)
	 */
	public Document(TextCollection collection, long documentId) {
		this.itsCollection = collection;
		this.itsID = documentId;
//		itsHGrams = new HashMap<Integer, Set<String>>();
	}

	@Override
	public boolean equals(Object obj) {
		// FIXME should we really bother about itsCollection as well?
		if (obj instanceof Document){
			Document o = (Document)obj;
			if (o.itsID != itsID)
				return false;
			if (o.itsCollection.equals(itsCollection))
				return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return 31 * (31 + Long.valueOf(itsID).hashCode()) + itsCollection.hashCode();
	}

	public Set<String> getTerms(){
		return itsCollection.getTerms(this);
	}
	
	public int getId() {
		return (int)itsID;
	}
	
	public TextCollection getCollection() {
		return itsCollection;
	}

	public void setTextCollection(TextCollection collection){
		itsCollection = collection;
	}
	
	public String getPath() {
		return itsCollection.getPath(this);
	}
	
	public boolean containsTerm(String term){
		return itsCollection.containsTerm(this,term);
	}
	
	public String toString() {
		return Long.toString(this.itsID);
	}

	public Set<String> getHGrams(int h) {
// If memory is not an issue
//		if (itsHGrams.get(h) == null){
//			itsHGrams.put(h,itsCollection.getHGrams(this, h));
//		}
//		return itsHGrams.get(h);
		
		return itsCollection.getHGrams(this, h);
		
	}

	public List<String> getTokenizedTerms() {
		return itsCollection.getTokenizedTerms(this);
	}

	public Map<String, Integer> getTermFreqMap() {
		return itsCollection.getTermFreqMap(this);
	}

	
}
