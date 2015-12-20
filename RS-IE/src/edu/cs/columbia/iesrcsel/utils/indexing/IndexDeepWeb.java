package edu.cs.columbia.iesrcsel.utils.indexing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.util.Version;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.google.gdata.util.common.base.Pair;

import edu.cs.columbia.iesrcsel.utils.MapBasedComparator;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;
import execution.workload.tuple.Tuple;

public class IndexDeepWeb {

	private static String[] extractor = {"SSK", "BONG"};
	private static String[] relation = {"Indictment-Arrest-Trial","ManMadeDisaster","NaturalDisaster","PersonCareer","VotingResult"};
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 * @throws TikaException 
	 * @throws SAXException 
	 */
	public static void main(String[] args) throws CorruptIndexException, IOException, SAXException, TikaException {

		boolean tf = Boolean.valueOf(args[0]);
		
		boolean idf = Boolean.valueOf(args[1]);
		
		int minSize = 10000;

		Similarity s;
		
		String suffix;
		
		if (idf && !tf){
			s = new DefaultSimilarity(){
				public float tf(float freq) {
					return 1.0f;
				}
			};
			suffix = "ntf";
		} else if (tf && !idf){
			s = new DefaultSimilarity(){
				@Override
				public float idf(int docFreq, int numDocs) {
					return 1.0f;
				}
			};
			suffix = "nidf";
		} else if (!tf && !idf){
			s = new DefaultSimilarity(){
				@Override
				public float tf(float freq) {
					return 1.0f;
				}
				@Override
				public float idf(int docFreq, int numDocs) {
					return 1.0f;
				}
			};
			suffix = "ntfidf";
		} else{
			s =  new DefaultSimilarity();
			suffix = "";
		}
		
		if (tf){
			
		}
		
//		createFile(minSize);
		
		Index(minSize,s,suffix);
		
	}

	private static void Index(int minSize, Similarity s, String suffix) throws IOException, SAXException, TikaException {
		
		Metadata metadata = new Metadata();

		Parser parser = new AutoDetectParser();

		List<String> databases = FileUtils.readLines(new File("data/ordereddeepwebdatabases.list"));
		
		System.out.println("\nFiltered " + databases.size());
		
		for (int i = 0; i < databases.size(); i++) {
			
			String indexName = "data/indexes/deepweb/tv-" + databases.get(i) + suffix+ ".idx";
			
			if (new File(indexName).exists())
				continue;
			
			System.out.println("Database: " + i + " out of: " + databases.size());
			System.out.format("Indexing: %s", indexName);
			List<Pair<Long, String>> list = (List<Pair<Long,String>>)SerializationHelper.deserialize("/proj/dbNoBackup/pjbarrio/workspace/ExperimentsAQG/cachingExperiments/" + databases.get(i) + ".ser");
			
			if (list.size() > minSize){
				indexDeepWeb(indexName,databases.get(i), list, metadata, parser,s);
				
				System.gc();
				
			}
			
		}

		
	}

	private static void createFile(int minSize) throws IOException {
		
		List<String> databases = FileUtils.readLines(new File("data/deepwebdatabases.list"));
		
		Map<String, Double> sizes = new HashMap<String, Double>();
		
		for (int i = 0; i < databases.size(); i++) {
			
			try {
			
				List<Pair<Long, String>> list = (List<Pair<Long,String>>)SerializationHelper.deserialize("/proj/dbNoBackup/pjbarrio/workspace/ExperimentsAQG/cachingExperiments/" + databases.get(i) + ".ser");
				
				if (list.size() > minSize){
					
					sizes.put(databases.get(i), (double) list.size());
					
				}
				
			} catch (Exception e) {
				System.err.println("file not found");
			}
			
			System.out.print(".");
		}
		
		
		List<String> filteredDList = new ArrayList<String>(sizes.keySet());
		
		Collections.sort(filteredDList, new MapBasedComparator<String>(sizes, false));
		
		FileUtils.writeLines(new File("data/ordereddeepwebdatabases.list"), filteredDList);
		
	}

	private static void indexDeepWeb(String indexName, String dbName,
			List<Pair<Long, String>> files, Metadata metadata, Parser parser, Similarity s) throws CorruptIndexException, IOException, SAXException, TikaException {
		
		Map<String,Map<Long,List<Tuple>>> extrMap = new HashMap<String, Map<Long,List<Tuple>>>();
		
		for (int j = 0; j < extractor.length; j++) {
			for (int k = 0; k < relation.length; k++) {
				System.out.print(".");
				String file = "/proj/dbNoBackup/pjbarrio/workspace/ExperimentsAQG/cachingExperiments/" + dbName + "-" + extractor[j] + "-" + relation[k] + ".ser";
				
				Map<Long,List<Tuple>> map = (Map<Long, List<Tuple>>) SerializationHelper.deserialize(file);
				
				extrMap.put(file, map);
				
			}
		}		
		
		System.out.println("Loaded\n");
		
		LockFactory lf = new NoLockFactory();
		
		Directory dir = FSDirectory.open(new File(indexName),lf);
		// The Version.LUCENE_XX is a required constructor argument in Version 3.
		Analyzer analysis = new StandardAnalyzer(Version.LUCENE_31);
		// IndexWriter will intelligently open an index for appending if the
		// index directory exists, else it will create a new index directory.
		
		System.out.println("Directory opened");
		
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_31, analysis);
		
		iwc.setSimilarity(s);
		
		System.out.println("IndexWriter initialized");
		
		IndexWriter idx = new IndexWriter (dir,iwc);

		// **** Tika specific-stuff.  Otherwise this is like the basic Lucene Indexer example.
		
		System.out.println("Indexing db:" + dbName);
		
		for (int i = 0; i < files.size(); i++) {
			
			if (i % 1000 == 0){
				System.out.println(i + " out of " + files.size());
			}
			
			File f = new File(files.get(i).getSecond());

			FileInputStream is = null;
			
			try{
			
				is = new FileInputStream(f);

			} catch (FileNotFoundException e){
				System.err.println("File not found!");
				continue;
			}
			
			ContentHandler contenthandler = new BodyContentHandler(-1);
			metadata.set(Metadata.RESOURCE_NAME_KEY, f.getName());
			
			ParseContext pc = new ParseContext();
			
			// OOXMLParser parser = new OOXMLParser();
			
			try {
				parser.parse(is, contenthandler, metadata, pc);

			} catch (Exception e) {
				System.err.println("Not indexed doc");
			}
			
			// **** End Tika-specific
			Document doc = new Document();
			// Fields you want to display in toto in search results need to be stored
			// using the Field.Store.YES. The NOT_ANALYZED and ANALYZED
			// constant has replaced UN_TOKENIZED and TOKENIZED from previous versions.
			doc.add(new Field("name",f.getName(),Field.Store.YES, Field.Index.NOT_ANALYZED));
			doc.add(new Field("path",f.getCanonicalPath(),Field.Store.YES, Field.Index.NOT_ANALYZED));
			
			
			doc.add(new Field("myId", files.get(i).getFirst().toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
//			doc.add(new Field("title",metadata.get(Metadata.TITLE),Field.Store.YES, Field.Index.ANALYZED));
//			doc.add(new Field("author",metadata.get(Metadata.AUTHOR),Field.Store.YES, Field.Index.ANALYZED));
			doc.add(new Field("contents",contenthandler.toString(),Field.Store.NO,Field.Index.ANALYZED,TermVector.YES));
			
			//Add the extractions!
			
			for (int j = 0; j < extractor.length; j++) {
				for (int k = 0; k < relation.length; k++) {
					
					String name = dbName + "-" + extractor[j] + "-" + relation[k];
					
					String file = "/proj/dbNoBackup/pjbarrio/workspace/ExperimentsAQG/cachingExperiments/" + name + ".ser";
					
					List<Tuple> tuples = extrMap.get(file).get(files.get(i).first);
					
					if (tuples == null)
						tuples = new ArrayList<Tuple>(0);
										
					doc.add(new Field("Number-"+name,Integer.toString(tuples.size()), Field.Store.YES,Field.Index.NOT_ANALYZED));
					
					doc.add(new Field("Tuples-"+name,tuples.toString(), Field.Store.YES,Field.Index.NOT_ANALYZED));
					
				}
			}
			
			
			idx.addDocument(doc);
			
		}
		
		idx.optimize();
		idx.close();
		
	}

}
