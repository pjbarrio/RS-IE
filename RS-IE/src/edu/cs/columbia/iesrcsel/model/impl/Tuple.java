package edu.cs.columbia.iesrcsel.model.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Tuple implements Serializable{

	private Map<String,String> values;
	
	public Tuple() {
		values = new HashMap<String,String>();
	}

	public void addFieldValue(String field, String value){
		values.put(field, value);
	}
	
	public String getFieldValue(String field){
		return values.get(field);
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof Tuple){
			Tuple t = (Tuple)obj;
			return t.values.equals(values);
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return values.hashCode();
	}
}
