package edu.cs.columbia.iesrcsel.sample.generator;

import java.util.Map;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;

public abstract class InformationExtractionBasedSampleGenerator extends SampleGenerator {

	private InformationExtractionSystem extractionSystem;

	public InformationExtractionBasedSampleGenerator(InformationExtractionSystem extractionSystem, Map<String, String> params){
		super(params);
		this.extractionSystem = extractionSystem;
	}

	@Override
	protected Sample _generateSample(TextCollection textCollection) {
		
		Sample s = _generateSample(textCollection,extractionSystem);
		
		return s;
	}

	protected abstract Sample _generateSample(TextCollection textCollection,
			InformationExtractionSystem extractionSystem);
	
}
