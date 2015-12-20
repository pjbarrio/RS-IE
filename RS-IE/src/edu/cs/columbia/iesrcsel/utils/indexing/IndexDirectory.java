package edu.cs.columbia.iesrcsel.utils.indexing;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.apache.commons.io.FileUtils;

import lemurproject.indri.IndexEnvironment;
import lemurproject.indri.ParsedDocument;
import edu.cs.columbia.iesrcsel.utils.SearchableUtils;
import edu.cs.columbia.iesrcsel.utils.indexing.nytlabs.corpus.IndexingNewYorkTimes;
import edu.cs.columbia.iesrcsel.utils.indexing.nytlabs.corpus.NYTCorpusDocumentParser;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class IndexDirectory {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String folder = "C:\\Users\\Pablo\\Downloads\\2003_\\";
		
		String name = "data/indexes/light_training_stemming.idx";
		
		File[] files = new File(folder).listFiles();
		
		String[] stopWords = FileUtils.readLines(new File("data/stopWords.txt")).toArray(new String[0]);
		
		TokenizerME tokenizer = new TokenizerME(new TokenizerModel(new File("models/en-token.bin")));
		
		createIndex(name,files,stopWords,tokenizer);
		
	}

	private static void createIndex(String indexName, File[] files, String[] stopWords, TokenizerME tokenizer) {
		
		try {

			IndexEnvironment env = new IndexEnvironment();
			env.setStoreDocs(false);
			env.setStopwords(stopWords);
			env.setStemmer("porter");
			env.setIndexedFields(new String[]{"doc"});
			env.setMetadataIndexedFields(new String[]{"doc"}, new String[0]);
			env.create(indexName);
			NYTCorpusDocumentParser np = new NYTCorpusDocumentParser();

			int i = 0;
			
			Set<String> set = new HashSet<String>();
			
			for (File file : files) {

				if (i++ % 1000 == 0)
					System.err.print(".");
				
				ParsedDocument p_doc = IndexingNewYorkTimes.createParsedDocument(np,file,tokenizer);
				
				env.addParsedDocument(p_doc);

				set.addAll(Arrays.asList(SearchableUtils.filterSearchableTerms(p_doc.terms, 0,true)));
				
			}

			System.err.print("\n");
			
			env.close();
		
			Map<String,Integer> index = new HashMap<String,Integer>();
			
			i = 0;
			
			for (String string : set) {
				
				index.put(string, i++);
				
			}
			
			SerializationHelper.serialize(indexName+"_termsId", index);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
