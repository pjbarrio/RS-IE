package edu.cs.columbia.iesrcsel.collection.estimation.impl.poolbased;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cs.columbia.iesrcsel.collection.estimation.UsefulDocumentCountEstimator;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.biasedestimator.CollisionCounter;
import edu.cs.columbia.iesrcsel.execution.logger.CostLogger;
import edu.cs.columbia.iesrcsel.model.collection.DeepWebLuceneCollection;
import edu.cs.columbia.iesrcsel.model.collection.IndriCollection;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.extractor.impl.CachedInformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.utils.MapBasedComparator;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class UsefulDocumentCountStratifiedEstimatorUsefulSize extends UsefulDocumentCountEstimator {

	private Map<Integer, List<Query>> strats;
	private Set<Query> queriesU;
	private int r1;
	private long k;
	private int r2;
	private static Map<String,TextCollection> globalCollections = new HashMap<String, TextCollection>();
	private TextCollection globalCollection;
	private double currentEstimation;
	private int hcorr;
	private String corrQueryTrainingCollection;
	private String gc;
	private Map<Integer,Double> avgCard;
	private String uncorrelatedQuery;
	private int L;
	private String globalCollectionName;

	public UsefulDocumentCountStratifiedEstimatorUsefulSize(Map<String,String> params, String corrQueryTrainingCollection,
			String uncorrelatedQuery, int L, int r1, int r2, long k, int h, String globalCollection) {

		super(params);
		
		
		this.uncorrelatedQuery = uncorrelatedQuery;
		
		this.L = L;
		
		this.globalCollectionName = globalCollection;
		
		this.gc = globalCollection;
		
		this.globalCollection = getGlobalCollection(gc);
	
		this.corrQueryTrainingCollection = corrQueryTrainingCollection;
		
		//Generate L+1 stratas
		
//		Map<Integer, List<Query>> corStrats = stratify(correlated,L, 0); The correlated
		
		this.r1 = r1;
		
		this.r2 = r2;
		
		this.k = k;
		
		this.hcorr = h;
		
	}

	private Map<Integer, Double> loadAverageCardinalities(
			String uncorrelatedQuery, String globalCollectionName, int l) {
		return (Map<Integer, Double>) SerializationHelper.deserialize("data/queries/AvgCardinalities." + uncorrelatedQuery + "." + globalCollectionName + "." + l +".ser");
	}

	private static TextCollection getGlobalCollection(String gc) {
		
		TextCollection ret = globalCollections.get(gc);
		
		if (ret == null){
			ret =new DeepWebLuceneCollection("TREC", "data/indexes/"+gc+"/tv-"+gc+".idx");
			globalCollections.put(gc, ret);
		}
		
		return ret;
		
	}

	private Map<Integer, List<Query>> loadStratifiedData(
			String uncorrelatedQuery, String globalCollection, int l) {
		
		String fname = "data/queries/Stratas." + uncorrelatedQuery + "." + globalCollection + "." + l +".ser";
		return (Map<Integer, List<Query>>) SerializationHelper.deserialize(fname);
		
	}

	private double computeIntraStatumVariance(List<Double> qvalues) {
		
		double avg = calculateAverage(qvalues);
		
		double sum = 0.0;
		
		for (int i = 0; i < qvalues.size(); i++) {
			
			sum+= qvalues.get(i) - avg;
			
		}
		
		return sum / avg;
	}

	

	private double estimateCardinality(Query query, TextCollection collection) {
		
		return collection.matchingItems(query);
		
	}

	@Override
	public double getNumberOfUsefulDocuments(TextCollection collection,
			InformationExtractionSystem ie,  CollisionCounter collisionCounter, CostLogger cl) {
		
		List<Query> uncorrelated = (List<Query>)SerializationHelper.deserialize("data/queries/"+uncorrelatedQuery+".ser");
		
		this.queriesU = new HashSet<Query>();
		
		
		while (!uncorrelated.isEmpty())
			queriesU.add(uncorrelated.remove(0));
				
		
		Map<Integer, List<Query>> uncorStrats = loadStratifiedData(uncorrelatedQuery,globalCollectionName,L);
		
		strats = new HashMap<Integer, List<Query>>();
		
		strats.putAll(uncorStrats);
		
		avgCard = loadAverageCardinalities(uncorrelatedQuery,globalCollectionName,L);
		
		List<Query> correlated = (List<Query>)SerializationHelper.deserialize("data/biasedestimator/termMapProbabilityUseful." + ie.getRelation() + "."+ie.getExtractor()+"."+corrQueryTrainingCollection+".INDEX.ser");
		
		List<Query> corrQuery = correlated.subList(0, hcorr);
		
		queriesU.addAll(corrQuery);
				
		strats.put(0,corrQuery);
		
		currentEstimation = Double.NaN;
		
		Map<Query,Double> cardMap = new HashMap<Query, Double>();
		
		//Issue all queries that are not correlated (strata > 1) - I know how many correlated I will query.
		
		cl.setCurrentStatus("PilotStep");
		
		Map<Integer,List<Query>> queries = new HashMap<Integer,List<Query>>();
		
		for (int i = 1; i < strats.size(); i++) {
			
			queries.put(i, issueQueries(strats.get(i),r1,collection,cardMap));
			
		}
		
		Map<Integer, Double> varMap = new HashMap<Integer, Double>();
		
		cl.setCurrentStatus("ObtainingIntraStratumVariance");
		
		for (int i = 1; i < strats.size(); i++) {
			
			System.out.println("Obtaining total weight: " + i + " out of: " + strats.size());
			
			List<Double> qvalues = obtainTotalWeight(queries.get(i),collection,cardMap, ie);
			
			varMap.put(i, computeIntraStatumVariance(qvalues));
			
		}		
		
		double den = 0.0;
		
		for (int k = 1; k < strats.size(); k++) {
			den += strats.get(k).size()*varMap.get(k);
		}
		
		queries = new HashMap<Integer,List<Query>>();
		
		cl.setCurrentStatus("IssuingQueriesByIntraStratumVariance");
		
		for (int i = 1; i < strats.size(); i++) {
			
			int r2i = (int)Math.round(r2*strats.get(i).size()*varMap.get(i) / den);
			
			queries.put(i, issueQueries(strats.get(i),r2i,collection,cardMap));
			
		}
		
		List<Double> tvals = new ArrayList<Double>(strats.size());
		
		cl.setCurrentStatus("ObtainingWeightsSecondRound");
		
		for (int k = 1; k < strats.size(); k++) {
			
			//From step 3 onwards.
			
			System.out.println("Obtaining second round total weight: " + k + " out of: " + strats.size());
			
			List<Double> qvalues = obtainTotalWeight(queries.get(k), collection, cardMap, ie);
			
			tvals.add(strats.get(k).size() * calculateAverage(qvalues)); 
			
		}
		
		//Issue the related ones, which are a fixed number.
		
		cl.setCurrentStatus("ComputingRelatedQueries");
		
		queries.put(0, corrQuery);
		
		List<Double> qvalues = obtainTotalWeight(queries.get(0), collection, cardMap, ie);
		
		tvals.add(strats.get(0).size() * calculateAverage(qvalues)); 
		
		cl.setCurrentStatus("CalculatingFinalEstimation");
		
		uncorStrats.clear();
		
		strats.clear();
		
		queriesU.clear();
		
		avgCard.clear();
		
		return calculateSum(tvals);
		
	}

	private List<Query> issueQueries(List<Query> queries, int toSample,
			TextCollection collection, Map<Query, Double> cardMap) {
		
		List<Integer> indexes = new ArrayList<Integer>(queries.size());
		
		for (int j = 0; j < queries.size(); j++) {
			
			indexes.add(j);
			
		}
		
		Collections.shuffle(indexes);
		
		//Issue
		
		List<Query> ret = new ArrayList<Query>(toSample);
		
		for (int j = 0; j < toSample; j++) {
			
			cardMap.put(queries.get(indexes.get(j)), estimateCardinality(queries.get(indexes.get(j)), collection));
			
			ret.add(queries.get(indexes.get(j)));
			
		}
		
		return ret;
		
	}

	private List<Double> obtainTotalWeight(List<Query> queries, TextCollection collection, Map<Query,Double> cardMap, InformationExtractionSystem ie) {
		
		List<Double> qvalues = new ArrayList<Double>(queries.size());
		
		System.out.println("Queryes: " + queries.size());
		
		for (int j = 0; j < queries.size(); j++) {
			
			//Algorithm 4
			
			
			
			List<ScoredDocument> res = collection.search(queries.get(j),this.k);
			
			double sum = computeWeights(res,queries.get(j),collection,cardMap,ie);
			
			//XXX I should store these values... someone needs them!
			
			qvalues.add(sum);
			
		}		
		
		return qvalues;
		
	}

	private double calculateSum(List<Double> values) {
		
		double ret = 0.0;
		
		for (int i = 0; i < values.size(); i++) {
			ret+=values.get(i);
		}
		
		return ret;
	}

	private double calculateAverage(List<Double> values) {
		
		if (values.isEmpty())
			return 0.0;
		
		return calculateSum(values)/values.size();
		
	}

	private double computeWeights(List<ScoredDocument> res, Query q, TextCollection collection, Map<Query, Double> cardMap, InformationExtractionSystem ie) {
		
		double sum = 0.0;
		
		System.out.println("Documents: " + res.size());
		
		for (int k = 0; k < res.size(); k++) {
			
			// Algorithm 4
			
			
			
			double edgeWeight = 0.0;
			
			if (ie.isUseful(res.get(k))){
				
				Set<Query> CX = generateCX(res.get(k), cardMap);
				
				if (CX.contains(q)){
					
					System.err.println(q);
					System.err.println(CX.toString());
					
					List<Query> queries = new ArrayList<Query>(CX);
					
					int h = 0;
					
					Collections.shuffle(queries);
					
					do {
						
						h++;
						
					} while (!queries.isEmpty() && collection.search(queries.remove(0),this.k).contains(res.get(k)));
					
					edgeWeight = (double)h / (double)CX.size();
					
				}else{
					
					List<Query> PX = getMatchingQueries(res.get(k),queriesU);
					
					Map<Query,Double> estcardMap = new HashMap<Query, Double>(PX.size());
					
					for (int l = 0; l < PX.size(); l++) {
						estcardMap.put(PX.get(l), Math.abs(estimateCardinality(PX.get(l),globalCollection)));
					}
					
					Collections.sort(PX, new MapBasedComparator<Query>(estcardMap, false));
					
					int l = 0;
					
					while (l < PX.size() && !q.equals(PX.get(l)) && !collection.search(PX.get(l),this.k).contains(res.get(k))) l++; 
						
					if (q.equals(PX.get(l)))
						edgeWeight = 1.0;
					else
						edgeWeight = 0.0;
				}
				
			}
			
			
			
			sum += edgeWeight;
			
		}
		
		return sum;
		
	}

	private Set<Query> generateCX(Document document, Map<Query,Double> cardMap) {
		
		//Algorithm 5
		
		List<Query> MX = getMatchingQueries(document,queriesU);
		
		System.out.println("Matching Queries: " + MX.size());
		
		Map<Query,Double> diffKMap = new HashMap<Query, Double>(MX.size());
		
		for (int i = 0; i < MX.size(); i++) {
			
			if (i % 10 == 0)
				System.out.print("+");
			
			diffKMap.put(MX.get(i), Math.abs(estimateCardinality(MX.get(i),globalCollection)-k));
		}
		
		Collections.sort(MX, new MapBasedComparator<Query>(diffKMap, false));
		
		Set<Query> CX = new HashSet<Query>();
		
		Query q = MX.remove(0);
		
		CX.add(q);
		
		int stratum = obtainStratum(strats,q);
		
		Map<Integer,List<Query>> sampled = new HashMap<Integer,List<Query>>();
		
		List<Query> quer = new ArrayList<Query>();
		
		sampled.put(stratum, quer);
		
		quer.add(q);
		
		while (!MX.isEmpty()){
			
			if (MX.size() % 100 == 0)
				System.out.println(MX.size());
			
			q = MX.remove(0);
			
			//COMPUTING qcost,qcostprime
			
			double den = 0.0;
			
			double qcost = 0.0;
			double qcostprime = 0.0;
			
			int cxc = 0;
			
			for (Query query : CX) {
				
				if(cxc++ % 100 == 0)
					System.out.print("*");
				
				double d = document.getCollection().search(query,this.k).size();
				
				if (d > 0)
					den += Math.max(1.0, (double)k / d);
				else
					den += 1;
				
			}

			if (den>0){
				qcost = (CX.size()) / den;
				
				double d = estimateCardinality(q, document.getCollection());
				
				if (d > 0)
					qcostprime = (CX.size() + 1) / (den + Math.max(1.0, (double)k / d));
				else{
					qcostprime = (CX.size() + 1) / (den + 1.0);
				}
			}
			
			//COMPUTE var and varprime
			
			double var = 0.0;
			
			double varprime = 0.0;
			
			stratum = obtainStratum(strats,q);
			
			for (int i = 1; i < strats.size(); i++) {
				
				double vari = 0.0;
				
				double ci = getAverageCardinality(i);
				
				double _ui = getUnderflow(strats.get(i),cardMap);
				
				double _ni = getSampled(strats.get(i),cardMap);
				
				double wj = Math.min(ci, k)/CX.size();
				
				double _wi = (wj) * CX.size()*(1 - _ui/strats.get(i).size());  
				
				for (int j = 0; j < strats.get(i).size(); j++) {
					vari += Math.pow(getWeight(j,_ui,wj)-_wi,2.0);
					
				}
			
				var += strats.get(i).size()/_ni * vari/strats.get(i).size();

				double r_i = 0;
				
				if (sampled.get(i) != null)
					r_i = sampled.get(i).size();
				
				double real_wi = 0.0;
				
				for (int j = 0; j < _ni; j++) {
					real_wi+= getRandomWeight(_ui,wj,(int)_ni); //weightMap.get(sampled.get(i).get(j));
				}
				
				real_wi /= _ni;

				double toSubstract = 0.0;
				
				for (int j = 0; j < r_i; j++) {
					
					toSubstract += (2*CX.size() + 1 + 2*(CX.size() + 1) * (CX.size()*getRandomWeight(_ui,wj,(int)_ni)-CX.size()*real_wi-1))/(CX.size()*CX.size()*(CX.size()+1)*(CX.size()+1)*strats.get(i).size());
					
				}
				
				double variprime = vari/strats.get(i).size() - toSubstract;
				
				if (i == stratum){
					//algorithm (7)
					
					//XXX FIGURE THIS OUT, although it may be correct!
					double w0 = getRandomWeight(_ui, wj, (int)_ni);
					
					variprime += (1 + 2*(CX.size() + 1)*(w0-real_wi))/((CX.size()+1)*(CX.size()+1)*strats.get(i).size());
					
				}
					
				varprime += strats.get(i).size() * variprime / _ni;
			
			}
			
			if (var*qcost > varprime*qcostprime){
				
				CX.add(q);
			
//				System.out.println("Greater");
				
				quer = sampled.get(stratum);
				
				if (quer == null){
					
					quer = new ArrayList<Query>();
					
					sampled.put(stratum, quer);
				}
				
				quer.add(q);
				
			} else {
				
//				System.err.println("Not greater");
				
			}
			
		}
		
		return CX;
	}

	private double getAverageCardinality(int i) {
		
		Double ci = avgCard.get(i);
			
		if (ci == null){
		
			ci = 0.0;
			int size = strats.get(i).size();	
			List<Query> list = strats.get(i);
			for (int j = 0; j < size; j++) {
				ci += estimateCardinality(list.get(j), globalCollection);
			}
			
			ci /= strats.get(i).size();

			avgCard.put(i,ci);
			
		}
		
		return ci;
		
	}

	private double getRandomWeight(double _ui, double wj, int total) {
	
		int index = (int)Math.round(Math.random()*total);
		
		return getWeight(index, _ui, wj);
	
	}

	private double getSampled(List<Query> list, Map<Query, Double> candMap) {
		
		int sampled = 0;
		
		for (int i = 0; i < list.size(); i++) {
			Double card = candMap.get(list.get(i));
			if (card != null){
				sampled++;
			}
		}
		
		return sampled;
		
	}

	private double getWeight(int index, double _underflow, double wj) {
		if (index <= _underflow)
			return 0;
		return wj;
	}

	private int getUnderflow(List<Query> list, Map<Query, Double> candMap) {
		
		int underflow = 0;
		
		for (int i = 0; i < list.size(); i++) {
			Double card = candMap.get(list.get(i));
			if (card != null && card == 0){
				underflow++;
			}
		}
		
		return underflow;
	}

	private int obtainStratum(Map<Integer, List<Query>> strats, Query q) {
		
		//TODO optimize
		
		for (int i = 0; i < strats.size(); i++) {
			
			for (int j = 0; j < strats.get(i).size(); j++) {
				
				if (strats.get(i).get(j).equals(q))
					return i;
				
			}
			
		}
		
		return -1;
	}

	private List<Query> getMatchingQueries(Document document, Set<Query> queries) {
		
		List<Query> matchingQueries = new ArrayList<Query>();
		
		Set<String> terms = document.getTerms();
		
		for (String t : terms) {
			if (queries.contains(new Query(t)))
				matchingQueries.add(new Query(t));
		}
		
		return matchingQueries;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String relation = "NaturalDisaster";
		String extractor = "SSK";
		int split = 1;
		boolean tupleAsStopWord = true;
		int L = 10;
		int r1 = 5;
		int r2 = 10;
		int k = 10;
		
		int limitOfCorrelation = 100;
		
//		List<Query> initialQueries = ((List<Query>)SerializationHelper.deserialize("data/queries/" + relation + "." + extractor + "." + split + "." + tupleAsStopWord + ".ser"));

		List<Query> correlated = (List<Query>)SerializationHelper.deserialize("natdis.uf");
		
		List<Query> uncorrelated = (List<Query>)SerializationHelper.deserialize("data/queries/ubuntuDictionary.ser");
		
		TextCollection collection = new IndriCollection("world", "data/indexes/onlyNotStemmedWords_topnewshealth.idx");		
		
//		TextCollection globalcollection = new IndriCollection("world", "data/indexes/onlyNotStemmedWords_topnewsus.idx");	
		
		String globalcollection = "TREC";
		
		UsefulDocumentCountStratifiedEstimatorUsefulSize estimator = new UsefulDocumentCountStratifiedEstimatorUsefulSize(/*initialQueries.subList(0, limitOfCorrelation)*/correlated,uncorrelated/*initialQueries.subList(limitOfCorrelation, initialQueries.size())*/, L, r1, r2, k, globalcollection);
		
		System.out.println(estimator.getNumberOfUsefulDocuments(collection, null));
		
		InformationExtractionSystem ies = new CachedInformationExtractionSystem(relation+extractor, relation, null, "NYT");
	
		System.out.println(ies.getId());
		
		int hasT = 0;
		
		for (int i = 0; i <= collection.size(); i++) {
			
			if (ies.extract(new Document(collection, i)) != null && !ies.extract(new Document(collection, i)).isEmpty()){
				
				hasT++;
				
			}
			
		}
		
		System.out.println(hasT + " - " + collection.size());
		
		
		
	}

	@Override
	public double getCurrentNumberOfUsefulDocuments() {
		return currentEstimation;
	}

	@Override
	public void reset() {
		//this.globalCollection.close();
		strats.clear();
		queriesU.clear();
		avgCard.clear();

	}


}
