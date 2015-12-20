package edu.cs.columbia.iesrcsel.classification.data.generation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weka.clusterers.Cobweb.CNode;

import Jama.Matrix;

import edu.cs.columbia.iesrcsel.model.collection.IndriCollection;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.utils.indexing.nytlabs.corpus.IndexingNewYorkTimes;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class GenerateConfusionMatrix {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Map<String, List<Query>> queriesMap = (Map<String,List<Query>>)SerializationHelper.deserialize("data/qprober/queries/probesMapNYT.ser");

		Map<String,Set<File>> map = (Map<String,Set<File>>)SerializationHelper.deserialize("data/cleanToIndex_tr.ser");

		Map<String,IndriCollection> indexes = new HashMap<String,IndriCollection>(); 
		
		for (String string : map.keySet()) {
			
			String name = IndexingNewYorkTimes.generateName(string);
			
			IndriCollection col = new IndriCollection(name, "data/indexes/tr_" + name + ".idx");
			
			indexes.put(string, col);
			
		}
		
		List<String> classNames = new ArrayList<String>(map.keySet());
		
		Matrix confusionMatrix = new Matrix(classNames.size(), classNames.size());
		
		for (int i = 0; i < classNames.size(); i++) {
			for (int j = 0; j < classNames.size(); j++) {
				confusionMatrix.set(i, j, getProbesMatch(queriesMap.get(classNames.get(i)),indexes.get(classNames.get(j))) / (double)indexes.get(classNames.get(j)).size());
			}
		}
		
		SerializationHelper.serialize("data/qprober/confusionMatrix.ser", confusionMatrix);
		
	}

	private static double getProbesMatch(List<Query> list,
			IndriCollection indriCollection) {
		
		double matches = 0.0;
		
		for (int i = 0; i < list.size(); i++) {
			
			matches += indriCollection.matchingItems(list.get(i)); //It's OK since queries aim at avoiding overlap.
			
		}
		
		return matches;
	}
	

}
