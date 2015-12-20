package edu.cs.columbia.iesrcsel.collection.estimation.impl.poolbased;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cs.columbia.iesrcsel.collection.estimation.UsefulDocumentCountEstimator;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.CollisionCounter;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.documentsampler.DocumentSampler;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.ideestimator.InverseDocumentDegreeEstimator;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.querysampler.QuerySampler;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.stoppingcondition.StoppingCondition;
import edu.cs.columbia.iesrcsel.execution.logger.CostLogger;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.utils.math.OutlierDetection;
import edu.cs.columbia.iesrcsel.utils.math.OutlierDetection.OutlierDetectionResult;

public class UsefulDocumentCountBiasedEstimatorRBAverage extends UsefulDocumentCountEstimator {

	private QuerySampler qs;
	private StoppingCondition sc;
	private long maxNumberOfDocuments;
	private InverseDocumentDegreeEstimator idee;
	private DocumentSampler ds;
	private double currentEstimate;
	private double fLimLow;
	private double fLimHigh;
	private boolean itsRemoveOutliers;

	public UsefulDocumentCountBiasedEstimatorRBAverage(Map<String, String> params, QuerySampler qs, StoppingCondition sc,  
			InverseDocumentDegreeEstimator idee, DocumentSampler ds, long maxNumberOfDocuments, boolean removeoutliers, double fLimLow, double fLimHigh) {
		super(params);
		this.qs = qs;
		this.sc = sc;
		this.idee = idee;
		this.ds = ds;
		this.maxNumberOfDocuments = maxNumberOfDocuments;
		this.fLimLow = fLimLow;
		this.fLimHigh = fLimHigh;
		this.itsRemoveOutliers = removeoutliers;
	}

	@Override
	public double getNumberOfUsefulDocuments(TextCollection collection,
			InformationExtractionSystem ie, CollisionCounter collisionCounter, CostLogger cl) {

		cl.setCurrentStatus("Estimating");

		
		Set<Query> sampledqueries = new HashSet<Query>();
		
		currentEstimate = Double.NaN;

		double pw,ide;

		qs.initialize(collection,ie);

		idee.initalize(ie,qs);

		double sum = 0;

		double WSE = 0;

		double round = 0.0;
		
		List<Double> toAvgSum = new ArrayList<Double>();
		List<Double> toAvgWSE = new ArrayList<Double>();
		List<Double> ftoAvgSum = new ArrayList<Double>();
		List<Double> ftoAvgWSE = new ArrayList<Double>();
		
		while (!sc.stopSamplingProcess()){

			round++;

			if (round % 100 == 0){
				System.out.print(".");
			}

			Query query = qs.sampleQuery();

			if (sampledqueries.contains(query))
				continue;

			sampledqueries.add(query);			

			sc.informSampledQuery(query);

			List<ScoredDocument> docs = collection.search(query, maxNumberOfDocuments);

			if (!docs.isEmpty()){

				Document doc = ds.sampleDocument(docs,ie);

				if (ie.isUseful(doc))
					collisionCounter.addDocument(doc);

				//need to compute ueff without fraction for the query
				
				double sumforq = 0.0;
				double sumforWSE = 0.0;

				pw = qs.getNotNormalizedProbability(query);
				
				for (int doci = 0; doci < docs.size(); doci++){

					ide = idee.estimateInverseDocumentDegree(docs.get(doci));

					sumforWSE += ide /pw;
					
					if (ie.isUseful(docs.get(doci))){

						sumforq += ide/pw;
						
					} //else add 0

				}
				
				toAvgSum.add(sumforq);
				toAvgWSE.add(sumforWSE);
				
				ftoAvgSum.clear();
				ftoAvgWSE.clear();
				
				if (itsRemoveOutliers && toAvgWSE.size()>=3) {
					OutlierDetection od = new OutlierDetection(getFLimLow(),getFLimHigh());
					OutlierDetectionResult odr = od.getOutliers(toAvgWSE);
					for (int i=0; i<toAvgWSE.size();i++) {
						Double val = toAvgWSE.get(i);
						if (!odr.isOutlier(val)) {
							ftoAvgWSE.add(val);
							ftoAvgSum.add(toAvgSum.get(i));
						}
					}
				} else {
					ftoAvgSum.addAll(toAvgSum);
					ftoAvgWSE.addAll(toAvgWSE);
				}
				
				sum = 0;
				WSE = 0;
				
				for (Double val : ftoAvgSum) {
					
					sum += val;

				}
				
				for (Double val : ftoAvgWSE) {
					WSE += val;
				}
				
				currentEstimate = collection.size() * (sum / WSE); 
				
				if (currentEstimate > 0)
					System.out.println("Greater than 0");
				
				sc.informEstimate(currentEstimate);
				
			} 

		}

		System.out.format("\nCollisions: %d, Unique: %d \n", collisionCounter.getCollisions(), collisionCounter.getUniqueDocuments());

		sampledqueries.clear();
		
		return currentEstimate;

	}

	private double getFLimHigh() {
		return fLimHigh;
	}

	private double getFLimLow() {
		return fLimLow;
	}

	@Override
	public double getCurrentNumberOfUsefulDocuments() {
		return currentEstimate;
	}

	@Override
	public void reset() {
		qs.reset();
		sc.reset();
		idee.reset();
		ds.reset();
	}

}
