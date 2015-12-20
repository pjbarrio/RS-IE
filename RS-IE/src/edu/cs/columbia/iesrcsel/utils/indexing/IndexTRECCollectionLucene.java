package edu.cs.columbia.iesrcsel.utils.indexing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.util.Version;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.mitre.jawb.io.SgmlDocument;
import org.xml.sax.ContentHandler;

import edu.cs.columbia.iesrcsel.model.collection.IndriCollection;
import edu.cs.columbia.iesrcsel.model.extractor.impl.CachedInformationExtractionSystem;
import edu.cs.columbia.iesrcsel.utils.SearchableUtils;
import edu.cs.columbia.iesrcsel.utils.indexing.nytlabs.corpus.NYTCorpusDocument;
import edu.cs.columbia.iesrcsel.utils.indexing.nytlabs.corpus.NYTCorpusDocumentParser;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;
import execution.workload.tuple.Tuple;

public class IndexTRECCollectionLucene {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		String prefixlist = "/proj/db-files2/NoBackup/pjbarrio/Dataset_from_dbNoBackup/TREC/CleanCollection/";

		Set<String> stopWords = new HashSet<String>(FileUtils.readLines(new File("data/stopWords.txt")));

		TokenizerME tokenizer = new TokenizerME(new TokenizerModel(new File("models/en-token.bin")));

		List<String> files = FileUtils.readLines(new File("data/TRECfiles.txt"));

		createIndex("data/indexes/TREC/tv-TREC.idx", files, stopWords,tokenizer);

	}

	private static void createIndex(String indexName, List<String> files, Set<String> stopWords, TokenizerME tokenizer) throws CorruptIndexException, IOException {

		System.out.println("Loaded\n");

		LockFactory lf = new NoLockFactory();

		Directory dir = FSDirectory.open(new File(indexName),lf);
		// The Version.LUCENE_XX is a required constructor argument in Version 3.
		Analyzer analysis = new StandardAnalyzer(Version.LUCENE_31);
		// IndexWriter will intelligently open an index for appending if the
		// index directory exists, else it will create a new index directory.

		System.out.println("Directory opened");

		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_31, analysis);

		System.out.println("IndexWriter initialized");

		IndexWriter idx = new IndexWriter (dir,iwc);

		// **** Tika specific-stuff.  Otherwise this is like the basic Lucene Indexer example.

		System.out.println("Indexing db: TREC");

		for (int i = 0; i < files.size(); i++) {

			if (i % 1000 == 0){
				System.out.println(i + " out of " + files.size());
			}

			File f = new File(files.get(i));

			String content = getContent(f);

			// **** End Tika-specific
			Document doc = new Document();
			// Fields you want to display in toto in search results need to be stored
			// using the Field.Store.YES. The NOT_ANALYZED and ANALYZED
			// constant has replaced UN_TOKENIZED and TOKENIZED from previous versions.
			doc.add(new Field("name",f.getName(),Field.Store.YES, Field.Index.NOT_ANALYZED));
			doc.add(new Field("path",f.getCanonicalPath(),Field.Store.YES, Field.Index.NOT_ANALYZED));


			doc.add(new Field("contents",content,Field.Store.NO,Field.Index.ANALYZED,TermVector.YES));

			//Add the extractions!

			idx.addDocument(doc);

		}

		idx.optimize();
		idx.close();

	}

	public static String getContent(File file) throws IOException{
		return new SgmlDocument(new StringReader(FileUtils.readFileToString(file))).getSignalText();
	}

}
