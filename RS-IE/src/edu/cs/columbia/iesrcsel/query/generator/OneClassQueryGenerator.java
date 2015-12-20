package edu.cs.columbia.iesrcsel.query.generator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;

public abstract class OneClassQueryGenerator extends QueryGenerator {

	protected InformationExtractionSystem extractionSystem;

	public OneClassQueryGenerator(InformationExtractionSystem extractionSystem){
		this.extractionSystem = extractionSystem;
	}
	
	@Override
	protected List<Query> _generateQueries(Sample sample) {
		
		Set<Document> useful = new HashSet<Document>();
		
		for (Document document : sample) {
			
			if (extractionSystem.extract(document).size() > 0)
				useful.add(document);
		
		}
		
		return _generateQueries(sample,useful);
	}

	protected abstract List<Query> _generateQueries(Sample sample, Set<Document> useful);

}
