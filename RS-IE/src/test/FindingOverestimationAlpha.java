package test;

import java.io.BufferedWriter;
import java.io.File;
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

import edu.cs.columbia.iesrcsel.model.collection.IndriCollection;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.extractor.impl.CachedInformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class FindingOverestimationAlpha {

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
				
				sampleBiased(collection,ies,relation,ds,extractor);
				
			}

		}

		

	}

	private static void sampleBiased(TextCollection collection,
			InformationExtractionSystem ies, String relation, String ds, String extractor) throws IOException {
		
		int split = 1;
		boolean tupleAsStopWord = false;
		int numberOfQueries = 100;
		int resultsPerQuery = 30;
		
		List<Query> biased = ((List<Query>)SerializationHelper.deserialize("data/queries/" + relation + "." + extractor + "." + split + "." + tupleAsStopWord + ".ser"));

		for (int i = 0; i < biased.size() && i < numberOfQueries; i++) {
			System.out.println(i + "-" + biased.get(i).toString());
		}
		
		
		
		Map<Query, Map<String, Integer>> mter = new HashMap<Query, Map<String, Integer>>();
		Map<Query, Integer> mq = new HashMap<Query,Integer>(numberOfQueries);
		
		Set<Document> biasedDocs = getDocuments(collection,biased.subList(0, numberOfQueries),resultsPerQuery,mter,mq);

		Set<Document> useful = new HashSet<Document>();
		Set<Document> useless = new HashSet<Document>();
		
		Set<String> terms = getAllTerms(biasedDocs);
			
		for (Document document : biasedDocs) {
			
			if (ies.extract(document).isEmpty())
				useless.add(document);
			else
				useful.add(document);
		}
		
		List<Query> queries = new ArrayList<Query>(terms.size());
		
		Map<String,int[]> samplePerformance = new HashMap<String,int[]>();
		
		for (String term : terms) {
			queries.add(new Query(term));
			samplePerformance.put(term, new int[]{0,0});
		}
		
		HashMap<String, int[]> collPerformance = new HashMap<String,int[]>();
		
		for (Query query : queries) {
			
			List<ScoredDocument> results = collection.search(query);
			
			int usefuls=0,uselesses=0,ufuls=0,ulesss=0;
			
			for (ScoredDocument scoredDocument : results) {
				if (ies.extract(scoredDocument).isEmpty()){
					uselesses++;
					if (biasedDocs.contains(scoredDocument))
						ulesss++;
				}
				else{
					usefuls++;
					if (biasedDocs.contains(scoredDocument))
						ufuls++;
				}
			}
			
			collPerformance.put(query.getTerms()[0], new int[]{usefuls,uselesses});
			samplePerformance.put(query.getTerms()[0], new int[]{ufuls,ulesss});
			
		}
		
		//Need to obtain the fraction of useful documents in the collection
		
		int uful=0;
		
		for (int i = 0; i < collection.size(); i++) {
			
			if (!ies.extract(new Document(collection,i)).isEmpty())
				uful++;
			
		}
		
		double fractionCollection = (double)uful / (double)collection.size();
		
		double fractionSample = (double)useful.size() / (double)biasedDocs.size();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("data/tmp/COL." +ds+"R."+relation+"ratios.RPQ."+resultsPerQuery+"TASW."+tupleAsStopWord+"Q."+numberOfQueries+"SPL"+split+"EX."+extractor+".csv")));
		
		bw.write("term,usefulSample,uselessSample,sampleSize,usefulCollection,uselessCollection,fractionSample,fractionCollection");
		
		for (String term : terms) {
			
			bw.newLine();
			
			bw.write(term + "," + samplePerformance.get(term)[0] + "," + samplePerformance.get(term)[1] + "," + biasedDocs.size() + ","+ collPerformance.get(term)[0] + "," + collPerformance.get(term)[1] +"," + fractionSample + "," + fractionCollection);
			
		}		
		
		bw.close();
		
		bw = new BufferedWriter(new FileWriter(new File("data/tmp/COL." +ds+"R."+relation+"ratios.RPQ."+resultsPerQuery+"TASW."+tupleAsStopWord+"Q."+numberOfQueries+"SPL"+split+"EX."+extractor+"-terms.csv")));
		
		bw.write("term");
		
		for (Query query : biased.subList(0, numberOfQueries)) {
			bw.write("," + query.toString() + "," + query.toString()+".total");
		}
		
		bw.newLine();
		
		for (String term : terms) {
			
			bw.write(term);
			
			for (Query query : biased.subList(0, numberOfQueries)) {
				
				Integer freq = mter.get(query).get(term);
				
				if (freq == null){
					freq = 0;
				}
				
				bw.write("," + freq + "," + mq.get(query));
			}			
			
			bw.newLine();
			
		}
		
		bw.close();
		
	}

	private static Set<String> getAllTerms(Set<Document> docs) {
		
		Set<String> terms = new HashSet<String>();
		
		for (Document document : docs) {
			
			terms.addAll(document.getTerms());
			
		}
		
		return terms;
	}

	
	private static Set<Document> getDocuments(TextCollection collection,
			List<Query> queries, int k, Map<Query,Map<String,Integer>> mapTerms, Map<Query,Integer> mapQuery) {
		
		Set<Document> d = new HashSet<Document>();
		
		for (int i = 0; i < queries.size(); i++) {
			
			List<ScoredDocument> dos = collection.search(queries.get(i), k);
			
			d.addAll(dos);
			
			mapQuery.put(queries.get(i), dos.size());
			
			Map<String,Integer> mm = new HashMap<String,Integer>();
			
			for (int j = 0; j < dos.size(); j++) {
				
				Map<String, Integer> terms = dos.get(j).getTermFreqMap();
				
				for (Entry<String,Integer> entry : terms.entrySet()) {
					
					Integer freq = mm.get(entry.getKey());
					
					if (freq == null) {
						
						freq = 0;
						
					}
					
					mm.put(entry.getKey(), freq + entry.getValue());
					
				}
				
			}

			mapTerms.put(queries.get(i), mm);
		
		}
		
		return d;
		
		
	}
	
}
