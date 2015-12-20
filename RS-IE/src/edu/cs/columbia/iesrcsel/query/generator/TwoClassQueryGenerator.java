package edu.cs.columbia.iesrcsel.query.generator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;

public abstract class TwoClassQueryGenerator extends QueryGenerator {

	protected InformationExtractionSystem extractionSystem;

	public TwoClassQueryGenerator(InformationExtractionSystem extractionSystem){
		this.extractionSystem = extractionSystem;
	}
	
	@Override
	protected List<Query> _generateQueries(Sample sample) {
		
		Set<Document> useful = new HashSet<Document>();
		Set<Document> useless = new HashSet<Document>();
		
		for (Document document : sample) {
			
			if (extractionSystem.extract(document).size() > 0){
				useful.add(document);
			}else{
				useless.add(document);
			}
			
		}
		
		return _generateQueries(sample,useful,useless);
	}

	protected abstract List<Query> _generateQueries(Sample sample, Set<Document> useful,
			Set<Document> useless);

}
