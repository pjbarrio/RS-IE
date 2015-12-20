package edu.cs.columbia.iesrcsel.collection.estimation.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.StatUtils;

import edu.cs.columbia.iesrcsel.collection.estimation.CollectionSizeEstimator;
import edu.cs.columbia.iesrcsel.descriptor.generator.impl.SampleBasedDescriptorGenerator;
import edu.cs.columbia.iesrcsel.model.impl.Descriptor;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGenerator;

public class SampleResampleCollectionSizeEstimator extends
		CollectionSizeEstimator {
	
	private SampleGenerator sampleGenerator;
	private Map<TextCollection, Double> itsSizeEstimate = new HashMap<TextCollection, Double>();
	private Map<TextCollection, Double> itsSizeEstimateStdev = new HashMap<TextCollection, Double>();
	private Map<TextCollection, Double> itsSizeEstimateSampleCount = new HashMap<TextCollection, Double>();

	final static double CI95FACTOR = 1.96d; 
	
	
	public SampleResampleCollectionSizeEstimator(Map<String,String> params, SampleGenerator sampleGenerator) {
		super(params);
		this.sampleGenerator = sampleGenerator;
	}
	
	@Override
	public long getCollectionSizeEstimate(TextCollection collection) {
		Double estimate = itsSizeEstimate.get(collection);
		if (estimate == null) {
			estimate = calculateCollectionSizeEstimate(collection);
		}
		return Math.round(estimate);
	}
	
	public double getCollectionSizeEstimateUnrounded(TextCollection collection) {
		Double estimate = itsSizeEstimate.get(collection);
		if (estimate == null) {
			estimate = calculateCollectionSizeEstimate(collection);
		}
		return estimate;
	}
	
	public double getCollectionSizeEstimate95CIhalfwidth(TextCollection collection) {
		Double stdev = itsSizeEstimateStdev.get(collection);
		if (stdev == null) {
			calculateCollectionSizeEstimate(collection);
			stdev = itsSizeEstimateStdev.get(collection);
		}
		final double sampleCount = itsSizeEstimateSampleCount.get(collection);
		final double ciHalfWidth = CI95FACTOR * stdev / Math.sqrt(sampleCount);
		return ciHalfWidth;
	}
	
	private double calculateCollectionSizeEstimate(TextCollection collection) {
		final Sample sample = sampleGenerator.generateSample(collection);
		final double sampleSize = sample.size();
		final Set<String> tset = getTerms(sample);
		double[] estimates = new double[tset.size()];
		int i = 0;
		// TODO maybe process only part of tset if stdev is small enough?
		// e.g., if 95% confidence interval width is <1% of estimate average?
		// thus if (1.96 * stdev/sqrt(n) * 2) / avg < 0.01
		// NOTE: [Ipeirotis & Gravano, ToIS 2008] mention to use 5 resampling queries
		for (String term : tset) {
			// FIXME check if term is stopword?
			final double sampleDocFreq = (double) sample.getDocumentFrequency(term);
			assert sampleDocFreq <= sample.size();
			final double collectionDocFreq = (double) collection.getDocumentFrequency(term);
			assert sampleDocFreq > 0.0d : "Sample should contain documents containing term <" + term + ">";
			assert collectionDocFreq > 0.0d : "Collection should contain documents containing term <" + term + ">";
			final double termEstimate = collectionDocFreq * (sampleSize / sampleDocFreq);
			assert termEstimate < Double.POSITIVE_INFINITY : "Term estimate should NOT be infinity";
			assert termEstimate >= collectionDocFreq : "Estimate can not be smaller than absolute document frequency";
			estimates[i++] = termEstimate;
//			System.out.println("### Collection size estimate -- avg = " + StatUtils.mean(estimates, 0, i)
//					+ " (stdev == " + Math.sqrt(StatUtils.variance(estimates, 0, i)) + ")"
//					+ " -- for term <" + term + ">: " + termEstimate );
		}
		final double avg = StatUtils.mean(estimates);
		final double stdev = Math.sqrt(StatUtils.variance(estimates, avg));
		itsSizeEstimate.put(collection, avg);
		itsSizeEstimateStdev.put(collection, stdev);
		itsSizeEstimateSampleCount.put(collection, (double) i);
		return avg;
	}
	
	protected Set<String> getTerms(Sample sample) {
		// DONE decide how to tackle this; [Si & Callan, SIGIR 2003] mention to get the 
		// terms from a resource description 		
		SampleBasedDescriptorGenerator x = new SampleBasedDescriptorGenerator();
		Descriptor<TextCollection> descr = x.generateDescriptor(sample);
		return descr.getTerms();
	}

}
