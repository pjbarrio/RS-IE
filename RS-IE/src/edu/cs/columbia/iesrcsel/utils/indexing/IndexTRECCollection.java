package edu.cs.columbia.iesrcsel.utils.indexing;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import lemurproject.indri.IndexEnvironment;
import lemurproject.indri.ParsedDocument;
import lemurproject.indri.ParsedDocument.TermExtent;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import org.apache.commons.io.FileUtils;
import org.mitre.jawb.io.SgmlDocument;

import edu.cs.columbia.iesrcsel.utils.SearchableUtils;
import edu.cs.columbia.iesrcsel.utils.indexing.nytlabs.corpus.NYTCorpusDocument;
import edu.cs.columbia.iesrcsel.utils.indexing.nytlabs.corpus.NYTCorpusDocumentParser;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class IndexTRECCollection {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		int col = 1;
		
		String db = "wsj";
		
		String year = "1987";
		
		String folder = "/proj/db-files2/NoBackup/pjbarrio/Dataset/TREC/CleanCollection/tipster_vol_" + col + "/" + db + "/" + year;
		
		String[] stopWords = FileUtils.readLines(new File("data/stopWords.txt")).toArray(new String[0]);

		TokenizerME tokenizer = new TokenizerME(new TokenizerModel(new File("models/en-token.bin")));

		createIndex("data/indexes/TREC"+col + "-" + db + "-" + year + ".idx", new File(folder), stopWords,tokenizer);

	}

	private static void createIndex(String indexName, File file,
			String[] stopWords, TokenizerME tokenizer) {
		
		try {

			IndexEnvironment env = new IndexEnvironment();
			env.setStoreDocs(true);
			env.setStopwords(stopWords);
			env.setStemmer("porter");
			env.setIndexedFields(new String[]{"doc"});
			env.setMetadataIndexedFields(new String[]{"doc"}, new String[0]);
			env.create(indexName);

			index(env,file,tokenizer);
			
			env.close();
		} catch(Exception e){
			e.printStackTrace();
		}

		
	}

	private static void index(IndexEnvironment env, File file,
			TokenizerME tokenizer) throws Exception {
		
		if (file.isDirectory()){
		
			File[] files = file.listFiles();

			int j = 0;
			
			for (int i = 0; i < files.length; i++) {

				if (j++ % 1000 == 0)
					System.err.print(".");
				
				index(env,files[i],tokenizer);
			}
			
			System.err.print("\n");
			
		} else{
			
			System.err.print(".");
			
			String content = getContent(file);
			
			ParsedDocument p_doc = createParsedDocument(content,file.getAbsolutePath(),tokenizer);
			
			env.addParsedDocument(p_doc);
		}
		
		
		
	}

	private static ParsedDocument createParsedDocument(String content,
			String name, TokenizerME tokenizer) {
		
		ParsedDocument doc = new ParsedDocument();

		doc.content = content;

		Map<String,String> map = new HashMap<String, String>();

		map.put("doc", name);

		doc.metadata = map;

		doc.text = content;

		String[] terms = tokenizer.tokenize(content);

		Span[] spans = tokenizer.tokenizePos(content);

		TermExtent[] termsE = new TermExtent[spans.length];

		for (int i = 0; i < spans.length; i++) {

			termsE[i] = new TermExtent(spans[i].getStart(), spans[i].getEnd());

		}

		doc.terms = terms;

		doc.positions = termsE;

		return doc;
		
	}

	public static String getContent(File file) throws IOException{
		return new SgmlDocument(new StringReader(FileUtils.readFileToString(file))).getSignalText();
	}
	
}
