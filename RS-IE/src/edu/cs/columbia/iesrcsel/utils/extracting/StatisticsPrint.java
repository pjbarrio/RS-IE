package edu.cs.columbia.iesrcsel.utils.extracting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;
import edu.stanford.nlp.ling.CoreAnnotations.LMiddleAnnotation;

public class StatisticsPrint {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		int round = 1;
		
		int limitsize = 1000;
		
		Set<String> fcollections = new HashSet<String>();
		
		Map<String,Integer> sizes = (Map<String, Integer>) SerializationHelper.deserialize("data/stats/sizes.ser");
		
		Map<Integer,Integer> counts = new HashMap<Integer, Integer>();
		
		for (Entry<String,Integer> size_d : sizes.entrySet()) {
			
			Integer size = size_d.getValue();
			
			size = (size / round) * round; 
			
			Integer count = counts.get(size);
			
			if (count == null){
				count = 0;
			}
			
			counts.put(size, count+1);
			
			if (size >= limitsize){
				
				fcollections.add(size_d.getKey());				
			}
			
		}

		for (Entry<Integer,Integer> siz : counts.entrySet()) {
			System.out.println(siz.getKey() + "*" + siz.getValue());
		} 

		System.out.print("c(");
		
		for (Entry<Integer,Integer> siz : counts.entrySet()) {
			for (int i = 0; i < siz.getValue(); i++) {
				System.out.print(siz.getKey()  + ",");
			}
			
		}
		
		System.out.println(")");
		
		System.out.println(fcollections.toString());
		
		SerializationHelper.serialize("data/stats/filtered_collections_" + limitsize + ".ser", fcollections);
		
	}

}
