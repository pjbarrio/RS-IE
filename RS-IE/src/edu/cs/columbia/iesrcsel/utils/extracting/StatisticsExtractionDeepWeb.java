package edu.cs.columbia.iesrcsel.utils.extracting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

public class StatisticsExtractionDeepWeb {

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

		System.setOut(new PrintStream(new File("data/stats/deepweb-extraction-stats.csv")));
		
		int minSize = 10000;

//		createFile(minSize);
		
		System.out.println("db,extractor,relation,dbsize,usefuldocs");
		
		Index(minSize);
		


	}

	private static void Index(int minSize) throws IOException, SAXException, TikaException {
		
		Metadata metadata = new Metadata();

		Parser parser = new AutoDetectParser();

		List<String> databases = FileUtils.readLines(new File("data/ordereddeepwebdatabases.list"));
		
		for (int i = 0; i < databases.size(); i++) {
						
			System.err.println(i);
			
			List<Pair<Long, String>> list = (List<Pair<Long,String>>)SerializationHelper.deserialize("/proj/dbNoBackup/pjbarrio/workspace/ExperimentsAQG/cachingExperiments/" + databases.get(i) + ".ser");
			
			if (list.size() > minSize){
				
				indexDeepWeb(databases.get(i), list, metadata, parser, list.size());
				
				System.gc();
				
			}
			
		}

		
	}


	private static void indexDeepWeb(String dbName,
			List<Pair<Long, String>> files, Metadata metadata, Parser parser, int l) throws CorruptIndexException, IOException, SAXException, TikaException {
		
		Map<String,Map<Long,List<Tuple>>> extrMap = new HashMap<String, Map<Long,List<Tuple>>>();
		
		for (int j = 0; j < extractor.length; j++) {
			for (int k = 0; k < relation.length; k++) {
				String file = "/proj/dbNoBackup/pjbarrio/workspace/ExperimentsAQG/cachingExperiments/" + dbName + "-" + extractor[j] + "-" + relation[k] + ".ser";
				
				Map<Long,List<Tuple>> map = (Map<Long, List<Tuple>>) SerializationHelper.deserialize(file);
				
				extrMap.put(file, map);
				
			}
		}		
		
		Map<String,Integer> tus = new HashMap<String, Integer>();
		
		for (int i = 0; i < files.size(); i++) {
			
			for (int j = 0; j < extractor.length; j++) {
				for (int k = 0; k < relation.length; k++) {
					
					String name = dbName + "-" + extractor[j] + "-" + relation[k];
					
					String file = "/proj/dbNoBackup/pjbarrio/workspace/ExperimentsAQG/cachingExperiments/" + name + ".ser";
					
					List<Tuple> tuples = extrMap.get(file).get(files.get(i).first);
					
					if (tuples == null)
						tuples = new ArrayList<Tuple>(0);
					else{
						Integer val = tus.get(extractor[j] + "," + relation[k]);
						if (val == null)
							val =0;
						tus.put(extractor[j] + "," + relation[k], val + 1);
					}
					
				}
			}
			
		}
		
		for (Entry<String,Integer> pair : tus.entrySet()) {
			System.out.println(dbName + "," + pair.getKey() + "," + l + "," + pair.getValue());
		}
		

		
	}

}
