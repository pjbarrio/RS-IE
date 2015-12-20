package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.ideestimator;

import java.util.HashMap;
import java.util.Map;

import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.querysampler.QuerySampler;
import edu.cs.columbia.iesrcsel.execution.logger.Parametrizable;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;

public abstract class InverseDocumentDegreeEstimator implements Parametrizable{

	private Map<String, String> params;
	private Map<Document, Double> ides;
	
	
	public InverseDocumentDegreeEstimator(Map<String, String> params){
		this.params = new HashMap<String, String>();
		this.params.putAll(params);
		this.params.put("inverse.document.degree.estimator", this.getClass().getSimpleName());
		reset();
	}
	
	public double estimateInverseDocumentDegree(Document doc){
		
		Double ide = ides.get(doc);
		
		if (ide == null){
			ide = _estimateInverseDocumentDegree(doc);
			ides.put(doc, ide);
		}
		
		return ide;
		
	}

	protected abstract double _estimateInverseDocumentDegree(Document doc);

	public void reset(){
		ides = new HashMap<Document, Double>();
		_reset();
	}

	protected abstract void _reset();

	public abstract void initalize(InformationExtractionSystem ie, QuerySampler qs);

	public Map<String,String> getParams(){
		return params;
	}
	
}
