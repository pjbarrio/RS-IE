package edu.cs.columbia.iesrcsel.model.extractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Tuple;

public abstract class InformationExtractionSystem {

	private String id;

	private Map<Document,List<Tuple>> tuplesMap;

	private List<Tuple> empty_List = new ArrayList<Tuple>(0);

	private String relation;

	private String extractor;
	
	
	public InformationExtractionSystem(String id, String relation, String extractor){
		this.id = id;
		tuplesMap = new HashMap<Document, List<Tuple>>();
		this.relation = relation;
		this.extractor = extractor;
	}
	
	public List<Tuple> extract(Document document){
		List<Tuple> tuples = tuplesMap.get(document);
		if (tuples == null) {
			tuples = extractTuples(document);
			if (tuples == null)
				tuples = empty_List ;
			tuplesMap.put(document,tuples);
		}
		return tuples;
	}

	protected abstract List<Tuple> extractTuples(Document document);
	
	public boolean isUseful(Document document) {
		List<Tuple> tuples = extract(document);
		return !tuples.isEmpty();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof InformationExtractionSystem){
			InformationExtractionSystem ie = (InformationExtractionSystem) obj;
			return ie.id.equals(id);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	public String getId() {
		return id;
	}

	public abstract Map<String,String> getParams();

	public String getRelation() {
		return relation;
	}

	public String getExtractor() {
		return extractor;
	}

}
