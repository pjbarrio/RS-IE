package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.documentsampler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;

import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.ideestimator.InverseDocumentDegreeEstimator;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.utils.Builder;

public class DocumentSamplerBuilder extends Builder{

	public static List<DocumentSampler> create(
			DocumentSamplerEnum documentSampler, Configuration config,
			InformationExtractionSystem ie) {
		
		List<DocumentSampler> ret = new ArrayList<DocumentSampler>();
		
		Map<String,String> params = new HashMap<String, String>();
		
		switch (documentSampler) {
		case RandomDocumentSampler:
			
			ret.add(new RandomDocumentSampler(params));
			
			break;

		default:
			break;
		}
		
		return ret;
		
	}

}
