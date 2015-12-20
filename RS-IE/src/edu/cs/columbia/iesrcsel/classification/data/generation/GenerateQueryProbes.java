package edu.cs.columbia.iesrcsel.classification.data.generation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.berkeley.compbio.jlibsvm.SolutionModel;
import edu.berkeley.compbio.jlibsvm.binary.BinaryModel;
import edu.berkeley.compbio.jlibsvm.util.SparseVector;
import edu.cs.columbia.iesrcsel.model.collection.IndriCollection;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.utils.indexing.nytlabs.corpus.IndexingNewYorkTimes;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class GenerateQueryProbes {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		double minPrecision = 0.5;
		
		Map<String,Set<File>> map = (Map<String,Set<File>>)SerializationHelper.deserialize("data/cleanToIndex_tr.ser");

		TextCollection tr_col = new IndriCollection("training", "data/indexes/full_training_stemming.idx");
		
		Map<String,IndriCollection> indexes = new HashMap<String,IndriCollection>(); 
		
		for (String string : map.keySet()) {
			
			String name = IndexingNewYorkTimes.generateName(string);
			
			IndriCollection col = new IndriCollection(name, "data/indexes/tr_" + name + ".idx");
			
			indexes.put(string, col);
			
		}
		
		Map<String,Integer> termMap = (Map<String,Integer>)SerializationHelper.deserialize("data/qprober/queries/termIdsMap.ser");
		
		Map<Integer,String> inverse = new HashMap<Integer, String>(termMap.size());
		
		for (Entry<String,Integer> entry : termMap.entrySet()) {
			inverse.put(entry.getValue(), entry.getKey());
		}
		
		double epsilon = 0.001;
		
		Map<String, List<Query>> queryMap = new HashMap<String,List<Query>>();
		
		for (String index : map.keySet()) {
			
			System.out.println(index);
			
			BinaryModel<Boolean, SparseVector> model = (BinaryModel<Boolean, SparseVector>) SolutionModel.identifyTypeAndLoad("data/qprober/model/" + IndexingNewYorkTimes.generateName(index) + ".model");
			
			Map<Integer,Double> weights = (Map<Integer,Double>)SerializationHelper.deserialize("data/qprober/model/" + IndexingNewYorkTimes.generateName(index) + "_weights.ser");
			
			List<Query>	probes = generateQueryProbes(weights, inverse,epsilon, model.rho, indexes.get(index), tr_col, minPrecision);		
			
			SerializationHelper.serialize("data/qprober/queries/" + IndexingNewYorkTimes.generateName(index), probes);
			
			queryMap.put(index, probes);
		}
		
		SerializationHelper.serialize("data/qprober/queries/probesMapNYT.ser", queryMap);
		
	}

	private static List<Query> generateQueryProbes(
			Map<Integer, Double> weights, Map<Integer, String> inverse,
			double epsilon, float rho, TextCollection cat_index, TextCollection full_indexes, double minPrecision) {
		
		Map<String,Double> feats = new HashMap<String,Double>();
		
		List<int[]> cand = new ArrayList<int[]>();
		
		for (Entry<Integer,Double> entry : weights.entrySet()) {
			if (entry.getValue() >= epsilon){
				feats.put(inverse.get(entry.getKey()),entry.getValue());
				cand.add(new int[]{entry.getKey()});
			}
		}
	
		int k = 1;
		
		List<Query> R = new ArrayList<Query>();
		
		while (!cand.isEmpty() && k <= 6){
			System.out.println("iteration: " + k);
			for (int i = 0; i < cand.size(); i++) {
				double support = calculateSupport(cand.get(i),weights);
				if (support > rho && isUseful(generateQuery(cand.get(i),inverse),cat_index,full_indexes,minPrecision)){
					R.add(generateQuery(cand.get(i),inverse));
					cand.remove(i--);
				}
			}
			if (k < 6)
				cand = generateNewSets(cand,k);
			k+=1;
		}
		
		return R;
	}

	private static boolean isUseful(Query query, TextCollection cat_index,
			TextCollection full_indexes, double minPrecision) {
		
		List<ScoredDocument> cat_docs = cat_index.search(query);
		
		List<ScoredDocument> full_docs = full_indexes.search(query);
		
		//In our case there might be overlap of the documents
		
		Set<String> paths = new HashSet<String>();
		
		for (int i = 0; i < cat_docs.size(); i++) {
			paths.add(cat_docs.get(i).getPath());
		}
		
		int wrongClass = 0;
		
		for (int i = 0; i < full_docs.size(); i++) {
			if (!paths.contains(full_docs.get(i).getPath())){
				wrongClass++;
			}
		}
		
		return ((double)cat_docs.size() / (double)wrongClass) >= minPrecision;
	}

	private static double calculateSupport(int[] indexes,
			Map<Integer, Double> weights) {
		double ret = 0.0;
		
		for (int i = 0; i < indexes.length; i++) {
			ret+=weights.get(indexes[i]);
		}
		
		return ret;
	}

	private static List<int[]> generateNewSets(List<int[]> cand, int k) {
		
		List<int[]> R = new ArrayList<int[]>();
		
		for (int i = 0; i < cand.size()-1; i++) {
			
			for (int j = i+1; j < cand.size(); j++) {
				
				if (shareAllButOne(cand.get(i),cand.get(j))){
					R.add(combine(cand.get(i),cand.get(j)));
				}
				
			}
			
		}
		
		return R;
	}

	private static int[] combine(int[] a1, int[] a2) {
		
		int[] ret = new int[a1.length+1];
		
		int index1 = 0;
		int index2 = 0;
		
		for (int i = 0; i < ret.length; i++) {
			
			if (index2 < a2.length && (index1 == a1.length || a1[index1] > a2[index2]))
				ret[i] = a2[index2++];
			else if (index1 < a1.length && (index2 == a2.length || a1[index1] < a2[index2]))
				ret[i] = a1[index1++];
			else{ //they are equal
				ret[i] = a1[index1++];
				index2++;
			}
		}
		
		return ret;
	}

	private static boolean shareAllButOne(int[] a1, int[] a2) {
		
		int diff = 0;
		
		int index1=0;
		int index2=0;
		
		while (diff<=2 && index1 < a1.length && index2 < a2.length){
			if (a1[index1] == a2[index2]){
				index1++;
				index2++;
			} else if (a1[index1] < a2[index2]){
				diff++;
				index1++;
			} else if (a1[index1] > a2[index2]){
				diff++;
				index2++;
			}
		}
		
		return diff >= 1 && diff <= 2;

	}

	private static Query generateQuery(int[] indexes, Map<Integer, String> inverse) {
		assert(indexes.length > 0);
		String concatenatedQueryTerms = new String("");
		for (int i : indexes) {
			concatenatedQueryTerms += " " + inverse.get(indexes[i]).toLowerCase();
		}
		return new Query(concatenatedQueryTerms.substring(1)); // substring to get rid of 1st space
	}

	

}
