package edu.cs.columbia.iesrcsel.dataset.generation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class SplitTrainingAndTesting {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		int limitsize = 1000;
		
		//run after StatisticsPrint.java
//		generateValidSet(limitsize);
//		
//		printCategoryDistribution("valid_collections", limitsize);
//		
//		generateTSV("valid_collections", limitsize);
//		
		double fraction = 0.15;
		
//		splitByPercentage(fraction,limitsize); 
		
		printCategoryDistribution("training" + fraction + "_", limitsize);
		
//		printCategoryDistribution("testing" + fraction + "_", limitsize);
		
	}

	private static void generateTSV(String nameprefix, int limitsize) throws FileNotFoundException {
		
		System.setOut(new PrintStream(new File("data/stats/" + nameprefix + "stats_" + limitsize + ".tsv")));
		
		Map<String,List<String>> aux = (Map<String,List<String>>)SerializationHelper.deserialize("/local/pjbarrio/Files/Downloads/"+nameprefix+"selected"+limitsize+".ser");
		
		System.out.println("First\tSecond\tThird\tFourth\tSize");
		
		for (Entry<String,List<String>> entry : aux.entrySet()) {
			
			System.out.println(entry.getKey().replaceAll("/", "\t") + "\t" + entry.getValue(). size());
			
		}

		
		
	}

	private static void printCategoryDistribution(String nameprefix, int limitsize) throws FileNotFoundException {
		
		Set<String> list = (Set<String>)SerializationHelper.deserialize("data/stats/" + nameprefix + limitsize + ".ser");
		
		Map<String,List<String>> aux = (Map<String,List<String>>)SerializationHelper.deserialize("/local/pjbarrio/Files/Downloads/preSelection.ser");
		
		Map<String,Integer> catFreq = new HashMap<String, Integer>();
		
		System.out.println("db, Category");
		
		for (Entry<String,List<String>> entry : aux.entrySet()) {
			
			entry.getValue().retainAll(list);
			
			String cate = entry.getKey();
			
			String[] spls = cate.split("/");
			
			String cat = spls[1];//take top category only
			
			Integer freq = catFreq.get(cat); 
			
			if (freq == null){
				catFreq.put(cat, entry.getValue().size());
			} else{
				catFreq.put(cat, freq + entry.getValue().size());
			}
			
			for (String db : entry.getValue()) {
				System.out.println(db + "," + cat);
			}
			
		}
//Uncomment to save
//		SerializationHelper.serialize("/local/pjbarrio/Files/Downloads/"+nameprefix+"selected"+limitsize+".ser", aux);
		
		System.out.format("category, frequency \n");
		for (Entry<String,Integer> entry : catFreq.entrySet()) {
			System.out.format("%s, %d \n", entry.getKey(),entry.getValue());
		}
		
	}

	/**
	 * Produces the training and testing split
	 * @param fraction
	 */
	
	private static void splitByPercentage(double fraction, int limitsize) {
				
		Set<String> filtered = (Set<String>)SerializationHelper.deserialize("data/stats/valid_collections" + limitsize + ".ser");
		
		List<String> collections = new ArrayList<String>(filtered);
		
		Collections.shuffle(collections);
		
		Set<String> training = new HashSet<String>(collections.subList(0, (int)Math.round(collections.size()*fraction)));
		
		Set<String> testing = new HashSet<String>(collections.subList((int)Math.round(collections.size()*fraction),collections.size()));
		
		SerializationHelper.serialize("data/stats/training" + fraction + "_" + limitsize + ".ser", training);
		
		SerializationHelper.serialize("data/stats/testing" + fraction + "_" + limitsize + ".ser", testing);

//		System.out.println(training.size() + "-" + training.toString());
//		
//		System.out.println(testing.size() + "-" + testing.toString());
		
	}

	/**
	 * Generates the distribution of the databases that we will use.
	 * @throws IOException
	 */
	
	private static void generateValidSet(int limitsize) throws IOException {
		
		Set<String> aux1 = new HashSet<String>(FileUtils.readLines(new File("data/websites/uniquesplits.txt")));		
				
		Set<String> filtered = (Set<String>)SerializationHelper.deserialize("data/stats/filtered_collections_" + limitsize + ".ser");
		
		Set<String> list = new HashSet<String>();
		
		for (String string : aux1) {
			if (filtered.contains(string)) //keep only those that include more than [limitsize] documents
				list.add(string.endsWith("/") ? string : string + "/");
		}
		
		SerializationHelper.serialize("data/stats/valid_collections" + limitsize + ".ser", list);
		
	}

}
