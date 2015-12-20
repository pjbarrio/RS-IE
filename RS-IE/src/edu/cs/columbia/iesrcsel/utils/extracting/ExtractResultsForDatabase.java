package edu.cs.columbia.iesrcsel.utils.extracting;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.cs.columbia.iesrcsel.model.impl.Tuple;
import edu.cs.columbia.iesrcsel.utils.extracting.impl.CIMPLEExtractionSystem;


public class ExtractResultsForDatabase {
	
	public static void main(String[] args) throws IllegalArgumentException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, SQLException {
		
		int splits = 10000;
		
		String prefix = args[0]; //local/pjbarrio/Files/Downloads/Indexing , /proj/db-files2/NoBackup/pjbarrio/Dataset/crawl
		
		String path = prefix + "/apache-solr-3.1.0/example/multicore/";
		
		String extractor = args[1];
		String relationship = args[2];
		String website = args[3].replaceAll("\\p{Punct}", "");;
		
		String outpref = "data/extraction/crawl/" + website + "/";
		
		new File(outpref).mkdirs();
		
		String ieSystemPath = "CIMPLEExecutionPlans/" + args[2] + "Plain.plan";
		
		CIMPLEExtractionSystem ieSystem = new CIMPLEExtractionSystem(ieSystemPath);
		
		Directory directory = FSDirectory.open(new File(path + website + "/data/index/"));

		IndexReader indexReader = IndexReader.open(directory);

		IndexSearcher indexSearcher = new IndexSearcher(indexReader);

		List<Integer> files = ExtractPablosResultsForDatabase.getFiles(indexSearcher);

		int currentSplit = 0;
		
		String outputFile = outpref + extractor + "_" + relationship + "-" +currentSplit+ ".data";
		
		for(int i=0; i< files.size(); i++){

			if (!new File(outputFile).exists()){
				
				Map<Integer,List<Tuple>> extractions = new HashMap<Integer, List<Tuple>>();
				
				for (int j = i; j < i+splits && j < files.size(); j++) {
					
					String content = ExtractPablosResultsForDatabase.getContent(indexSearcher,files.get(j));
					
					List<Tuple> t = ieSystem.extractTuplesFrom(content);
					if(t.size()!=0){
						extractions.put(files.get(j),t);
					}
					System.out.println((j*100)/(double)files.size() + "% of the documents processed!");
					
				}
			
				ExtractPablosResultsForDatabase.storeResults(extractions,outputFile);
				
			} 

			i += splits-1;
			
			currentSplit++;
			
			outputFile = outpref + extractor + "_" + relationship + "-" +currentSplit+ ".data";
		}

		indexSearcher.close();
		indexReader.close();
		directory.close();
		
	}

}
