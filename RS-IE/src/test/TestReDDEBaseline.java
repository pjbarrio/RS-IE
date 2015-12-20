package test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cs.columbia.iesrcsel.collection.estimation.CollectionSizeEstimator;
import edu.cs.columbia.iesrcsel.collection.estimation.impl.ExactCollectionSizeEstimator;
import edu.cs.columbia.iesrcsel.model.collection.CrawledLuceneCollection;
import edu.cs.columbia.iesrcsel.model.impl.Sample;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.query.generator.QueryGenerator;
import edu.cs.columbia.iesrcsel.query.generator.impl.CachedQueryGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.impl.QBSSampleGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.utils.QuerySelectionStrategy;
import edu.cs.columbia.iesrcsel.sample.generator.utils.impl.AvgTfIdfQuerySelection;
import edu.cs.columbia.iesrcsel.score.estimation.method.ScoreCalculator;
import edu.cs.columbia.iesrcsel.score.estimation.method.impl.ReDDEOriginalScoreCalculatorWithOverlapCorrection;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class TestReDDEBaseline {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		int numberOfCollections = 100;
		int numberOfQueries = 50;
		
		String task = "training";
		Set<String> set = (Set<String>) SerializationHelper.deserialize("data/stats/"+task+"0.15_1000.ser");
		Map<String,String> hostwebmap =  (Map<String,String>) SerializationHelper.deserialize("data/stats/host_web_map.ser");
		
		
		String relation = "ManMadeDisaster";
		String extractor = "SSK";
		String split = "1";
		String tasw = "true";
		
		String extractorFile = "Sub-sequences";
		
		if (extractor.equals("BONG"))
			extractorFile = "N-Grams";
		
		Map<String,Integer> usefuls = (Map<String,Integer>) SerializationHelper.deserialize("data/stats/"+task+"0.15_1000_useful"+extractorFile+"_"+relation+".ser");
		
		CollectionSizeEstimator collectionSizeEstimator = new ExactCollectionSizeEstimator(); 
		double relevanceRatio = 0.003d;
				
		ScoreCalculator<Sample, Sample> calculator = new ReDDEOriginalScoreCalculatorWithOverlapCorrection(relevanceRatio, collectionSizeEstimator);
				
		QueryGenerator qg = new CachedQueryGenerator("data/queries/" + relation + "." + extractor + "." + split + "." + tasw + ".ser",numberOfQueries);
		
		QuerySelectionStrategy querySelectionStrategy = new AvgTfIdfQuerySelection();
		
		SampleGenerator sg = new QBSSampleGenerator("data/queries/ubuntuDictionary.ser", 300, 150, 4, querySelectionStrategy, new HashMap<String, String>(0));
		
		List<Sample> samples = new ArrayList<Sample>();
		
		int nc = 0;
		
		List<TextCollection> tcs = new ArrayList<TextCollection>();
		
		for (String website : set) {

			if (nc++ > numberOfCollections)
				break;
			
			System.out.println("Sampling: " + website);
			
			String url = website.replaceAll("\\p{Punct}", "");

			TextCollection tc = new CrawledLuceneCollection(url, hostwebmap.get(website));

			samples.add(sg.generateSample(tc));
			
		}
					
		calculator.initialize(samples, qg);
		
		for (TextCollection textCollection : tcs) {
			textCollection.close();
		}
		
		nc = 0;
		
		for (String website : set) {

			if (nc++ > numberOfCollections)
				break;
			
			String url = website.replaceAll("\\p{Punct}", "");

			TextCollection tc = new CrawledLuceneCollection(url, hostwebmap.get(website));
			
			int ufuls = (usefuls.get(website) == null ? 0 : usefuls.get(website));
			
			System.out.format("%s,%f,%d,%d \n",website,calculator.estimateScore(sg.generateSample(tc), qg),ufuls,tc.size());
		
			tc.close();
			
		}
		
	}

}
