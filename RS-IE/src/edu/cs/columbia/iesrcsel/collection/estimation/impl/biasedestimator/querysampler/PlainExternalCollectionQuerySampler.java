package edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.querysampler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.random.MersenneTwister;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class PlainExternalCollectionQuerySampler extends QuerySampler {

	protected String prefixFolder;
	protected String trainingcollection;
	private double maxWeight;
	private Map<String,Double> termWeights;
	private double biasedWeight;
	private double normalizationWeight;
	private List<String> termList;
	private MersenneTwister rg;
	private boolean relationSpecific;
	
	public PlainExternalCollectionQuerySampler(Map<String,String> params,String prefixFolder, String trainingCollection, double biasedWeight, boolean relationSpecific){
		super(params);
		this.prefixFolder = prefixFolder; // e.g., /proj/dbNoBackup/pjbarrio/workspacedb-pc02/ResourceSelectionIE/data/biasedestimator/
		this.trainingcollection = trainingCollection; //e.g., TREC
		this.biasedWeight = biasedWeight;
		this.rg = new MersenneTwister();
		this.relationSpecific = relationSpecific;
		reset();
	}
	
	
	@Override
	public void initialize(TextCollection collection,
			InformationExtractionSystem ie) {
		
		// termMapUseful.Indictment-Arrest-Trial.SSK.TREC.ser
		
		Map<String,Integer> usefuls = (Map<String,Integer>)SerializationHelper.deserialize(getUsefulFileName(ie));
		
		Map<String,Integer> useless = (Map<String,Integer>)SerializationHelper.deserialize(getUselessFileName(ie));
		
		for (Entry<String,Integer> entry : usefuls.entrySet()) {
			
			Integer uselessFreq = useless.remove(entry.getKey());
			
			if (uselessFreq == null)
				uselessFreq = 0;
			
			double sum = biasedWeight * entry.getValue() + uselessFreq;
			
			termWeights.put(entry.getKey(), sum);
			
			if (sum > maxWeight)
				maxWeight = sum;
			
			normalizationWeight += sum;
			
		}
	
		usefuls.clear();
		
		if (!relationSpecific){

		for (Entry<String,Integer> entry : useless.entrySet()) {
			
			double sum = entry.getValue();
			
			termWeights.put(entry.getKey(), sum);
			
			if (sum > maxWeight)
				maxWeight = sum;
			
			normalizationWeight += sum;
			
		}
		}

		useless.clear();
		
		termList.addAll(termWeights.keySet());
		
	}

	protected String getUselessFileName(InformationExtractionSystem ie) {
		return prefixFolder + "termMapUseless." + 
				ie.getRelation() + "." + ie.getExtractor() + "." + trainingcollection + ".ser";
	}


	protected String getUsefulFileName(InformationExtractionSystem ie) {
		return prefixFolder + "termMapUseful." + 
				ie.getRelation() + "." + ie.getExtractor() + "." + trainingcollection + ".ser";
	}


	@Override
	public Query sampleQuery() {
		
		boolean accept = false;
		
		String term = null;
		
		while (!accept){
			
			double u = rg.nextDouble();
			
			term = termList.get(rg.nextInt(termList.size()));
			
			accept = (u < (termWeights.get(term) / maxWeight)); 
			
		}
		
		return new Query(term);
	}

	@Override
	public double normalizationValue() {
		return normalizationWeight;
	}

	@Override
	public double getBiasWeight() {
		return biasedWeight;
	}

	@Override
	public double getNotNormalizedProbability(Query query) {
		return termWeights.get(query.getTerms()[0]);
	}

	@Override
	public void reset() {
		maxWeight = 0.0;
		if (termWeights != null)
			termWeights.clear();
		termWeights = new HashMap<String, Double>();
		if (termList != null)
			termList.clear();
		termList = new ArrayList<String>();
		normalizationWeight = 0.0;
	}


	@Override
	public List<String> getQueries() {
		return new ArrayList<String>(termWeights.keySet());
	}

}
