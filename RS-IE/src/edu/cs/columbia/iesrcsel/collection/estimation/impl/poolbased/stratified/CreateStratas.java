package edu.cs.columbia.iesrcsel.collection.estimation.impl.poolbased.stratified;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cs.columbia.iesrcsel.model.collection.DeepWebLuceneCollection;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.utils.MapBasedComparator;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class CreateStratas {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		int todo = Integer.valueOf(args[1]);
		
		int startNumber = 1; //because I need to save the 0 for related queries. (see estimation method)

		String uncorrelatedQuery = args[0];//"TREC";// "ubuntuDictionary";
		String globalCollection = "TREC";
		int[] strs = new int[]{2,5,10};

		if (todo == 1){
			createStratas(uncorrelatedQuery,globalCollection,strs,startNumber);
		}else if (todo == 2){
			calculateAverageCardinalities(uncorrelatedQuery,globalCollection,strs,startNumber);
		}



	}



	private static void calculateAverageCardinalities(String uncorrelatedQuery,
			String globalCollection, int[] strs, int startNumber) {

		TextCollection tc = new DeepWebLuceneCollection("TREC", "data/indexes/"+globalCollection+"/tv-"+globalCollection+".idx");
		
		for (Integer l : strs) {
		
			Map<Integer,List<Query>> strats = (Map<Integer,List<Query>>)SerializationHelper.deserialize("data/queries/Stratas." + uncorrelatedQuery + "." + globalCollection + "." + l +".ser");
		
			Map<Integer, Double> avgCard = new HashMap<Integer, Double>();
			
			for (int i = startNumber; i <= l; i++) {
				
				double ci = 0.0;
				int size = strats.get(i).size();	
				List<Query> list = strats.get(i);
				for (int j = 0; j < size; j++) {
					if (j % 1000 == 0)
						System.out.println("Avg Card:" + j + " - " + size);
					ci += estimateCardinality(list.get(j), tc);
				}
				
				ci /= strats.get(i).size();

				avgCard.put(i,ci);
				
			}
			
			SerializationHelper.serialize("data/queries/AvgCardinalities." + uncorrelatedQuery + "." + globalCollection + "." + l +".ser", avgCard);
			
		}
		
		
		
	}



	private static void createStratas(String uncorrelatedQuery,
			String globalCollection, int[] strs, int startNumber) {
		
		List<Query> queries;

		if (uncorrelatedQuery.equals("ubuntuDictionary")){

			queries = (List<Query>)SerializationHelper.deserialize("data/queries/"+uncorrelatedQuery+".ser");

		}else{

			String fName = "data/queries/"+uncorrelatedQuery+".ser";

			if (!new File(fName).exists()){

				Map<String,Integer> termMap = (Map<String,Integer>)SerializationHelper.deserialize("data/biasedestimator/termMapUseful.ManMadeDisaster.SSK."+uncorrelatedQuery+".ser");

				Set<String> words = new HashSet<String>(termMap.keySet());

				termMap.clear();

				Map<String,Integer> termMap2 =  (Map<String,Integer>)SerializationHelper.deserialize("data/biasedestimator/termMapUseless.ManMadeDisaster.SSK."+uncorrelatedQuery+".ser");

				words.addAll(termMap2.keySet());

				termMap2.clear();

				queries = new ArrayList<Query>();

				for (String word : words) {
					queries.add(new Query(word));
				}	

				SerializationHelper.serialize(fName, queries);

			}else{
				
				queries = (List<Query>)SerializationHelper.deserialize("data/queries/"+uncorrelatedQuery+".ser");
				
			}

		}

		TextCollection tc = new DeepWebLuceneCollection("TREC", "data/indexes/"+globalCollection+"/tv-"+globalCollection+".idx");

		Map<Query,Double> cardinalityMap = new HashMap<Query, Double>();

		for (int i = 0; i < queries.size(); i++) {

			if (i % 1000 == 0)
				System.out.print(".");

			cardinalityMap.put(queries.get(i), estimateCardinality(queries.get(i),tc));

		}

		List<Query> toSort = new ArrayList<Query>(queries);

		Collections.sort(toSort,new MapBasedComparator<Query>(cardinalityMap, false));

		for (Integer l : strs) {

			System.out.format("Strata: %d", l);

			double div = (double)queries.size() / (double)l;

			Map<Integer,List<Query>> stratas = new HashMap<Integer, List<Query>>();

			for (double i = 0; i <= l-1; i++) {

				List<Query> quer = new ArrayList<Query>(toSort.subList((int)Math.round(i*div), (int)Math.round((i+1.0)*div)));
				
				stratas.put((int)Math.round(i) + startNumber, quer);

			}

			SerializationHelper.serialize("data/queries/Stratas." + uncorrelatedQuery + "." + globalCollection + "." + l +".ser", stratas);

		}
				
	}



	private static double estimateCardinality(Query query, TextCollection collection) {

		return collection.matchingItems(query);

	}

}
