package trial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cs.columbia.iesrcsel.collection.estimation.impl.StratifiedEstimatorUsefulSize;
import edu.cs.columbia.iesrcsel.model.collection.IndriCollection;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.extractor.impl.CachedInformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class FindOnlyUsefulWords {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String relation = "NaturalDisaster";
		String extractor = "SSK";
		int split = 1;
		boolean tupleAsStopWord = true;
		
		List<Query> initialQueries = ((List<Query>)SerializationHelper.deserialize("data/queries/" + relation + "." + extractor + "." + split + "." + tupleAsStopWord + ".ser"));

		TextCollection collection = new IndriCollection("world", "data/indexes/onlyNotStemmedWords_topnewshealth.idx");		
		
		InformationExtractionSystem ies = new CachedInformationExtractionSystem(relation+extractor, relation, null, "NYT");
	
		Set<String> uselessWords = new HashSet<String>();
		
		Set<String> usefulWords = new HashSet<String>();
		
		for (int i = 1; i <= collection.size(); i++) {
			
			if (ies.extract(new Document(collection, i)) != null && !ies.extract(new Document(collection, i)).isEmpty()){
				
				usefulWords.addAll(collection.getTerms(new Document(collection, i)));
				
			} else {
				
				uselessWords.addAll(Arrays.asList(collection.getTerms(new Document(collection, i))));
				
			}
			
		}
		
		System.out.println("All: " + uselessWords.size());
		System.out.println("Useful: " + usefulWords.size());

		usefulWords.removeAll(uselessWords);
		
		System.out.println(usefulWords.toString());
		
		Set<Document> docs = new HashSet<Document>();
		
		List<Query> queries = new ArrayList<Query>();
		
		for (String word: usefulWords) {
			
			Query q = new Query(word);
			
			docs.addAll(collection.search(q));
			
			queries.add(q);
			
		}
		
		int hasT = 0;
		
		for (Document doc: docs) {
			
			if (ies.extract(doc) != null && !ies.extract(doc).isEmpty()){
				
				hasT++;
				
			}
			
		}
		
		System.out.println(docs.size() + " - " + hasT);
		
		SerializationHelper.serialize("natdis.uf", queries);
		
	}

}
