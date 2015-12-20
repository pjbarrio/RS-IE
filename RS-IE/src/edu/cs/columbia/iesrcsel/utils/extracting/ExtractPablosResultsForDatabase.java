package edu.cs.columbia.iesrcsel.utils.extracting;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.CodeSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.cs.columbia.iesrcsel.model.impl.Tuple;
import edu.cs.columbia.iesrcsel.utils.extracting.impl.REELRelationExtractionSystem;
import edu.cs.columbia.iesrcsel.utils.serialization.SerializationHelper;

public class ExtractPablosResultsForDatabase {

	private static String CONTENT_FIELD = "content";

	public static FieldSelector f =  new FieldSelector() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public FieldSelectorResult accept(String fieldName) {
			if (fieldName == CONTENT_FIELD ) return FieldSelectorResult.LOAD_AND_BREAK;
			return FieldSelectorResult.NO_LOAD;
		}

		
		

	};

	public static void main(String[] args) throws IllegalArgumentException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, SQLException {

		Class klass = cern.colt.map.AbstractMap.class;

	    CodeSource codeSource = klass.getProtectionDomain().getCodeSource();

	    if ( codeSource != null) {

	        System.out.println("Location:" + codeSource.getLocation());

	    }

		
		int splits = 1000;
		
		String prefix = args[0]; //local/pjbarrio/Files/Downloads/Indexing , /proj/db-files2/NoBackup/pjbarrio/Dataset/crawl
		
		String path = prefix + "/apache-solr-3.1.0/example/multicore/";
		
		int extractorId = Integer.parseInt(args[1]);
		int relationshipId = Integer.parseInt(args[2]);
		String pathModelsEnts = args[3]; ///proj/dbNoBackup/pjbarrio/models/LEARNING-TO-RANK-IE
		String pathModelsRE = args[4]; ///proj/dbNoBackup/pjbarrio/models/LEARNING-TO-RANK
		String website = args[5].replaceAll("\\p{Punct}", "");

		Set<String> set = (Set<String>) SerializationHelper.deserialize("data/stats/valid_collections1000.ser");
		
		String aux = args[5].endsWith("/")? args[5] : args[5] + "/";
		
		if (!set.contains(aux)){
			return;
		}
		
		System.out.println("Processing website " + website);

		//String extractor = args[0];
		//String relationship = args[1];
		//String ieSystemPath = args[2];
		//String pathFile = "/home/goncalo/Desktop/sample";
		//String pathModelsEnts = "/home/goncalo/modelsLTR/LEARNING-TO-RANK-IE/";
		//String pathModelsRE = "/home/goncalo/modelsLTR/LEARNING-TO-RANK/";

		REELRelationExtractionSystem ieSystem = new REELRelationExtractionSystem(pathModelsRE,pathModelsEnts,extractorId,relationshipId);

		String extractor = ieSystem.getExtractor();
		String relationship = ieSystem.getRelationship();

		Directory directory = FSDirectory.open(new File(path + website + "/data/index/"));

		IndexReader indexReader = IndexReader.open(directory);

		IndexSearcher indexSearcher = new IndexSearcher(indexReader);

		List<Integer> files = getFiles(indexSearcher);

		String outpref = "data/extraction/crawl/" + website + "/";
		
		new File(outpref).mkdirs();
		
		int currentSplit = 1;
		
		String outputFile = outpref + extractor + "_" + relationship + "-" + currentSplit+ ".data";
		
		for(int i=0; i< files.size(); i++){

			if (!new File(outputFile).exists()){

				Map<Integer,List<Tuple>> extractions = new HashMap<Integer, List<Tuple>>();
				
				for (int j = i; j < i + splits && j < files.size(); j++) {
					
					String content = getContent(indexSearcher,files.get(j));
					
					List<Tuple> t = ieSystem.extractTuplesFrom(content);
					if(t.size()!=0){
						extractions.put(files.get(j),t);
					}
					
					System.out.println((j*100)/(double)files.size() + "% of the documents processed!");
				
				}
				
				storeResults(extractions,outputFile);
				
			}else{
				System.out.println("file exists! (" + outputFile + ")");
			}

			i += splits-1;
			
			currentSplit++;
		
			outputFile = outpref + extractor + "_" + relationship + "-" +currentSplit+ ".data";

			
		}
		
		indexSearcher.close();
		indexReader.close();
		directory.close();

	}

	public static String getContent(IndexSearcher indexSearcher,
			Integer docId) throws CorruptIndexException, IOException {

		return indexSearcher.doc(docId, f).get(CONTENT_FIELD);

	}

	public static List<Integer> getFiles(IndexSearcher indexSearcher) throws IOException {

		MatchAllDocsQuery q = new MatchAllDocsQuery();

		TopDocs docs = indexSearcher.search(q, Integer.MAX_VALUE);

		List<Integer> ret = new ArrayList<Integer>(docs.totalHits);

		for (int i = 0; i < docs.totalHits; i++) {

			ret.add(docs.scoreDocs[i].doc);

		}

		return ret;

	}

	public static void storeResults(Map<Integer,List<Tuple>> extractions, String path)  {
		SerializationHelper.serialize(path, extractions);
	}
}
