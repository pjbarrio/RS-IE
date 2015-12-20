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

public class AllUsefulOnlyWordPrecisionDistribution {

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
				
				printAllWords(collection,ies,relation,ds);

			}

		}



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

		BufferedWriter bsw = new BufferedWriter(new FileWriter("data/tmp/"+relation+"-"+ds + "precision.csv"));

		bsw.write("term, precision, allFreq");

		for (String term : mapAllFreq.keySet()) {

			double uffreq = (mapUsefulFreq.get(term)== null? 0 : (double)mapUsefulFreq.get(term)/(double)mapAllFreq.get(term));

			bsw.write("\n" + term + "," + uffreq + "," + (double)mapAllFreq.get(term));

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


	public static Set<String> getAllTerms(Set<Document> docs) {

		Set<String> terms = new HashSet<String>();

		for (Document document : docs) {

			terms.addAll(document.getTerms());

		}

		return terms;
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
