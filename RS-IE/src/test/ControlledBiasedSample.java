package test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cs.columbia.iesrcsel.model.collection.IndriCollection;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.extractor.impl.CachedInformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.model.impl.Tuple;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.impl.CyclicQuerySampleGeneration;
import edu.cs.columbia.iesrcsel.sample.generator.impl.QBSSampleGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.utils.QuerySelectionStrategy;
import edu.cs.columbia.iesrcsel.sample.generator.utils.impl.AvgTfIdfQuerySelection;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class ControlledBiasedSample {

	static Map<String,Map<Integer,Double>> uful;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		int split = 1;
		boolean tupleAsStopWord = true;

		List<Query> random = (List<Query>)SerializationHelper.deserialize("data/queries/ubuntuDictionary.ser");

		String relations[] = {"NaturalDisaster","ManMadeDisaster","VotingResult","PersonCareer","Indictment-Arrest-Trial"};
		String extractor = "SSK";

		String[] dss = {"topfeaturestravel","topfeaturesarts"};

		String[] donedss = {"topnewshealth","topclassifiedsautomobiles","topfeaturesstyle","topnewstechnology","topnewsobituaries","topnewsscience"};
		
		int[] docsperQuery = {10/*,30,50*/,100/*,300,500*/,1000};

		int[] numQueries = {50,/*100,150,200,*/250,/*300,350,400,450,*/500};

		int[] sampleSize = {500/*,1000,1500,2000*/,2500/*,3000,3500,4000,4500*/,5000};

		for (int k = 0; k < dss.length; k++) {

			String ds = dss[k];

			TextCollection collection = new IndriCollection("health", "data/indexes/onlyNotStemmedWords_"+ds+".idx");		

			System.out.println("Collection:" + ds);
			
			for (int j = 0; j < relations.length; j++) {

				String relation = relations[j];
				
				String termsFile = "data/controlled/terms."+relation+"." + extractor +"."+ds+".ser";
				
				if (new File(termsFile).exists())
					continue;
				
				uful = new HashMap<String,Map<Integer, Double>>();
				
				System.out.println("Relation: " + relation);

				InformationExtractionSystem ies = new CachedInformationExtractionSystem(relation+extractor, relation, null, "NYT");

				List<Query> biasQueries = ((List<Query>)SerializationHelper.deserialize("data/queries/" + relation + "." + extractor + "." + split + "." + tupleAsStopWord + ".ser"));

				for (int s = 0; s < sampleSize.length; s++) {

					System.out.println("Sample:" + s + " out of " + sampleSize.length);
					
					for (int r = 0; r < docsperQuery.length; r++) {

						System.out.println("DocsPerQuery:" + r + " out of " + docsperQuery.length);
						
						for (int i = 0; i < numQueries.length; i++) {

							System.out.println("NumQueries:" + i + " out of " + numQueries.length);
							
							printVals(relation, extractor, ds, collection,ies,docsperQuery[r],numQueries[i], sampleSize[s], random, biasQueries, docsperQuery);

						}

					}


				}

				SerializationHelper.serialize(termsFile, uful);
				
			}

		}





	}

	private static void printVals(String relation, String extractor, String collectionName, TextCollection collection,
			InformationExtractionSystem ies, int docsPerQuery,
			int numQueries, int sampleSize, List<Query> random, List<Query> biasQueries, int[] docsperTerm) {

		String randomFile = "data/controlled/random."+relation+"." + extractor +"."+collectionName+"."+docsPerQuery+"."+numQueries+"."+sampleSize+".ser";
		
		String termsFile = "data/controlled/termsTemp."+relation+"." + extractor +"."+collectionName+".ser";
		
		if (new File(randomFile).exists()){
			if (uful==null || uful.isEmpty())
				uful = (Map<String,Map<Integer,Double>>)SerializationHelper.deserialize(termsFile);
			return;
		}
		
		QuerySelectionStrategy qss = new AvgTfIdfQuerySelection();

		SampleGenerator qbs = new QBSSampleGenerator(random, sampleSize, numQueries, docsPerQuery, qss);

		Set<Document> randomDocs = qbs.generateSample(collection).getDocuments();

		SampleGenerator csg = new CyclicQuerySampleGeneration(sampleSize, numQueries, docsPerQuery, biasQueries);

		Set<Document> biasedDocs = csg.generateSample(collection).getDocuments();

		Set<Document> useful = new HashSet<Document>();

		Set<Document> useless = new HashSet<Document>();

		Set<Document> allDocs = new HashSet<Document>();

		for (Document document : randomDocs) {

			List<Tuple> aux = ies.extract(document);
			
			if (aux != null && !aux.isEmpty()){

				useful.add(document);

			}else{
				useless.add(document);
			}

			allDocs.add(document);

		}

		for (Document document : biasedDocs) {

			List<Tuple> aux = ies.extract(document);
			
			if (aux != null && !aux.isEmpty()){

				useful.add(document);

			}else{
				useless.add(document);
			}

			allDocs.add(document);

		}

		Set<String> terms = AllUsefulOnlyWordDistribution.getAllTerms(allDocs);

		Map<String,Double> ufulfreq = AllUsefulOnlyWordDistribution.wordFreqN(useful);

		Map<String,Double> ulessfreq = AllUsefulOnlyWordDistribution.wordFreqN(useless);

		Map<String,Double> randomFreq = AllUsefulOnlyWordDistribution.wordFreqN(randomDocs);

		for (String term : terms) {

			if (uful.containsKey(term))
				continue;

			List<ScoredDocument> docs = collection.search(new Query(term));
						
			if (docs == null) //an error happened
				continue;
			
			Map<Integer,Double> map = new HashMap<Integer,Double>();

			for (int i = 0; i < docsperTerm.length; i++) {

				int count = 0;

				for (int j = 0; j < docs.size() && j < docsperTerm[i]; j++) {

					if (ies.extract(docs.get(j)) != null && !ies.extract(docs.get(j)).isEmpty()){

						count++;

					}

				}
				if (docs.size() > 0)
					map.put(docsperTerm[i], (double)count/(double)Math.min(docs.size(), docsperTerm[i]));
				else{
					map.put(docsperTerm[i], 0.0);
				}
			}

			uful.put(term, map);

		}

		SerializationHelper.serialize("data/controlled/terms."+relation+"." + extractor +"."+collectionName+"."+docsPerQuery+"."+numQueries+"."+sampleSize+".ser", terms);
		SerializationHelper.serialize("data/controlled/useful."+relation+"." + extractor +"."+collectionName+"."+docsPerQuery+"."+numQueries+"."+sampleSize+".ser", ufulfreq);
		SerializationHelper.serialize("data/controlled/useless."+relation+"." + extractor +"."+collectionName+"."+docsPerQuery+"."+numQueries+"."+sampleSize+".ser", ulessfreq);
		SerializationHelper.serialize(randomFile, randomFreq);
		SerializationHelper.serialize(termsFile, uful);
	}

}
