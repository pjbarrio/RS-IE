package edu.cs.columbia.iesrcsel.dataset.generation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.FileUtils;
import org.apache.xerces.impl.dv.util.Base64;
import org.apache.xerces.util.URI;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.util.FileManager;

import edu.cs.columbia.iesrcsel.dataset.generation.utils.DirectoryTree;
import edu.cs.columbia.iesrcsel.utils.MapBasedComparator;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class StatisticsGenerator {

	private static final String TOPIC_PREFIX = "<Topic";
	private static final String TOPIC_END = "</Topic>";
	private static final String LINK_PREFIX = "<link ";

	private static int numberOfSites = 4500; //or total in bing / number we want * number in each cat.

	private static int maxNumber = 5;

	private static int wantedSize = 10000;

	private static int remainingRequests = 175;

//	next: , , , , , , rOmf0BVgre5o9F/6xOU7wYIsPkTwRSPqVMv83o3Atgo=, bdx4gWFHCXtJZH6KdMZV7Ek4vUgojNkFScQ7LZF9rxA=, Pv2fyO2r9MeB6ZCuY9j+L4u5sbhIFM+YXVc3goSiA7g=
	
	private static String appl = "2809mV5aHw3LbmL1Nhi9OSmplv/rFKh5uBrI79o1p/A=";
			
	//"7RJbry54IKJyujFgYKDuVL6EKRk3xJxYI7JqHbrJhis=";
	//"/a8TRz7JJk2J47HMPxbUp784YcG1Aj7zjYekEjll0Rg=";
	//"Gm1j6QGlbNUmxO1RQH37v0Zf6xw5vN30gBSjYF1KL/U=";
	//"ZkXZ70GtdG+AHK6qQT3oqD8xveizExX71G+hGqUFHAU="; 
	//"R5CIq9zmtnDwQ26dyeCOfF+2IwFvkFooTw16GiwBmS4=";
	//"x4JT53TCDnxwmYaQZCj6NURRq9BzlDNkg2cW0idM7Dk=";
	//"NOGe7lFqCz7tqO0HBfxjJqSLlH86eeivxD7DrfVLz1s=";
	//"UONym5OSZJn6Gpzu31Tm3T0Brx1WeDViRXQ2Egh3IAU=";
	//"B/PCHlGzXkT+t5sRzaNIGZT3v3jOKX7BQi16RUjsyZ8=";
	//"g8MIzS0dlBDoWFt1069DkhomL/LhZrv/hZGqu/E03aA=";
	//"cl+CGEC5TNbMOpk+QOGLlbwLXAihfnwscJZRQdmNDDE=";
	//"gAQWhdOorE4LJ5ntPJT9mbKSemDzsrDxsN/xcZQA8Fo=";
	//"maTzksULCaZPZqo/HPGg0mR+ZtvapnehgEGje5md1Ds="; 
	//"KLKltVT3ZcleKr+PXv2syR+FfRZz6gVo7VWOLqXWK50=";
	//"tfXfeYcUCuEk1sgdW/1vUCRnKx5FTqg9eBwCO05Skvc=";
	//"qhoA3ZNi3uIxUpjHcHBedrnxJ9O2LQ1QyzWJ2+Bmddg=";
	//"eECeOiLBFOie0G3C03YjoHSqb1aMhEfqk8qe7Xi2YMs=";
	//"ssvj8qwAVbzdMrScwlvo/aQwH00uC7bml0GapeekuCQ=";
	//"TN+S3j9iR4yqrFADfV/IqCowNPsex8boAClzliKDh/s=";
	//"xBVzsi/Wfxl3sEXAx9Jn/nCzNuUempQOStfrsWzc48Y";
	//"3ICg085GJMsH+cJ/SM1KdVHqxzGxi2aBsDk1gmMMgbk=";
	//"troBf72dETWWVPyZCL5wDCZbiOQgss+cMMPEE3Yf3Ho=";
	//"1LK+gIYedwrF8l7bNmS35U8Cnl/6G7ZUgPI3hiI9Y9k=";
	//"mhHRCa+0Uadgyvdu4SpKXGAqRJjnkVyePbVtIR/ZjRI=";
	//"MWQrrA8YW+6ciAUTJh56VHz1vi/Mdqu0lSbzms3N7NY=";
	//"GsDwZEdzNPHvtA8zVUrTEHqRiz7uMOIh5z1N4+VDLC4=";
	//"avZlG4wVLTP4es5BYtOnQtL4I5h/NRalcpQg5yQAqCE=";

	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		//		useJena("C:\\dmoz\\content.rdf.u8");

		//		useHandWritenRules("C:\\dmoz\\content.rdf.u8");

		//		computeNumberOfLevelsAndFrequency("C:\\dmoz\\categoryHostTree.ser");

		//		obtainUniqueSites("C:\\dmoz\\categoryHostTree.ser");

//				obtainNumberOfSitesPerNode("C:\\dmoz\\categoryHostTree.ser");

//				sampleCollectionsPopularBased("C:\\dmoz\\categoryHostTree.ser","C:\\dmoz\\FreqLevel.ser");

		printPopular("C:\\dmoz\\FreqLevel.ser");
		
//		preSelection("C:\\dmoz\\selection.ser");
//
//		sizeEstimation("C:\\dmoz\\preSelection.ser");
//
//		System.out.println("Remaining requests: " + remainingRequests);
		
//		for (int i = 1; i < 10; i++) {
//		
//			printCollections("/local/pjbarrio/Files/Downloads/finalSelectedDataset.ser",i,i+1);
//			
//		}
		
//-------------------------------------
		
//		sizeFreqDataGeneration("C:\\dmoz\\preSelection.ser");

//		printData("/local/pjbarrio/Files/Downloads/sizeFreq.ser");
		
		System.err.println("k");
		
	}

	private static void printCollections(String fileName, int start, int end) {
		
		Map<String,List<String>> selected = (Map<String,List<String>>)SerializationHelper.deserialize(fileName);
		
		int var = 1;
		
		try {
			System.setOut(new PrintStream(new File("data/websites/websites.list." + start)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		for (Entry<String,List<String>> entr : selected.entrySet()) {
			
//			System.out.println(++var + "-" +  entr.getKey());
			
			for (int i = start; i < entr.getValue().size() && i < end; i++) {
				System.out.println(entr.getValue().get(i));

			}
		}
		
	}

	private static void printPopular(String FreqFile) {
		
		Map<String,Long> freqs = (Map<String,Long>)SerializationHelper.deserialize(FreqFile);
		
		for (Entry<String,Long> freq : freqs.entrySet()) {
			String val = freq.getKey().replace("Top/", "");
			if (!val.contains("/"))
				System.out.println(freq.getKey() + " - " + freq.getValue());
		}
		
	}

	private static void printData(String sizeFreqFile) {
		
		Map<String,Map<Integer,Integer>> map = (Map<String,Map<Integer,Integer>>)SerializationHelper.deserialize(sizeFreqFile);
		
		for (Entry<String,Map<Integer,Integer>> entry : map.entrySet()) {
			
			System.out.print(entry.getKey());
			
			for (int i = 0; i <= 11; i++) {
				
				Integer ineg = entry.getValue().get(i);
				
				if (ineg == null)
					ineg = 0;
				
				System.out.print("," + ineg);
				
			}
			
			System.out.print("\n");
		}
		
	}

	private static void sizeFreqDataGeneration(String preSelectionFile) {
		
		Map<String,List<String>> finalMap = (Map<String,List<String>>)SerializationHelper.deserialize(preSelectionFile);
		
		Map<String,Map<Integer,Integer>> map = new HashMap<String,Map<Integer,Integer>>();
		
		for (Entry<String, List<String>> entry : finalMap.entrySet()) {

			Map<Integer,Integer> sizeFreqMap = new HashMap<Integer,Integer>();
			
			for (int i = 0; i < entry.getValue().size(); i++) {

				String website;
				website = entry.getValue().get(i).substring(0,entry.getValue().get(i).length()-1);
				if (saved(website)){
					 int value = savedValue(website);
					 int splitValue = getSplit(value);
					 Integer freq = sizeFreqMap.get(splitValue);
					 
					 if (freq == null){
						 freq=0;
					 }
					 sizeFreqMap.put(splitValue, freq+1);
				}

			}
			
			map.put(entry.getKey(), sizeFreqMap);
			
		}
		
		SerializationHelper.serialize("C:\\dmoz\\sizeFreq.ser", map);
		
		
		
	}

	private static int getSplit(int value) {
		if (value == 0)
			return value;
		
		int ret = value/1000 + 1;
		
		if (ret > 10)
			return 11;
		
		return ret;
	}

	private static void sizeEstimation(String preSelectionFile) {

		Map<String,List<String>> finalMap = (Map<String,List<String>>)SerializationHelper.deserialize(preSelectionFile);
		
		Set<String> visited = new HashSet<String>();
		
		Map<String,List<String>> selected = new HashMap<String,List<String>>();
		
		for (Entry<String, List<String>> entry : finalMap.entrySet()) {

			int numberOfSuccesses = 0;

			selected.put(entry.getKey(), new ArrayList<String>());
			
			for (int i = 0; i < entry.getValue().size(); i++) {

				String website;
				website = entry.getValue().get(i).substring(0,entry.getValue().get(i).length()-1);
				
				if (visited.contains(website))
					continue;
				
				System.err.println(numberOfSuccesses + ":" + website + " - " + entry.getValue().get(i));
				if (saved(website)){
					
					if (savedValue(website)> wantedSize){
						numberOfSuccesses++;
						selected.get(entry.getKey()).add(website);
					}
					
					visited.add(website);
				
					if (numberOfSuccesses >= maxNumber){
						break;
					}
					
					continue;
				}
				
				

				int estimatedSize = getSize(website);

				remainingRequests--;
				
				if (estimatedSize >= 0){
					save(website, estimatedSize);
					visited.add(website);
				}
				if (estimatedSize > wantedSize){
					numberOfSuccesses++;
					selected.get(entry.getKey()).add(website);
				}
				
				if (numberOfSuccesses >= maxNumber){
					break;
				}
				
				if (remainingRequests == 0)
					return;
				
				try {
					Thread.sleep(Math.round(250*Math.random()));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

			System.out.println("Remaining requests: " + remainingRequests);
			
		}
		
		SerializationHelper.serialize("C:\\dmoz\\finalSelectedDataset.ser", selected);
		
		for (Entry<String,List<String>> entr : selected.entrySet()) {
			System.out.println(entr.getKey() + " - " + entr.getValue().size());
		}
		
	}


	private static void save(String website, int estimatedSize) {

		List<String> save = new ArrayList<String>();
		save.add(Integer.toString(estimatedSize));
		try {
			FileUtils.writeLines(getFile(website), save);
			System.out.println("Saved: " + website + " - " + estimatedSize);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static int savedValue(String website) {

		try {
			List<String> lines = FileUtils.readLines(getFile(website));
			return Integer.valueOf(lines.get(0));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	private static boolean saved(String website) {

		return getFile(website).exists();
	}

	private static File getFile(String website) {
		return new File("C:\\dmoz\\size\\" + website + ".size");
	}

	private static int getSize(String website){

		try {
			URL url = new URL("https://api.datamarket.azure.com/Bing/SearchWeb/v1/Composite?Query=%27site%3A"+URLEncoder.encode(website,"ISO-8859-1")+"%27&$format=JSON");

			HttpURLConnection http = (HttpURLConnection)url.openConnection();


			String applicationId = Base64.encode((appl + ":" + appl).getBytes());

			http.setRequestProperty("Authorization", "Basic " + applicationId);

			BufferedReader in = new BufferedReader(
					new InputStreamReader(http.getInputStream()));

			StringBuilder sb = new StringBuilder();

			String inputLine;
			while ((inputLine = in.readLine()) != null)
				sb.append(inputLine);

			in.close();

			JSONObject json = (JSONObject) JSONSerializer.toJSON( sb.toString() );

			return json.getJSONObject("d").getJSONArray("results").getJSONObject(0).getInt("WebTotal");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return -1;
	}

	private static void preSelection(String string) {

		Map<String,List<String>> samplemap = (Map<String,List<String>>)SerializationHelper.deserialize("C:\\dmoz\\selection.ser");

		Map<String,List<String>> finalMap = new HashMap<String,List<String>>();

		for (Entry<String,List<String>> entry : samplemap.entrySet()) {

			finalMap.put(entry.getKey(), new ArrayList<String>());

			for (int i = 0; i < entry.getValue().size() && i < numberOfSites; i++) {

				if (i == numberOfSites)
					break;


				finalMap.get(entry.getKey()).add(entry.getValue().get(i));

			}

		}

		SerializationHelper.serialize("C:\\dmoz\\preSelection.ser", finalMap);

	}

	private static boolean accepts(String string) {
		// TODO uses bing to count the number of indexed pages.
		return false;
	}

	private static void sampleCollectionsPopularBased(String treeFile,
			String FreqFile) {

		DirectoryTree tree = (DirectoryTree)SerializationHelper.deserialize(treeFile);

		Map<String,Long> freqs = (Map<String,Long>)SerializationHelper.deserialize(FreqFile);

		int[] catLevelConfiguration = new int[]{16,5,5};

		Map<String,List<String>> samplemap = new HashMap<String,List<String>>();

		Set<String> dbsFromTopLevels = new HashSet<String>();

		collectSample(tree,catLevelConfiguration,0,"Top",samplemap,freqs,dbsFromTopLevels);

		SerializationHelper.serialize("C:\\dmoz\\selection.ser", samplemap);

	}

	private static void collectSample(DirectoryTree tree,
			int[] catLevelConfiguration, int index, String prefix,
			Map<String, List<String>> samplemap, Map<String, Long> freqs, Set<String> dbsFromTopLevels) {

		if (index < catLevelConfiguration.length){

			//Need to select the catLevelConfiguration[index] most popular ones

			Set<String> nodes = tree.getNodes();

			Map<String,Double> currentFreq = new HashMap<String,Double>();

			Set<String> dbsFromThisLevel = new HashSet<String>();

			for (String node : nodes) {

				if (tree.getNode(node).getNodes().isEmpty()){
					dbsFromThisLevel.add(getName(node));
					continue; //it's a website...
				}
				double freq = freqs.get(prefix + "/" + node);

				currentFreq.put(node, freq);

			}

			List<String> listNodes = new ArrayList<String>(currentFreq.keySet());

			Collections.sort(listNodes, new MapBasedComparator<String>(currentFreq, true));

			for (int i = 0; i < catLevelConfiguration[index] && i < listNodes.size(); i++) {

				Set<String> toSend = new HashSet<String>(dbsFromThisLevel);

				toSend.addAll(dbsFromThisLevel);

				collectSample(tree.getNode(listNodes.get(i)), catLevelConfiguration, index+1, prefix + "/" + listNodes.get(i), samplemap, freqs, toSend);

			}

		}else{
			Set<String> documents = getSites(tree,prefix);
			documents.addAll(dbsFromTopLevels);
			List<String> list = new ArrayList<String>(documents);
			Collections.shuffle(list);
			samplemap.put(prefix, list);
		}

	}

	private static Set<String> getSites(DirectoryTree tree,String prefix) {

		Set<String> nodes = tree.getNodes();

		Set<String> sites = new HashSet<String>();

		for (String node : nodes) {
			if (tree.getNode(node).getNodes().isEmpty()){
				sites.add(getName(node));
			}else{

				sites.addAll(getSites(tree.getNode(node),prefix + "/" + node));

			}
		}

		return sites;

	}

	private static void obtainNumberOfSitesPerNode(String treeFile) {

		DirectoryTree tree = (DirectoryTree)SerializationHelper.deserialize(treeFile);

		Map<String,Long> freqs = new HashMap<String,Long>();

		getNumberOfSites(tree,freqs,"Top");

		SerializationHelper.serialize("C:\\dmoz\\FreqLevel.ser", freqs);

	}

	private static Set<String> getNumberOfSites(DirectoryTree tree, Map<String, Long> freqs, String prefix) {

		Set<String> nodes = tree.getNodes();

		Set<String> sites = new HashSet<String>();

		for (String node : nodes) {
			if (tree.getNode(node).getNodes().isEmpty()){
				sites.add(getName(node));
			}else{

				sites.addAll(getNumberOfSites(tree.getNode(node),freqs,prefix + "/" + node));

			}
		}

		freqs.put(prefix, (long)sites.size());

		return sites;
	}

	private static void obtainSitesSize(String collectionsFile) {

		Set<String> collections = (Set<String>)SerializationHelper.deserialize(collectionsFile);

		System.out.println(collections.size());

		Map<String,Long> sizes = new HashMap<String,Long>();

		for (String collection : collections) {

			sizes.put(collection, estimateSize(collection));

		}

		SerializationHelper.serialize("C:\\dmoz\\collectionSizes.ser", sizes);
	}

	private static Long estimateSize(String collection) {
		// TODO Auto-generated method stub
		return null;
	}

	private static void obtainUniqueSites(String treeFile) {

		DirectoryTree tree = (DirectoryTree)SerializationHelper.deserialize(treeFile);

		Set<String> set = obtainUniqueSites(tree);

		SerializationHelper.serialize("C:\\dmoz\\uniqueWebsites.ser", set);

	}

	private static Set<String> obtainUniqueSites(DirectoryTree tree) {

		Set<String> nodes = tree.getNodes();

		Set<String> ret = new HashSet<String>();

		for (String node : nodes) {

			if (tree.getNode(node).getNodes().isEmpty()){ //is the last node
				ret.add(getName(node));
			}
			else
				ret.addAll(obtainUniqueSites(tree.getNode(node)));

		}

		return ret;

	}

	private static String getName(String node) {
		if (!node.startsWith("www"))
			node = "www." + node;
		if (!node.endsWith("/"))
			node = node + "/";
		return node;
	}

	private static void computeNumberOfLevelsAndFrequency(String treeFile) {

		DirectoryTree tree = (DirectoryTree)SerializationHelper.deserialize(treeFile);

		Set<String> nodes = tree.getNodes();

		Map<String,Map<Integer,Integer>> levelsFreq = new HashMap<String,Map<Integer,Integer>>();

		for (String node : nodes) {

			System.out.println(node);

			Map<Integer,Integer> map = new HashMap<Integer,Integer>();

			computeNumberOfLevelsAndFrequency(tree.getNode(node),map,1);

			System.out.println(map.toString());

			levelsFreq.put(node, map);
		}

		SerializationHelper.serialize("C:\\dmoz\\LevelFreq.ser", levelsFreq);

	}

	private static void computeNumberOfLevelsAndFrequency(DirectoryTree node, Map<Integer, Integer> map, int level) {

		if (node.getNodes().isEmpty()){ //no more nodes
			Integer freq = map.get(level-1);
			if (freq == null){
				freq = 0;
			}
			map.put(level-1, freq+1);
		}else{

			Set<String> nodes = node.getNodes();

			for (String children : nodes) {
				computeNumberOfLevelsAndFrequency(node.getNode(children), map, level+1);
			}
		}
	}

	private static void useHandWritenRules(String inputFileName) throws IOException{

		BufferedReader br = new BufferedReader(new FileReader(new File(inputFileName)));

		String line;

		String currentParent = null,site;
		String[] parents = null;
		DirectoryTree tree = new DirectoryTree();
		DirectoryTree hosttree = new DirectoryTree();

		int i = 0;

		while ( (line = br.readLine()) != null){

			i++;

			if (i % 100000 == 0)
				System.out.print(".");

			if (line.trim().startsWith(TOPIC_PREFIX)){
				currentParent = line.split("\"")[1];
				parents = currentParent.split("/");
			} else if (line.trim().startsWith(TOPIC_END)){
				currentParent = null;
			} else if (line.trim().startsWith(LINK_PREFIX)){
				site = line.split("\"")[1];

				try {
					URL url = new URL(site);
					hosttree.addNode(url.getHost(),parents,1); //needs to skip Top
					tree.addNode(url.toString(), parents, 1);
				} catch (MalformedURLException e) {
					if (e.getMessage().startsWith("no protocol: ")){
						URL url;
						try {
							url = new URL("http://" + site);
							hosttree.addNode(url.getHost(),parents,1); //needs to skip Top
							tree.addNode(url.toString(), parents, 1);
						} catch (MalformedURLException e1) {
							System.err.println(e1.getMessage());
						}

					}else{
						System.err.println(e.getMessage());
					}
				}

			}

		}

		System.out.println("\n");

		br.close();

		SerializationHelper.serialize("C:\\dmoz\\categoryHostTree.ser", hosttree);
		SerializationHelper.serialize("C:\\dmoz\\categoryTree.ser", tree);

	}

	private static void useJena(String inputFileName) {
		// create an empty model
		Model model = ModelFactory.createDefaultModel();

		// use the FileManager to find the input file
		InputStream in = FileManager.get().open( inputFileName );
		if (in == null) {
			throw new IllegalArgumentException(
					"File: " + inputFileName + " not found");
		}

		// read the RDF/XML file
		model.read(in, null);


		//				write it to standard out
		model.write(System.out);
	}

}
