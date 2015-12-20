package test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
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
import edu.cs.columbia.iesrcsel.sample.generator.SampleGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.impl.CyclicQuerySampleGeneration;
import edu.cs.columbia.iesrcsel.sample.generator.impl.QBSSampleGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.utils.QuerySelectionStrategy;
import edu.cs.columbia.iesrcsel.sample.generator.utils.impl.AvgTfIdfQuerySelection;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class ControlledBiasedSampleToR {

	private static final String UFUL = "uful";
	private static final String ULESS = "uless";
	private static final String RAND = "rand";

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		String relations[] = {"NaturalDisaster","ManMadeDisaster","VotingResult","PersonCareer","Indictment-Arrest-Trial"};
		String extractor = "SSK";

		String[] dss = {"topfeaturestravel","topfeaturesarts"/*,"topnewshealth","topclassifiedsautomobiles","topnewsscience","topfeaturesstyle","topnewstechnology","topnewsobituaries"*/};

		int[] docsperQuery = {10/*,30,50*/,100/*,300,500*/,1000};

		int[] numQueries = {50/*,100,150,200*/,250/*,300,350,400,450*/,500};

		int[] sampleSize = {500/*,1000,1500,2000*/,2500/*,3000,3500,4000,4500*/,5000};

		for (int k = 0; k < dss.length; k++) {

			String ds = dss[k];

			System.out.println("Collection:" + ds);

			for (int j = 0; j < relations.length; j++) {

				String relation = relations[j];

				System.out.println("Relation: " + relation);

				//Change Number of Queries

				for (int s = 0; s < sampleSize.length; s++) {

					System.out.println("Sample:" + s + " out of " + sampleSize.length);

					for (int r = 0; r < docsperQuery.length; r++) {

						System.out.println("DocsPerQuery:" + r + " out of " + docsperQuery.length);

						Map<String,Map<String,Double>> toPrint = new HashMap<String,Map<String,Double>>();
						
						for (int i = 0; i < numQueries.length; i++) {

							System.out.println("NumQueries:" + i + " out of " + numQueries.length);

							Set<String> terms = (Set<String>)SerializationHelper.deserialize("data/controlled/terms."+relation+"." + extractor +"."+ds+"."+docsperQuery[r]+"."+numQueries[i]+"."+sampleSize[s]+".ser");
							Map<String,Double> ufulfreq = (Map<String,Double>)SerializationHelper.deserialize("data/controlled/useful."+relation+"." + extractor +"."+ds+"."+docsperQuery[r]+"."+numQueries[i]+"."+sampleSize[s]+".ser");
							Map<String,Double> ulessfreq = (Map<String,Double>)SerializationHelper.deserialize("data/controlled/useless."+relation+"." + extractor +"."+ds+"."+docsperQuery[r]+"."+numQueries[i]+"."+sampleSize[s]+".ser");
							Map<String,Double> randomfreq = (Map<String,Double>)SerializationHelper.deserialize("data/controlled/random."+relation+"." + extractor +"."+ds+"."+docsperQuery[r]+"."+numQueries[i]+"."+sampleSize[s]+".ser");

							for (String term : terms) {
								
								Map<String,Double> map = toPrint.get(term);
								
								if (map == null){
									map = new HashMap<String,Double>();
									toPrint.put(term, map);
								}
								
								map.put(UFUL + "." + numQueries[i], (ufulfreq.get(term) == null)? 0.0 : ufulfreq.get(term));
								map.put(ULESS + "." + numQueries[i], (ulessfreq.get(term) == null)? 0.0 : ulessfreq.get(term));
								map.put(RAND + "." + numQueries[i], (randomfreq.get(term) == null)? 0.0 : randomfreq.get(term));
							}
							
						}

						BufferedWriter bw = new BufferedWriter(new FileWriter(new File("data/controlled/term."+relation+"." + extractor +"."+ds+"."+docsperQuery[r]+".NQ_VAR."+sampleSize[s]+".csv")));

						bw.write("term");
						
						for (int i = 0; i < numQueries.length; i++) {
							bw.write("," + UFUL + "." + numQueries[i] + "," +  ULESS + "." + numQueries[i] + "," + RAND + "." + numQueries[i]);
						}
						
						bw.newLine();
						
						for (Entry<String,Map<String,Double>> entry : toPrint.entrySet()) {
							
							bw.write(entry.getKey());
							
							for (int i = 0; i < numQueries.length; i++) {
								
								bw.write("," + getVal(entry.getValue().get(UFUL + "." + numQueries[i])));
								bw.write("," + getVal(entry.getValue().get(ULESS + "." + numQueries[i])));
								bw.write("," + getVal(entry.getValue().get(RAND + "." + numQueries[i])));
								
							}
							
							bw.newLine();
							
						}
						
						bw.close();
						
					}

				}
/*
				//Change DocsPerQuery

				for (int s = 0; s < sampleSize.length; s++) {

					System.out.println("Sample:" + s + " out of " + sampleSize.length);

					for (int i = 0; i < numQueries.length; i++) {

						System.out.println("NumQueries:" + i + " out of " + numQueries.length);

						Map<String,Map<String,Double>> toPrint = new HashMap<String,Map<String,Double>>();
												
						for (int r = 0; r < docsperQuery.length; r++) {

							System.out.println("DocsPerQuery:" + r + " out of " + docsperQuery.length);

							Set<String> terms = (Set<String>)SerializationHelper.deserialize("data/controlled/terms."+relation+"." + extractor +"."+ds+"."+docsperQuery[r]+"."+numQueries[i]+"."+sampleSize[s]+".ser");
							Map<String,Double> ufulfreq = (Map<String,Double>)SerializationHelper.deserialize("data/controlled/useful."+relation+"." + extractor +"."+ds+"."+docsperQuery[r]+"."+numQueries[i]+"."+sampleSize[s]+".ser");
							Map<String,Double> ulessfreq = (Map<String,Double>)SerializationHelper.deserialize("data/controlled/useless."+relation+"." + extractor +"."+ds+"."+docsperQuery[r]+"."+numQueries[i]+"."+sampleSize[s]+".ser");
							Map<String,Double> randomfreq = (Map<String,Double>)SerializationHelper.deserialize("data/controlled/random."+relation+"." + extractor +"."+ds+"."+docsperQuery[r]+"."+numQueries[i]+"."+sampleSize[s]+".ser");

							for (String term : terms) {
								
								Map<String,Double> map = toPrint.get(term);
								
								if (map == null){
									map = new HashMap<String,Double>();
									toPrint.put(term, map);
								}
								
								map.put(UFUL + "." + docsperQuery[r], (ufulfreq.get(term) == null)? 0.0 : ufulfreq.get(term));
								map.put(ULESS + "." + docsperQuery[r], (ulessfreq.get(term) == null)? 0.0 : ulessfreq.get(term));
								map.put(RAND + "." + docsperQuery[r], (randomfreq.get(term) == null)? 0.0 : randomfreq.get(term));
							}
							
						}

						BufferedWriter bw = new BufferedWriter(new FileWriter(new File("data/controlled/terms."+relation+"." + extractor +"."+ds+".DPQ_VAR."+numQueries[i]+"."+sampleSize[s]+".csv")));

						bw.write("term");
						
						for (int r = 0; r < docsperQuery.length; r++) {
							bw.write("," + UFUL + "." + docsperQuery[r] + "," +  ULESS + "." + docsperQuery[r] + "," + RAND + "." + docsperQuery[r]);
						}
						
						bw.newLine();
						
						for (Entry<String,Map<String,Double>> entry : toPrint.entrySet()) {
							
							bw.write(entry.getKey());
							
							for (int r = 0; r < docsperQuery.length; r++) {
								
								bw.write("," + getVal(entry.getValue().get(UFUL + "." + docsperQuery[r])));
								bw.write("," + getVal(entry.getValue().get(ULESS + "." + docsperQuery[r])));
								bw.write("," + getVal(entry.getValue().get(RAND + "." + docsperQuery[r])));
								
							}
							
							bw.newLine();
							
						}
						
						bw.close();
						
					}

				}
*/
				//Change SampleSize

				/*

				for (int r = 0; r < docsperQuery.length; r++) {

					System.out.println("DocsPerQuery:" + r + " out of " + docsperQuery.length);

					for (int i = 0; i < numQueries.length; i++) {

						System.out.println("NumQueries:" + i + " out of " + numQueries.length);

						Map<String,Map<String,Double>> toPrint = new HashMap<String,Map<String,Double>>();
						
						for (int s = 0; s < sampleSize.length; s++) {

							System.out.println("Sample:" + s + " out of " + sampleSize.length);

							Set<String> terms = (Set<String>)SerializationHelper.deserialize("data/controlled/terms."+relation+"." + extractor +"."+ds+"."+docsperQuery[r]+"."+numQueries[i]+"."+sampleSize[s]+".ser");
							Map<String,Double> ufulfreq = (Map<String,Double>)SerializationHelper.deserialize("data/controlled/useful."+relation+"." + extractor +"."+ds+"."+docsperQuery[r]+"."+numQueries[i]+"."+sampleSize[s]+".ser");
							Map<String,Double> ulessfreq = (Map<String,Double>)SerializationHelper.deserialize("data/controlled/useless."+relation+"." + extractor +"."+ds+"."+docsperQuery[r]+"."+numQueries[i]+"."+sampleSize[s]+".ser");
							Map<String,Double> randomfreq = (Map<String,Double>)SerializationHelper.deserialize("data/controlled/random."+relation+"." + extractor +"."+ds+"."+docsperQuery[r]+"."+numQueries[i]+"."+sampleSize[s]+".ser");

							for (String term : terms) {
								
								Map<String,Double> map = toPrint.get(term);
								
								if (map == null){
									map = new HashMap<String,Double>();
									toPrint.put(term, map);
								}
								
								map.put(UFUL + "." + sampleSize[s], (ufulfreq.get(term) == null)? 0.0 : ufulfreq.get(term));
								map.put(ULESS + "." + sampleSize[s], (ulessfreq.get(term) == null)? 0.0 : ulessfreq.get(term));
								map.put(RAND + "." + sampleSize[s], (randomfreq.get(term) == null)? 0.0 : randomfreq.get(term));
							}
							
						}

						BufferedWriter bw = new BufferedWriter(new FileWriter(new File("data/controlled/terms."+relation+"." + extractor +"."+ds+"."+docsperQuery[r]+"."+numQueries[i]+".SS_VAR.csv")));

						bw.write("term");
						
						for (int s = 0; s < sampleSize.length; s++) {
							bw.write("," + UFUL + "." + sampleSize[s] + "," +  ULESS + "." + sampleSize[s] + "," + RAND + "." + sampleSize[s]);
						}
						
						bw.newLine();
						
						for (Entry<String,Map<String,Double>> entry : toPrint.entrySet()) {
							
							bw.write(entry.getKey());
							
							for (int s = 0; s < sampleSize.length; s++) {
								
								bw.write("," + getVal(entry.getValue().get(UFUL + "." + sampleSize[s])));
								bw.write("," + getVal(entry.getValue().get(ULESS + "." + sampleSize[s])));
								bw.write("," + getVal(entry.getValue().get(RAND + "." + sampleSize[s])));
								
							}
							
							bw.newLine();
							
						}
						
						bw.close();

					}

				}
*/
				Map<String,Map<Integer,Double>> uful = (Map<String,Map<Integer,Double>>)SerializationHelper.deserialize("data/controlled/terms."+relation+"." + extractor +"."+ds+".ser");

				BufferedWriter bw = new BufferedWriter(new FileWriter(new File("data/controlled/terms."+relation+"." + extractor +"."+ds+".csv")));

				bw.write("term");

				for (int q = 0; q < docsperQuery.length; q++) {
					bw.write("," + docsperQuery[q]);
				}

				bw.newLine();

				for (Entry<String,Map<Integer,Double>> entry : uful.entrySet()) {

					bw.write(entry.getKey());

					for (int q = 0; q < docsperQuery.length; q++) {

						bw.write("," + entry.getValue().get(docsperQuery[q]));

					}

					bw.newLine();

				}

				bw.close();
			}

		}

	}

	private static double getVal(Double val) {
		if (val == null)
			return 0.0;
		return val;
	}


}
