package edu.cs.columbia.iesrcsel.model.extractor.impl.termcounter;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.apache.commons.io.FileUtils;
import org.mitre.jawb.io.SgmlDocument;

import edu.cs.columbia.iesrcsel.model.collection.DeepWebLuceneCollection;
import edu.cs.columbia.iesrcsel.model.collection.IndriCollection;
import edu.cs.columbia.iesrcsel.model.extractor.impl.CachedInformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Document;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.utils.MapBasedComparator;
import edu.cs.columbia.iesrcsel.utils.databaseWriter;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class FromIndexUsefulQueryProbabilityCalculator {

	public static void main(String[] args) throws IOException {

		String[] relations = {"ManMadeDisaster","NaturalDisaster","Indictment-Arrest-Trial","PersonCareer","VotingResult"};
		int[] relationConf = {3,2,6,1,5};

		String[] extractors = {"BONG","SSK"};
		int[] idExtractors = {17,19};

		String collection = "TREC";

		String prefixdb = "/proj/db-files2/NoBackup/pjbarrio/Dataset/TREC/CleanCollection/";
		String prefixlist = "/proj/db-files2/NoBackup/pjbarrio/Dataset_from_dbNoBackup/TREC/CleanCollection/";

		//Need all words to query and I can obtain them from any extractor.

		TextCollection tc = new DeepWebLuceneCollection("TREC", "data/indexes/TREC/tv-TREC.idx");

		databaseWriter dW = new databaseWriter();

		for (int i = 0; i < relations.length; i++) {

			System.out.format("Relation: %s \n", relations[i]);

			String relation = relations[i];

			for (int ext = 0; ext < idExtractors.length; ext++) {

				String extractor = extractors[ext];
				int idExtractor = idExtractors[ext];

				Set<String> usefulDocs = new HashSet<String>();

				if (extractor.equals("SSK")){

					CachedInformationExtractionSystem cie = new CachedInformationExtractionSystem("test", relation, null, new IndriCollection("TREC", null));

					Set<String> keys = cie.tupleData.keySet();

					for (String string : keys) {
						usefulDocs.add(string.replace(prefixdb, ""));
					}

				}else{

					String relationName = dW.getInformationExtractionSystemName(dW.getRelationExtractionSystemId(relationConf[i], idExtractor));

					File useful = new File(dW.getUsefulDocumentsForCollection(collection,relation,relationName));

					//				File useless = new File(dW.getUselessDocumentsForCollection(collection,relation,relationName));

					List<String> tmp = FileUtils.readLines(useful);

					usefulDocs = new HashSet<String>(tmp.size());

					for (String string : tmp) {
						usefulDocs.add(string.replace(prefixdb, ""));
					}

				}

				//I have all useful documents. Recover their Ids.
				
				Map<String,Double> termFreq = new HashMap<String, Double>();
				
				int is = 0;
				
				for (int index = 0; index < tc.size(); index++) {
					
					if (is++ % 50000 == 0)
						System.out.print(".");
					
					Document doc = new Document(tc, index);
					
					String path = tc.getPath(doc);
					
					if (usefulDocs.contains(path.replace(prefixlist, ""))){
						
						Map<String,Integer> termMap = tc.getTermFreqMap(doc);
						
						for (Entry<String,Integer> entry : termMap.entrySet()) {
							
							Double current = termFreq.get(entry.getKey());
							
							if (current == null){
								current = 0d;
							}
							
							termFreq.put(entry.getKey(), entry.getValue() + current);
							
						}
						
					}
										
				}
				
				//I already have all the term frequencies of all useful terms
				//I need to sort them and print them as queries.

				List<String> terms = new ArrayList<String>(termFreq.keySet());
				
				Collections.sort(terms, new MapBasedComparator<String>(termFreq, true));
				
				List<Query> queries = new ArrayList<Query>(terms.size());
				
				for (int j = 0; j < terms.size(); j++) {
					System.out.println(terms.get(j) + " - " + termFreq.get(terms.get(j)));
					queries.add(new Query(terms.get(j)));
				}
				
				String fileName = "data/biasedestimator/termMapProbabilityUseful." + relation + "."+extractor+".TREC.INDEX.ser";

				SerializationHelper.serialize(fileName, queries);

			}

		}


		tc.close();

	}

}
