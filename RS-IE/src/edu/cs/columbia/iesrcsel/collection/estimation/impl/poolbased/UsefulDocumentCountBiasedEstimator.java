package edu.cs.columbia.iesrcsel.collection.estimation.impl.poolbased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;

import edu.cs.columbia.iesrcsel.collection.estimation.UsefulDocumentCountEstimator;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.CollisionCounter;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.documentsampler.DocumentSampler;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.ideestimator.InverseDocumentDegreeEstimator;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.poolsizefractionestimator.PoolSizeFractionEstimator;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.querysampler.QuerySampler;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.stoppingcondition.StoppingCondition;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.usefulnessCalculator.QueryUsefulnessCalculator;
import edu.cs.columbia.iesrcsel.execution.logger.CostLogger;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.utils.math.OutlierDetection;
import edu.cs.columbia.iesrcsel.utils.math.OutlierDetection.OutlierDetectionResult;

public class UsefulDocumentCountBiasedEstimator extends UsefulDocumentCountEstimator {

	private QuerySampler qs;
	private StoppingCondition sc;
	private long maxNumberOfDocuments;
	private QueryUsefulnessCalculator quc;
	private InverseDocumentDegreeEstimator idee;
	private DocumentSampler ds;
	private double currentEstimate;
	private PoolSizeFractionEstimator psfe;
	private double fLimLow;
	private double fLimHigh;
	private boolean itsRemoveOutliers;

	public UsefulDocumentCountBiasedEstimator(Map<String, String> params, QuerySampler qs, StoppingCondition sc, QueryUsefulnessCalculator quc, 
			InverseDocumentDegreeEstimator idee, DocumentSampler ds, PoolSizeFractionEstimator psfe, long maxNumberOfDocuments, boolean removeoutliers, double fLimLow, double fLimHigh) {
		super(params);
		this.qs = qs;
		this.sc = sc;
		this.quc = quc;
		this.idee = idee;
		this.ds = ds;
		this.psfe = psfe;
		this.maxNumberOfDocuments = maxNumberOfDocuments;
		this.fLimLow = fLimLow;
		this.fLimHigh = fLimHigh;
		this.itsRemoveOutliers = removeoutliers;
	}

	@Override
	public double getNumberOfUsefulDocuments(TextCollection collection,
			InformationExtractionSystem ie, CollisionCounter collisionCounter, CostLogger cl) {

		Map<Query,Set<Document>> samplededges = new HashMap<Query, Set<Document>>();
		
		currentEstimate = Double.NaN;

		double pw,zw,w,qu,ide,fractionps;

		qs.initialize(collection,ie);

		cl.setCurrentStatus("ComputingFraction");
		
		fractionps = psfe.calculateFraction(qs.getQueries(),collection,ie, (int)maxNumberOfDocuments);

		cl.setCurrentStatus("Estimating");
		
		idee.initalize(ie,qs);

		zw = qs.normalizationValue();

		w = qs.getBiasWeight();

		double sum = 0;

		double round = 0;

		List<Double> toAvg = new ArrayList<Double>();

		List<Double> ftoAvg = new ArrayList<Double>();
		
		while (fractionps !=0 && !sc.stopSamplingProcess()){

			round++;

			if (round % 100 == 0){
				System.out.print(".");
			}

			Query query = qs.sampleQuery();

			sc.informSampledQuery(query);

			Set<Document> sd = samplededges.get(query);
			if (sd == null){
				sd = new HashSet<Document>();
				samplededges.put(query,sd);
			}
			
			List<ScoredDocument> docs = collection.search(query, maxNumberOfDocuments);

			if (!docs.isEmpty()){

				Document doc = ds.sampleDocument(docs,ie);

				if (ie.isUseful(doc)){

					if (sd.contains(doc))
						continue;
					
					sd.add(doc);
					
					psfe.informQuery(true);
					
					collisionCounter.addDocument(doc);

					ide = idee.estimateInverseDocumentDegree(doc);

					pw = qs.getNotNormalizedProbability(query);

					qu = quc.calculateQueryUsefulness(qs,query,docs,ie);

					double avg = ((qu * ide)/pw);
					
					toAvg.add(avg);
					
					fractionps = psfe.calculateFraction(qs.getQueries(),collection,ie, (int)maxNumberOfDocuments);
					
					ftoAvg.clear();
										
					if (itsRemoveOutliers && toAvg.size()>=3) {
						OutlierDetection od = new OutlierDetection(getFLimLow(),getFLimHigh());
						OutlierDetectionResult odr = od.getOutliers(toAvg);
						for (Double val : toAvg) {
							if (!odr.isOutlier(val)) {
								ftoAvg.add(val);
							}
						}
					} else {
						ftoAvg.addAll(toAvg);
					}
					
					sum = 0;
					double count = ftoAvg.size();
					
					for (Double val : ftoAvg) {
						
						sum += val;

					}
					
					sum *= zw * fractionps;

					currentEstimate = sum / count; 

					sc.informEstimate(currentEstimate);

				}else{

					psfe.informQuery(false);

				}

			} else {
				psfe.informQuery(false);
			}

		}

		System.err.println("Fraction of queries in Pool Size:" + fractionps);
		System.out.format("\nCollisions: %d, Unique: %d \n", collisionCounter.getCollisions(), collisionCounter.getUniqueDocuments());

		samplededges.clear();
		
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
		quc.reset();
		idee.reset();
		ds.reset();
		psfe.reset();
	}

}
