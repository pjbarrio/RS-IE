package test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import edu.cs.columbia.iesrcsel.model.collection.IndriCollection;
import edu.cs.columbia.iesrcsel.model.extractor.InformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.extractor.impl.CachedInformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class openqueries {

	public static void main(String[] args) throws IOException {
		int useful,useless;
		int split = 1;
		boolean tupleAsStopWord = false;
		String relation = "NaturalDisaster";
		String extractor = "SSK";
		int k = 100;
	
		List<Query> queries = ((List<Query>)SerializationHelper.deserialize("data/queries/" + relation + "." + extractor + "." + split + "." + tupleAsStopWord + ".ser"));

		String ds = "topfeaturesarts";
		
		TextCollection collection = new IndriCollection("health", "data/indexes/onlyNotStemmedWords_"+ds+".idx");		
		
		InformationExtractionSystem ies = new CachedInformationExtractionSystem(relation+extractor, relation, null, "NYT");
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("data/tmp/COL." +ds+"R."+relation+"ranked.RPQ."+k+"TASW."+tupleAsStopWord+"SPL"+split+"EX."+extractor+".csv")));
		
		bw.write("position,term,useful,useless\n");
		
		int position = 1;
		
		for (Query query : queries) {
			
			try {
			
				List<ScoredDocument> dos = collection.search(query, k);
				
				useless = useful = 0;
				
				for (ScoredDocument scoredDocument : dos) {
					
					if (ies.extract(scoredDocument).isEmpty())
						useless++;
					else
						useful++;
									
				}
				
				bw.write(position++ + "," + query.toString() + "," + useful + "," + useless + "\n");
				
			} catch (Exception e){
				continue;
			}
			
			
					
		}
	
		bw.close();
	}
	
}
