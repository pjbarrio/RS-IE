package edu.cs.columbia.iesrcsel.model.impl;

import java.io.Serializable;

public class Query implements Serializable{

	private String query;
	private String[] terms;

	public Query(String query) {
		this.query = query.toLowerCase();
		this.terms = this.query.split(" ");
	}

	public Query(String[] quer) {
		this.terms = quer;
		query = quer[0].toLowerCase();
		for (int i = 1; i < quer.length; i++) {
			query += " " + quer[i];
		}
		
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Query){
			Query q = (Query)obj;
			return q.query.equals(query);
		}
		return false;
	}
	
	public String toString(){
		return query;
	}
	
	@Override
	public int hashCode() {
		return query.hashCode();
	}

	public String[] getTerms() {
		return terms;
	}
}
