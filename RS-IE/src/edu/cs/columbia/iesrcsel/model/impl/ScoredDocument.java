package edu.cs.columbia.iesrcsel.model.impl;

public class ScoredDocument extends Document {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double score;

	public ScoredDocument(TextCollection collection, int documentId, double score) {
		super(collection, documentId);
		this.score = score;
	}

	public ScoredDocument(Document document, double score) {
		this(document.getCollection(),document.getId(),score);
	}

	public Double getScore(){
		return score;
	}
	
}
