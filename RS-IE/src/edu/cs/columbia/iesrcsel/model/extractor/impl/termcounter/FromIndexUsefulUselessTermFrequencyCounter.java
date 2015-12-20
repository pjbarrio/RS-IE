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
import java.util.Set;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.apache.commons.io.FileUtils;
import org.mitre.jawb.io.SgmlDocument;

import edu.cs.columbia.iesrcsel.model.collection.DeepWebLuceneCollection;
import edu.cs.columbia.iesrcsel.model.collection.IndriCollection;
import edu.cs.columbia.iesrcsel.model.extractor.impl.CachedInformationExtractionSystem;
import edu.cs.columbia.iesrcsel.model.impl.Query;
import edu.cs.columbia.iesrcsel.model.impl.ScoredDocument;
import edu.cs.columbia.iesrcsel.model.impl.TextCollection;
import edu.cs.columbia.iesrcsel.utils.databaseWriter;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class FromIndexUsefulUselessTermFrequencyCounter {

	public static void main(String[] args) throws IOException {

		String[] relations = {"ManMadeDisaster","NaturalDisaster","Indictment-Arrest-Trial","PersonCareer","VotingResult"};
		int[] relationConf = {3,2,6,1,5};

		String[] extractors = {"BONG","SSK"};
		int[] idExtractors = {17,19};

		String collection = "TREC";

		String prefixdb = "/proj/db-files2/NoBackup/pjbarrio/Dataset/TREC/CleanCollection/";
		String prefixlist = "/proj/db-files2/NoBackup/pjbarrio/Dataset_from_dbNoBackup/TREC/CleanCollection/";

		//Need all words to query and I can obtain them from any extractor.

		Map<String,Integer> termMap = (Map<String,Integer>)SerializationHelper.deserialize("data/biasedestimator/termMapUseful." + relations[0] + ".SSK.TREC.ser");

		Set<String> words = new HashSet<String>(termMap.keySet());

		termMap.clear();

		Map<String,Integer> termMap2 =  (Map<String,Integer>)SerializationHelper.deserialize("data/biasedestimator/termMapUseless." + relations[0] + ".SSK.TREC.ser");

		words.addAll(termMap2.keySet());

		termMap2.clear();

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

				for (int j = 100; j <= 1000; j+=100) {

					System.out.println("Limit: " + j);

					Map<String, Integer> usefuls = new HashMap<String, Integer>();

					Map<String, Integer> useless = new HashMap<String, Integer>();

					String fileName = "data/biasedestimator/termMapUseful." + relation + "."+extractor+".TREC.INDEX"+j+".ser";

					String fileName2 = "data/biasedestimator/termMapUseless." + relation + "."+extractor+".TREC.INDEX"+j+".ser";

					//				if (new File(fileName).exists())
					//					continue;

					int is = 0;

					for (String word : words) {

						if (is++ % 1000 == 0)
							System.out.print(".");


						List<ScoredDocument> docs = tc.search(new Query(word), j);

						int countuful = 0;
						int countuless = 0;
						for (ScoredDocument scoredDocument : docs) {

							if (usefulDocs.contains(scoredDocument.getPath().replace(prefixlist, ""))){
								countuful++;
							}else{
								countuless++;
							}

						}

						if (countuful >0)
							usefuls.put(word, countuful);
						if (countuless > 0)
							useless.put(word, countuless);
					}

					SerializationHelper.serialize(fileName, usefuls);
					SerializationHelper.serialize(fileName2, useless);

				}

			}

		}


		tc.close();

	}

}
