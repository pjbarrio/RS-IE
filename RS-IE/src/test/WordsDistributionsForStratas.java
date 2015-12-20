package test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cs.columbia.iesrcsel.collection.estimation.impl.StratifiedEstimatorUsefulSize;
import edu.cs.columbia.iesrcsel.model.collection.IndriCollection;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.extractor.impl.CachedInformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.impl.QBSSampleGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.utils.QuerySelectionStrategy;
import edu.cs.columbia.iesrcsel.sample.generator.utils.impl.AvgTfIdfQuerySelection;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class WordsDistributionsForStratas {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String relation = "NaturalDisaster";
		String extractor = "SSK";
		int split = 1;
		boolean tupleAsStopWord = true;
		int k = 10;
		
		int limitOfCorrelation = 100;
		
		List<Query> biased = ((List<Query>)SerializationHelper.deserialize("data/queries/" + relation + "." + extractor + "." + split + "." + tupleAsStopWord + ".ser"));

		List<Query> random = (List<Query>)SerializationHelper.deserialize("data/queries/ubuntuDictionary.ser");
		
		TextCollection collection = new IndriCollection("health", "data/indexes/onlyNotStemmedWords_topnewshealth.idx");		
		
		InformationExtractionSystem ies = new CachedInformationExtractionSystem(relation+extractor, relation, null, "NYT");
	
		Set<Document> biasedDocs = getDocuments(collection,biased.subList(0, limitOfCorrelation),k);
		
		QuerySelectionStrategy qss = new AvgTfIdfQuerySelection();
		
		SampleGenerator sg = new QBSSampleGenerator(random, biasedDocs.size(), 400, k, qss);
		
		Set<Document> randomDocs = sg.generateSample(collection).getDocuments();
		
		Set<Document> useful = new HashSet<Document>();
		
		Set<Document> useless = new HashSet<Document>();
		
		populate(biasedDocs,ies,useful,useless);
		
		populate(randomDocs,ies,useful,useless);
		
		Set<String> terms = getAllTerms(biasedDocs);
		terms.addAll(getAllTerms(randomDocs));

		BufferedWriter bsw = new BufferedWriter(new FileWriter("data/tmp/"+relation+"docwordmap"+-k+".csv"));
		
		bsw.write("doc, term, pdocterm");
		
		Set<Document> allDocs = new HashSet<Document>(randomDocs);
		
		for (Document document : allDocs) {
			
			Set<String> te = document.getTerms();
			
			for (String t : te) {
				
				bsw.write("\n" + document.getId() + "," + t + "," + 1.0);
				
			}
			
		}
		
		bsw.close();
		
		bsw = new BufferedWriter(new FileWriter("data/tmp/"+relation+"docwordmapUseful"+-k+".csv"));
		
		bsw.write("doc, term, pdocterm");
		
		for (Document document : useful) {
			
			String[] te = document.getTerms();
			
			for (int i = 0; i < te.length; i++) {
				
				bsw.write("\n" + document.getId() + "," + te[i] + "," + 1.0);
				
			}
			
		}
		
		bsw.close();
		
		
		Map<String, Double> biasedFreq = generateFrequencyMap(collection,biasedDocs,terms,Integer.MAX_VALUE);

		Map<String, Double> randomFreq = generateFrequencyMap(collection,randomDocs,terms,Integer.MAX_VALUE);
		
		Map<String, Double> usefulFreq = generateFrequencyMap(collection,useful,terms,Integer.MAX_VALUE);
		
		Map<String, Double> uselessFreq = generateFrequencyMap(collection,useless,terms,Integer.MAX_VALUE);
		
		Set<Document> onlyBiased = new HashSet<Document>(biasedDocs);
		onlyBiased.removeAll(randomDocs);
		
		Set<Document> onlyRandom = new HashSet<Document>(randomDocs);
		onlyRandom.removeAll(biasedDocs);
		
		Set<Document> bothBiasedRandom = new HashSet<Document>(biasedDocs);
		bothBiasedRandom.retainAll(randomDocs);
		
		Map<String, Double> onlyBiasedFreq = generateFrequencyMap(collection,onlyBiased,terms,Integer.MAX_VALUE);
		
		Map<String, Double> onlyRandomFreq = generateFrequencyMap(collection,onlyRandom,terms,Integer.MAX_VALUE);
		
		Map<String, Double> bothBiasedRandomFreq = generateFrequencyMap(collection,bothBiasedRandom,terms,Integer.MAX_VALUE);
		
		Set<Document> onlyUsefulBiased = new HashSet<Document>(useful);
		onlyUsefulBiased.removeAll(onlyRandom);
		onlyUsefulBiased.removeAll(bothBiasedRandom);
		
		Set<Document> onlyUsefulRandom = new HashSet<Document>(useful);
		onlyUsefulRandom.removeAll(onlyBiased);
		onlyUsefulRandom.removeAll(bothBiasedRandom);
		
		Set<Document> bothBiasedRandomUseful = new HashSet<Document>(useful);
		bothBiasedRandomUseful.removeAll(onlyBiased);
		bothBiasedRandomUseful.removeAll(onlyRandom);
		
		Set<Document> onlyUselessBiased = new HashSet<Document>(useless);
		onlyUselessBiased.removeAll(onlyRandom);
		onlyUselessBiased.removeAll(bothBiasedRandom);
		
		Set<Document> onlyUselessRandom = new HashSet<Document>(useless);
		onlyUselessRandom.removeAll(onlyBiased);
		onlyUselessRandom.removeAll(bothBiasedRandom);
		
		Set<Document> bothBiasedRandomUseless = new HashSet<Document>(useless);
		bothBiasedRandomUseless.removeAll(onlyBiased);
		bothBiasedRandomUseless.removeAll(onlyRandom);
		
		Map<String, Double> onlyUsefulBiasedFreq = generateFrequencyMap(collection,onlyUsefulBiased,terms,Integer.MAX_VALUE);
		Map<String, Double> onlyUsefulRandomFreq = generateFrequencyMap(collection,onlyUsefulRandom,terms,Integer.MAX_VALUE);
		Map<String, Double> bothBiasedRandomUsefulFreq = generateFrequencyMap(collection,bothBiasedRandomUseful,terms,Integer.MAX_VALUE);
		Map<String, Double> onlyUselessBiasedFreq = generateFrequencyMap(collection,onlyUselessBiased,terms,Integer.MAX_VALUE);
		Map<String, Double> onlyUselessRandomFreq = generateFrequencyMap(collection,onlyUselessRandom,terms,Integer.MAX_VALUE);
		Map<String, Double> bothBiasedRandomUselessFreq = generateFrequencyMap(collection,bothBiasedRandomUseless,terms,Integer.MAX_VALUE);
				
		BufferedWriter bw = new BufferedWriter(new FileWriter("data/tmp/"+relation+"stats"+-k+".csv"));		
		
		List<Map<String,Double>> allVals = new ArrayList<Map<String,Double>>();
		
		bw.write("term");
		
		allVals.add(biasedFreq);
		bw.write(",Biased");
		allVals.add(randomFreq);
		bw.write(",Random");
		allVals.add(usefulFreq);
		bw.write(",Useful");
		allVals.add(uselessFreq);
		bw.write(",Useless");

		
		allVals.add(onlyBiasedFreq);
		bw.write(",O.Biased");
		allVals.add(onlyRandomFreq);
		bw.write(",O.Random");
		allVals.add(bothBiasedRandomFreq);
		bw.write(",B.BR");
		allVals.add(onlyUsefulBiasedFreq);
		bw.write(",O.UFB");
		allVals.add(onlyUsefulRandomFreq);
		bw.write(",O.UFR");
		allVals.add(bothBiasedRandomUsefulFreq);
		bw.write(",B.BRUF");
		allVals.add(onlyUselessBiasedFreq);
		bw.write(",O.ULB");
		allVals.add(onlyUselessRandomFreq);
		bw.write(",O.ULR");
		allVals.add(bothBiasedRandomUselessFreq);
		bw.write(",B.BRUL");
		
		bw.newLine();
		
		for (String term : terms) {
			
			bw.write(term);
			
			for (Map<String, Double> map : allVals) {
				bw.write("," + map.get(term));
			}
			
			bw.newLine();
			
		}
		
		bw.close();
		
	}

	private static Map<String, Double> generateFrequencyMap(
			TextCollection collection, Set<Document> docs,
			Set<String> terms, int docsPerQuery) {
		
		Map<String,Double> ret = new HashMap<String, Double>();
		
		for (String term : terms) {
			
			if (docs.isEmpty()){
				ret.put(term, 0.0);
			}else{
				double d = collection.matchingItems(new Query(term),docs,docsPerQuery);
				ret.put(term, d/(double)docs.size());
			}
		}
		
		return ret;
	}

	private static Set<String> getAllTerms(Set<Document> docs) {
		
		Set<String> terms = new HashSet<String>();
		
		for (Document document : docs) {
			
			terms.addAll(document.getTerms());
			
		}
		
		return terms;
	}

	private static void populate(Set<Document> biasedDocs,
			InformationExtractionSystem ies, Set<Document> useful,
			Set<Document> useless) {
		
		for (Document document : biasedDocs) {
			
			if (ies.extract(document) != null && !ies.extract(document).isEmpty()){
				
				useful.add(document);
				
			} else {
				
				useless.add(document);
				
			}
			
		}

		
	}

	private static Set<Document> getDocuments(TextCollection collection,
			List<Query> queries, int k) {
		
		Set<Document> d = new HashSet<Document>();
		
		for (int i = 0; i < queries.size(); i++) {
			
			d.addAll(collection.search(queries.get(i), k));
			
		}
		
		return d;
		
		
	}

}
