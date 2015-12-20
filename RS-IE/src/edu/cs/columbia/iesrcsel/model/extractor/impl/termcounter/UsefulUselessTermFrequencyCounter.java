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

import weka.core.UnsupportedAttributeTypeException;

import edu.cs.columbia.iesrcsel.model.collection.IndriCollection;
import edu.cs.columbia.iesrcsel.model.extractor.impl.CachedInformationExtractionSystem;
import edu.cs.columbia.iesrcsel.utils.databaseWriter;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class UsefulUselessTermFrequencyCounter {

	public static void main(String[] args) throws IOException, UnsupportedAttributeTypeException {

		String[] rels = {"ManMadeDisaster","NaturalDisaster","Indictment-Arrest-Trial","PersonCareer","VotingResult"};
		int[] relConf = {3,2,6,1,5};

		String[] relations = {rels[Integer.valueOf(args[0])]};
		int[] relationConf = {relConf[Integer.valueOf(args[0])]};

		String[] extractors = {"BONG","SSK"}; //
		int[] idExtractors = {17,19};

		String collection = "TREC";

		String prefixdb = "/proj/db-files2/NoBackup/pjbarrio/Dataset/TREC/CleanCollection/";
		String prefixlist = "/proj/db-files2/NoBackup/pjbarrio/Dataset_from_dbNoBackup/TREC/CleanCollection/";

		Set<String> stopWords = new HashSet<String>(FileUtils.readLines(new File("data/stopWords.txt")));

		TokenizerME tokenizer = new TokenizerME(new TokenizerModel(new File("models/en-token.bin")));

		List<String> files = FileUtils.readLines(new File("data/TRECfiles.txt"));

		databaseWriter dW = new databaseWriter();

		for (int i = 0; i < relations.length; i++) {

			System.out.format("Relation: %s \n", relations[i]);

			for (int ext = 0; ext < idExtractors.length; ext++) {

				String extractor = extractors[ext];
				int idExtractor = idExtractors[ext];
				
				List<String> filesClean = new ArrayList<String>(files.size());

				for (String string : files) {

					filesClean.add(string.replace(prefixlist, ""));

				}

				String relation = relations[i];

				Set<String> usefulDocs = null;

				if (extractor.equals("SSK")){

					CachedInformationExtractionSystem cie = new CachedInformationExtractionSystem("test", relation, null, new IndriCollection("TREC", null));

					Set<String> keys = cie.tupleData.keySet();

					usefulDocs = new HashSet<String>(keys.size());

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

				if (!new File("data/biasedestimator/termMapUseful." + relation + "."+extractor+".TREC.ser").exists())
					createTermMap(usefulDocs, prefixlist,stopWords,tokenizer, "data/biasedestimator/termMapUseful." + relation + "."+extractor+".TREC.ser");

				filesClean.removeAll(usefulDocs);

				Collections.sort(filesClean);
				if (!new File("data/biasedestimator/termMapUseless." + relation + "."+extractor+".TREC.ser").exists())
					createTermMap(filesClean, prefixlist,stopWords,tokenizer, "data/biasedestimator/termMapUseless." + relation + "."+extractor+".TREC.ser");

				filesClean.clear();

			}



		}

	}

	private static void createTermMap(Collection<String> docs,
			String prefixlist, Set<String> stopWords, TokenizerME tokenizer,
			String fileName) throws IOException {

		Map<String, Integer> termsMap = new HashMap<String, Integer>();

		int count = 0;

		for (String string : docs) {

			if ((count++ % 100) == 0)
				System.out.format("Processed: %f \n", (double)count / (double)docs.size());

			String content = getContent(new File(prefixlist,string));

			String[] tokens = tokenizer.tokenize(content);

			Set<String> terms = new HashSet<String>();

			for (int i = 0; i < tokens.length; i++) {
				terms.add(tokens[i].toLowerCase());
			}

			terms.removeAll(stopWords);

			for (String term : terms) {

				Integer freq = termsMap.get(term);

				if (freq == null){
					freq = 0;
				}

				termsMap.put(term, freq+1);
			}
			terms.clear();
		}			
		System.out.println("");
		SerializationHelper.serialize(fileName, termsMap);
		termsMap.clear();
	}

	public static String getContent(File file) throws IOException{
		return new SgmlDocument(new StringReader(FileUtils.readFileToString(file))).getSignalText();
	}

}
