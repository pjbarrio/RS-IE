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
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.sample.generator.SampleGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.impl.QBSSampleGenerator;
import edu.cs.columbia.iesrcsel.sample.generator.utils.QuerySelectionStrategy;
import edu.cs.columbia.iesrcsel.sample.generator.utils.impl.AvgTfIdfQuerySelection;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class ReportResultsOfQueriesForProposalVersion {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String[] relations = {/*"VotingResult","PersonCareer",*/"NaturalDisaster"};
		String extractor = "SSK";
		String[] cols = {/*"topfeaturesarts","topfeaturestravel",*/"topnewshealth"};
		int[] ks = {/*100,250,500,*/750/*,1000*/};
		
		for (String col : cols) {
		
			TextCollection collection = new IndriCollection("health", "data/indexes/onlyNotStemmedWords_"+col+".idx");		
			
			Set<String> allTerms = new HashSet<String>();
			
			for (int i = 1; i <= collection.size(); i++) {
				
				Document d = new Document(collection,i);
				
				allTerms.addAll(collection.getTerms(d));
				
			}
			
			for (String relation : relations) {
			
				for (int k : ks) {
					
					InformationExtractionSystem ies = new CachedInformationExtractionSystem(relation+extractor, relation, null, "NYT");
									
					BufferedWriter bsw = new BufferedWriter(new FileWriter("data/proposal/"+relation+"-" + col +"-" +k+ "-" +extractor+ ".csv"));
					
					bsw.write("term, useful, useless");
					
					BufferedWriter bsw2 = new BufferedWriter(new FileWriter("data/proposal/terms.map."+relation+"-" + col +"-" +k+ "-" +extractor+ ".csv"));
					
					bsw2.write("term, document, useful");
					
					for (String term : allTerms) {
						
						List<ScoredDocument> results = collection.search(new Query(term), k);
						
						int useful = 0;
						int useless = 0;
						
						for (ScoredDocument scoredDocument : results) {
							bsw2.newLine();
							if (!ies.extract(scoredDocument).isEmpty()){
								useful++;
								bsw2.write(term + "," + scoredDocument.getId() + ", 1");
							}else{
								useless++;
								bsw2.write(term + "," + scoredDocument.getId() + ", 0");
							}
							
						}
						
						bsw.newLine();
						bsw.write(term + "," + useful + "," + useless);
						
					}
					
					bsw.close();
					
					bsw2.close();
					
					
				}
				
			}
			
		}
		
		
		
	}

}
