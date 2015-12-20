package edu.cs.columbia.iesrcsel.model.impl;

import java.util.HashMap;
import java.util.Map;

import edu.cs.columbia.iesrcsel.model.BinaryRelevanceJudge;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;

public class BinaryRelevanceJudgeIE implements BinaryRelevanceJudge {
	
	protected InformationExtractionSystem itsIESystem = null;
	protected Map<Document, Boolean> itsRelevanceCache = new HashMap<Document, Boolean>();
	
	public BinaryRelevanceJudgeIE(InformationExtractionSystem ies) {
		itsIESystem = ies;
	}

	@Override
	public boolean isRelevant(Document d, Query q) {
		Boolean relevant = itsRelevanceCache.get(d);
		if (relevant == null) {
			relevant = !itsIESystem.extract(d).isEmpty();
			itsRelevanceCache.put(d, relevant);
		}
		return relevant;
	}

}
