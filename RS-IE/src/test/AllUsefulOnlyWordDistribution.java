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
import java.util.Map.Entry;
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

public class AllUsefulOnlyWordDistribution {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		String relations[] = {"NaturalDisaster","ManMadeDisaster","VotingResult","PersonCareer","Indictment-Arrest-Trial"};
		String extractor = "SSK";

		String[] dss = {"topnewshealth","topclassifiedsautomobiles","topfeaturestravel","topnewsscience","topfeaturesstyle","topfeaturesarts","topnewstechnology","topnewsobituaries"};

		for (int k = 0; k < dss.length; k++) {

			String ds = dss[k];
			
			TextCollection collection = new IndriCollection("health", "data/indexes/onlyNotStemmedWords_"+ds+".idx");		

			for (int j = 0; j < relations.length; j++) {

				String relation = relations[j];

				System.out.println("Relation rel: ");

				InformationExtractionSystem ies = new CachedInformationExtractionSystem(relation+extractor, relation, null, "NYT");
				
//				printAllWords(collection,ies,relation,ds);

				printAllWordsByQuery(collection,ies,relation,ds);
				
			}

		}



	}

	private static void printAllWordsByQuery(TextCollection collection,
			InformationExtractionSystem ies, String relation, String ds) throws IOException {
		
		int numOfQueries = 20;
		int docsperQuery = 100;
		String extractor = "SSK";
		int split = 1;
		boolean tupleAsStopWord = true;
		
		List<Query> queries = ((List<Query>)SerializationHelper.deserialize("data/queries/" + relation + "." + extractor + "." + split + "." + tupleAsStopWord + ".ser"));

		List<Set<Document>> d = new ArrayList<Set<Document>>();
		
		Set<Document> allDocs = new HashSet<Document>();
		
		for (int i = 0; i < queries.size() && i < numOfQueries; i++) {
			
			d.add(new HashSet<Document>(collection.search(queries.get(i), docsperQuery)));
			
			allDocs.addAll(d.get(i));
			
		}
		
		List<Map<String,Integer>> freqs = new ArrayList<Map<String,Integer>>(d.size());
		
		Set<String> terms = getAllTerms(allDocs);
		
		for (int i = 0; i < d.size(); i++) {
			freqs.add(wordFreq(d.get(i)));
		}
		
		BufferedWriter bw = new BufferedWriter(new FileWriter("data/tmp/"+relation+"bybiasedquery"+ds+"wordmap"+numOfQueries + "-"+ docsperQuery+".csv"));
		
		bw.write("term");
		
		for (int i = 0; i < queries.size() && i < numOfQueries; i++) {
			bw.write("," + queries.get(i).toString());
		}
		
		bw.write("\n");
		
		for (String term : terms) {
			
			bw.write(term);
			
			for (int i = 0; i < queries.size() && i < numOfQueries; i++) {
				
				Integer val = freqs.get(i).get(term);
				
				if (val == null){
					val = 0;
				}
				
				double ff = (double)val / (double)d.get(i).size();
				
				bw.write("," + ff);

			}
			
			bw.write("\n");

		}
		
		bw.close();
		
	}

	private static void printAllWords(TextCollection collection, InformationExtractionSystem ies, String relation, String ds) throws IOException {
		
		Set<Document> useful = new HashSet<Document>();

		Set<Document> allDocs = new HashSet<Document>();

		for (int i = 1; i < collection.size(); i++) {

			Document doc = new Document(collection,i);

			if (ies.extract(doc) != null && !ies.extract(doc).isEmpty()){

				useful.add(doc);

			}

			allDocs.add(doc);

		}

		System.out.println(allDocs.size() + " - " + useful.size());

		Map<String,Integer> mapAllFreq = wordFreq(allDocs);

		Map<String,Integer> mapUsefulFreq = wordFreq(useful);

		BufferedWriter bsw = new BufferedWriter(new FileWriter("data/tmp/"+relation+"docwordmap.csv"));

		bsw.write("term, pall, puseful");

		for (String term : mapAllFreq.keySet()) {

			double uffreq = (mapUsefulFreq.get(term)== null? (double)useful.size()/(double)mapUsefulFreq.size() : (double)mapUsefulFreq.get(term));

			bsw.write("\n" + term + "," + (double)mapAllFreq.get(term)/collection.size() + "," + (uffreq)/(double)(useful.size() + useful.size()));

		}

		bsw.close();

		bsw = new BufferedWriter(new FileWriter("data/tmp/alltermsuseful"+ds+".csv"));

		bsw.write("document,term");

		for (Document docu : allDocs) {

			Set<String> ts = docu.getTerms();

			for (String t : ts) {

				bsw.write("\n" + docu.getId() + "," + t);

			}

		}

		bsw.close();

		
	}

	public static Map<String, Integer> wordFreq(Set<Document> allDocs) {

		Map<String, Integer> ret = new HashMap<String,Integer>();

		for (Document document : allDocs) {

			Map<String,Integer> te = document.getTermFreqMap();

			for (Entry<String,Integer> entry : te.entrySet()) {

				Integer in = ret.get(entry.getKey());

				if (in == null){

					in = 0;

				}

				ret.put(entry.getKey(), in+entry.getValue());

			}

		}

		return ret;
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

	public static Set<String> getAllTerms(Set<Document> docs) {

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

	public static Map<String, Double> wordFreqN(Set<Document> docs) {
		
		Map<String,Integer> wf = wordFreq(docs);
		
		Map<String,Double> ret = new HashMap<String,Double>();
		
		for (Entry<String,Integer> entry : wf.entrySet()) {
			ret.put(entry.getKey(), (double)entry.getValue()/(double)docs.size());
		}
	
		return ret;
	}

}
