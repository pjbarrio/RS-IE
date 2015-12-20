package edu.cs.columbia.iesrcsel.collection.estimation.impl.scorebased;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.cs.columbia.iesrcsel.collection.estimation.UsefulDocumentCountEstimator;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.CollisionCounter;
import edu.cs.columbia.iesrcsel.collection.loader.CollectionLoader;
import edu.cs.columbia.iesrcsel.execution.logger.CostLogger;
import edu.cs.columbia.iesrcsel.model.CostAwareTextCollection;
import edu.cs.columbia.iesrcsel.model.collection.CrawledLuceneCollection;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.query.generator.QueryGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGenerator;
import edu.cs.columbia.iesrcsel.score.estimation.method.ScoreCalculator;

public class ScoreBasedUsefulDocumentCountEstimator extends
		UsefulDocumentCountEstimator {

	private ScoreCalculator<Sample, Sample> scoreCalculator;

	private double currentEstimate;

	private SampleGenerator sg;

	private QueryGenerator qg;

	private CollectionLoader colLoad;
	
	public ScoreBasedUsefulDocumentCountEstimator(Map<String, String> params, ScoreCalculator<Sample,Sample> scoreCalculator,
			SampleGenerator sg, QueryGenerator qg, CollectionLoader colLoad) {
		super(params);
		this.scoreCalculator = scoreCalculator;
		this.sg = sg;
		this.qg = qg;
		this.colLoad = colLoad;
	}

	@Override
	public double getNumberOfUsefulDocuments(TextCollection collection,
			InformationExtractionSystem ie, CollisionCounter collisionCounter,
			CostLogger cl) {
		
		currentEstimate = Double.NaN;
		
		cl.setCurrentStatus("CollectingSamples");
		
		List<Sample> samples = new ArrayList<Sample>();
		
		for (TextCollection tc : colLoad.collections()) {

			if (tc.equals(collection)) //Like this, I am taking into account the cost!
				samples.add(sg.generateSample(collection));
			else{
				samples.add(sg.generateSample(tc));
			}
			
		}
				
		cl.setCurrentStatus("Initializing");
		
		scoreCalculator.initialize(samples, qg);
		
		cl.setCurrentStatus("EstimatingScore");
		
		currentEstimate = scoreCalculator.estimateScore(sg.generateSample(collection), qg);
		
		return currentEstimate;
	}

	@Override
	public double getCurrentNumberOfUsefulDocuments() {
		return currentEstimate;
	}

	@Override
	public void reset() {
		scoreCalculator.reset();
	}

}
