package edu.cs.columbia.iesrcsel.model.extractor.impl;

import java.util.List;
import java.util.Map;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Tuple;

public class DummyInformationExtractionSystem extends
		InformationExtractionSystem {

	public DummyInformationExtractionSystem() {
		super("dummy", "dummy", "dummy");
	}

	@Override
	protected List<Tuple> extractTuples(Document document) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getParams() {
		// TODO Auto-generated method stub
		return null;
	}

}
