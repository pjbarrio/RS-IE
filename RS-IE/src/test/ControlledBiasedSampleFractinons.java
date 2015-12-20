package test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class ControlledBiasedSampleFractinons {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		String relations[] = {"NaturalDisaster","ManMadeDisaster","VotingResult","PersonCareer","Indictment-Arrest-Trial"};
		String extractor = "SSK";

		String[] dss = {"topfeaturestravel","topnewshealth","topclassifiedsautomobiles","topnewsscience","topfeaturesstyle","topfeaturesarts","topnewstechnology","topnewsobituaries"};

		System.out.println("collection, relation, numberOfTuples, collectionSize, fraction");
		
		for (String ds : dss) {
			
			TextCollection collection = new IndriCollection("health", "data/indexes/onlyNotStemmedWords_"+ds+".idx");		

			Map<String, List<Document>> termsAll = new HashMap<String, List<Document>>();
			
			Set<Document> documents = new HashSet<Document>();
						
			for (int i=1;i<=collection.size();i++){
				
				Document d = new Document(collection, i);
				
				documents.add(d);
				
				Set<String> termsAl = d.getTerms();
				
				for (String t : termsAl) {
					
					List<Document> doc = termsAll.get(t);
					
					if (doc == null){
						doc = new ArrayList<Document>();
						termsAll.put(t, doc);
					}
					
					doc.add(d);
				}

			}
			
			export(ds,"all",documents,termsAll,"all");
			
			for (String relation : relations) {
				
				InformationExtractionSystem ies = new CachedInformationExtractionSystem(relation+extractor, relation, null, "NYT");

				int hasT = 0;

				Set<Document> usefuls = new HashSet<Document>();
				
				Map<String, List<Document>> terms = new HashMap<String, List<Document>>();
								
				for (int i = 1; i <= collection.size(); i++) {

					Document d = new Document(collection, i);
					
					if (ies.extract(d) != null && !ies.extract(d).isEmpty()){

						hasT++;

						usefuls.add(d);
						
						Set<String> termsA = d.getTerms();
						
						for (String t : termsA) {
							
							List<Document> doc = terms.get(t);
							
							if (doc == null){
								doc = new ArrayList<Document>();
								terms.put(t, doc);
							}
							
							doc.add(d);
						}
						
					}

				}

				export(ds,relation,usefuls,terms,"useful");
				
				System.out.println(ds + ", " + relation + ", " + hasT + ", " + collection.size() + ", " + (double)hasT / (double)collection.size());
				
			}
			

			
		}
		



	}

	private static void export(String ds, String relation,
			Set<Document> usefuls, Map<String, List<Document>> terms, String docs) throws IOException {
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("data/tmp/term.doc.map-" + ds + "-" + relation + "-" + docs + ".csv")));
		
		bw.write("term, document, value, position");
		
		for (Entry<String,List<Document>> entry : terms.entrySet()) {
			
			int pos = 1;
			
			for (Document document : entry.getValue()) {
				
				bw.newLine();
								
				bw.write(entry.getKey() + ", " + document.getId() + ", 1" + "," + pos);
				
				pos++;
				
				if (pos > 400)
					break;
			}
			
		}
		
		bw.close();
		
	}

}
