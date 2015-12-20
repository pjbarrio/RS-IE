package edu.cs.columbia.iesrcsel.query.generator.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.query.generator.QueryGenerator;
import edu.cs.columbia.iesrcsel.query.generator.TwoClassQueryGenerator;

public class SVMQueryGenerator extends TwoClassQueryGenerator {

	private InformationExtractionSystem extractionSystem;

	public SVMQueryGenerator(InformationExtractionSystem extractionSystem){
		super(extractionSystem);
	}

	@Override
	protected List<Query> _generateQueries(Sample sample, Set<Document> useful,
			Set<Document> useless) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
