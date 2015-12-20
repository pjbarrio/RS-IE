package test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import lemurproject.indri.IndexEnvironment;

import edu.cs.columbia.iesrcsel.model.collection.DeepWebLuceneCollection;
import edu.cs.columbia.iesrcsel.model.collection.IndriCollection;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.extractor.impl.CachedInformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.extractor.impl.DeepWebInformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;

public class QueryDeepWebCollection {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		List<String> dbs = FileUtils.readLines(new File("data/ordereddeepwebdatabases.list"));
		
		
		
		for (int j = 0; j < dbs.size(); j++) {
			
			Integer in = Integer.valueOf(dbs.get(j));
			
			if (in == 386)//(in==2053 || in == 226 || in == 531 || in == 1158 ||  in == 1562 || in == 2239 ||  in == 2159)
				continue;
			
			DeepWebLuceneCollection collection = new DeepWebLuceneCollection(in.toString(), "data/indexes/deepweb/tv-"+dbs.get(j)+".idx");

			DeepWebInformationExtractionSystem ie = new DeepWebInformationExtractionSystem("JustAnId", "SSK", "NaturalDisaster",collection);
			
			List<ScoredDocument> res = collection.search(new Query("house"));
			
			System.out.println("Matches: " + res.size() + " documents.");
			
//			int total = 0;
//			
			
			System.out.println(in + "-Col" );
			
			Set<String> aux = collection.getTerms(new Document(collection,1));
			
			System.out.println(in + "-" + aux.size());
			
			for (int i = 0; i < res.size(); i++) {

				if (ie.extract(res.get(i)).size()>1)
					System.out.println(i + "- out of - " + res.size() + "-" + ie.extract(res.get(i)));
				
				
//				System.out.println(collection.getPath(res.get(i)));
				
//				System.out.println(collection.containsTerm(res.get(i), "house"));
//				
//				System.out.println(collection.getStopWords().toString());
//				
//				System.out.println(collection.getDocumentFrequency("house"));
				
//				System.out.println(res.get(i).getTerms().length);
				
				
			}
//			
//			System.out.println(total);
			
		}
		

	}

}
