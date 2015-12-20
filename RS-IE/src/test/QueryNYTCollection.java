package test;

import java.io.File;
import java.util.List;

import lemurproject.indri.IndexEnvironment;

import edu.cs.columbia.iesrcsel.model.collection.IndriCollection;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.extractor.impl.CachedInformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;

public class QueryNYTCollection {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		TextCollection collection = new IndriCollection("world", "data/indexes/onlyNotStemmedWords_topnewsworld.idx");

//		TextCollection collection = new NYTCollection("trec", "data/indexes/TREC1-wsj-1987.idx");
		
		List<ScoredDocument> res = collection.search(new Query("earthquake"));
		
		System.out.println("Matches: " + res.size() + " documents.");
		
//		System.out.println(collection.getDocumentFrequency("treasonous"));
		
		InformationExtractionSystem ies = new CachedInformationExtractionSystem("natDis", "NaturalDisaster", null, "NYT");
		
		int total = 0;
		
		Document d = new Document(collection, 14513);
		
		d.getTerms();
		
		for (int i = 0; i < res.size(); i++) {
			
//			System.out.println(collection.getPath(res.get(i)));
			
			collection.getTerms(res.get(i));
			if (ies.extract(res.get(i)).size() > 0){
				collection.getTerms(res.get(i));
//				System.out.println(i + " - " + collection.getPath(res.get(i)) + " - " + ies.extract(res.get(i)).size() + " - " + res.get(i).getId() + " - " + res.get(i).getScore());
				System.out.println(i + "," + 1);
				total++;
			}else{
				System.out.println(i + "," + 0);
			}
		}
		
		System.out.println(total);
	}

}
