package test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.cs.columbia.iesrcsel.model.collection.IndriCollection;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.extractor.impl.CachedInformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.utils.MapBasedComparator;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class ClassAffinityQueryProbes {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
//		createValues();
		
		Map<String,Map<Integer,Map<String,Integer>>> rankRet = generateRanking(true);
		
		Map<String,Map<Integer,Map<String,Integer>>> rankExt = generateRanking(false);
		
		print(rankRet);
		print(rankExt);
	
	}

	private static void print(
			Map<String, Map<Integer, Map<String, Integer>>> rankRet) {
		
		for (Entry<String,Map<Integer,Map<String,Integer>>> entry : rankRet.entrySet()) {
			
			System.out.println("\n" + entry.getKey());
			
			printForRelation(entry.getValue());
			
		}
		
	}

	private static void printForRelation(
			Map<Integer, Map<String, Integer>> mapQueryIndex) {
		
		List<String> entries = new ArrayList<String>(mapQueryIndex.get(10).keySet());
		
		Collections.sort(entries);
		
		System.out.print("," + entries.toString().substring(1,entries.toString().length()-1));
		
		for (int i = 10; i <= 100; i+=10) {
			System.out.print("\n" + i );
			for (String index : entries) {
				System.out.print("," + mapQueryIndex.get(i).get(index));
			}
		}
		
	}

	private static Map<String,Map<Integer,Map<String,Integer>>> generateRanking(boolean extractions) {
		
		String ext = extractions? "Ext" : "";
		
		Map<Integer,Map<String,Map<String,List<Integer>>>> qforRel = (Map<Integer,Map<String,Map<String,List<Integer>>>>)SerializationHelper.deserialize("data/trial/matchingAffinityMap"+ext+".ser");
		
		Map<String,Map<Integer,Map<String,Integer>>> r = new HashMap<String,Map<Integer,Map<String,Integer>>>();
		
		for(Entry<Integer,Map<String,Map<String,List<Integer>>>> forRel : qforRel.entrySet()){
			
			Integer query = forRel.getKey();
			
			for (Entry<String,Map<String,List<Integer>>> entryRel : forRel.getValue().entrySet()) {
				
				System.out.println("Relation: " + entryRel.getKey());
				
				List<String> ranking = produceRanking(entryRel.getValue());
				
				Map<String,Integer> rankMap = new HashMap<String,Integer>();
				
				for (int i = 0; i < ranking.size(); i++) {
					rankMap.put(ranking.get(i),i+1);
				}
				
				Map<Integer, Map<String, Integer>> aux = r.get(entryRel.getKey());
				
				if (aux == null){
					aux = new HashMap<Integer,Map<String,Integer>>();
					r.put(entryRel.getKey(), aux);
				}
				
				aux.put(query, rankMap);
			}
			
		}
		
		return r;
		
	}

	private static List<String> produceRanking(Map<String, List<Integer>> matches) {
		
		List<String> list = new ArrayList<String>(matches.keySet());
		
		Map<String,Double> map = new HashMap<String, Double>(matches.size());
		
		for (Entry<String,List<Integer>> mat : matches.entrySet()) {
			
			double d = 0;
			
			for (int i = 0; i < mat.getValue().size(); i++) {
				d+=mat.getValue().get(i);
			}
			
			map.put(mat.getKey(), d);
			
		}
		
		Collections.sort(list,new MapBasedComparator<String>(map, true));
		
		return list;
		
	}

	private static void createValues() {
		
		String[] relations = {"NaturalDisaster","ManMadeDisaster","PersonCareer","VotingResult","Indictment-Arrest-Trial","diseaseOutbreak","orgAff"};
		
		String[] indexes = {"lighttopclassifiedsautomobiles.idx"
		,"lighttopclassifiedsjobmarket.idx"
		,"lighttopclassifiedspaiddeathnotices.idx"
		,"lighttopfeaturesarts.idx"
//		,"lighttopfeaturescrosswordandgames.idx"
//		,"lighttopfeatureshomeandgarden.idx"
		,"lighttopfeaturesmagazine.idx"
		,"lighttopfeaturesstyle.idx"
		,"lighttopfeaturestravel.idx"
		,"lighttopfeaturesweekinreview.idx"
		,"lighttopnewsbusiness.idx"
		,"lighttopnewseducation.idx"
		,"lighttopnewsfrontpage.idx"
		,"lighttopnewshealth.idx"
//		,"lighttopnewsnewyorkandregion.idx"
		,"lighttopnewsobituaries.idx"
		,"lighttopnewsscience.idx"
		,"lighttopnewssports.idx"
		,"lighttopnewstechnology.idx"
		,"lighttopnewsus.idx"
		,"lighttopnewswashington.idx"
		,"lighttopnewsworld.idx"
		,"lighttopopinionopinion.idx"};
		
		List<TextCollection> collections = new ArrayList<TextCollection>(indexes.length);
		
		for (int i = 0; i < indexes.length; i++) {
			collections.add(new IndriCollection(indexes[i], "data/indexes/"+ indexes[i]));
		}
		
		int[] numQuerieses = {10,20,30,40,50,60,70,80,90,100};
		
		Map<Integer,Map<String,Map<String,List<Integer>>>> qforRel = new HashMap<Integer, Map<String,Map<String,List<Integer>>>>();
		
		Map<Integer,Map<String,Map<String,List<Integer>>>> qforRelExt = new HashMap<Integer, Map<String,Map<String,List<Integer>>>>();
		
		for (int numQueries : numQuerieses) {
						
			List<Query> queries;
			
			Map<String,Map<String,List<Integer>>> forRel = new HashMap<String, Map<String,List<Integer>>>(relations.length);
			
			Map<String,Map<String,List<Integer>>> forRelExt = new HashMap<String, Map<String,List<Integer>>>(relations.length);
			
			for (String relation : relations) {
				
				System.err.println(relation);
				
				InformationExtractionSystem ies;
				
				if (!relation.equals("diseaseOutbreak") && !relation.equals("orgAff"))
					ies = new CachedInformationExtractionSystem(relation, relation, null, "NYT");
				else{
					String rel = null;
					if (relation.equals("diseaseOutbreak"))
						rel = "Outbreaks";
					else
						rel = "OrgAff";
					ies = new CachedInformationExtractionSystem(relation, rel, null, "NYT");
				}
				
				Map<String,List<Integer>> forColls = new HashMap<String, List<Integer>>();
				
				Map<String,List<Integer>> forCollsExt = new HashMap<String, List<Integer>>();
				
				if (!relation.equals("diseaseOutbreak") && !relation.equals("orgAff"))
					queries = (List<Query>)SerializationHelper.deserialize("data/queries/"+relation+".SSK.1.false.ser");
				else
					queries = (List<Query>)SerializationHelper.deserialize("data/queries/"+relation+".DIC.1.false.ser");

				for (TextCollection coll : collections) {
					
					System.err.println(coll.getId());
					
					List<Integer> matches = new ArrayList<Integer>(numQueries);
					
					List<Integer> matchesExt = new ArrayList<Integer>(numQueries);
					
					for (int i = 0; i < numQueries; i++) {
						
						List<ScoredDocument> res = coll.search(queries.get(i));
						
						int numMatches = res.size();
						
						matches.add(numMatches);
						
						int numExt = 0;
						
						for (int j = 0; j < res.size(); j++) {
							numExt += ies.extract(res.get(j)).size();
						}
						
						matchesExt.add(numExt);
					}
					
					forColls.put(coll.getId(), matches);
					
					forCollsExt.put(coll.getId(), matchesExt);
					
				}
				
				forRel.put(relation, forColls);
				
				forRelExt.put(relation, forCollsExt);
				
			}
			
			qforRel.put(numQueries, forRel);
			
			qforRelExt.put(numQueries, forRelExt);
			
		}
		
		SerializationHelper.serialize("data/trial/matchingAffinityMap.ser", qforRel);
		
		SerializationHelper.serialize("data/trial/matchingAffinityMapExt.ser", qforRelExt);
		
	}

}
