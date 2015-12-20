package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.querysampler;

import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.MersenneTwister;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class RandomExternalCollectionQuerySampler extends QuerySampler {

	private String prefixFolder;
	private String trainingcollection;
	private MersenneTwister rg;
	private List<String> queries;
	private boolean relationSpecific;

	public RandomExternalCollectionQuerySampler(Map<String, String> params,
			String prefixFolder, String trainingCollection, boolean relationSpecific) {
		super(params);
		this.prefixFolder = prefixFolder; // e.g., /proj/dbNoBackup/pjbarrio/workspacedb-pc02/ResourceSelectionIE/data/biasedestimator/
		this.trainingcollection = trainingCollection; //e.g., TREC
		this.rg = new MersenneTwister();
		this.relationSpecific = relationSpecific;
		reset();
	}

	@Override
	public void initialize(TextCollection collection,
			InformationExtractionSystem ie) {
		if (relationSpecific)
			queries = (List<String>)SerializationHelper.deserialize(prefixFolder + "1-gram."+trainingcollection+".querypool"+ie.getRelation()+"."+ie.getExtractor()+".ser");
		else
			queries = (List<String>)SerializationHelper.deserialize(prefixFolder + "1-gram." + trainingcollection + ".querypool.ser");
	}

	@Override
	public Query sampleQuery() {
		
		String term = queries.get(rg.nextInt(queries.size()));
	
		return new Query(term);
		
	}

	@Override
	public double normalizationValue() {
		return queries.size();
	}

	@Override
	public double getBiasWeight() {
		return 1.0;
	}

	@Override
	public double getNotNormalizedProbability(Query query) {
		return 1.0;
	}

	@Override
	public void reset() {
		if (queries!=null)
			queries.clear();
	}

	@Override
	public List<String> getQueries() {
		return queries;
	}

}
