package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.documentsampler;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.MersenneTwister;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;

public class RandomDocumentSampler extends DocumentSampler {

	private MersenneTwister rg;
	private Set<Integer> sampled;

	public RandomDocumentSampler(Map<String,String> params){
		super(params);
		this.rg = new MersenneTwister();
		this.sampled = new HashSet<Integer>();
	}
	
	@Override
	public Document sampleDocument(List<ScoredDocument> docs, InformationExtractionSystem ie) {
		
		Document sel;
		
		sampled.clear(); 
		
		do {
			int index = rg.nextInt(docs.size());
			sampled.add(index);
			sel = docs.get(index);
		} while (!ie.isUseful(sel) && sampled.size()<docs.size());
		
		sampled.clear();
		
		return sel;
	}

	@Override
	public void reset() {
		sampled.clear();
	}

}
