package edu.cs.columbia.iesrcsel.ranking.generator.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import nl.peterbloem.powerlaws.Discrete;

import edu.cs.columbia.iesrcsel.collection.estimation.CollectionSizeEstimator;
import edu.cs.columbia.iesrcsel.descriptor.generator.DescriptorGenerator;
import edu.cs.columbia.iesrcsel.descriptor.generator.impl.SampleBasedDescriptorGenerator;
import edu.cs.columbia.iesrcsel.descriptor.generator.impl.ShrinkageDescriptorGenerator;
import edu.cs.columbia.iesrcsel.model.DatabasesHierarchy;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Category;
import edu.cs.columbia.iesrcsel.model.impl.Descriptor;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.query.generator.QueryGenerator;
import edu.cs.columbia.iesrcsel.ranking.generator.RankingGenerator;
import edu.cs.columbia.iesrcsel.ranking.generator.impl.classification.CombinationGenerator;
import edu.cs.columbia.iesrcsel.ranking.generator.impl.classification.ResourceSelectionAlgorithm;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGenerator;
import edu.cs.columbia.iesrcsel.utils.MapBasedComparator;
import edu.cs.columbia.iesrcsel.utils.PairUnordered;

public class ClassificationAwareRankingGenerator extends RankingGenerator {

	private SampleGenerator querySampleGenerator;
	private ResourceSelectionAlgorithm resourceSelectionAlgorithm;
	private SampleGenerator descriptorSampleGenerator;
	private DatabasesHierarchy dbH;
	private CollectionSizeEstimator collectionSizeEstimator;
	private int numberOfProbesforEstimation;
	private DescriptorGenerator<Category,Category> descriptorCategoryGenerator;

	public ClassificationAwareRankingGenerator(SampleGenerator querySampleGenerator, 
			ResourceSelectionAlgorithm resourceSelectionAlgorithm, SampleGenerator descriptorSampleGenerator, DatabasesHierarchy dbH, CollectionSizeEstimator collectionSizeEstimator, DescriptorGenerator<Category,Category> descriptorCategoryGenerator, int numberOfProbesforEstimation){
		
		this.querySampleGenerator = querySampleGenerator;
		
		this.resourceSelectionAlgorithm = resourceSelectionAlgorithm;
		
		this.descriptorSampleGenerator = descriptorSampleGenerator;
		
		this.dbH = dbH;
		
		this.collectionSizeEstimator = collectionSizeEstimator;
		
		this.descriptorCategoryGenerator = descriptorCategoryGenerator;
		
		this.numberOfProbesforEstimation = numberOfProbesforEstimation;
		
	}
	
	@Override
	public Map<PairUnordered<Query, TextCollection>, Double>  _estimateQueryCollectionScores(Set<TextCollection> collections,
			InformationExtractionSystem extractionSystem, QueryGenerator queryGenerator) {

		DescriptorGenerator<TextCollection,Sample> d = new SampleBasedDescriptorGenerator();
		
		ShrinkageDescriptorGenerator s = new ShrinkageDescriptorGenerator(dbH, d, descriptorSampleGenerator, descriptorCategoryGenerator);
		
		Map<TextCollection, Double> scs = new HashMap<TextCollection, Double>();
		
		Map<PairUnordered<Query, TextCollection>, Double> ret = new HashMap<PairUnordered<Query,TextCollection>, Double>();
		
		for (TextCollection collection : collections) {
			
			List<Query> queries = queryGenerator.generateQueries(querySampleGenerator.generateSample(collection));

			Map<Query, Double> scores = new HashMap<Query, Double>();
			
			for (Query query : queries) {
				
				Descriptor<TextCollection> e = d.generateDescriptor(descriptorSampleGenerator.generateSample(collection));
				
				if (useShrinkage(query,collection,e, descriptorSampleGenerator.generateSample(collection))){
					e = s.generateDescriptor(collection);
				} 
					
				ret.put(new PairUnordered<Query, TextCollection>(query, collection), resourceSelectionAlgorithm.calculateScore(query,e));
				
			}
			
		}
		
		return ret;
		
	}

	private Double combineScores(Map<Query, Double> scores) {
		// FIXME Might need to change it to a more sophisticated strategy, unless it returns the # of documents
		
		double score = 0.0;
		
		for (Double value : scores.values()) {
			
			score += value;
		}
		
		return score;
		
	}

	private boolean useShrinkage(Query query, TextCollection collection, Descriptor<TextCollection> descriptor, Sample baseSample) {
		
		String[] words = query.getTerms();
		
		double[] minValues = new double[words.length];
		
		double estSize = collectionSizeEstimator.getCollectionSizeEstimate(collection);
		
		for (int i = 0; i < words.length; i++) {
			minValues[i] = descriptor.getProbabilityOfWord(words[i])*estSize;
		}
		
		CombinationGenerator combinationGenerator = new CombinationGenerator(words.length, minValues);
		
		List<Double> scores = new ArrayList<Double>();
		
		List<Double> probabilities = new ArrayList<Double>();
		
		double collectionSize = collectionSizeEstimator.getCollectionSizeEstimate(collection);
		
		double B = calculateB(baseSample.getDocumentsAsAdded(), numberOfProbesforEstimation, collectionSize);
		
		double gamma = 1.0/B - 1.0;
		
		while (combinationGenerator.hasNext()){
			
			double[] combination = combinationGenerator.next();
			
			double prob = calculateProbabilities(collection, descriptor, combination, words, gamma, collectionSize, baseSample);
			
			probabilities.add(prob);
			
			scores.add(calculateScore(words,combination,collectionSize,descriptor));
			
			probabilities.add(prob);
			
		}
	
		if (resourceSelectionAlgorithm.calculateStandardDeviation(scores) > resourceSelectionAlgorithm.calculateMean(scores)){
			return true;
		}
		
		return false;
		
	}

	

	private double calculateB(List<Document> documents, int numberOfProbesforEstimation, double collectionSize) {
		
		int after = documents.size() / numberOfProbesforEstimation;
		
		List<Document> docs = new ArrayList<Document>();
		
		Map<String,Integer> termsMap = new HashMap<String,Integer>();
		
		List<PairUnordered<Double, Double>> PB = new ArrayList<PairUnordered<Double, Double>>(numberOfProbesforEstimation);
		
		for (int i = 0; i < documents.size(); i++) {
			
			docs.add(documents.get(i));
			
			Map<String,Integer> terms = documents.get(i).getTermFreqMap();
			
			for (Entry<String,Integer> entry : terms.entrySet()) {
				
				Integer freq = termsMap.get(entry.getKey());
				
				if (freq == null){
					
					freq = 0;
					
				}
				
				termsMap.put(entry.getKey(), freq+entry.getValue());
			}
			
			if (i > 0 && i % after == 0){
				
				PB.add(calculatePB(termsMap));
				
			}
			
		}
		
		PairUnordered<Double, Double> B1B2 = calculateB1B2(PB);
		
		return B1B2.getFirst() * Math.log(collectionSize) + B1B2.getSecond();
		
	}

	private PairUnordered<Double, Double> calculateB1B2(
			List<PairUnordered<Double, Double>> pB) {
		
		SimpleRegression regression = new SimpleRegression();
		
		for (int i = 0; i < pB.size(); i++) {
			regression.addData(Math.log(i+1), pB.get(i).getSecond());
		}
		
		return new PairUnordered<Double, Double>(regression.getSlope(), regression.getIntercept());
		
	}

	private PairUnordered<Double, Double> calculatePB(
			Map<String, Integer> termsMap) {
		
		//Returns P and B as in f = Pr^B
		
		Discrete d = Discrete.fit(termsMap.values()).fit();
		
		return new PairUnordered<Double, Double>((double)d.xMin(), d.exponent());
		
	}

	private Double calculateScore(String[] words, double[] combination,
			double collectionSize, Descriptor<TextCollection> descriptor) {
		
		double[] newCombination = new double[combination.length];
		
		for (int i = 0; i < combination.length; i++) {
			newCombination[i] = combination[i]/collectionSize;
		}
		
		return resourceSelectionAlgorithm.calculateScore(words, newCombination, descriptor, collectionSize);
		
	}

	private Double calculateProbabilities(TextCollection collection,
			Descriptor<TextCollection> descriptor, double[] combination, String[] words, double gamma, double collectionSize, Sample baseSample) {
		
		double score = 0.0;

		double S = baseSample.size();
		
		for (int k = 0; k < words.length; k++) {
			
			double dk = combination[k];
			
			double sk = descriptor.getProbabilityOfWord(words[k])*S;
			
			score*= (Math.pow(dk, gamma)*Math.pow(dk/collectionSize, sk)*Math.pow(1.0 - dk/collectionSize, S - sk))/(calculateDenominator(gamma, collectionSize, sk, S));
		
		}
		
		return score;
		
	}

	private double calculateDenominator(double gamma, double D, double sk,
			double S) {
		
		double ret = 0.0;
		
		for (int i = 0; i < D; i++) {
			
			ret += Math.pow(i, gamma)*(Math.pow(i/D, sk))*(Math.pow(1-i/D, S-sk));
			
		}
		
		return ret;
		
	}

	

}
