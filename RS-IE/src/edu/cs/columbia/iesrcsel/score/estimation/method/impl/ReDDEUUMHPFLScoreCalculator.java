package edu.cs.columbia.iesrcsel.score.estimation.method.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import edu.cs.columbia.iesrcsel.collection.estimation.CollectionSizeEstimator;
import edu.cs.columbia.iesrcsel.model.BinaryRelevanceJudge;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.SampleCollection;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.query.generator.QueryGenerator;
import edu.cs.columbia.iesrcsel.score.estimation.method.ScoreCalculator;
import edu.cs.columbia.iesrcsel.utils.PairUnordered;

/**
 * An extension of ReDDE-UUM-HP-FL to multiple queries.
 * ReDDE-UUM-HP-FL is the Unified Utility Maximization (UUM) variant of ReDDE as per [1],
 * optimizing for high precision (HP) with fixed length (FL) of the document list retrieved for
 * a query from each of the collections.
 * The score for a Sample is actually the estimated number of relevant document among the top-K results for 
 * each of the queries issued against the corresponding (entire!) text collection.
 * Important note: We do NOT correct for possible document overlap among the query results.
 * 
 * [1]  M. Shokouhi and J. Zobel, "Federated text retrieval from uncooperative overlapped collections",
 * in Proc. 30th Int. ACM SIGIR Conf. Research and Development in Information Retrieval (SIGIR 2007),
 * Amsterdam, The Netherlands, ACM, 23-27 Jul. 2007, pp. 495-502.
 * 
 * @author Chris Develder
 */
public class ReDDEUUMHPFLScoreCalculator extends ScoreCalculator<Sample,Sample> {

	protected long itsNumberOfDocumentsPerQuery = 0L; // top-K documents that will be used for each query score calculation
	protected CollectionSizeEstimator itsCollectionSizeEstimator = null;
	protected QueryTopScoreProvider itsQueryTopScoreProvider = null;
	protected ReDDEUUMScoreCalculatorConfiguration itsReddeConfiguration = null;
	protected BinaryRelevanceJudge itsRelevanceJudge = null;
	
	protected SampleCollection itsSampleCollection = new SampleCollection(); // centralized index of all sample documents

	
	protected Map<Query, Double> itsTrainingQueryToMaxDocScore = new HashMap<Query, Double>();
	
	protected String[] itsClassifierOptions = weka.core.Utils.splitOptions("-R 1.0E-8 -M -1"); // these are the default options in Weka GUI
	
	/**
	 * 
	 * @param queryGenerator  To obtain set of queries for each sample/collection
	 * @param numberOfDocumentsToRetrievePerQuery  This parameter, say K, will determine the
	 *    top-K documents per query to take into account for calculating the score
	 * @param collectionSizeEstimator   To determine the size of the collection
	 * @param relevanceTrainingQueries   Set of queries with their associated scored documents
	 *    (where scores may be provided by different resources)
	 * @param relevanceJudge  For training the relevance estimation
	 * @param topScoreTrainingQueries   Set of training queries (should be subset of
	 *   relevanceTrainingQueries!) with samples, for normalized top-document score estimation
	 * @param queryTopScoreProvider  Provides the top score among all samples for a given
	 *   query -- TODO this should be simply top score of centralized sample collection??
	 */
	public ReDDEUUMHPFLScoreCalculator(
			long numberOfDocumentsToRetrievePerQuery,
			CollectionSizeEstimator collectionSizeEstimator,
			Map<Query, Set<ScoredDocument>> relevanceTrainingQueries,
			BinaryRelevanceJudge relevanceJudge,
			Map<Query, Set<Sample>>  topScoreTrainingQueries,
			QueryTopScoreProvider queryTopScoreProvider
			) throws Exception {
		this.itsNumberOfDocumentsPerQuery = numberOfDocumentsToRetrievePerQuery;
		this.itsCollectionSizeEstimator = collectionSizeEstimator;
		this.itsQueryTopScoreProvider = queryTopScoreProvider;
		this.itsReddeConfiguration = new ReDDEUUMScoreCalculatorConfiguration();
		this.itsRelevanceJudge = relevanceJudge;
		trainRelevanceEstimation(relevanceTrainingQueries, itsRelevanceJudge);
		trainTopDocumentScoreEstimation(topScoreTrainingQueries);
	}
	
	protected void trainRelevanceEstimation(
			Map<Query, Set<ScoredDocument>> relevanceTrainingQueries,
			BinaryRelevanceJudge relevanceJudge)
					throws Exception {
		// Create Weka instances from training queries and documents
		Attribute attScore = new Attribute("Score");
		Attribute attRelevant = new Attribute("Relevance");
		ArrayList<Attribute> attInfo = new ArrayList<Attribute>();
		attInfo.add(attScore);
		attInfo.add(attRelevant);
		Instances trainingset = new Instances("relevanceTraining", attInfo, relevanceTrainingQueries.entrySet().size());
		for (Query q : relevanceTrainingQueries.keySet()) {
			double maxQueryScore = Double.NEGATIVE_INFINITY;
			List<Instance> queryInstances = new ArrayList<Instance>();
			for (ScoredDocument d : relevanceTrainingQueries.get(q)) {
				final double docScore = d.getScore();
				if (maxQueryScore < docScore) {
					maxQueryScore = docScore;
				}
				Instance instance = new DenseInstance(2);
				instance.setValue(attScore, docScore);
				instance.setValue(attRelevant, relevanceJudge.isRelevant(d, q) ? 1 : 0);
				queryInstances.add(instance);
			}
			itsTrainingQueryToMaxDocScore.put(q, maxQueryScore);
			// normalize scores & add to training set
			for (Instance instance : queryInstances) {
				instance.setValue(attScore, instance.value(attScore)/maxQueryScore);
				trainingset.add(instance);
			}
		}
		trainingset.setClass(attRelevant);
		
		// train logistic classifier
		Logistic classifier = new Logistic();
		classifier.setOptions(itsClassifierOptions);
		
		classifier.buildClassifier(trainingset);
		final double[][] coefficients = classifier.coefficients();
		assert coefficients.length == 2
				: "Single score parameter + constant feature: we expect 2 coefficients";
		assert coefficients[0].length == 1
				: "Single classification dimension expected";
		
		// output model parameters		
		itsReddeConfiguration.itsRelevanceParameterA = coefficients[0][0];
		itsReddeConfiguration.itsRelevanceParameterB = coefficients[1][0];
	}
	
	protected void trainTopDocumentScoreEstimation(Map<Query, Set<Sample>> topScoreTrainingQueries) throws Exception {
		// Create Weka instances from training queries and documents
		Attribute attCollectionScore1 = new Attribute("CollectionScore1");
		Attribute attSampleScore1 = new Attribute("SampleScore1");
		Attribute attSampleScore2 = new Attribute("SampleScore2");
		ArrayList<Attribute> attInfo = new ArrayList<Attribute>();
		attInfo.add(attCollectionScore1);
		attInfo.add(attSampleScore1);
		attInfo.add(attSampleScore2);
		Instances trainingset = new Instances("scoreTraining", attInfo, topScoreTrainingQueries.entrySet().size());
		for (Query q : topScoreTrainingQueries.keySet()) {
			assert itsTrainingQueryToMaxDocScore.containsKey(q)
				: "Query should have been seen during relevance training";
			final double scoreNormalization = itsTrainingQueryToMaxDocScore.get(q);
			for (Sample sample : topScoreTrainingQueries.get(q)) {
				List<ScoredDocument> x = sample.getCollection().search(q, 1);
				assert x.size() >= 1 : "We expect 1 top document from collection";
				final double collectionScore1 = x.get(0).getScore();
				
				x = sample.search(q, 2);
				assert x.size() >= 2 : "We expect 2 top documents from sample";
				final double sampleScore1 = x.get(0).getScore();
				final double sampleScore2 = x.get(1).getScore();
								
				Instance instance = new DenseInstance(3);
				instance.setValue(attCollectionScore1, collectionScore1/scoreNormalization);
				instance.setValue(attSampleScore1, sampleScore1); // normalized doc score is estimated from UNNORMALIZED sample scores!
				instance.setValue(attSampleScore2, sampleScore2);
				trainingset.add(instance);
			}
		}
		trainingset.setClass(attCollectionScore1);
		
		// train logistic classifier
		Logistic classifier = new Logistic();
		classifier.setOptions(itsClassifierOptions);
				
		classifier.buildClassifier(trainingset);
		final double[][] coefficients = classifier.coefficients();
		assert coefficients.length == 3
				: "Two sample score parameters + constant feature: we expect 3 coefficients";
		assert coefficients[0].length == 1
				: "Single classification dimension expected";
				
		// output model parameters		
		itsReddeConfiguration.itsTopDocScoreParameterA0 = coefficients[0][0];
		itsReddeConfiguration.itsTopDocScoreParameterA1 = coefficients[1][0];
		itsReddeConfiguration.itsTopDocScoreParameterA2 = coefficients[2][0];
	}
	
	/**
	 * @return   The estimated number of relevant documents in the top-K lists
	 *   of each query q, summed over all queries. (K == itsNumberOfDocumentsPerQuery)
	 */
	@Override
	protected Double _estimateScore(Sample sample, QueryGenerator queryGenerator) {
		final List<PairUnordered<Query, Double>> queryScores = estimateQueryScores(sample, queryGenerator);
		Double sum = 0.0d;
		for (PairUnordered<Query, Double> queryScorePair : queryScores) {
			sum += queryScorePair.getSecond();
		}
		return sum;
	}

	@Override
    /**
	 * @param samples
	 * @param queryGenerator   Not used!
	 */
	public void initialize(List<Sample> samples, QueryGenerator queryGenerator) {
		itsSampleCollection.clear();
		for (Sample s : samples) {
			itsSampleCollection.addSampleDocuments(s);
		}
	}

	/**
	 *  @return The estimated *absolute* number of relevant documents that can be found in the 
	 *   top-K results for all queries issued against the sample's corresponding collection
	 */
	@Override
	public List<PairUnordered<Query, Double>> estimateQueryScores(
			Sample sample, QueryGenerator queryGenerator) {
		if (!itsSampleCollection.contains(sample)) {
			throw new IllegalArgumentException(this.getClass().getSimpleName() 
					+ " does not contain the given sample (from collection "
					+ sample.getCollection().getId() + "). Make sure to call "
					+ this.getClass().getSimpleName() + ".initialize(...) first.");
		}
		
		final List<Query> queryList = queryGenerator.generateQueries(sample);
		List<PairUnordered<Query, Double>> resultScoreList = new ArrayList<PairUnordered<Query,Double>>(queryList.size());
		
		final long numberOfDocumentsToConsider
			= Math.max(itsCollectionSizeEstimator.getCollectionSizeEstimate(sample.getCollection()),
					itsNumberOfDocumentsPerQuery);
		for (Query q: queryList) {
			double queryScore = 0.0d;
			ReDDEUUMQueryRelevanceScorer rsc
				= new ReDDEUUMQueryRelevanceScorer(
						sample,
						itsSampleCollection,
						q,
						itsNumberOfDocumentsPerQuery,
						itsCollectionSizeEstimator, 
						itsReddeConfiguration,
						itsQueryTopScoreProvider.getTopScore(q));
			for (long i = 0; i < numberOfDocumentsToConsider; ++i) {
				queryScore += rsc.getDocumentAtRankRelevanceScore(i);
			}
			resultScoreList.add(new PairUnordered<Query, Double>(q, queryScore));
		}
		return resultScoreList;
	}

	@Override
	protected void _reset() {
		itsSampleCollection.clear();
		itsTrainingQueryToMaxDocScore.clear();
	}

	
}
