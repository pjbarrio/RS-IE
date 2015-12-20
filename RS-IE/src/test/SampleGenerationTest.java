package test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import edu.cs.columbia.iesrcsel.model.collection.IndriCollection;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.extractor.impl.CachedInformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.impl.CyclicQuerySampleGeneration;
import edu.cs.columbia.iesrcsel.sample.generator.impl.QBSSampleGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.impl.ReScheduleSampleGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.utils.QuerySelectionStrategy;
import edu.cs.columbia.iesrcsel.sample.generator.utils.ScheduleQueryFunction;
import edu.cs.columbia.iesrcsel.sample.generator.utils.impl.AvgTfIdfQuerySelection;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class SampleGenerationTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
//		createGenericQueries();
		
		createQueries();
		
//		testQBS();
		
//		testCyclic();
		
//		testReschedule();
		
	}

	private static void createGenericQueries() throws IOException {
		
		List<String> queries = FileUtils.readLines(new File("data/queries/ubuntuDictionary.txt"));
		
		List<Query> ubquers = new ArrayList<Query>(queries.size());
		
		for (String query : queries) {
			
			ubquers.add(new Query(query));
			
		}
		
		SerializationHelper.serialize("data/queries/ubuntuDictionary.ser",ubquers);
		
	}

	private static void testReschedule() {
		
		TextCollection collection = new IndriCollection("world", "data/indexes/topnewsworld.idx");
		
		InformationExtractionSystem ie = new CachedInformationExtractionSystem("natdis", "NaturalDisaster", null, "NYT");
		
		String relation = "NaturalDisaster";
		int split =1;
		boolean tupleAsStopWord = true;
		String extractor = "SSK";
		
		List<Query> initialQueries = ((List<Query>)SerializationHelper.deserialize("data/queries/" + relation + "." + extractor + "." + split + "." + tupleAsStopWord + ".ser"));
		
		SampleGenerator sampleG = new ReScheduleSampleGenerator(ie, 2000, 10, 50, new ScheduleQueryFunction(true),initialQueries);
		
		Sample sample = sampleG.generateSample(collection);
		
		for (Document document : sample) {
			System.out.println(document.getPath());
		}
		
	}

	private static void testCyclic() {
		
		String relation = "NaturalDisaster";
		int split = 1;
		boolean tupleAsStopWord= true;
		String extractor = "SSK";

		List<Query> initialQueries = ((List<Query>)SerializationHelper.deserialize("data/queries/" + relation + "." + extractor + "." + split + "." + tupleAsStopWord + ".ser"));
		
		SampleGenerator sampleG = new CyclicQuerySampleGeneration(2000, 1000, 10, initialQueries);
		
		TextCollection collection = new IndriCollection("world", "data/indexes/topnewsworld.idx");
		
		Sample sample = sampleG.generateSample(collection);
		
		for (Document document : sample) {
			System.out.println(document.getPath());
		}
		
	}

	private static void createQueries() throws IOException {
		
		String[] relations = new String[]{"Indictment-Arrest-Trial","ManMadeDisaster","NaturalDisaster","PersonCareer","VotingResult","diseaseOutbreak","orgAff"};
		
		int[] splits = new int[]{1,2,3,4,5};
		
		boolean[] tasws = new boolean[]{true,false};
		
		for (String relation : relations) {
			
			String model = "CRF";
			
			if (relation.equals("ManMadeDisaster"))
				model = "HMM";
			
			String extr = "BONG";
			
			for (int split : splits) {
				
				for (boolean tasw : tasws) {
					
					System.err.println(relation + " - " + split + " - " + tasw);
					
					String queryFile = "../LearningToRankForIE/QUERIES/" + relation + "/"+tasw+"/SelectedAttributes/"+extr+ "-" +relation+"-SF-"+model+"-relationWords_Ranker_ChiSquaredWithYatesCorrectionAttributeEval_"+split+"_5000.words";
					
					if (relation.equals("diseaseOutbreak") || relation.equals("orgAff")){
						queryFile = "../LearningToRankForIE/QUERIES/" + relation + "-" + tasw + "-" + split;
						extr = "DIC";
					}
					
					List<String> initialQ = FileUtils.readLines(new File(queryFile));
					
					List<Query> initialQueries = new ArrayList<Query>(initialQ.size());
	
					for (int i = 0; i < initialQ.size(); i++) {
						
						String[] spl = initialQ.get(i).split(",");
						
						String quer;
						
						if (spl.length > 1)
							quer = spl[1];
						else
							quer = spl[0];
						
						initialQueries.add(new Query(quer));
					}
					
					SerializationHelper.serialize("data/queries/"+relation+"."+extr+"." +split+"." +tasw+ ".ser", initialQueries);

				}
					
			}
			
		}
		
	}

	private static void testQBS() {
		
		QuerySelectionStrategy querySelectionStrategy = new AvgTfIdfQuerySelection();
		
		TextCollection collection = new IndriCollection("world", "data/indexes/topnewsworld.idx");
		
		SampleGenerator sampleG = new QBSSampleGenerator("data/queries/ubuntuDictionary.ser", 2000, 1000, 50, querySelectionStrategy);

		Sample sample = sampleG.generateSample(collection);
		
		for (Document document : sample) {
			System.out.println(document.getPath());
		}
		
	}

}
