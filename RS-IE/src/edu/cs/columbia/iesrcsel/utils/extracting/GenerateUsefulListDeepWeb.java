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

public class GenerateUsefulListDeepWeb {

	private static String[] extractor = {"SSK", "BONG"};
	private static String[] extractor_file = {"Sub-sequences", "N-Grams"};
	private static String[] relation = {"Indictment-Arrest-Trial","ManMadeDisaster","NaturalDisaster","PersonCareer","VotingResult"};

	/**
	 * @param args
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 * @throws TikaException 
	 * @throws SAXException 
	 */
	public static void main(String[] args) throws CorruptIndexException, IOException, SAXException, TikaException {

		int minSize = 10000;

		List<String> databases = FileUtils.readLines(new File("data/ordereddeepwebdatabases.list"));

		for (int j = 0; j < extractor.length; j++) {

			for (int k = 0; k < relation.length; k++) {

				Map<String,Integer> usefuls = new HashMap<String, Integer>();
				
				for (int i = 0; i < databases.size(); i++) {

					System.err.println(i);

					List<Pair<Long, String>> list = (List<Pair<Long,String>>)SerializationHelper.deserialize("/proj/dbNoBackup/pjbarrio/workspace/ExperimentsAQG/cachingExperiments/" + databases.get(i) + ".ser");

					if (list.size() > minSize){

						String dbName = databases.get(i);

						String file = "/proj/dbNoBackup/pjbarrio/workspace/ExperimentsAQG/cachingExperiments/" + dbName + "-" + extractor[j] + "-" + relation[k] + ".ser";

						Map<Long,List<Tuple>> map = (Map<Long, List<Tuple>>) SerializationHelper.deserialize(file);

						int count = 0;
						
						for (int ii = 0; ii < list.size(); ii++) {

							List<Tuple> tuples = map.get(list.get(ii).first);

							if (tuples != null && !tuples.isEmpty()){
								count++;
							}

						}

						if (count > 0 )
							usefuls.put(dbName, count);				

					}

					System.gc();
					
				}

				SerializationHelper.serialize("data/stats/deepweb_useful"+extractor_file[j]+"_"+relation[k]+".ser",usefuls);
				
			}
		}	


	}


}
