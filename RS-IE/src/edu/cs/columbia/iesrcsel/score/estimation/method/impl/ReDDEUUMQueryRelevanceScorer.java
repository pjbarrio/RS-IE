package edu.cs.columbia.iesrcsel.score.estimation.method.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cs.columbia.iesrcsel.collection.estimation.CollectionSizeEstimator;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.SampleCollection;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;

/**
 * Class to calculate scores for a Sample against a given (single!) Query
 *
 * @author Chris Develder
 *
 */
public class ReDDEUUMQueryRelevanceScorer {
	protected TextCollection itsCollection = null;
	protected Sample itsSample = null;
	protected Query itsQuery = null;
	protected long itsNumberOfDocumentsPerQuery = 0;
	protected CollectionSizeEstimator itsCollectionSizeEstimator = null;
	protected ReDDEUUMScoreCalculatorConfiguration itsConfiguration = null;
	protected double itsTopQueryDocumentScore = 1.0d;
	
	protected List<Double> itsInterpolationRanks = null; // starting from ScalingFactor/2 ... Does *not* contain the top-document score 
	protected Map<Double, Double> itsInterpolationRelevanceScores = new HashMap<Double, Double>(); // score = probability of relevance
	protected double itsTopDocumentScore = 0.0d;
	protected double itsInterpolB0 = Double.NaN;
	protected double itsInterpolB1 = Double.NaN;
	
	protected List<ScoredDocument> itsSampleDocuments = null;
	protected Map<Document, Double> itsSampleDocumentScores = new HashMap<Document, Double>();
	
	protected SampleCollection itsSampleCollection = null;
	
	public ReDDEUUMQueryRelevanceScorer(Sample s,
			SampleCollection allSamples,
			Query q,
			long numberOfDocumentsPerQuery,
			CollectionSizeEstimator collectionSizeEstimator,
			ReDDEUUMScoreCalculatorConfiguration reddeConfig,
			double maxDocScoreForQueryAcrossCollections) {
		itsSample = s;
		itsSampleCollection = allSamples;
		itsCollection = s.getCollection();
		itsQuery = q;
		itsNumberOfDocumentsPerQuery = numberOfDocumentsPerQuery;
		itsCollectionSizeEstimator = collectionSizeEstimator;
		itsConfiguration = reddeConfig;
		itsTopQueryDocumentScore = maxDocScoreForQueryAcrossCollections;
		init();
	}

	protected void init() {
		final double collectionSize = itsCollectionSizeEstimator.getCollectionSizeEstimate(itsCollection);
		final double scalingFactor = collectionSize/((double) itsSample.size());

		// We will need scores for actual collection documents ranked 1..itsNumberOfDocumentsPerQuery
		// Thus, we need only 1/scalingFactor of that number from the sample, +1 
		int numberOfSampleDocumentsRequired = (int) Math.round(itsNumberOfDocumentsPerQuery/scalingFactor) + 1;
		if (numberOfSampleDocumentsRequired < 2) {
			numberOfSampleDocumentsRequired = 2; // need at least 2
		}
		
		itsSampleDocumentScores.clear();
		itsSampleDocuments = itsSampleCollection.search(itsQuery, itsSample, numberOfSampleDocumentsRequired);
		// itsSampleDocuments = itsSample.search(itsQuery, numberOfSampleDocumentsRequired);
		final int numberOfSampleDocumentsRetrieved = itsSampleDocuments.size();
		
		itsInterpolationRanks = new ArrayList<Double>(itsSampleDocuments.size()+1);
		itsInterpolationRelevanceScores.clear();
		
		assert itsSampleDocuments.size() >= 2; 
		itsTopDocumentScore = getTopDocumentScoreEstimate(itsSampleDocuments.get(0), itsSampleDocuments.get(1));
		
		itsInterpolB1 = Math.log(getRelevanceScore(itsSampleDocuments.get(0)))/(scalingFactor*0.5d - 1.0d);
		itsInterpolB0 = Math.log(itsConfiguration.itsTopDocScoreParameterA0) - itsInterpolB1;
		
		for (int i = 0; i < numberOfSampleDocumentsRetrieved; ++i) {
			final double rank = (i + 0.5) * scalingFactor;
			addInterpolationScore(rank, getRelevanceScore(itsSampleDocuments.get(i)));
		}
		// We need to be able to interpolate until the very last document of the entire collection; 
		// We assume the relevance score to be the same for all remaining documents, thus
		// repeat the last score.
		// (This was not mentioned explicitly in [1], I believe.)
		addInterpolationScore(Math.ceil(collectionSize),
				getRelevanceScore(itsSampleDocuments.get(numberOfSampleDocumentsRetrieved - 1)));
	}
	
	/**
	 * Return estimated top document score; relies on itsTopDocScoreParameterA*
	 * @param sampleDocs
	 * @return
	 */
	private double getTopDocumentScoreEstimate(ScoredDocument ds1, ScoredDocument ds2) {
		double x = Math.exp(itsConfiguration.itsTopDocScoreParameterA0
				+ itsConfiguration.itsTopDocScoreParameterA1 * ds1.getScore()
				+ itsConfiguration.itsTopDocScoreParameterA2 * ds2.getScore());
		return x/(1+x);
	}

	protected void addInterpolationScore(Double rank, Double score) {
		itsInterpolationRanks.add(rank);
		itsInterpolationRelevanceScores.put(rank, score);
	}
	
	/**
	 * Gets the estimated document relevance for a given query using the learned
	 * relevance function
	 * @param d  document
	 * @param q  query
	 * @return
	 */
	protected double getRelevanceScore(Document sampledoc) {
		assert itsSampleDocumentScores.containsKey(sampledoc)
			: "Only applicable for sample documents";
		final double x 
			= Math.exp(itsConfiguration.itsRelevanceParameterA
					+ itsConfiguration.itsRelevanceParameterB
					* getNormalizedRetrievalScore(sampledoc));
		return x/(1.0d + x);
	}
	
	protected double getNormalizedRetrievalScore(Document sampledoc) {
		double score = itsSampleDocumentScores.get(sampledoc);
		return score/itsTopQueryDocumentScore;
	}
	
	/**
	 * 
	 * @param rank  Rank in the query result from the sample's complete document collection
	 * @return Score for document at given rank, where score is an estimate of the
	 *         probability of relevance
	 */
	public double getDocumentAtRankRelevanceScore(long rank) {
		if (rank < 1) {
			throw new IllegalArgumentException("Rank should be at least 1 (you used rank ==" + rank + ")");
		}
		if (rank > itsInterpolationRanks.get(itsInterpolationRanks.size() - 1)) {
			throw new IllegalArgumentException("Rank should be within the range of possible ranks for collection "
					+ itsCollection.getId() + ", which is estimated to contain "
					+ itsInterpolationRanks.get(itsInterpolationRanks.size() - 1)
					+ " documents (you used rank == " + rank + ")");
		}
		
		double intRankA = 1.0d;
		// Optional TODO: we use linear search -- could be made more efficient if we consider very large ranks
		for (Double intRankB : itsInterpolationRanks) {
			if (rank <= intRankB) {
				if (intRankA > 1.0d) {
					// linear interpolation
					final double scoreA = itsInterpolationRelevanceScores.get(intRankA);
					final double scoreB = itsInterpolationRelevanceScores.get(intRankB);
					assert (intRankB > intRankA)
						: "Interpolation ranks should be increasing";
					return scoreA + (rank - intRankA)/(intRankB - intRankA) * (scoreB - scoreA);
				} else {
					return Math.exp(itsInterpolB0 + itsInterpolB1 * rank);
				}
			}
			intRankA = intRankB;
		}
		return Double.NaN; // error: we should have found an interpolated value!
	}
	
}
