package edu.cs.columbia.iesrcsel.utils.indexing;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.cs.columbia.iesrcsel.utils.MapBasedComparator;
import edu.cs.columbia.iesrcsel.utils.indexing.nytlabs.corpus.IndexingNewYorkTimes;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class TreeAnalysis {

	public static void main(String[] args) {
		
//		getClasses();
		
//		getOverlap();
		
		produceReducedMap();
		
	}

	private static void produceReducedMap() {
		
		Map<String,String> replace = new HashMap<String, String>();
		
		replace.put("Top/Features/Theater", "Top/Features/Arts");
		replace.put("Top/Features/Movies", "Top/Features/Arts");
		replace.put("Top/Features/Books", "Top/Features/Arts");
		replace.put("Top/Features/Dining and Wine", "Top/Features/Travel");
		replace.put("Top/News/Corrections", "Top/News/New York and Region");
		
		Set<String> omit = new HashSet<String>();
		
		omit.add("Top/Reference/People");
		omit.add("Top/Opinion");
		omit.add("Top/Opinion/The Public Editor");
		omit.add("Top/News/Editors' Notes");
		omit.add("Top/Classifieds/Real Estate");
		omit.add("Top/News");
		
		Map<String,List<File>> map = (Map<String,List<File>>)SerializationHelper.deserialize("data/toIndex_tr.ser");
		
		map.keySet().removeAll(omit);
		
		Map<String,Set<File>> mapC = new HashMap<String, Set<File>>();
		
		for (Entry<String,List<File>> entry : map.entrySet()){
			
			String repl = replace.get(entry.getKey());
			
			if (repl == null){
				repl = entry.getKey();
			}
			
			Set<File> files = mapC.get(repl);
			
			if (files == null){
				files = new HashSet<File>();
				mapC.put(repl, files);
			}
			
			files.addAll(entry.getValue());
			
		}
		
		SerializationHelper.serialize("data/cleanToIndex_tr.ser", mapC);
		
	}

	private static void getOverlap() {
		
		double threshold = 0.25;
		
		Map<String,List<File>> map = (Map<String,List<File>>)SerializationHelper.deserialize("data/toIndex.ser");
		
		int total = 0;
		
		Set<File> s = new HashSet<File>();
		
		for (Entry<String,List<File>> entry : map.entrySet()) {
			
			System.out.println(".");
			
			for (Entry<String,List<File>> entry2 : map.entrySet()){
			
				Set<File> ss = new HashSet<File>(entry.getValue());
				
				Set<File> ss2 = new HashSet<File>(entry2.getValue());
				
				ss.removeAll(ss2);
				
				if ((double)ss.size() <= threshold * (double)entry.getValue().size()){
					
					System.err.println(entry.getKey() + " - " + entry2.getKey() + " - " + ss.size() + " . " + entry.getValue().size());
					
				}
				
			}

			System.out.println(entry.getKey() + " - " + entry.getValue().size());
			total += entry.getValue().size();
			s.addAll(entry.getValue());
			
		}
		
		System.out.println(total + " - " + s.size());
		
	}

	private static void getClasses() {
		
		Map<String,Integer> map = (Map<String,Integer>)SerializationHelper.deserialize("data/classesFreq.ser");
		
		Map<String,Double> mapC = new HashMap<String, Double>(map.size()); 
		
		for (Entry<String,Integer> entry : map.entrySet()) {
			mapC.put(entry.getKey(), (double)entry.getValue());
		}
		
		List<String> classes = new ArrayList<String>(map.keySet());
		
		Collections.sort(classes, new MapBasedComparator<String>(mapC, false));
		
		for (int i = 0; i < classes.size(); i++) {
			
			System.out.println(IndexingNewYorkTimes.getName(classes.get(i)) + " - " + map.get(classes.get(i)));
			
		}
		
	}
	
}
