package trial;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class CreateQueryPool {

	private static String trainingcollection;
	private static String prefixFolder;


	public static void main(String[] args) {
		
		// I can produce a single query pool by forcing one relation and saving into a file without reference to a relation.
		
		boolean produceSingleQueryPool = false;
		
		prefixFolder = "/proj/dbNoBackup/pjbarrio/workspacedb-pc02/ResourceSelectionIE/data/biasedestimator/";
		trainingcollection = "TREC";
				
		String[] relations = {"ManMadeDisaster", "Indictment-Arrest-Trial", "VotingResult", "NaturalDisaster", "PersonCareer"};
		
		String[] extractors = {/*"SSK",*/"BONG"};
		
		for (int i = 0; i < extractors.length; i++) {
			
			for (int j = 0; j < relations.length; j++) {

				Map<String,Integer> usefuls = (Map<String,Integer>)SerializationHelper.deserialize(getUsefulFileName(relations[j], extractors[i]));
				
				Set<String> terms = new HashSet<String>(usefuls.keySet());

//				Map<String,Integer> useless = (Map<String,Integer>)SerializationHelper.deserialize(getUselessFileName(relations[j], extractors[i]));
//				terms.addAll(useless.keySet());
				
				System.out.println(relations[j] + "-" + extractors[i] + "-" + terms.size());
				
				List<String> queries = new ArrayList<String>(terms);
				
				SerializationHelper.serialize("/proj/dbNoBackup/pjbarrio/workspacedb-pc02/ResourceSelectionIE/data/biasedestimator/1-gram.TREC.querypool"+relations[j]+"."+extractors[i]+".ser", queries);

			}
			

		}
		
		if (produceSingleQueryPool){
			
			Map<String,Integer> usefuls = (Map<String,Integer>)SerializationHelper.deserialize(getUsefulFileName(relations[0], extractors[0]));
			
			Set<String> terms = new HashSet<String>(usefuls.keySet());

			Map<String,Integer> useless = (Map<String,Integer>)SerializationHelper.deserialize(getUselessFileName(relations[0], extractors[0]));
			terms.addAll(useless.keySet());
			
			List<String> queries = new ArrayList<String>(terms);
			
			System.out.println("Generic dataset: " + queries.size());
			
			SerializationHelper.serialize("/proj/dbNoBackup/pjbarrio/workspacedb-pc02/ResourceSelectionIE/data/biasedestimator/1-gram.TREC.querypool.ser", queries);
			
		}
		
		
	}
	
	protected static String getUselessFileName(String relation, String extractor) {
		return prefixFolder + "termMapUseless." + 
				relation + "." + extractor + "." + trainingcollection + ".ser";
	}


	protected static String getUsefulFileName(String relation, String extractor) {
		return prefixFolder + "termMapUseful." + 
				relation + "." + extractor + "." + trainingcollection + ".ser";
	}
	
}
