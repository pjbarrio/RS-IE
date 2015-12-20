package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.documentsampler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cs.columbia.iesrcsel.execution.logger.Parametrizable;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;

public abstract class DocumentSampler implements Parametrizable{

	private Map<String, String> params;

	public DocumentSampler(Map<String, String> params){
		this.params = new HashMap<String, String>();
		this.params.putAll(params);
		this.params.put("document.sampler", this.getClass().getSimpleName());
	}
	
	public abstract Document sampleDocument(List<ScoredDocument> docs, InformationExtractionSystem ie);

	public abstract void reset();

	public Map<String,String> getParams(){
		return params;
	}
}
