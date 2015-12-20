package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.querysampler;

import java.util.Map;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;

public class IndexedexternalCollectionQuerySampler extends PlainExternalCollectionQuerySampler{

	private int limitsize;

	public IndexedexternalCollectionQuerySampler(Map<String, String> params,
			String prefixFolder, String trainingCollection, double biasedWeight, int limitsize, boolean relationSpecific) {
		super(params, prefixFolder, trainingCollection, biasedWeight, relationSpecific);
		this.limitsize=limitsize;
	}

	@Override
	protected String getUsefulFileName(InformationExtractionSystem ie) {
		return prefixFolder + "termMapUseful." + 
		ie.getRelation() + "." + ie.getExtractor() + "." + trainingcollection + ".INDEX"+limitsize+".ser";
	}
	
	@Override
	protected String getUselessFileName(InformationExtractionSystem ie) {
		return prefixFolder + "termMapUseless." + 
				ie.getRelation() + "." + ie.getExtractor() + "." + trainingcollection + ".INDEX"+limitsize+".ser";
	}
	
	
}
